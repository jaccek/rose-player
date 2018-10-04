package com.github.jaccek.roseplayer.presentation.playingqueue

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.jaccek.roseplayer.dto.Song
import com.github.jaccek.roseplayer.player.PlayerController

class PlayingQueueAdapter(
    private val playerController: PlayerController
) : RecyclerView.Adapter<PlayingQueueItemViewHolder>() {

    private val items = mutableListOf<Song>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        index: Int
    ): PlayingQueueItemViewHolder {
        return PlayingQueueItemViewHolder.create(
            LayoutInflater.from(parent.context),
            playerController
        )
    }

    override fun getItemCount(): Int =
        items.count()

    override fun onBindViewHolder(viewHolder: PlayingQueueItemViewHolder, index: Int) {
        viewHolder.bind(items[index])
    }

    fun addItems(songs: List<Song>) {
        items.clear()
        items.addAll(songs)
        notifyDataSetChanged()
    }
}