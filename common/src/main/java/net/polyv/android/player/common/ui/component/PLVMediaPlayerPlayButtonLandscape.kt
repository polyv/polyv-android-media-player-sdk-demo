package net.polyv.android.player.common.ui.component

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.View
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.sdk.foundation.graphics.isLandscape
import net.polyv.android.player.sdk.foundation.lang.watchStates

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerPlayButtonLandscape @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle), View.OnClickListener {

    init {
        setOnClickListener(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val mediaViewModel = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaViewModel>()
        val mediaControllerViewModel = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaControllerViewModel>()

        watchStates {
            val isPlaying = mediaViewModel.mediaPlayViewState.value?.isPlaying ?: false
            if (isPlaying) {
                setImageResource(R.drawable.plv_media_player_play_button_icon_to_pause_landscape)
            } else {
                setImageResource(R.drawable.plv_media_player_play_button_icon_to_play_landscape)
            }
        }.disposeOnDetached(this)

        watchStates {
            val controllerState = mediaControllerViewModel.mediaControllerViewState.value ?: return@watchStates
            val isVisible = controllerState.controllerVisible
                    && !controllerState.isMediaStopOverlayVisible
                    && !controllerState.controllerLocking
                    && !(controllerState.isFloatActionLayoutVisible && isLandscape())
            visibility = if (isVisible) VISIBLE else GONE
        }.disposeOnDetached(this)

    }

    override fun onClick(v: View?) {
        val mediaViewModel = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaViewModel>()
        val isPlaying = mediaViewModel.mediaPlayViewState.value?.isPlaying ?: false
        if (isPlaying) {
            mediaViewModel.pause()
        } else {
            mediaViewModel.start()
        }
    }

}
