package net.polyv.android.player.common.ui.component.gesture

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.utils.ui.PLVViewUtil

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerBrightnessVolumeHintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val brightnessVolumeIv by lazy { findViewById<ImageView>(R.id.plv_media_player_brightness_volume_iv) }
    private val brightnessVolumeProgress by lazy { findViewById<ProgressBar>(R.id.plv_media_player_brightness_volume_progress) }

    init {
        LayoutInflater.from(context).inflate(R.layout.plv_media_player_ui_component_brightness_volume_hint_layout, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
        val controllerViewModel = dependScope.get<PLVMPMediaControllerViewModel>()

        controllerViewModel.brightnessUpdateEvent.observe { brightness ->
            brightnessVolumeIv.setImageResource(R.drawable.plv_media_player_brightness_hint_icon)
            brightnessVolumeProgress.progress = brightness
            PLVViewUtil.showViewForDuration(this, 2000)
        }.disposeOnDetached(this)

        controllerViewModel.volumeUpdateEvent.observe { volume ->
            brightnessVolumeIv.setImageResource(R.drawable.plv_media_player_volume_hint_icon)
            brightnessVolumeProgress.progress = volume
            PLVViewUtil.showViewForDuration(this, 2000)
        }.disposeOnDetached(this)
    }

}
