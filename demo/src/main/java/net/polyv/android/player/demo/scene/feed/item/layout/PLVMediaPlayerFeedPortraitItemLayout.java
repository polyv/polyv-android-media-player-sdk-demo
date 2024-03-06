package net.polyv.android.player.demo.scene.feed.item.layout;

import static com.plv.foundationsdk.component.event.PLVEventKt.observeUntilViewDetached;
import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeForeverUntilViewDetached;
import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.app.Activity;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import net.polyv.android.player.business.scene.common.model.api.vo.PLVVodVideoJsonVO;
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode;
import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerAudioModeCoverLayoutPortrait;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerBackImageView;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerLongPressSpeedControlLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerLongPressSpeedHintLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerMoreActionImageView;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerMoreActionLayoutPortrait;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerPlayButtonPortraitFullScreen;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerProgressSeekBar;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerProgressTextView;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerSwitchToFullScreenButtonPortraitFullScreen;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerTitleTextView;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerVideoFirstImageView;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowHelper;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager;
import net.polyv.android.player.common.ui.component.floatwindow.layout.PLVMediaPlayerFloatWindowContentLayout;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.common.utils.floatwindow.permission.PLVFloatPermissionUtils;
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerPlayingState;
import net.polyv.android.player.demo.R;
import net.polyv.android.player.sdk.PLVDeviceManager;
import net.polyv.android.player.sdk.PLVVideoView;

import java.lang.ref.WeakReference;

/**
 * @author Hoshiiro
 *
 *  纵向-全屏 播放器皮肤 layout
 */
public class PLVMediaPlayerFeedPortraitItemLayout extends FrameLayout implements View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="Layout-属性-UI-view属性和布局示意">
    /**
     * 纵向-全屏 播放器皮肤 layout 布局示意
     *
     * -- 播放器皮肤布局  itemLayout ：ConstraintLayout
     *   |-- 顶部状态栏控制边距 statusBarGuildLine ：Guideline
     *   |-- 底部导航栏控制边距 navigationBarGuildLine ：Guideline
     *   |-- 视频首图预览画面 firstImageView ：PLVMediaPlayerVideoFirstImageView
     *   |-- 裸播放器容器 videoViewContainer ：FrameLayout
     *   |-- 音频模式覆盖层 audioModeCoverLayout ：PLVMediaPlayerAudioModeCoverLayoutPortrait
     *   |-- 切换全屏按钮 switchToFullScreenButton : PLVMediaPlayerSwitchToFullScreenButtonPortraitFullScreen
     *   |-- 长按快进手势控制层 longPressSpeedControlLayout : PLVMediaPlayerLongPressSpeedControlLayout
     *   |-- 播放/暂停按钮 playButton ：PLVMediaPlayerPlayButtonLandscape
     *   |-- 返回按钮 backIv ：PLVMediaPlayerBackImageView
     *   |-- 视频标题 titleTextView ：PLVMediaPlayerTitleTextView
     *   |-- 更多功能按钮 moreActionButton ：PLVMediaPlayerMoreActionImageView
     *   |-- 播放进度文本 progressTextView ：PLVMediaPlayerProgressTextView
     *   |-- 进度条 progressSeekBar : PLVMediaPlayerProgressSeekBar
     *   |-- 长按快进控制提示 longPressSpeedHintLayout : PLVMediaPlayerLongPressSpeedHintLayout
     *   |-- 更多功能弹层 moreActionLayout : PLVMediaPlayerMoreActionLayoutPortrait
     **/

    // 整个皮肤的容器，但不包括 更多功能的菜单弹层布局
    private ConstraintLayout itemLayout;

    // 顶部状态栏控制边距
    private Guideline statusBarGuildLine;

    // 底部导航栏控制边距
    private Guideline navigationBarGuildLine;

    // 视频首图预览画面
    private PLVMediaPlayerVideoFirstImageView firstImageView;

    // 裸播放器容器
    private FrameLayout videoViewContainer;

    // 音频模式覆盖层
    private PLVMediaPlayerAudioModeCoverLayoutPortrait audioModeCoverLayout;

    // 切换到全屏按钮
    private PLVMediaPlayerSwitchToFullScreenButtonPortraitFullScreen switchToFullScreenButton;

    // 长按快进手势控制层
    private PLVMediaPlayerLongPressSpeedControlLayout longPressSpeedControlLayout;

    // 播放/暂停按钮
    private PLVMediaPlayerPlayButtonPortraitFullScreen playButton;

    // 返回按钮
    private PLVMediaPlayerBackImageView backIv;

    // 视频标题
    private PLVMediaPlayerTitleTextView titleTextView;

    // 更多功能按钮
    private PLVMediaPlayerMoreActionImageView moreActionButton;

    // 播放进度文本
    private PLVMediaPlayerProgressTextView progressTextView;

    // 进度条
    private PLVMediaPlayerProgressSeekBar progressSeekBar;

    // 长按快进控制提示
    private PLVMediaPlayerLongPressSpeedHintLayout longPressSpeedHintLayout;

    // 更多功能弹层
    private PLVMediaPlayerMoreActionLayoutPortrait moreActionLayout;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-属性-数据">
    // 裸播放器的弱引用，持有外部传入的裸播放器 PLVVideoView 对象，并 addView 到 videoViewContainer容器 中
    @NonNull
    private WeakReference<PLVVideoView> videoViewWeakRef = new WeakReference<>(null);

    // 当前视频尺寸
    protected float currentVideoRatioWidthHeight = 0;

    // 当前视频播放形式 —— 正常模式（音频+视频）、音频模式
    protected PLVMediaOutputMode currentMediaOutputMode = null;

    // 当前浮窗是否显示
    protected Boolean currentFloatWindowShowing = null;
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

        statusBarGuildLine.setGuidelineBegin(PLVDeviceManager.getStatusBarHeight());
        navigationBarGuildLine.setGuidelineEnd(PLVDeviceManager.getNavigationBarHeight());

        setOnClickListener(this);
    }

    private void findViewById() {
        itemLayout = findViewById(R.id.plv_media_player_video_layout);
        statusBarGuildLine = findViewById(R.id.plv_media_player_status_bar_guild_line);
        navigationBarGuildLine = findViewById(R.id.plv_media_player_navigation_bar_guild_line);
        firstImageView = findViewById(R.id.plv_media_player_video_first_image_view);
        videoViewContainer = findViewById(R.id.plv_media_player_video_view_container);
        audioModeCoverLayout = findViewById(R.id.plv_media_player_audio_mode_cover_protrait);
        switchToFullScreenButton = findViewById(R.id.plv_media_player_switch_video_mode_btn);
        longPressSpeedControlLayout = findViewById(R.id.plv_media_player_long_press_speed_control_layout);
        playButton = findViewById(R.id.plv_media_player_play_button);
        backIv = findViewById(R.id.plv_media_player_back_iv);
        titleTextView = findViewById(R.id.plv_media_player_title_tv);
        moreActionButton = findViewById(R.id.plv_media_player_more_action_button);
        progressTextView = findViewById(R.id.plv_media_player_progress_text_view);
        progressSeekBar = findViewById(R.id.plv_media_player_progress_seek_bar);
        longPressSpeedHintLayout = findViewById(R.id.plv_media_player_long_press_speed_hint_layout);
        moreActionLayout = findViewById(R.id.plv_media_player_more_action_layout_protrait);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        // 从点播 videoJson，监听 视频尺寸 变化，触发 皮肤UI尺寸 更新
        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getBusinessListenerRegistry()
                        .getVodVideoJson(),
                this,
                new Observer<PLVVodVideoJsonVO>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVVodVideoJsonVO videoJsonVO) {
                        if (videoJsonVO == null || videoJsonVO.getRatio() == null) {
                            return;
                        }
                        currentVideoRatioWidthHeight = videoJsonVO.getRatio().floatValue();
                        onViewStateChanged();
                    }
                }
        );

        // 从播放器回调，监听 视频尺寸 变化，触发 皮肤UI尺寸 更新
        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getStateListenerRegistry()
                        .getVideoSize(),
                this,
                new Observer<Rect>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable Rect rect) {
                        currentVideoRatioWidthHeight = calculateWidthHeightRatio(rect);
                        onViewStateChanged();
                    }
                }
        );

        // 监听 视频播放形式 变化，触发 正常模式（音频+视频）、音频模式 的切换
        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getBusinessListenerRegistry()
                        .getCurrentMediaOutputMode(),
                this,
                new Observer<PLVMediaOutputMode>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVMediaOutputMode mediaOutputMode) {
                        currentMediaOutputMode = mediaOutputMode;
                        onViewStateChanged();
                    }
                }
        );

        // 监听 浮窗状态 变化，触发 Layout UI 更新
        observeForeverUntilViewDetached(
                PLVMediaPlayerFloatWindowManager.getInstance().getFloatingViewShowState(),
                this,
                new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable Boolean showing) {
                        currentFloatWindowShowing = showing;
                        onViewStateChanged();
                    }
                }
        );

        // 监听浮窗按钮点击事件，切换到 浮窗播放
        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current())
                        .getControlActionEvent(),
                this,
                new PLVSugarUtil.Consumer<PLVMediaPlayerControlAction>() {
                    @Override
                    public void accept(PLVMediaPlayerControlAction action) {
                        if (action instanceof PLVMediaPlayerControlAction.LaunchFloatWindow) {
                            onLaunchFloatWindowEvent(((PLVMediaPlayerControlAction.LaunchFloatWindow) action).reason);
                        }
                    }
                }
        );
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onUpdateVideoViewContainer")
                .compareLastAndSet(currentVideoRatioWidthHeight, currentMediaOutputMode)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeVideoSize();
                    }
                });

        PLVRememberState.rememberStateOf(this, "onFloatWindowShowingChanged")
                .compareLastAndSet(currentFloatWindowShowing)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onFloatWindowShowingChanged();
                    }
                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-播放器皮肤-点击事件处理">
    @Override
    public void onClick(View v) {
        // 暂停/播放 视频
        final IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(PLVMediaPlayerFeedPortraitItemLayout.this).current();
        final PLVMediaPlayerControlViewModel controlViewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(PLVMediaPlayerFeedPortraitItemLayout.this).current();
        if (mediaPlayer == null || controlViewModel == null) {
            return;
        }
        PLVMediaPlayerPlayingState playingState = mediaPlayer.getStateListenerRegistry().getPlayingState().getValue();
        if (playingState == PLVMediaPlayerPlayingState.PLAYING) {
            mediaPlayer.pause();
            controlViewModel.requestControl(PLVMediaPlayerControlAction.hintManualPauseVideo(true));
        } else {
            mediaPlayer.start();
            controlViewModel.requestControl(PLVMediaPlayerControlAction.hintManualPauseVideo(false));
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
    public void setVideoView(PLVVideoView videoView) {
        videoViewWeakRef = new WeakReference<>(videoView);
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
            lp.height = ConvertUtils.dp2px(211);
        } else {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = (int) (ScreenUtils.getScreenOrientatedWidth() / currentVideoRatioWidthHeight);
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

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-浮窗逻辑-响应普通模式切换到浮窗模式的处理逻辑">
    protected void onLaunchFloatWindowEvent(final int reason) {
        PLVFloatPermissionUtils.requestPermission((Activity) getContext(),
                new PLVFloatPermissionUtils.IPLVOverlayPermissionListener() {
                    @Override
                    public void onResult(boolean isGrant) {
                        if (isGrant) {
                            launchFloatWindow(reason);
                        }
                    }
                });
    }

    protected void launchFloatWindow(int reason) {
        final PLVVideoView videoView = videoViewWeakRef.get();
        Rect floatWindowPosition = PLVMediaPlayerFloatWindowHelper.calculateFloatWindowPosition(videoView);
        if (videoView == null || floatWindowPosition == null) {
            return;
        }
        PLVMediaPlayerFloatWindowContentLayout contentLayout = new PLVMediaPlayerFloatWindowContentLayout(getContext());
        PLVMediaPlayerLocalProvider.localMediaPlayer.on(contentLayout).provide(videoView);
        if (videoView.getParent() != null) {
            ((ViewGroup) videoView.getParent()).removeView(videoView);
        }
        contentLayout.getContainer().addView(videoView);

        PLVMediaPlayerFloatWindowManager.getInstance()
                .bindContentLayout(contentLayout)
                .saveData(new PLVSugarUtil.Consumer<Bundle>() {
                    @Override
                    public void accept(Bundle bundle) {
                        bundle.putParcelable(
                                PLVMediaPlayerFloatWindowManager.KEY_SAVE_MEDIA_RESOURCE,
                                videoView.getBusinessListenerRegistry().getCurrentMediaResource().getValue()
                        );
                    }
                })
                .setFloatingSize(floatWindowPosition.width(), floatWindowPosition.height())
                .setFloatingPosition(floatWindowPosition.left, floatWindowPosition.top)
                .show(reason);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-浮窗逻辑-响应从浮窗模式切换到普通模式的处理逻辑">
    protected void onFloatWindowShowingChanged() {
        if (currentFloatWindowShowing != null && !currentFloatWindowShowing) {
            setVideoView(videoViewWeakRef.get());
        }
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
