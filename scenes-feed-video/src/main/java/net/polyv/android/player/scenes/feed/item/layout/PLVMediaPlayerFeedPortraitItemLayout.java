package net.polyv.android.player.scenes.feed.item.layout;

import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.dp;
import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.getScreenWidth;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaInfoViewState;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerAudioModeCoverLayoutPortrait;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerAutoContinueHintFeedStyleLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerBackImageView;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerLongPressSpeedControlLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerLongPressSpeedHintLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerMoreActionImageView;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerMoreActionLayoutPortrait;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerPlayButtonPortraitFullScreen;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerPlayErrorOverlayLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerProgressSeekBar;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerProgressTextView;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerSwitchToFullScreenButtonPortraitFullScreen;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerTitleTextView;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerVideoFirstImageView;
import net.polyv.android.player.scenes.feed.R;
import net.polyv.android.player.sdk.PLVDeviceManager;
import net.polyv.android.player.sdk.foundation.di.DependScope;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 * <p>
 * 纵向-全屏 播放器皮肤 layout
 */
public class PLVMediaPlayerFeedPortraitItemLayout extends FrameLayout implements View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="Layout-views">
    private ConstraintLayout videoLayout;
    private Guideline statusBarGuildLine;
    private Guideline navigationBarGuildLine;
    private PLVMediaPlayerVideoFirstImageView videoFirstImageView;
    private FrameLayout videoViewContainer;
    private PLVMediaPlayerAudioModeCoverLayoutPortrait audioModeCoverPortrait;
    private PLVMediaPlayerPlayErrorOverlayLayout errorOverlayLayout;
    private PLVMediaPlayerSwitchToFullScreenButtonPortraitFullScreen switchVideoModeBtn;
    private PLVMediaPlayerLongPressSpeedControlLayout longPressSpeedControlLayout;
    private PLVMediaPlayerPlayButtonPortraitFullScreen playButton;
    private PLVMediaPlayerBackImageView backIv;
    private PLVMediaPlayerTitleTextView titleTv;
    private PLVMediaPlayerMoreActionImageView moreActionButton;
    private PLVMediaPlayerProgressTextView progressTextView;
    private PLVMediaPlayerProgressSeekBar progressSeekBar;
    private PLVMediaPlayerAutoContinueHintFeedStyleLayout autoContinueHintLayout;
    private PLVMediaPlayerLongPressSpeedHintLayout longPressSpeedHintLayout;
    private PLVMediaPlayerMoreActionLayoutPortrait moreActionLayoutPortrait;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-属性-数据">
    // 当前视频尺寸
    protected float currentVideoRatioWidthHeight = 0;

    // 当前视频播放形式 —— 正常模式（音频+视频）、音频模式
    protected PLVMediaOutputMode currentMediaOutputMode = null;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-构造方法">
    public PLVMediaPlayerFeedPortraitItemLayout(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerFeedPortraitItemLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerFeedPortraitItemLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-初始化 & 设置监听">
    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_feed_portrait_item_layout, this);
        findViewById();

        statusBarGuildLine.setGuidelineBegin(PLVDeviceManager.getStatusBarHeight().px());
        navigationBarGuildLine.setGuidelineEnd(PLVDeviceManager.getNavigationBarHeight().px());

        setOnClickListener(this);
    }

    private void findViewById() {
        videoLayout = findViewById(R.id.plv_media_player_video_layout);
        statusBarGuildLine = findViewById(R.id.plv_media_player_status_bar_guild_line);
        navigationBarGuildLine = findViewById(R.id.plv_media_player_navigation_bar_guild_line);
        videoFirstImageView = findViewById(R.id.plv_media_player_video_first_image_view);
        videoViewContainer = findViewById(R.id.plv_media_player_video_view_container);
        audioModeCoverPortrait = findViewById(R.id.plv_media_player_audio_mode_cover_portrait);
        errorOverlayLayout = findViewById(R.id.plv_media_player_error_overlay_layout);
        switchVideoModeBtn = findViewById(R.id.plv_media_player_switch_video_mode_btn);
        longPressSpeedControlLayout = findViewById(R.id.plv_media_player_long_press_speed_control_layout);
        playButton = findViewById(R.id.plv_media_player_play_button);
        backIv = findViewById(R.id.plv_media_player_back_iv);
        titleTv = findViewById(R.id.plv_media_player_title_tv);
        moreActionButton = findViewById(R.id.plv_media_player_more_action_button);
        progressTextView = findViewById(R.id.plv_media_player_progress_text_view);
        progressSeekBar = findViewById(R.id.plv_media_player_progress_seek_bar);
        autoContinueHintLayout = findViewById(R.id.plv_media_player_auto_continue_hint_layout);
        longPressSpeedHintLayout = findViewById(R.id.plv_media_player_long_press_speed_hint_layout);
        moreActionLayoutPortrait = findViewById(R.id.plv_media_player_more_action_layout_portrait);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        final DependScope dependScope = requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current());

        dependScope.get(PLVMPMediaViewModel.class)
                .getMediaInfoViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaInfoViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaInfoViewState viewState) {
                        currentVideoRatioWidthHeight = calculateWidthHeightRatio(viewState.getVideoSize());
                        currentMediaOutputMode = viewState.getOutputMode();
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangeVideoSize")
                .compareLastAndSet(currentVideoRatioWidthHeight, currentMediaOutputMode)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onChangeVideoSize();
                        return null;
                    }
                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-播放器皮肤-点击事件处理">
    @Override
    public void onClick(View v) {
        final DependScope dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current();
        if (dependScope == null) {
            return;
        }
        final PLVMPMediaViewModel mediaViewModel = dependScope.get(PLVMPMediaViewModel.class);
        boolean isPlaying = mediaViewModel.getMediaPlayViewState().getValue().isPlaying();
        if (isPlaying) {
            mediaViewModel.pause();
        } else {
            mediaViewModel.start();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-播放器皮肤-手势事件处理">
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (longPressSpeedControlLayout.handleOnTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-播放器皮肤-设置裸播放器到皮肤容器">
    public void setVideoView(View videoView) {
        videoViewContainer.removeAllViews();
        if (videoView != null && videoView.getParent() != null) {
            ((ViewGroup) videoView.getParent()).removeView(videoView);
        }
        if (videoView != null && videoView.getParent() == null) {
            videoViewContainer.addView(videoView);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-响应视频尺寸变化的处理逻辑">
    protected void onChangeVideoSize() {
        if (currentVideoRatioWidthHeight <= 0) {
            return;
        }
        final boolean isPortraitVideo = currentVideoRatioWidthHeight < 1;
        PLVMediaOutputMode mediaOutputMode = currentMediaOutputMode;
        if (mediaOutputMode == null) {
            mediaOutputMode = PLVMediaOutputMode.AUDIO_VIDEO;
        }

        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) videoViewContainer.getLayoutParams();
        if (mediaOutputMode == PLVMediaOutputMode.AUDIO_ONLY) {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = dp(211).px();
        } else {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = (int) (getScreenWidth().px() / currentVideoRatioWidthHeight);
        }
        if (isPortraitVideo && mediaOutputMode != PLVMediaOutputMode.AUDIO_ONLY) {
            lp.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
            lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        } else {
            lp.bottomToTop = progressSeekBar.getId();
            lp.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
        }
        videoViewContainer.setLayoutParams(lp);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="工具方法">

    private float calculateWidthHeightRatio(Rect size) {
        if (size == null || size.width() == 0 || size.height() == 0) {
            return 0;
        }
        return (float) size.width() / size.height();
    }

    // </editor-fold>

}
