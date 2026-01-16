package net.polyv.android.player.common.ui.component.gesture

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerLongPressSpeedHintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val speedTv by lazy { findViewById<TextView>(R.id.plv_media_player_speed_tv) }
    private val speedHintTv by lazy { findViewById<TextView>(R.id.plv_media_player_speed_hint_tv) }

    init {
        LayoutInflater.from(context).inflate(R.layout.plv_media_player_ui_component_long_press_speed_hint_layout, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
        dependScope.get<PLVMPMediaControllerViewModel>()
            .mediaControllerViewState
            .observe { viewState: PLVMPMediaControllerViewState ->
                visibility = if (viewState.longPressSpeeding) VISIBLE else GONE
            }
            .disposeOnDetached(this)
    }

}
