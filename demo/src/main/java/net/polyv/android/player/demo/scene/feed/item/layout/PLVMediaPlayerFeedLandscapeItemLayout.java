package net.polyv.android.player.demo.scene.feed.item.layout;

import static com.plv.foundationsdk.component.event.PLVEventKt.observeUntilViewDetached;
import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeForeverUntilViewDetached;
import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import net.polyv.android.player.common.ui.component.PLVMediaPlayerAudioModeCoverLayoutLandscape;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerBackImageView;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerBitRateSelectLayoutLandscape;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerBitRateTextView;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerBrightnessVolumeControlLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerBrightnessVolumeHintLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerControllerGradientMaskLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerHorizontalDragControlLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerLockControllerImageView;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerLongPressSpeedControlLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerLongPressSpeedHintLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerMoreActionImageView;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerMoreActionLayoutLandscape;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerPlayButtonLandscape;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerPlayErrorOverlayLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerProgressSeekBar;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerProgressTextView;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerSpeedSelectLayoutLandscape;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerSpeedTextView;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerTitleTextView;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowHelper;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager;
import net.polyv.android.player.common.ui.component.floatwindow.layout.PLVMediaPlayerFloatWindowContentLayout;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.common.utils.floatwindow.permission.PLVFloatPermissionUtils;
import net.polyv.android.player.common.utils.ui.PLVOnDoubleClickListener;
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerPlayingState;
import net.polyv.android.player.demo.R;
import net.polyv.android.player.sdk.PLVVideoView;

import java.lang.ref.WeakReference;

/**
 * @author Hoshiiro
 *
 *  横向-全屏 播放器皮肤 layout
 */
public class PLVMediaPlayerFeedLandscapeItemLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="Layout-属性-UI-view属性和布局示意">
    /**
     * 横向-全屏 播放器皮肤 layout 布局示意
     *
     * -- 播放器皮肤布局  itemLayout ：ConstraintLayout
     *   |-- 裸播放器容器 videoViewContainer ：FrameLayout
     *   |-- 音频模式覆盖层 audioModeCoverLayout：PLVMediaPlayerAudioModeCoverLayoutPortrait
     *   |-- 亮度/音量手势控制层 brightnessVolumeControlLayout ：PLVMediaPlayerBrightnessVolumeControlLayout
     *   |-- 长按快进手势控制层 longPressSpeedControlLayout ：PLVMediaPlayerLongPressSpeedControlLayout
     *   |-- 皮肤控件底部渐变遮罩蒙层 controllerGradientMaskLayout：PLVMediaPlayerControllerGradientMaskLayout
     *   |-- 播放结束弹层 playCompleteOverlayLayout：PLVMediaPlayerPlayCompleteOverlayLayout
     *   |-- 播放异常弹层 playErrorOverlayLayout：PLVMediaPlayerPlayErrorOverlayLayout
     *   |-- 返回按钮 backIv ：PLVMediaPlayerBackImageView
     *   |-- 视频标题 titleTextView ：PLVMediaPlayerTitleTextView
     *   |-- 更多功能按钮 moreActionButton ：PLVMediaPlayerMoreActionImageView
     *   |-- 横屏操作锁定按钮 lockControllerIV ：PLVMediaPlayerLockControllerImageView
     *   |-- 播放/暂停按钮 playButton ：PLVMediaPlayerPlayButtonLandscape
     *   |-- 播放进度文本 progressTextView ：PLVMediaPlayerProgressTextView
     *   |-- 清晰度 bitRateTextView ：PLVMediaPlayerBitRateTextView
     *   |-- 倍速 speedTextView ：PLVMediaPlayerBitRateTextView
     *   |-- 进度条 progressSeekBar : PLVMediaPlayerProgressSeekBar
     *   |-- 亮度/音量控制提示 brightnessVolumeHintLayout : PLVMediaPlayerBrightnessVolumeHintLayout
     *   |-- 长按快进控制提示 longPressSpeedHintLayout : PLVMediaPlayerLongPressSpeedHintLayout
     *   |-- 更多功能菜单 moreActionLayout : PLVMediaPlayerMoreActionLayoutLandscape
     *   |-- 清晰度选择菜单 bitRateSelectLayout ：PLVMediaPlayerBitRateSelectLayoutLandscape
     *   |-- 倍速选择菜单 speedSelectLayout ：PLVMediaPlayerSpeedSelectLayoutLandscape
     */

    // 整个皮肤的容器，但不包括 更多功能的菜单弹层布局
    private ConstraintLayout itemLayout;

    // 裸播放器容器
    private FrameLayout videoViewContainer;

    // 音频模式覆盖层
    private PLVMediaPlayerAudioModeCoverLayoutLandscape audioModeCoverLayout;

    // 亮度/音量手势控制层
    private PLVMediaPlayerBrightnessVolumeControlLayout brightnessVolumeControlLayout;

    // 长按快进手势控制层
    private PLVMediaPlayerLongPressSpeedControlLayout longPressSpeedControlLayout;

    // 皮肤控件底部渐变遮罩蒙层
    private PLVMediaPlayerControllerGradientMaskLayout controllerGradientMaskLayout;

    // 播放异常弹层
    private PLVMediaPlayerPlayErrorOverlayLayout playErrorOverlayLayout;

    // 返回按钮
    private PLVMediaPlayerBackImageView backIv;

    // 视频标题
    private PLVMediaPlayerTitleTextView titleTextView;

    // 更多功能按钮
    private PLVMediaPlayerMoreActionImageView moreActionButton;

    // 横屏操作锁定按钮
    private PLVMediaPlayerLockControllerImageView lockControllerIV;

    // 播放/暂停按钮
    private PLVMediaPlayerPlayButtonLandscape playButton;

    // 播放进度文本
    private PLVMediaPlayerProgressTextView progressTextView;

    // 清晰度
    private PLVMediaPlayerBitRateTextView bitRateTextView;

    // 倍速
    private PLVMediaPlayerSpeedTextView speedTextView;

    // 进度条
    private PLVMediaPlayerProgressSeekBar progressSeekBar;

    // 亮度/音量控制提示
    private PLVMediaPlayerBrightnessVolumeHintLayout brightnessVolumeHintLayout;

    // 长按快进控制提示
    private PLVMediaPlayerLongPressSpeedHintLayout longPressSpeedHintLayout;

    // 更多功能菜单
    private PLVMediaPlayerMoreActionLayoutLandscape moreActionLayout;

    // 清晰度选择菜单
    private PLVMediaPlayerBitRateSelectLayoutLandscape bitRateSelectLayout;

    // 倍速选择菜单
    private PLVMediaPlayerSpeedSelectLayoutLandscape speedSelectLayout;

    private PLVMediaPlayerHorizontalDragControlLayout horizontalDragControlLayout;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-属性-数据">
    // 裸播放器的弱引用，持有外部传入的裸播放器 PLVVideoView 对象，并 addView 到 videoViewContainer容器 中
    private WeakReference<PLVVideoView> videoViewWeakRef = null;

    // 当前视频尺寸
    protected Rect currentVideoSize = null;

    // 当前是否正在浮窗显示
    protected Boolean currentFloatWindowShowing = null;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-构造方法">
    public PLVMediaPlayerFeedLandscapeItemLayout(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerFeedLandscapeItemLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerFeedLandscapeItemLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-初始化 & 设置监听">
    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_feed_landscape_item_layout, this);
        findViewById();
        setOnClickListener();
    }

    private void findViewById() {
        itemLayout = findViewById(R.id.plv_media_player_video_layout);
        videoViewContainer = findViewById(R.id.plv_media_player_video_view_container);
        audioModeCoverLayout = findViewById(R.id.plv_media_player_audio_mode_cover_landscape);
        brightnessVolumeControlLayout = findViewById(R.id.plv_media_player_brightness_volume_control_layout);
        longPressSpeedControlLayout = findViewById(R.id.plv_media_player_long_press_speed_control_layout);
        horizontalDragControlLayout = findViewById(R.id.plv_media_player_horizontal_drag_control_layout);
        controllerGradientMaskLayout = findViewById(R.id.plv_media_player_controller_gradient_mask_layout);
        playErrorOverlayLayout = findViewById(R.id.plv_media_player_error_overlay_layout);
        backIv = findViewById(R.id.plv_media_player_back_iv);
        titleTextView = findViewById(R.id.plv_media_player_title_tv);
        moreActionButton = findViewById(R.id.plv_media_player_more_action_button);
        lockControllerIV = findViewById(R.id.plv_media_player_lock_controller_iv);
        playButton = findViewById(R.id.plv_media_player_play_button);
        progressTextView = findViewById(R.id.plv_media_player_progress_text_view);
        speedTextView = findViewById(R.id.plv_media_player_speed_text_view);
        progressSeekBar = findViewById(R.id.plv_media_player_progress_seek_bar);
        brightnessVolumeHintLayout = findViewById(R.id.plv_media_player_brightness_volume_hint_layout);
        longPressSpeedHintLayout = findViewById(R.id.plv_media_player_long_press_speed_hint_layout);
        bitRateTextView = findViewById(R.id.plv_media_player_bit_rate_text_view);
        bitRateSelectLayout = findViewById(R.id.plv_media_player_bit_rate_select_layout);
        speedSelectLayout = findViewById(R.id.plv_media_player_speed_select_layout);
        moreActionLayout = findViewById(R.id.plv_media_player_more_action_layout);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        // 监听 视视频尺寸 变化，触发 皮肤UI尺寸 更新
        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getStateListenerRegistry()
                        .getVideoSize(),
                this,
                new Observer<Rect>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable Rect rect) {
                        currentVideoSize = rect;
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
        PLVRememberState.rememberStateOf(this, "onFloatWindowShowingChanged")
                .compareLastAndSet(currentFloatWindowShowing)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onFloatWindowShowingChanged();
                    }
                });

        PLVRememberState.rememberStateOf(this, "onChangeVideoSize")
                .compareLastAndSet(currentVideoSize)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeVideoSize();
                    }
                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-播放器皮肤-点击事件处理">

    private void setOnClickListener() {
        setOnClickListener(new PLVOnDoubleClickListener() {
            @Override
            protected void onSingleClick() {
                // 显示/隐藏 皮肤
                PLVMediaPlayerControlViewModel controlViewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(PLVMediaPlayerFeedLandscapeItemLayout.this).current();
                if (controlViewModel != null) {
                    boolean visible = controlViewModel.getControlViewStateLiveData().getValue() != null && controlViewModel.getControlViewStateLiveData().getValue().controllerVisible;
                    if (visible) {
                        controlViewModel.requestControl(PLVMediaPlayerControlAction.hideMediaController());
                    } else {
                        controlViewModel.requestControl(PLVMediaPlayerControlAction.showMediaController());
                    }
                }
            }

            @Override
            protected void onDoubleClick() {
                // 暂停/播放
                PLVVideoView videoView = videoViewWeakRef.get();
                if (videoView == null) {
                    return;
                }
                boolean isPlaying = videoView.getStateListenerRegistry().getPlayingState().getValue() == PLVMediaPlayerPlayingState.PLAYING;
                if (isPlaying) {
                    videoView.pause();
                } else {
                    videoView.start();
                }
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-播放器皮肤-手势事件处理">
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (brightnessVolumeControlLayout.handleOnTouchEvent(event)) {
            return true;
        }
        if (horizontalDragControlLayout.handleOnTouchEvent(event)) {
            return true;
        }
        if (longPressSpeedControlLayout.handleOnTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-播放器皮肤-设置裸播放器到皮肤容器">
    public void setVideoView(@Nullable PLVVideoView videoView) {
        videoViewWeakRef = new WeakReference<>(videoView);
        videoViewContainer.removeAllViews();
        if (videoView != null && videoView.getParent() != null) { // 把裸播放器从原来的父容器中移除，比如从小窗容器中移除
            ((ViewGroup) videoView.getParent()).removeView(videoView);
        }
        if (videoView != null && videoView.getParent() == null) { // 把裸播放器添加到当前的皮肤容器中
            videoViewContainer.addView(videoView);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-响应视频尺寸变化的处理逻辑">
    protected void onChangeVideoSize() {
        if (currentVideoSize == null || currentVideoSize.width() == 0 || currentVideoSize.height() == 0) {
            return;
        }
        boolean isVideoPortrait = currentVideoSize.width() < currentVideoSize.height();

        ConstraintLayout.LayoutParams containerParam = (ConstraintLayout.LayoutParams) videoViewContainer.getLayoutParams();
        if (isVideoPortrait) {
            containerParam.width = ViewGroup.LayoutParams.MATCH_PARENT;
            containerParam.height = ViewGroup.LayoutParams.MATCH_PARENT;
            containerParam.leftMargin = containerParam.rightMargin = ConvertUtils.dp2px(72);
        } else {
            containerParam.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            containerParam.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            containerParam.leftMargin = containerParam.rightMargin = 0;
        }
        videoViewContainer.setLayoutParams(containerParam);

        PLVVideoView videoView = videoViewWeakRef.get();
        if (videoView != null && videoView.getLayoutParams() instanceof LayoutParams) {
            LayoutParams videoViewParam = (LayoutParams) videoView.getLayoutParams();
            videoViewParam.gravity = Gravity.CENTER;
            if (isVideoPortrait) {
                videoViewParam.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            } else {
                videoViewParam.width = ViewGroup.LayoutParams.MATCH_PARENT;
            }
            videoView.setLayoutParams(videoViewParam);
        }
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

}
