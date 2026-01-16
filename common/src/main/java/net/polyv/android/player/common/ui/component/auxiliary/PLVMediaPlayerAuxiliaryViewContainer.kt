package net.polyv.android.player.common.ui.component.auxiliary

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import net.polyv.android.common.libs.lang.ui.removeFromParent
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.auxiliary.viewmodel.PLVMPAuxiliaryViewModel
import net.polyv.android.player.sdk.foundation.lang.watchStates

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerAuxiliaryViewContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val auxiliaryVideoViewContainer by lazy { findViewById<FrameLayout>(R.id.plv_media_player_auxiliary_video_view_container) }
    private val auxiliaryCountDownTv by lazy { findViewById<PLVMediaPlayerAuxiliaryCountDownTextView>(R.id.plv_media_player_auxiliary_count_down_tv) }

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.plv_media_player_ui_component_auxiliary_view_container_layout, this)

        setOnClickListener {
            // 点击事件拦截，防止点击到下层
        }
    }

    fun setAuxiliaryVideoView(auxiliaryVideoView: View?) {
        auxiliaryVideoViewContainer.removeAllViews()
        auxiliaryVideoView?.removeFromParent()
        if (auxiliaryVideoView != null && auxiliaryVideoView.parent == null) {
            auxiliaryVideoViewContainer.addView(auxiliaryVideoView)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!

        watchStates {
            val infoState = dependScope.get<PLVMPAuxiliaryViewModel>().auxiliaryInfoViewState.value
            val isAdvertShowing = infoState != null && infoState.stage.isAuxiliaryStage()
            visibility = if (isAdvertShowing) VISIBLE else GONE
        }.disposeOnDetached(this)
    }

}
