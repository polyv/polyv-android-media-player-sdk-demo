package net.polyv.android.player.scenes.feed.item.layout

import android.content.Context
import android.graphics.Rect
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.ui.component.PLVMediaPlayerAudioModeCoverLayoutPortrait
import net.polyv.android.player.common.ui.component.PLVMediaPlayerAutoContinueHintFeedStyleLayout
import net.polyv.android.player.common.ui.component.PLVMediaPlayerBackImageView
import net.polyv.android.player.common.ui.component.PLVMediaPlayerGestureLongPressSpeedControlLayout
import net.polyv.android.player.common.ui.component.PLVMediaPlayerLongPressSpeedHintLayout
import net.polyv.android.player.common.ui.component.PLVMediaPlayerMoreActionImageView
import net.polyv.android.player.common.ui.component.PLVMediaPlayerMoreActionLayoutPortrait
import net.polyv.android.player.common.ui.component.PLVMediaPlayerPlayButtonPortraitFullScreen
import net.polyv.android.player.common.ui.component.PLVMediaPlayerPlayErrorOverlayLayout
import net.polyv.android.player.common.ui.component.PLVMediaPlayerProgressSeekBar
import net.polyv.android.player.common.ui.component.PLVMediaPlayerProgressTextView
import net.polyv.android.player.common.ui.component.PLVMediaPlayerSwitchToFullScreenButtonPortraitFullScreen
import net.polyv.android.player.common.ui.component.PLVMediaPlayerTitleTextView
import net.polyv.android.player.common.ui.component.PLVMediaPlayerVideoFirstImageView
import net.polyv.android.player.scenes.feed.R
import net.polyv.android.player.sdk.PLVDeviceManager.getNavigationBarHeight
import net.polyv.android.player.sdk.PLVDeviceManager.getStatusBarHeight
import net.polyv.android.player.sdk.foundation.graphics.dp
import net.polyv.android.player.sdk.foundation.graphics.getScreenWidth
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState.Companion.rememberStateOf
import net.polyv.android.player.sdk.foundation.lang.requireNotNull

/**
 * @author Hoshiiro
 *
 *
 * 纵向-全屏 播放器皮肤 layout
 */
class PLVMediaPlayerFeedPortraitItemLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="Layout-views">
    private val videoLayout by lazy { findViewById<ConstraintLayout>(R.id.plv_media_player_video_layout) }
    private val statusBarGuildLine by lazy { findViewById<Guideline>(R.id.plv_media_player_status_bar_guild_line) }
    private val navigationBarGuildLine by lazy { findViewById<Guideline>(R.id.plv_media_player_navigation_bar_guild_line) }
    private val videoFirstImageView by lazy { findViewById<PLVMediaPlayerVideoFirstImageView>(R.id.plv_media_player_video_first_image_view) }
    private val videoViewContainer by lazy { findViewById<FrameLayout>(R.id.plv_media_player_video_view_container) }
    private val audioModeCoverPortrait by lazy { findViewById<PLVMediaPlayerAudioModeCoverLayoutPortrait>(R.id.plv_media_player_audio_mode_cover_portrait) }
    private val errorOverlayLayout by lazy { findViewById<PLVMediaPlayerPlayErrorOverlayLayout>(R.id.plv_media_player_error_overlay_layout) }
    private val switchVideoModeBtn by lazy { findViewById<PLVMediaPlayerSwitchToFullScreenButtonPortraitFullScreen>(R.id.plv_media_player_switch_video_mode_btn) }
    private val longPressSpeedControlLayout by lazy { findViewById<PLVMediaPlayerGestureLongPressSpeedControlLayout>(R.id.plv_media_player_long_press_speed_control_layout) }
    private val playButton by lazy { findViewById<PLVMediaPlayerPlayButtonPortraitFullScreen>(R.id.plv_media_player_play_button) }
    private val backIv by lazy { findViewById<PLVMediaPlayerBackImageView>(R.id.plv_media_player_back_iv) }
    private val titleTv by lazy { findViewById<PLVMediaPlayerTitleTextView>(R.id.plv_media_player_title_tv) }
    private val moreActionButton by lazy { findViewById<PLVMediaPlayerMoreActionImageView>(R.id.plv_media_player_more_action_button) }
    private val progressTextView by lazy { findViewById<PLVMediaPlayerProgressTextView>(R.id.plv_media_player_progress_text_view) }
    private val progressSeekBar by lazy { findViewById<PLVMediaPlayerProgressSeekBar>(R.id.plv_media_player_progress_seek_bar) }
    private val autoContinueHintLayout by lazy { findViewById<PLVMediaPlayerAutoContinueHintFeedStyleLayout>(R.id.plv_media_player_auto_continue_hint_layout) }
    private val longPressSpeedHintLayout by lazy { findViewById<PLVMediaPlayerLongPressSpeedHintLayout>(R.id.plv_media_player_long_press_speed_hint_layout) }
    private val moreActionLayoutPortrait by lazy { findViewById<PLVMediaPlayerMoreActionLayoutPortrait>(R.id.plv_media_player_more_action_layout_portrait) }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-属性-数据">
    // 当前视频尺寸
    private var currentVideoRatioWidthHeight: Float = 0f

    // 当前视频播放形式 —— 正常模式（音频+视频）、音频模式
    private var currentMediaOutputMode: PLVMediaOutputMode? = null
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-初始化 & 设置监听">
    init {
        LayoutInflater.from(context).inflate(R.layout.plv_media_player_feed_portrait_item_layout, this)

        statusBarGuildLine.setGuidelineBegin(getStatusBarHeight().px())
        navigationBarGuildLine.setGuidelineEnd(getNavigationBarHeight().px())

        setOnClickListener(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        val dependScope = requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())

        dependScope.get(PLVMPMediaViewModel::class.java)
            .mediaInfoViewState
            .observeUntilViewDetached(this) { viewState ->
                currentVideoRatioWidthHeight = calculateWidthHeightRatio(viewState.videoSize)
                currentMediaOutputMode = viewState.outputMode
                onViewStateChanged()
            }
    }

    private fun onViewStateChanged() {
        rememberStateOf("onChangeVideoSize")
            .compareLastAndSet(currentVideoRatioWidthHeight, currentMediaOutputMode)
            .ifNotEquals {
                onChangeVideoSize()
            }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-播放器皮肤-点击事件处理">
    override fun onClick(v: View) {
        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current() ?: return
        val mediaViewModel = dependScope.get(PLVMPMediaViewModel::class.java)
        val isPlaying = mediaViewModel.mediaPlayViewState.value!!.isPlaying
        if (isPlaying) {
            mediaViewModel.pause()
        } else {
            mediaViewModel.start()
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-播放器皮肤-手势事件处理">
    override fun onTouchEvent(event: MotionEvent): Boolean {
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
            (videoView.parent as ViewGroup).removeView(videoView)
        }
        if (videoView != null && videoView.parent == null) {
            videoViewContainer.addView(videoView)
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-响应视频尺寸变化的处理逻辑">
    private fun onChangeVideoSize() {
        if (currentVideoRatioWidthHeight <= 0) {
            return
        }
        val isPortraitVideo = currentVideoRatioWidthHeight < 1
        var mediaOutputMode = currentMediaOutputMode
        if (mediaOutputMode == null) {
            mediaOutputMode = PLVMediaOutputMode.AUDIO_VIDEO
        }

        val lp = videoViewContainer.layoutParams as ConstraintLayout.LayoutParams
        if (mediaOutputMode == PLVMediaOutputMode.AUDIO_ONLY) {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp.height = 211.dp().px()
        } else {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp.height = (getScreenWidth().px() / currentVideoRatioWidthHeight).toInt()
        }
        if (isPortraitVideo && mediaOutputMode != PLVMediaOutputMode.AUDIO_ONLY) {
            lp.bottomToTop = ConstraintLayout.LayoutParams.UNSET
            lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        } else {
            lp.bottomToTop = progressSeekBar.id
            lp.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
        }
        videoViewContainer.layoutParams = lp
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="工具方法">
    private fun calculateWidthHeightRatio(size: Rect?): Float {
        if (size == null || size.width() == 0 || size.height() == 0) {
            return 0F
        }
        return size.width().toFloat() / size.height()
    }
    // </editor-fold>
}
