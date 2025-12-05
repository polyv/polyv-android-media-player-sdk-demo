package net.polyv.android.player.common.ui.component

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import android.util.AttributeSet
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.core.api.listener.event.PLVMediaPlayerOnInfoEvent
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerState
import net.polyv.android.player.sdk.foundation.lang.MutableState
import net.polyv.android.player.sdk.foundation.lang.watchStates

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerPlayButtonPortraitFullScreen @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {
    private val isFirstFrameRendered = MutableState(false)

    init {
        setImageResource(R.drawable.plv_media_player_play_button_icon_to_play_portrait_full_screen)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val mediaViewModel = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaViewModel>()

        mediaViewModel.onInfoEvent
            .observe { event ->
                if (event.what == PLVMediaPlayerOnInfoEvent.MEDIA_INFO_VIDEO_RENDERING_START) {
                    isFirstFrameRendered.setValue(true)
                }
            }
            .disposeOnDetached(this)

        watchStates {
            val isPaused = mediaViewModel.mediaPlayViewState.value?.playerState == PLVMediaPlayerState.STATE_PAUSED
            val isFirstFrameRendered = isFirstFrameRendered.value ?: false
            val isVisible = isPaused && isFirstFrameRendered
            visibility = if (isVisible) VISIBLE else GONE
        }.disposeOnDetached(this)
    }

}
