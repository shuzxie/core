package moe.rukamori.archivetune.video

import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfo
import moe.rukamori.archivetune.innertube.YouTube

object VideoExtractor {

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
            e.printStackTrace()
            null
        }
    }
}
