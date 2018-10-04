package com.github.jaccek.roseplayer.player

import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import com.github.jaccek.roseplayer.dto.Song
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor

class MediaBrowserSubscriptionCallback : MediaBrowserCompat.SubscriptionCallback() {

    private val queueChangesPublisher = BehaviorProcessor.create<List<Song>>()

    val queueChanges: Flowable<List<Song>>
        get() = queueChangesPublisher

    override fun onChildrenLoaded(
        parentId: String,
        children: MutableList<MediaBrowserCompat.MediaItem>
    ) {
        super.onChildrenLoaded(parentId, children)

        val songs = children.map { it.toSong() }
        queueChangesPublisher.onNext(songs)
    }

    private fun MediaBrowserCompat.MediaItem.toSong() =
        Song(
            id = this.description.mediaId?.toLong() ?: -1,
            title = this.description.title?.toString() ?: "",
            uri = this.description.mediaUri ?: Uri.EMPTY
        )
}