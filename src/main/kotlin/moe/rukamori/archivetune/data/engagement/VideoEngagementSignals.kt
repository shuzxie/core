package moe.rukamori.archivetune.data.engagement

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import moe.rukamori.archivetune.data.model.Video
import moe.rukamori.archivetune.data.recommendation.FlowNeuroEngine
import moe.rukamori.archivetune.data.recommendation.InteractionType
import moe.rukamori.archivetune.data.repository.YouTubeRepository
import javax.inject.Inject

/**
 * The learning signals an engagement action feeds the recommendation engine.
 *
 * [FlowNeuroEngine] is still reached through a context-keyed global, so this is the one place that
 * touches it: every engagement caller injects this instead, which keeps the global access isolated
 * behind a dependency a test can replace.
 */
class VideoEngagementSignals
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val repository: YouTubeRepository,
    ) {
        suspend fun channelSubscriptionChanged(
            channelId: String,
            channelName: String,
            subscribed: Boolean,
        ) = FlowNeuroEngine.onChannelSubscriptionChanged(context, channelId, channelName, subscribed)

        suspend fun videoInteraction(
            video: Video,
            interactionType: InteractionType,
        ) = FlowNeuroEngine.onVideoInteraction(context, video, interactionType)

        /** Learns a newly subscribed channel's declared keyword tags. */
        suspend fun channelTagsLearned(channelId: String) = repository.learnChannelTags(context, channelId)
    }
