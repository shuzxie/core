package moe.rukamori.archivetune.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import moe.rukamori.archivetune.data.local.PlayerPreferences
import moe.rukamori.archivetune.data.repository.YouTubeRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideYouTubeRepository(playerPreferences: PlayerPreferences): YouTubeRepository = YouTubeRepository.getInstance(playerPreferences)

    @Provides
    @Singleton
    fun provideSubscriptionRepository(
        @ApplicationContext context: Context,
    ): moe.rukamori.archivetune.data.local.SubscriptionRepository =
        moe.rukamori.archivetune.data.local.SubscriptionRepository
            .getInstance(context)

    @Provides
    @Singleton
    fun provideLikedVideosRepository(
        @ApplicationContext context: Context,
    ): moe.rukamori.archivetune.data.local.LikedVideosRepository =
        moe.rukamori.archivetune.data.local.LikedVideosRepository
            .getInstance(context)

    @Provides
    @Singleton
    fun provideViewHistory(
        @ApplicationContext context: Context,
    ): moe.rukamori.archivetune.data.local.ViewHistory =
        moe.rukamori.archivetune.data.local.ViewHistory
            .getInstance(context)

    @Provides
    @Singleton
    fun provideHomeFeedCacheRepository(
        @ApplicationContext context: Context,
    ): moe.rukamori.archivetune.data.local.HomeFeedCacheRepository =
        moe.rukamori.archivetune.data.local
            .HomeFeedCacheRepository(context)

    @Provides
    @Singleton
    fun provideMusicPlaylistRepository(
        @ApplicationContext context: Context,
    ): moe.rukamori.archivetune.data.music.PlaylistRepository =
        moe.rukamori.archivetune.data.music
            .PlaylistRepository(context)

    // VideoDownloadManager is now @Singleton @Inject — Hilt provides it automatically
    @Provides
    @Singleton
    fun providePlayerPreferences(
        @ApplicationContext context: Context,
    ): moe.rukamori.archivetune.data.local.PlayerPreferences =
        moe.rukamori.archivetune.data.local
            .PlayerPreferences(context)

    /** Transitional: the music player manager still reaches the queue store through getInstance, so this hands injected consumers the same instance. */
    @Provides
    @Singleton
    fun provideQueuePersistence(
        @ApplicationContext context: Context,
    ): moe.rukamori.archivetune.data.local.QueuePersistence =
        moe.rukamori.archivetune.data.local.QueuePersistence
            .getInstance(context)

    /**
     * Transitional: [moe.rukamori.archivetune.data.local.BackupRepository] still builds its own
     * collaborators, so it is provided here rather than injected through its constructor. One
     * instance serves the whole app.
     */
    @Provides
    @Singleton
    fun provideBackupRepository(
        @ApplicationContext context: Context,
    ): moe.rukamori.archivetune.data.local.BackupRepository =
        moe.rukamori.archivetune.data.local
            .BackupRepository(context)
}
