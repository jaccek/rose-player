package com.github.jaccek.roseplayer.presentation.playerwidget

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import com.github.jaccek.roseplayer.databinding.PlayerWidgetBinding

class PlayerWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    interface PlayPauseButtonListener {
        fun pauseButtonClicked()
        fun playButtonClicked()
    }

    enum class ButtonType {
        PLAY,
        PAUSE
    }

    var title: String
        get() = binding.titleTextView.text.toString()
        set(value) {
            binding.titleTextView.text = value
        }

    var buttonType: ButtonType = ButtonType.PLAY
        set(value) {
            binding.playPauseButton.text = value.name
            field = value
        }

    private val binding: PlayerWidgetBinding =
        PlayerWidgetBinding.inflate(LayoutInflater.from(context), this, true)

    var playPauseButtonListener: PlayPauseButtonListener? = null
        set(value) {
            field = value
            binding.playPauseButton.setOnClickListener {
                when (buttonType) {
                    ButtonType.PLAY -> {
                        buttonType = ButtonType.PAUSE
                        value?.playButtonClicked()
                    }
                    ButtonType.PAUSE -> {
                        buttonType = ButtonType.PLAY
                        value?.pauseButtonClicked()
                    }
                }
            }
        }
}
