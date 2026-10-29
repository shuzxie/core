package moe.rukamori.archivetune.video

import android.util.Log
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfo

object VideoExtractor {
    private const val TAG = "VideoExtractor"

    suspend fun getVideoUrl(videoId: String): String? {
        return try {
            val url = "https://www.youtube.com/watch?v=$videoId"
            val info = StreamInfo.getInfo(ServiceList.YouTube, url)
            
            val videoStream = info.videoStreams
                .filter { !it.isVideoOnly }
                .maxByOrNull { it.resolution?.replace("p", "")?.toIntOrNull() ?: 0 }

            val finalStream = videoStream ?: info.videoStreams.firstOrNull()
            
            finalStream?.url
        } catch (e: Exception) {
            Log.e(TAG, "Gagal ambil video $videoId: ${e.message}")
            null
        }
    }
}
