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
import net.polyv.android.player.sdk.foundation.lang.watchStates

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerSeekProgressPreviewTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val progressTv by lazy { findViewById<TextView>(R.id.plv_media_player_ui_component_media_progress_text_progress) }
    private val durationTv by lazy { findViewById<TextView>(R.id.plv_media_player_ui_component_media_progress_text_duration) }

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.plv_media_player_ui_component_preview_image_progress_text_layout, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
        watchStates {
            val mediaViewModel = dependScope.get<PLVMPMediaViewModel>()
            val controllerViewModel = dependScope.get<PLVMPMediaControllerViewModel>()
            val hasProgressImage = mediaViewModel.mediaInfoViewState.value?.progressPreviewImage != null
            val currentDuration = mediaViewModel.mediaPlayViewState.value?.duration ?: 0
            val isVisible = controllerViewModel.mediaControllerViewState.value?.progressSeekBarDragging ?: false
            val dragPosition = controllerViewModel.mediaControllerViewState.value?.progressSeekBarDragPosition ?: 0

            visibility = if (isVisible) VISIBLE else GONE
            val textSizeSp = if (hasProgressImage) 20 else 28
            progressTv.textSize = textSizeSp.toFloat()
            durationTv.textSize = textSizeSp.toFloat()
            progressTv.text = PLVTimeUtils.formatTime(dragPosition)
            durationTv.text = PLVTimeUtils.formatTime(currentDuration)
        }.disposeOnDetached(this)
    }

}
