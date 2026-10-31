package moe.rukamori.archivetune.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import moe.rukamori.archivetune.data.local.dao.CacheDao
import moe.rukamori.archivetune.data.local.dao.DownloadCollectionDao
import moe.rukamori.archivetune.data.local.dao.DownloadDao
import moe.rukamori.archivetune.data.local.dao.HomeFeedCacheDao
import moe.rukamori.archivetune.data.local.dao.MusicGraphDao
import moe.rukamori.archivetune.data.local.dao.NoteDao
import moe.rukamori.archivetune.data.local.dao.NotificationDao
import moe.rukamori.archivetune.data.local.dao.PlaylistDao
import moe.rukamori.archivetune.data.local.dao.RecognitionHistoryDao
import moe.rukamori.archivetune.data.local.dao.SubscriptionGroupDao
import moe.rukamori.archivetune.data.local.dao.SyncLogDao
import moe.rukamori.archivetune.data.local.dao.SyncPeerDao
import moe.rukamori.archivetune.data.local.dao.VideoDao
import moe.rukamori.archivetune.data.local.dao.WatchHistoryDao
import moe.rukamori.archivetune.data.local.entity.DownloadCollectionEntity
import moe.rukamori.archivetune.data.local.entity.DownloadCollectionItemEntity
import moe.rukamori.archivetune.data.local.entity.DownloadEntity
import moe.rukamori.archivetune.data.local.entity.DownloadItemEntity
import moe.rukamori.archivetune.data.local.entity.HomeFeedCacheEntity
import moe.rukamori.archivetune.data.local.entity.MusicGraphAlbumEntity
import moe.rukamori.archivetune.data.local.entity.MusicGraphArtistEntity
import moe.rukamori.archivetune.data.local.entity.MusicGraphEdgeEntity
import moe.rukamori.archivetune.data.local.entity.MusicGraphPlaylistEntity
import moe.rukamori.archivetune.data.local.entity.MusicGraphTrackEntity
import moe.rukamori.archivetune.data.local.entity.MusicHomeCacheEntity
import moe.rukamori.archivetune.data.local.entity.MusicHomeChipEntity
import moe.rukamori.archivetune.data.local.entity.NoteEntity
import moe.rukamori.archivetune.data.local.entity.NotificationEntity
import moe.rukamori.archivetune.data.local.entity.PlaylistEntity
import moe.rukamori.archivetune.data.local.entity.PlaylistVideoCrossRef
import moe.rukamori.archivetune.data.local.entity.RecognitionHistoryEntity
import moe.rukamori.archivetune.data.local.entity.SubscriptionFeedEntity
import moe.rukamori.archivetune.data.local.entity.SubscriptionGroupEntity
import moe.rukamori.archivetune.data.local.entity.SyncLogEntity
import moe.rukamori.archivetune.data.local.entity.SyncPeerEntity
import moe.rukamori.archivetune.data.local.entity.VideoEntity
import moe.rukamori.archivetune.data.local.entity.WatchHistoryEntity
import moe.rukamori.archivetune.data.local.migrations.MIGRATIONS
import moe.rukamori.archivetune.data.local.migrations.Migration24To25
import moe.rukamori.archivetune.data.profile.ProfileFiles

@Database(
    entities = [
        VideoEntity::class,
        PlaylistEntity::class,
        PlaylistVideoCrossRef::class,
        NotificationEntity::class,
        SubscriptionFeedEntity::class,
        MusicHomeCacheEntity::class,
        MusicHomeChipEntity::class,
        DownloadEntity::class,
        DownloadItemEntity::class,
        WatchHistoryEntity::class,
        HomeFeedCacheEntity::class,
        SubscriptionGroupEntity::class,
        RecognitionHistoryEntity::class,
        SyncLogEntity::class,
        SyncPeerEntity::class,
        MusicGraphTrackEntity::class,
        MusicGraphArtistEntity::class,
        MusicGraphAlbumEntity::class,
        MusicGraphPlaylistEntity::class,
        MusicGraphEdgeEntity::class,
        NoteEntity::class,
        DownloadCollectionEntity::class,
        DownloadCollectionItemEntity::class,
    ],
    autoMigrations = [
        AutoMigration(from = 24, to = 25, spec = Migration24To25::class),
        AutoMigration(from = 25, to = 26),
        AutoMigration(from = 26, to = 27),
        AutoMigration(from = 27, to = 28),
        AutoMigration(from = 28, to = 29),
        AutoMigration(from = 29, to = 30),
        AutoMigration(from = 30, to = 31),
        AutoMigration(from = 31, to = 32),
        AutoMigration(from = 32, to = 33),
    ],
    version = 33,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao

    abstract fun playlistDao(): PlaylistDao

    abstract fun notificationDao(): NotificationDao

    abstract fun noteDao(): NoteDao

    abstract fun cacheDao(): CacheDao

    abstract fun downloadDao(): DownloadDao

    abstract fun downloadCollectionDao(): DownloadCollectionDao

    abstract fun watchHistoryDao(): WatchHistoryDao

    abstract fun homeFeedCacheDao(): HomeFeedCacheDao

    abstract fun subscriptionGroupDao(): SubscriptionGroupDao

    abstract fun recognitionHistoryDao(): RecognitionHistoryDao

    abstract fun syncLogDao(): SyncLogDao

    abstract fun syncPeerDao(): SyncPeerDao

    abstract fun musicGraphDao(): MusicGraphDao

    companion object {
        const val DEFAULT_NAME = "flow_database"

        @Volatile
        private var deviceDatabase: AppDatabase? = null

        @Volatile
        private var profileDatabase: AppDatabase? = null

        /** Default's file, which also holds the download tables every profile shares. */
        fun device(context: Context): AppDatabase =
            deviceDatabase ?: synchronized(this) {
                deviceDatabase ?: open(context.applicationContext, DEFAULT_NAME).also { deviceDatabase = it }
            }

        /** The running profile's own file. With Default running it is [device]: Room must never open one file twice. */
        fun profile(context: Context): AppDatabase =
            profileDatabase ?: synchronized(this) {
                profileDatabase ?: ProfileFiles
                    .database(context, DEFAULT_NAME)
                    .let { name -> if (name == DEFAULT_NAME) device(context) else open(context.applicationContext, name) }
                    .also { profileDatabase = it }
            }

        internal fun open(
            context: Context,
            name: String,
        ): AppDatabase =
            databaseBuilder(context, AppDatabase::class.java, name)
                .addMigrations(*MIGRATIONS)
                .fallbackToDestructiveMigration(false)
                .build()
    }
}
