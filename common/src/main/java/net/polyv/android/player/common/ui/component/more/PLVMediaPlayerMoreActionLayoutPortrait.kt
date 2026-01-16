package net.polyv.android.player.common.ui.component.more

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TextView
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerFloatAction
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState
import net.polyv.android.player.common.ui.component.PLVMediaPlayerBitRateSelectLayoutPortrait
import net.polyv.android.player.common.ui.component.PLVMediaPlayerSpeedSelectLayoutPortrait

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerMoreActionLayoutPortrait @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener {

    private val moreActionCloseIv by lazy { findViewById<ImageView>(R.id.plv_media_player_more_action_close_iv) }
    private val moreLayoutActionContainer by lazy { findViewById<ConstraintLayout>(R.id.plv_media_player_more_layout_action_container) }
    private val moreAudioModeActionView by lazy { findViewById<PLVMediaPlayerMoreLayoutAudioModeActionView>(R.id.plv_media_player_more_audio_mode_action_view) }
    private val moreFloatWindowActionView by lazy { findViewById<PLVMediaPlayerMoreLayoutFloatWindowActionView>(R.id.plv_media_player_more_float_window_action_view) }
    private val moreSubtitleActionView by lazy { findViewById<PLVMediaPlayerMoreLayoutSubtitleActionViewPort>(R.id.plv_media_player_more_subtitle_action_view) }
    private val moreDownloadActionView by lazy { findViewById<PLVMediaPlayerMoreLayoutDownloadActionView>(R.id.plv_media_player_more_download_action_view) }
    private val moreBitRateHintTv by lazy { findViewById<TextView>(R.id.plv_media_player_more_bit_rate_hint_tv) }
    private val moreBitRateLl by lazy { findViewById<PLVMediaPlayerBitRateSelectLayoutPortrait>(R.id.plv_media_player_more_bit_rate_ll) }
    private val moreBitRateGroup by lazy { findViewById<Group>(R.id.plv_media_player_more_bit_rate_group) }
    private val moreSpeedHintTv by lazy { findViewById<TextView>(R.id.plv_media_player_more_speed_hint_tv) }
    private val moreSpeedSv by lazy { findViewById<HorizontalScrollView>(R.id.plv_media_player_more_speed_sv) }
    private val moreSpeedLl by lazy { findViewById<PLVMediaPlayerSpeedSelectLayoutPortrait>(R.id.plv_media_player_more_speed_ll) }

    init {
        LayoutInflater.from(getContext())
            .inflate(R.layout.plv_media_player_ui_component_more_action_layout_portrait, this)
        setOnClickListener(this)
        moreActionCloseIv.setOnClickListener(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!

        dependScope.get<PLVMPMediaViewModel>()
            .mediaInfoViewState
            .observe { viewState ->
                val currentSupportMediaOutputModes = viewState.supportOutputModes
                val currentMediaBitRate = viewState.bitRate
                val currentMediaOutputMode = viewState.outputMode
                val showAudioModeAction = currentSupportMediaOutputModes.contains(PLVMediaOutputMode.AUDIO_ONLY)
                moreAudioModeActionView.visibility = if (showAudioModeAction) VISIBLE else GONE
                val bitRateVisible = currentMediaBitRate != null && currentMediaOutputMode != PLVMediaOutputMode.AUDIO_ONLY
                moreBitRateGroup.visibility = if (bitRateVisible) VISIBLE else GONE
            }
            .disposeOnDetached(this)

        dependScope.get<PLVMPMediaControllerViewModel>()
            .mediaControllerViewState
            .observe { viewState: PLVMPMediaControllerViewState ->
                val isVisible = viewState.lastFloatActionLayout == PLVMPMediaControllerFloatAction.MORE
                visibility = if (isVisible) VISIBLE else GONE
                parent?.requestDisallowInterceptTouchEvent(!isVisible)
                parent?.requestDisallowInterceptTouchEvent(isVisible)
            }
            .disposeOnDetached(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            moreActionCloseIv.id -> closeLayout()
            this.id -> closeLayout()
        }
    }

    private fun closeLayout() {
        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
            ?.get<PLVMPMediaControllerViewModel>()
            ?.popFloatActionLayout()
    }
}
