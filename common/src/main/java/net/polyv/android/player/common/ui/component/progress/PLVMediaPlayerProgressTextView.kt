package net.polyv.android.player.common.ui.component.progress

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.utils.data.PLVTimeUtils
import net.polyv.android.player.sdk.foundation.graphics.isLandscape
import net.polyv.android.player.sdk.foundation.lang.watchStates

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerProgressTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    private val progressTv by lazy { findViewById<TextView>(R.id.plv_media_player_ui_component_media_progress_text_progress) }
    private val durationTv by lazy { findViewById<TextView>(R.id.plv_media_player_ui_component_media_progress_text_duration) }

    init {
        LayoutInflater.from(getContext())
            .inflate(R.layout.plv_media_player_ui_component_media_progress_text_layout, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val mediaViewModel = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaViewModel>()
        val mediaControllerViewModel = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaControllerViewModel>()

        watchStates {
            val progress = mediaViewModel.mediaPlayViewState.value?.currentProgress ?: 0
            val duration = mediaViewModel.mediaPlayViewState.value?.duration ?: 0
            if (progress > 0) {
                progressTv.text = PLVTimeUtils.formatTime(progress)
            }
            if (duration > 0) {
                durationTv.text = PLVTimeUtils.formatTime(duration)
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

}
