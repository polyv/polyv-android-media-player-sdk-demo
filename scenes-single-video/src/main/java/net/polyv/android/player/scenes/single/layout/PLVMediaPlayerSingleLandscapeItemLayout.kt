package net.polyv.android.player.scenes.single.layout

import android.content.Context
import android.graphics.Rect
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.ui.component.PLVMediaPlayerAudioModeCoverLayoutLandscape
import net.polyv.android.player.common.ui.component.PLVMediaPlayerAutoContinueHintLayout
import net.polyv.android.player.common.ui.component.PLVMediaPlayerAuxiliaryViewContainer
import net.polyv.android.player.common.ui.component.PLVMediaPlayerBackImageView
import net.polyv.android.player.common.ui.component.PLVMediaPlayerBitRateSelectLayoutLandscape
import net.polyv.android.player.common.ui.component.PLVMediaPlayerBitRateTextView
import net.polyv.android.player.common.ui.component.PLVMediaPlayerBrightnessVolumeHintLayout
import net.polyv.android.player.common.ui.component.PLVMediaPlayerControllerGradientMaskLayout
import net.polyv.android.player.common.ui.component.PLVMediaPlayerGestureBlockWindowInsetsMotionEventLayout
import net.polyv.android.player.common.ui.component.PLVMediaPlayerGestureBrightnessVolumeControlLayout
import net.polyv.android.player.common.ui.component.PLVMediaPlayerGestureHorizontalDragControlLayout
import net.polyv.android.player.common.ui.component.PLVMediaPlayerGestureLongPressSpeedControlLayout
import net.polyv.android.player.common.ui.component.PLVMediaPlayerLockControllerImageView
import net.polyv.android.player.common.ui.component.PLVMediaPlayerLongPressSpeedHintLayout
import net.polyv.android.player.common.ui.component.PLVMediaPlayerMoreActionImageView
import net.polyv.android.player.common.ui.component.PLVMediaPlayerMoreActionLayoutLandscape
import net.polyv.android.player.common.ui.component.PLVMediaPlayerMoreSubtitleSettingLayoutLand
import net.polyv.android.player.common.ui.component.PLVMediaPlayerPlayButtonLandscape
import net.polyv.android.player.common.ui.component.PLVMediaPlayerPlayCompleteManualRestartOverlayLayout
import net.polyv.android.player.common.ui.component.PLVMediaPlayerPlayErrorOverlayLayout
import net.polyv.android.player.common.ui.component.PLVMediaPlayerProgressSeekBar
import net.polyv.android.player.common.ui.component.PLVMediaPlayerProgressTextView
import net.polyv.android.player.common.ui.component.PLVMediaPlayerSpeedSelectLayoutLandscape
import net.polyv.android.player.common.ui.component.PLVMediaPlayerSpeedTextView
import net.polyv.android.player.common.ui.component.PLVMediaPlayerTitleTextView
import net.polyv.android.player.common.utils.ui.PLVOnDoubleClickListener
import net.polyv.android.player.scenes.single.R
import net.polyv.android.player.sdk.foundation.graphics.dp
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState.Companion.rememberStateOf

/**
 * @author Hoshiiro
 *
 *
 * 横向-全屏 播放器皮肤 layout
 */
class PLVMediaPlayerSingleLandscapeItemLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // <editor-fold defaultstate="collapsed" desc="Layout-views">
    private val videoLayout by lazy { findViewById<ConstraintLayout>(R.id.plv_media_player_video_layout) }
    private val videoViewContainer by lazy { findViewById<FrameLayout>(R.id.plv_media_player_video_view_container) }
    private val audioModeCoverPortrait by lazy { findViewById<PLVMediaPlayerAudioModeCoverLayoutLandscape>(R.id.plv_media_player_audio_mode_cover_portrait) }
    private val brightnessVolumeControlLayout by lazy {
        findViewById<PLVMediaPlayerGestureBrightnessVolumeControlLayout>(
            R.id.plv_media_player_brightness_volume_control_layout
        )
    }
    private val longPressSpeedControlLayout by lazy { findViewById<PLVMediaPlayerGestureLongPressSpeedControlLayout>(R.id.plv_media_player_long_press_speed_control_layout) }
    private val horizontalDragControlLayout by lazy { findViewById<PLVMediaPlayerGestureHorizontalDragControlLayout>(R.id.plv_media_player_horizontal_drag_control_layout) }
    private val controllerGradientMaskLayout by lazy { findViewById<PLVMediaPlayerControllerGradientMaskLayout>(R.id.plv_media_player_controller_gradient_mask_layout) }
    private val completeOverlayLayout by lazy { findViewById<PLVMediaPlayerPlayCompleteManualRestartOverlayLayout>(R.id.plv_media_player_complete_overlay_layout) }
    private val errorOverlayLayout by lazy { findViewById<PLVMediaPlayerPlayErrorOverlayLayout>(R.id.plv_media_player_error_overlay_layout) }
    private val backIv by lazy { findViewById<PLVMediaPlayerBackImageView>(R.id.plv_media_player_back_iv) }
    private val titleTv by lazy { findViewById<PLVMediaPlayerTitleTextView>(R.id.plv_media_player_title_tv) }
    private val moreActionButton by lazy { findViewById<PLVMediaPlayerMoreActionImageView>(R.id.plv_media_player_more_action_button) }
    private val lockControllerIv by lazy { findViewById<PLVMediaPlayerLockControllerImageView>(R.id.plv_media_player_lock_controller_iv) }
    private val playButton by lazy { findViewById<PLVMediaPlayerPlayButtonLandscape>(R.id.plv_media_player_play_button) }
    private val progressTextView by lazy { findViewById<PLVMediaPlayerProgressTextView>(R.id.plv_media_player_progress_text_view) }
    private val bitRateTextView by lazy { findViewById<PLVMediaPlayerBitRateTextView>(R.id.plv_media_player_bit_rate_text_view) }
    private val speedTextView by lazy { findViewById<PLVMediaPlayerSpeedTextView>(R.id.plv_media_player_speed_text_view) }
    private val progressSeekBar by lazy { findViewById<PLVMediaPlayerProgressSeekBar>(R.id.plv_media_player_progress_seek_bar) }
    private val autoContinueHintLayout by lazy { findViewById<PLVMediaPlayerAutoContinueHintLayout>(R.id.plv_media_player_auto_continue_hint_layout) }
    private val brightnessVolumeHintLayout by lazy { findViewById<PLVMediaPlayerBrightnessVolumeHintLayout>(R.id.plv_media_player_brightness_volume_hint_layout) }
    private val longPressSpeedHintLayout by lazy { findViewById<PLVMediaPlayerLongPressSpeedHintLayout>(R.id.plv_media_player_long_press_speed_hint_layout) }
    private val auxiliaryViewContainer by lazy { findViewById<PLVMediaPlayerAuxiliaryViewContainer>(R.id.plv_media_player_auxiliary_view_container) }
    private val moreActionLayout by lazy { findViewById<PLVMediaPlayerMoreActionLayoutLandscape>(R.id.plv_media_player_more_action_layout) }
    private val bitRateSelectLayout by lazy { findViewById<PLVMediaPlayerBitRateSelectLayoutLandscape>(R.id.plv_media_player_bit_rate_select_layout) }
    private val speedSelectLayout by lazy { findViewById<PLVMediaPlayerSpeedSelectLayoutLandscape>(R.id.plv_media_player_speed_select_layout) }
    private val subtitleSettingLayout by lazy { findViewById<PLVMediaPlayerMoreSubtitleSettingLayoutLand>(R.id.plv_media_player_subtitle_setting_layout) }

    private val gestureBlockWindowInsetsMotionEventLayout = PLVMediaPlayerGestureBlockWindowInsetsMotionEventLayout(
        context
    )

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-属性-数据">

    // 当前视频尺寸
    private var currentVideoSize: Rect? = null

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-初始化 & 设置监听">
    init {
        LayoutInflater.from(context).inflate(R.layout.plv_media_player_single_landscape_item_layout, this)
        setOnClickListener()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!

        // 监听 视视频尺寸 变化，触发 皮肤UI尺寸 更新
        dependScope.get(PLVMPMediaViewModel::class.java)
            .mediaInfoViewState
            .observeUntilViewDetached(this) { viewState ->
                currentVideoSize = viewState.videoSize
                onViewStateChanged()
            }
    }

    private fun onViewStateChanged() {
        rememberStateOf("onChangeVideoSize")
            .compareLastAndSet(currentVideoSize)
            .ifNotEquals {
                onChangeVideoSize()
            }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-播放器皮肤-点击事件处理">
    private fun setOnClickListener() {
        setOnClickListener(object : PLVOnDoubleClickListener() {
            override fun onSingleClick() {
                val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this@PLVMediaPlayerSingleLandscapeItemLayout)
                    .current()
                    ?: return
                dependScope.get(PLVMPMediaControllerViewModel::class.java).onClickChangeControllerVisible()
            }

            override fun onDoubleClick() {
                val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this@PLVMediaPlayerSingleLandscapeItemLayout)
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
        if (brightnessVolumeControlLayout.handleOnTouchEvent(event)) {
            return true
        }
        if (horizontalDragControlLayout.handleOnTouchEvent(event)) {
            return true
        }
        if (longPressSpeedControlLayout.handleOnTouchEvent(event)) {
            return true
        }
        return super.onTouchEvent(event)
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-播放器皮肤-设置裸播放器到皮肤容器">
    fun setVideoView(videoView: View?) {
        videoViewContainer.removeAllViews()
        if (videoView != null && videoView.parent != null) {
            // 把裸播放器从原来的父容器中移除，比如从小窗容器中移除
            (videoView.parent as ViewGroup).removeView(videoView)
        }
        if (videoView != null && videoView.parent == null) {
            // 把裸播放器添加到当前的皮肤容器中
            videoViewContainer.addView(videoView)
        }
    }

    fun setAuxiliaryVideoView(auxiliaryVideoView: View?) {
        auxiliaryViewContainer.setAuxiliaryVideoView(auxiliaryVideoView)
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-响应视频尺寸变化的处理逻辑">
    private fun onChangeVideoSize() {
        if (currentVideoSize == null || currentVideoSize!!.width() == 0 || currentVideoSize!!.height() == 0) {
            return
        }
        val isVideoPortrait = currentVideoSize!!.width() < currentVideoSize!!.height()

        val containerParam = videoViewContainer.layoutParams as ConstraintLayout.LayoutParams
        if (isVideoPortrait) {
            containerParam.width = ViewGroup.LayoutParams.MATCH_PARENT
            containerParam.height = ViewGroup.LayoutParams.MATCH_PARENT
            containerParam.rightMargin = 72.dp().px()
            containerParam.leftMargin = containerParam.rightMargin
        } else {
            containerParam.width = ViewGroup.LayoutParams.WRAP_CONTENT
            containerParam.height = ViewGroup.LayoutParams.WRAP_CONTENT
            containerParam.rightMargin = 0
            containerParam.leftMargin = containerParam.rightMargin
        }
        videoViewContainer.layoutParams = containerParam
    }
    // </editor-fold>
}
