package net.polyv.android.player.common.ui.component.more

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerFloatAction

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerMoreLayoutSubtitleActionViewLand @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener {
    private val subtitleActionIv by lazy { findViewById<ImageView>(R.id.plv_media_player_subtitle_action_iv) }
    private val subtitleActionTv by lazy { findViewById<TextView>(R.id.plv_media_player_subtitle_action_tv) }

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.plv_media_player_ui_component_more_subtitle_action_layout_land, this)
        setOnClickListener(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaViewModel>()
            .mediaInfoViewState
            .observe { viewState ->
                val isEnable = viewState.currentSubtitle != null && !viewState.currentSubtitle.isEmpty()
                val isVisible = !viewState.supportSubtitles.isEmpty()
                if (isEnable) {
                    subtitleActionIv!!.setImageResource(R.drawable.plv_media_player_more_subtitle_action_icon_land_enabled)
                } else {
                    subtitleActionIv!!.setImageResource(R.drawable.plv_media_player_more_subtitle_action_icon_land_disabled)
                }
                visibility = if (isVisible) VISIBLE else GONE
            }
            .disposeOnDetached(this)
    }

    override fun onClick(v: View?) {
        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
        dependScope?.get<PLVMPMediaControllerViewModel>()
            ?.popFloatActionLayout()
        dependScope?.get<PLVMPMediaControllerViewModel>()
            ?.pushFloatActionLayout(PLVMPMediaControllerFloatAction.SUBTITLE)
    }
}
