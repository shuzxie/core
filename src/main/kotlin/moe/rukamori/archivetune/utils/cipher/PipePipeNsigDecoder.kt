package moe.rukamori.archivetune.utils.cipher

import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Collections
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object PipePipeNsigDecoder {
    private const val LATEST_PLAYER_URL = "https://api.pipepipe.dev/decoder/latest-player"
    private const val DECODE_URL = "https://api.pipepipe.dev/decoder/decode"
    private const val USER_AGENT = "PipePipe/4.9.0"
    private const val PLAYER_TTL_MS = 24L * 60L * 60L * 1000L

    private const val PREFS_NAME = "flow_prefs"
    private const val KEY_PLAYER_ID = "nsig_player_id"
    private const val KEY_PLAYER_STS = "nsig_player_sts"
    private const val KEY_PLAYER_EXPIRY_MS = "nsig_player_expiry_ms"

    private const val N_CACHE_MAX_ENTRIES = 512

    private val N_PARAM_REGEX = Regex("([?&])n=([^&]+)")

    private val nCache: MutableMap<String, String> =
        Collections.synchronizedMap(
            object : LinkedHashMap<String, String>(64, 0.75f, true) {
                override fun removeEldestEntry(eldest: Map.Entry<String, String>): Boolean = size > N_CACHE_MAX_ENTRIES
            },
        )

    private val sigCache: MutableMap<String, String> =
        Collections.synchronizedMap(
            object : LinkedHashMap<String, String>(64, 0.75f, true) {
                override fun removeEldestEntry(eldest: Map.Entry<String, String>): Boolean = size > N_CACHE_MAX_ENTRIES
            },
        )

    private val playerIdMutex = Mutex()

    @Volatile private var appContext: Context? = null

    @Volatile private var playerId: String? = null

    @Volatile private var playerIdExpiryMs = 0L

    @Volatile private var cachedSignatureTimestamp: Int? = null

    @Volatile private var restoredFromDisk = false

    @Volatile private var cachedClient: OkHttpClient? = null

    private val clientLock = Any()

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    suspend fun warmUp() {
        runCatching { ensurePlayerId() }
    }

    private fun client(): OkHttpClient {
        cachedClient?.let { return it }
        return synchronized(clientLock) {
            cachedClient?.let { return@synchronized it }
            OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .build()
                .also { cachedClient = it }
        }
    }

    private suspend fun Call.await(): Response =
        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { cancel() }
            enqueue(
                object : Callback {
                    override fun onResponse(
                        call: Call,
                        response: Response,
                    ) {
                        continuation.resume(response)
                    }

                    override fun onFailure(
                        call: Call,
                        e: IOException,
                    ) {
                        if (continuation.isCancelled) return
                        continuation.resumeWithException(e)
                    }
                },
            )
        }

    private suspend fun get(url: String): String? {
        val request =
            Request
                .Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build()
        return client().newCall(request).await().use { response ->
            if (!response.isSuccessful) {
                null
            } else {
                response.body.string()
            }
        }
    }

    private fun restorePersistedPlayerId() {
        if (restoredFromDisk) return
        restoredFromDisk = true
        val prefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
        val expiry = prefs.getLong(KEY_PLAYER_EXPIRY_MS, 0L)
        if (expiry <= System.currentTimeMillis()) return
        val id = prefs.getString(KEY_PLAYER_ID, null)?.takeIf { it.isNotEmpty() } ?: return
        playerId = id
        playerIdExpiryMs = expiry
        cachedSignatureTimestamp = prefs.getInt(KEY_PLAYER_STS, 0).takeIf { it != 0 }
    }

    private fun persistPlayerId(
        id: String,
        expiryMs: Long,
        sts: Int?,
    ) {
        val prefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
        prefs
            .edit()
            .putString(KEY_PLAYER_ID, id)
            .putLong(KEY_PLAYER_EXPIRY_MS, expiryMs)
            .putInt(KEY_PLAYER_STS, sts ?: 0)
            .apply()
    }

    private suspend fun ensurePlayerId(): String? =
        withContext(Dispatchers.IO) {
            restorePersistedPlayerId()
            playerId?.let { if (System.currentTimeMillis() < playerIdExpiryMs) return@withContext it }
            playerIdMutex.withLock {
                playerId?.let { if (System.currentTimeMillis() < playerIdExpiryMs) return@withLock it }
                try {
                    val body = get(LATEST_PLAYER_URL) ?: return@withLock null
                    val json = JSONObject(body)
                    val id = json.optString("player").takeIf { it.isNotEmpty() } ?: return@withLock null
                    val sts = json.optInt("signatureTimestamp").takeIf { it != 0 }
                    val expiry = System.currentTimeMillis() + PLAYER_TTL_MS
                    cachedSignatureTimestamp = sts
                    playerId = id
                    playerIdExpiryMs = expiry
                    persistPlayerId(id, expiry, sts)
                    id
                } catch (e: Exception) {
                    null
                }
            }
        }

    internal fun rawN(url: String): String? =
        N_PARAM_REGEX.find(url)?.groupValues?.get(2)?.let {
            try {
                URLDecoder.decode(it, "UTF-8")
            } catch (e: Exception) {
                it
            }
        }

    internal fun replaceNParam(
        url: String,
        decodedN: String,
    ): String = url.replaceFirst(N_PARAM_REGEX, "$1n=${URLEncoder.encode(decodedN, "UTF-8")}")

    suspend fun prefetch(urls: List<String>) {
        val pid = ensurePlayerId() ?: return
        val ns = urls.mapNotNull { rawN(it) }.distinct().filter { nCache["$pid:$it"] == null }
        if (ns.isEmpty()) return
        withContext(Dispatchers.IO) {
            try {
                val joined = ns.joinToString(",") { URLEncoder.encode(it, "UTF-8") }
                val data = parseData(get("$DECODE_URL?player=$pid&n=$joined")) ?: return@withContext
                for (n in ns) {
                    val decoded = data.optString(n)
                    if (decoded.isNotEmpty()) {
                        nCache["$pid:$n"] = decoded
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    private suspend fun decodeN(n: String): String? {
        val pid = ensurePlayerId() ?: return null
        nCache["$pid:$n"]?.let { return it }
        return withContext(Dispatchers.IO) {
            try {
                val encoded = URLEncoder.encode(n, "UTF-8")
                val data = parseData(get("$DECODE_URL?player=$pid&n=$encoded")) ?: return@withContext null
                val decoded = data.optString(n).takeIf { it.isNotEmpty() } ?: return@withContext null
                nCache["$pid:$n"] = decoded
                decoded
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun parseData(body: String?): JSONObject? {
        if (body.isNullOrEmpty()) return null
        return try {
            JSONObject(body).getJSONArray("responses").getJSONObject(0).getJSONObject("data")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signatureTimestamp(): Int? {
        ensurePlayerId() ?: return null
        return cachedSignatureTimestamp
    }

    suspend fun decodeSignatures(
        signatures: Collection<String>,
        playerId: String? = null,
    ): Map<String, String> {
        val pid = playerId ?: ensurePlayerId() ?: return emptyMap()
        val wanted = signatures.distinct()
        val missing = wanted.filter { sigCache["$pid:$it"] == null }
        if (missing.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val data = parseData(get(signatureRequestUrl(pid, missing))) ?: return@withContext
                    for (signature in missing) {
                        val sigValue: String? = data.optString(signature).takeIf { it.isNotEmpty() }
                        if (sigValue != null) {
                            sigCache["$pid:$signature"] = sigValue
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                }
            }
        }
        return wanted.mapNotNull { signature -> sigCache["$pid:$signature"]?.let { signature to it } }.toMap()
    }

    internal fun signatureRequestUrl(
        playerId: String,
        signatures: Collection<String>,
    ): String = "$DECODE_URL?player=$playerId&sig=" + signatures.joinToString(",") { URLEncoder.encode(it, "UTF-8") }

    suspend fun deobfuscateUrl(url: String): String? {
        val n = rawN(url) ?: return null
        val decoded = decodeN(n) ?: return null
        if (decoded == n) return null
        return replaceNParam(url, decoded)
    }
}
