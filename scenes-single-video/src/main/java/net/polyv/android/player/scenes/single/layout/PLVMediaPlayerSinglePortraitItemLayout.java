package net.polyv.android.player.scenes.single.layout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaPlayViewState;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerAudioModeCoverLayoutPortrait;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerAutoContinueHintLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerAuxiliaryViewContainer;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerBackImageView;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerBrightnessVolumeControlLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerBrightnessVolumeHintLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerControllerGradientMaskLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerHorizontalDragControlLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerLongPressSpeedControlLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerLongPressSpeedHintLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerMoreActionImageView;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerPlayButtonLandscape;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerPlayCompleteManualRestartOverlayLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerPlayErrorOverlayLayout;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerProgressSeekBar;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerProgressTextView;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerSwitchToFullScreenButtonPortraitHalfScreen;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerTitleTextView;
import net.polyv.android.player.common.utils.ui.PLVOnDoubleClickListener;
import net.polyv.android.player.scenes.single.R;
import net.polyv.android.player.sdk.foundation.di.DependScope;

/**
 * @author Hoshiiro
 * <p>
 * 纵向-半屏 播放器皮肤 layout
 */
public class PLVMediaPlayerSinglePortraitItemLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="Layout-views">
    private ConstraintLayout videoLayout;
    private FrameLayout videoViewContainer;
    private PLVMediaPlayerAudioModeCoverLayoutPortrait audioModeCoverPortrait;
    private PLVMediaPlayerBrightnessVolumeControlLayout brightnessVolumeControlLayout;
    private PLVMediaPlayerLongPressSpeedControlLayout longPressSpeedControlLayout;
    private PLVMediaPlayerHorizontalDragControlLayout horizontalDragControlLayout;
    private PLVMediaPlayerControllerGradientMaskLayout controllerGradientMaskLayout;
    private PLVMediaPlayerPlayCompleteManualRestartOverlayLayout completeOverlayLayout;
    private PLVMediaPlayerPlayErrorOverlayLayout errorOverlayLayout;
    private PLVMediaPlayerBackImageView backIv;
    private PLVMediaPlayerTitleTextView titleTv;
    private PLVMediaPlayerMoreActionImageView moreActionButton;
    private PLVMediaPlayerPlayButtonLandscape playButton;
    private PLVMediaPlayerProgressTextView progressTextView;
    private PLVMediaPlayerSwitchToFullScreenButtonPortraitHalfScreen switchVideoModeBtn;
    private PLVMediaPlayerProgressSeekBar progressSeekBar;
    private PLVMediaPlayerAutoContinueHintLayout autoContinueHintLayout;
    private PLVMediaPlayerBrightnessVolumeHintLayout brightnessVolumeHintLayout;
    private PLVMediaPlayerLongPressSpeedHintLayout longPressSpeedHintLayout;
    private PLVMediaPlayerAuxiliaryViewContainer auxiliaryViewContainer;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-构造方法">
    public PLVMediaPlayerSinglePortraitItemLayout(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerSinglePortraitItemLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerSinglePortraitItemLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-初始化 & 设置监听">
    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_single_portrait_item_layout, this);
        findViewById();
        setOnClickListener();
    }

    private void findViewById() {
        videoLayout = findViewById(R.id.plv_media_player_video_layout);
        videoViewContainer = findViewById(R.id.plv_media_player_video_view_container);
        audioModeCoverPortrait = findViewById(R.id.plv_media_player_audio_mode_cover_portrait);
        brightnessVolumeControlLayout = findViewById(R.id.plv_media_player_brightness_volume_control_layout);
        longPressSpeedControlLayout = findViewById(R.id.plv_media_player_long_press_speed_control_layout);
        horizontalDragControlLayout = findViewById(R.id.plv_media_player_horizontal_drag_control_layout);
        controllerGradientMaskLayout = findViewById(R.id.plv_media_player_controller_gradient_mask_layout);
        completeOverlayLayout = findViewById(R.id.plv_media_player_complete_overlay_layout);
        errorOverlayLayout = findViewById(R.id.plv_media_player_error_overlay_layout);
        backIv = findViewById(R.id.plv_media_player_back_iv);
        titleTv = findViewById(R.id.plv_media_player_title_tv);
        moreActionButton = findViewById(R.id.plv_media_player_more_action_button);
        playButton = findViewById(R.id.plv_media_player_play_button);
        progressTextView = findViewById(R.id.plv_media_player_progress_text_view);
        switchVideoModeBtn = findViewById(R.id.plv_media_player_switch_video_mode_btn);
        progressSeekBar = findViewById(R.id.plv_media_player_progress_seek_bar);
        autoContinueHintLayout = findViewById(R.id.plv_media_player_auto_continue_hint_layout);
        brightnessVolumeHintLayout = findViewById(R.id.plv_media_player_brightness_volume_hint_layout);
        longPressSpeedHintLayout = findViewById(R.id.plv_media_player_long_press_speed_hint_layout);
        auxiliaryViewContainer = findViewById(R.id.plv_media_player_auxiliary_view_container);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-播放器皮肤-点击事件处理">

    private void setOnClickListener() {
        setOnClickListener(new PLVOnDoubleClickListener() {
            @Override
            protected void onSingleClick() {
                final DependScope dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(PLVMediaPlayerSinglePortraitItemLayout.this).current();
                if (dependScope == null) {
                    return;
                }
                dependScope.get(PLVMPMediaControllerViewModel.class).changeControllerVisible();
            }

            @Override
            protected void onDoubleClick() {
                final DependScope dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(PLVMediaPlayerSinglePortraitItemLayout.this).current();
                if (dependScope == null) {
                    return;
                }
                final PLVMPMediaViewModel mediaViewModel = dependScope.get(PLVMPMediaViewModel.class);
                final PLVMPMediaPlayViewState viewState = mediaViewModel.getMediaPlayViewState().getValue();
                if (viewState == null) {
                    return;
                }
                if (viewState.isPlaying()) {
                    mediaViewModel.pause();
                } else {
                    mediaViewModel.start();
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
    public void setVideoView(@Nullable View videoView) {
        videoViewContainer.removeAllViews();
        if (videoView != null && videoView.getParent() != null) { // 把裸播放器从原来的父容器中移除，比如从小窗容器中移除
            ((ViewGroup) videoView.getParent()).removeView(videoView);
        }
        if (videoView != null && videoView.getParent() == null) { // 把裸播放器添加到当前的皮肤容器中
            videoViewContainer.addView(videoView);
        }
    }

    public void setAuxiliaryVideoView(@Nullable View auxiliaryVideoView) {
        auxiliaryViewContainer.setAuxiliaryVideoView(auxiliaryVideoView);
    }
    // </editor-fold>

}
