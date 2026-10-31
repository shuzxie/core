package moe.rukamori.archivetune.di

import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import moe.rukamori.archivetune.data.localmedia.LocalMediaIds
import moe.rukamori.archivetune.data.localmedia.LocalSubtitles
import moe.rukamori.archivetune.data.recommendation.FlowNeuroEngine
import moe.rukamori.archivetune.data.video.OfflineSubtitleStore
import moe.rukamori.archivetune.data.video.VideoDownloadManager
import moe.rukamori.archivetune.player.EnhancedPlayerManager
import moe.rukamori.archivetune.player.FeedExclusionsSource
import moe.rukamori.archivetune.player.LocalCaptionSource
import moe.rukamori.archivetune.player.LocalCaptions
import moe.rukamori.archivetune.player.LocalCopySource
import moe.rukamori.archivetune.utils.PerformanceDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkIoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * Transitional providers around the player-path singletons so consumers can inject them instead of
 * reaching for the static accessors. Every provider delegates to the existing singleton, so object
 * identity and first-creation timing are unchanged (see CLAUDE.md, DI rule 10).
 */
@Module
@InstallIn(SingletonComponent::class)
object PlayerManagerModule {
    @Provides
    fun provideEnhancedPlayerManager(
        videoDownloadManager: VideoDownloadManager,
        neuroEngine: Lazy<FlowNeuroEngine>,
        localSubtitles: Lazy<LocalSubtitles>,
        offlineSubtitleStore: Lazy<OfflineSubtitleStore>,
    ): EnhancedPlayerManager =
        EnhancedPlayerManager.getInstance().also {
            it.localCopySource =
                LocalCopySource { videoId ->
                    LocalMediaIds.videoUri(videoId)?.toString() ?: videoDownloadManager.localCopyPath(videoId)
                }
            it.localCaptionSource =
                LocalCaptionSource { videoId, path ->
                    val subtitles = localSubtitles.get()
                    val own =
                        if (LocalMediaIds.isLocal(videoId)) subtitles.beside(path) else offlineSubtitleStore.get().load(videoId)
                    LocalCaptions(own + subtitles.picked(videoId), subtitles.offsetMs(videoId))
                }
            it.feedExclusionsSource = FeedExclusionsSource { neuroEngine.get().feedExclusions() }
        }

    @Provides
    @NetworkIoDispatcher
    fun provideNetworkIoDispatcher(): CoroutineDispatcher = PerformanceDispatcher.networkIO

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = PerformanceDispatcher.diskIO
}
