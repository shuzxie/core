package moe.rukamori.archivetune.player.stream

import moe.rukamori.archivetune.utils.cipher.CipherDeobfuscator
import moe.rukamori.archivetune.utils.cipher.PlayerJsFetcher
import moe.rukamori.archivetune.utils.potoken.WebPoTokenSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Re-attesting alone cannot lift a verdict the stream servers already reached about this visitor,
 * so the identity itself is discarded, along with the client demotions graded against it: the
 * supported form of the "clear app data" workaround for walled playback.
 */
class ResetYouTubeSession
    @Inject
    constructor() {
        suspend operator fun invoke() {
            WebPoTokenSession.resetIdentity()
            ClientGateTracker.clear()
            CipherDeobfuscator.invalidateSignatureTimestamp()
            withContext(Dispatchers.IO) { PlayerJsFetcher.invalidateCache() }
        }
    }
