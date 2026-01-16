package net.polyv.android.player.scenes.single.layout

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.ui.component.PLVMediaPlayerAudioModeCoverLayoutPortrait
import net.polyv.android.player.common.ui.component.PLVMediaPlayerAutoContinueHintLayout
import net.polyv.android.player.common.ui.component.PLVMediaPlayerBackImageView
import net.polyv.android.player.common.ui.component.PLVMediaPlayerControllerGradientMaskLayout
import net.polyv.android.player.common.ui.component.PLVMediaPlayerPlayButtonLandscape
import net.polyv.android.player.common.ui.component.PLVMediaPlayerPlayCompleteManualRestartOverlayLayout
import net.polyv.android.player.common.ui.component.PLVMediaPlayerPlayErrorOverlayLayout
import net.polyv.android.player.common.ui.component.PLVMediaPlayerSwitchToFullScreenButtonPortraitHalfScreen
import net.polyv.android.player.common.ui.component.PLVMediaPlayerTitleTextView
import net.polyv.android.player.common.ui.component.PLVMediaPlayerVideoViewContainer
import net.polyv.android.player.common.ui.component.auxiliary.PLVMediaPlayerAuxiliaryViewContainer
import net.polyv.android.player.common.ui.component.gesture.PLVMediaPlayerBrightnessVolumeHintLayout
import net.polyv.android.player.common.ui.component.gesture.PLVMediaPlayerGestureBlockWindowInsetsMotionEventLayout
import net.polyv.android.player.common.ui.component.gesture.PLVMediaPlayerGestureHandleLayout
import net.polyv.android.player.common.ui.component.gesture.PLVMediaPlayerLongPressSpeedHintLayout
import net.polyv.android.player.common.ui.component.more.PLVMediaPlayerMoreActionImageView
import net.polyv.android.player.common.ui.component.progress.PLVMediaPlayerProgressSeekBar
import net.polyv.android.player.common.ui.component.progress.PLVMediaPlayerProgressTextView
import net.polyv.android.player.common.utils.ui.PLVOnDoubleClickListener
import net.polyv.android.player.scenes.single.R

/**
 * @author Hoshiiro
 *
 *
 * 纵向-半屏 播放器皮肤 layout
 */
class PLVMediaPlayerSinglePortraitItemLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // <editor-fold defaultstate="collapsed" desc="Layout-views">
    private val videoLayout by lazy { findViewById<ConstraintLayout>(R.id.plv_media_player_video_layout) }
    private val videoViewContainer by lazy { findViewById<PLVMediaPlayerVideoViewContainer>(R.id.plv_media_player_video_view_container) }
    private val audioModeCoverPortrait by lazy { findViewById<PLVMediaPlayerAudioModeCoverLayoutPortrait>(R.id.plv_media_player_audio_mode_cover_portrait) }
    private val gestureHandleLayout by lazy { findViewById<PLVMediaPlayerGestureHandleLayout>(R.id.plv_media_player_gesture_handle_layout) }
    private val controllerGradientMaskLayout by lazy { findViewById<PLVMediaPlayerControllerGradientMaskLayout>(R.id.plv_media_player_controller_gradient_mask_layout) }
    private val completeOverlayLayout by lazy { findViewById<PLVMediaPlayerPlayCompleteManualRestartOverlayLayout>(R.id.plv_media_player_complete_overlay_layout) }
    private val errorOverlayLayout by lazy { findViewById<PLVMediaPlayerPlayErrorOverlayLayout>(R.id.plv_media_player_error_overlay_layout) }
    private val backIv by lazy { findViewById<PLVMediaPlayerBackImageView>(R.id.plv_media_player_back_iv) }
    private val titleTv by lazy { findViewById<PLVMediaPlayerTitleTextView>(R.id.plv_media_player_title_tv) }
    private val moreActionButton by lazy { findViewById<PLVMediaPlayerMoreActionImageView>(R.id.plv_media_player_more_action_button) }
    private val playButton by lazy { findViewById<PLVMediaPlayerPlayButtonLandscape>(R.id.plv_media_player_play_button) }
    private val progressTextView by lazy { findViewById<PLVMediaPlayerProgressTextView>(R.id.plv_media_player_progress_text_view) }
    private val switchVideoModeBtn by lazy { findViewById<PLVMediaPlayerSwitchToFullScreenButtonPortraitHalfScreen>(R.id.plv_media_player_switch_video_mode_btn) }
    private val progressSeekBar by lazy { findViewById<PLVMediaPlayerProgressSeekBar>(R.id.plv_media_player_progress_seek_bar) }
    private val autoContinueHintLayout by lazy { findViewById<PLVMediaPlayerAutoContinueHintLayout>(R.id.plv_media_player_auto_continue_hint_layout) }
    private val brightnessVolumeHintLayout by lazy { findViewById<PLVMediaPlayerBrightnessVolumeHintLayout>(R.id.plv_media_player_brightness_volume_hint_layout) }
    private val longPressSpeedHintLayout by lazy { findViewById<PLVMediaPlayerLongPressSpeedHintLayout>(R.id.plv_media_player_long_press_speed_hint_layout) }
    private val auxiliaryViewContainer by lazy { findViewById<PLVMediaPlayerAuxiliaryViewContainer>(R.id.plv_media_player_auxiliary_view_container) }

    private val gestureBlockWindowInsetsMotionEventLayout = PLVMediaPlayerGestureBlockWindowInsetsMotionEventLayout(
        context
    )
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-初始化 & 设置监听">
    init {
        LayoutInflater.from(context).inflate(R.layout.plv_media_player_single_portrait_item_layout, this)
        setOnClickListener()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-播放器皮肤-点击事件处理">
    private fun setOnClickListener() {
        setOnClickListener(object : PLVOnDoubleClickListener() {
            override fun onSingleClick() {
                val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this@PLVMediaPlayerSinglePortraitItemLayout)
                    .current()
                    ?: return
                dependScope.get(PLVMPMediaControllerViewModel::class.java).onClickChangeControllerVisible()
            }

            override fun onDoubleClick() {
                val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this@PLVMediaPlayerSinglePortraitItemLayout)
                    .current()
                    ?: return
                val mediaViewModel = dependScope.get(PLVMPMediaViewModel::class.java)
                val viewState = mediaViewModel.mediaPlayViewState.value ?: return
                if (viewState.isPlaying) {
                    mediaViewModel.pause()
                } else {
                    mediaViewModel.start()
                }
            }
        })
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-播放器皮肤-手势事件处理">
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gestureBlockWindowInsetsMotionEventLayout.handleOnTouchEvent(event)) {
            return true
        }
        if (gestureHandleLayout.handleOnTouchEvent(event)) {
            return true
        }
        return super.onTouchEvent(event)
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-播放器皮肤-设置裸播放器到皮肤容器">
    fun setVideoView(videoView: View?) {
        videoViewContainer.setVideoView(videoView)
    }

    fun setAuxiliaryVideoView(auxiliaryVideoView: View?) {
        auxiliaryViewContainer.setAuxiliaryVideoView(auxiliaryVideoView)
    }
    // </editor-fold>
}
