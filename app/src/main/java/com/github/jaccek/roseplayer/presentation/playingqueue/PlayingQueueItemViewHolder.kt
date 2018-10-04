package com.github.jaccek.roseplayer.presentation.playingqueue

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import com.github.jaccek.roseplayer.databinding.SingleSongListItemBinding
import com.github.jaccek.roseplayer.dto.Song
import com.github.jaccek.roseplayer.player.PlayerController

class PlayingQueueItemViewHolder(
    private val binding: SingleSongListItemBinding,
    private val playerController: PlayerController
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(layoutInflater: LayoutInflater, playerController: PlayerController) =
            PlayingQueueItemViewHolder(
                SingleSongListItemBinding.inflate(layoutInflater),
                playerController
            )
    }

    fun bind(song: Song) {
        binding.textView.text = song.title

        binding.textView.setOnClickListener {
            playerController.play(song)
        }
    }
}
