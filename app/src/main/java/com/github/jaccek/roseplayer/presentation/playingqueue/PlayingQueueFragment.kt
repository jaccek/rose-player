package com.github.jaccek.roseplayer.presentation.playingqueue


import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.jaccek.roseplayer.Manifest
import com.github.jaccek.roseplayer.databinding.FragmentSongsListBinding
import com.github.jaccek.roseplayer.dto.PlayingState
import com.github.jaccek.roseplayer.player.PlayerController
import com.github.jaccek.roseplayer.presentation.playerwidget.PlayerWidget
import org.koin.android.ext.android.inject


// TODO: block clicking when song is not chosen
class PlayingQueueFragment
    : Fragment(), PlayerWidget.PlayPauseButtonListener {

    companion object {
        private const val MY_PERMISSIONS_REQUEST_READ_MEDIA = 1
    }

    private lateinit var binding: FragmentSongsListBinding
    private val playerController: PlayerController by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSongsListBinding.inflate(inflater, container, false)

        val permissionCheck =
            ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_READ_MEDIA
            )
        } else {
            // TODO: mediaBrowser.subscribe()
        }

        binding.playerWidget.playPauseButtonListener = this
        binding.songsList.let {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = PlayingQueueAdapter(playerController)
        }

        // TODO: remember subscribtions and unsubscribe on onPause
        // TODO: retry subscribe
        playerController.playingStateChanges
            .subscribe(
                {
                    binding.playerWidget.buttonType = when (it) {
                        PlayingState.PLAYING -> PlayerWidget.ButtonType.PAUSE
                        else -> PlayerWidget.ButtonType.PLAY
                    }
                },
                { Log.e("PlayingQueueFragment", "Error", it) }
            )
        playerController.songChanges
            .subscribe(
                { binding.playerWidget.title = it.title },
                { Log.e("PlayingQueueFragment", "Error", it) }
            )
        playerController.queueChanges
            .subscribe(
                { (binding.songsList.adapter as PlayingQueueAdapter).addItems(it) },
                { Log.e("PlayingQueueFragment", "Error", it) }
            )

        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_MEDIA -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // TODO: mediaBrowser.subscribe()
            }

            else -> {
                // TODO: inform user about lack of permission
            }
        }
    }

    override fun onStart() {
        super.onStart()
        playerController.connect()
    }

    override fun onResume() {
        super.onResume()
        activity?.volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()
        playerController.disconnect()
    }

    override fun pauseButtonClicked() {
        playerController.pause()
    }

    override fun playButtonClicked() {
        playerController.resume()
    }
}
