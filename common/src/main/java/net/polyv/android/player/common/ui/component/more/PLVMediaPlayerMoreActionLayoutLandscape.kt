package net.polyv.android.player.common.ui.component.more

import android.content.Context
import androidx.constraintlayout.widget.Guideline
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import net.polyv.android.common.libs.lang.state.watchStates
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerFloatAction

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerMoreActionLayoutLandscape @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener {

    private val moreActionStartGuideLine by lazy { findViewById<Guideline>(R.id.plv_media_player_more_action_start_guide_line) }
    private val moreAudioModeActionView by lazy { findViewById<PLVMediaPlayerMoreLayoutAudioModeActionView>(R.id.plv_media_player_more_audio_mode_action_view) }
    private val moreFloatWindowActionView by lazy { findViewById<PLVMediaPlayerMoreLayoutFloatWindowActionView>(R.id.plv_media_player_more_float_window_action_view) }
    private val moreSubtitleActionView by lazy { findViewById<PLVMediaPlayerMoreLayoutSubtitleActionViewLand>(R.id.plv_media_player_more_subtitle_action_view) }
    private val moreDownloadActionView by lazy { findViewById<PLVMediaPlayerMoreLayoutDownloadActionView>(R.id.plv_media_player_more_download_action_view) }
    private val moreKnowledgeActionView by lazy { findViewById<PLVMediaPlayerMoreLayoutKnowledgeActionView>(R.id.plv_media_player_more_knowledge_action_view) }
    private val moreActionCloseIv by lazy { findViewById<ImageView>(R.id.plv_media_player_more_action_close_iv) }

    init {
        LayoutInflater.from(context).inflate(R.layout.plv_media_player_ui_component_more_action_layout_landscape, this)
        setOnClickListener(this)
        moreActionCloseIv.setOnClickListener(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!

        watchStates {
            val infoState = dependScope.get<PLVMPMediaViewModel>().mediaInfoViewState.value ?: return@watchStates
            val currentSupportMediaOutputModes = infoState.supportOutputModes
            val showAudioModeAction = currentSupportMediaOutputModes.contains(PLVMediaOutputMode.AUDIO_ONLY)
            moreAudioModeActionView.visibility = if (showAudioModeAction) VISIBLE else GONE
        }.disposeOnDetached(this)

        watchStates {
            val controllerState = dependScope.get<PLVMPMediaControllerViewModel>().mediaControllerViewState.value
            val isVisible = controllerState?.lastFloatActionLayout == PLVMPMediaControllerFloatAction.MORE
            visibility = if (isVisible) VISIBLE else GONE
        }.disposeOnDetached(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            moreActionCloseIv.id -> closeLayout()
            this.id -> closeLayout()
        }
    }

    private fun closeLayout() {
        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaControllerViewModel>()
            .popFloatActionLayout()
    }
}
