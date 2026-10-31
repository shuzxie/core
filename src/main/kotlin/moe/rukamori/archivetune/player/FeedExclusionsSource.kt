package moe.rukamori.archivetune.player

import moe.rukamori.archivetune.data.recommendation.FeedExclusions

/** What the viewer hid, so autoplay picked outside the player screen skips it too. */
fun interface FeedExclusionsSource {
    suspend fun current(): FeedExclusions
}
