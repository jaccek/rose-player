package com.github.jaccek.roseplayer.player

import android.support.v4.media.MediaBrowserCompat
import com.github.jaccek.roseplayer.dto.Song
import com.github.jaccek.roseplayer.dto.toSong
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
}