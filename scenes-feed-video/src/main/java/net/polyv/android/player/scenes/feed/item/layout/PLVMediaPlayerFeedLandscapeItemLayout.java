package net.polyv.android.player.scenes.feed.item.layout;

import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.dp;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaInfoViewState;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaPlayViewState;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerAudioModeCoverLayoutLandscape;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerAutoContinueHintFeedStyleLayout;
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
import net.polyv.android.player.common.utils.ui.PLVOnDoubleClickListener;
import net.polyv.android.player.scenes.feed.R;
import net.polyv.android.player.sdk.foundation.di.DependScope;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 * <p>
 * 横向-全屏 播放器皮肤 layout
 */
public class PLVMediaPlayerFeedLandscapeItemLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="Layout-views">
    private ConstraintLayout videoLayout;
    private FrameLayout videoViewContainer;
    private PLVMediaPlayerAudioModeCoverLayoutLandscape audioModeCoverLandscape;
    private PLVMediaPlayerBrightnessVolumeControlLayout brightnessVolumeControlLayout;
    private PLVMediaPlayerLongPressSpeedControlLayout longPressSpeedControlLayout;
    private PLVMediaPlayerHorizontalDragControlLayout horizontalDragControlLayout;
    private PLVMediaPlayerControllerGradientMaskLayout controllerGradientMaskLayout;
    private PLVMediaPlayerPlayErrorOverlayLayout errorOverlayLayout;
    private PLVMediaPlayerBackImageView backIv;
    private PLVMediaPlayerTitleTextView titleTv;
    private PLVMediaPlayerMoreActionImageView moreActionButton;
    private PLVMediaPlayerLockControllerImageView lockControllerIv;
    private PLVMediaPlayerPlayButtonLandscape playButton;
    private PLVMediaPlayerProgressTextView progressTextView;
    private PLVMediaPlayerBitRateTextView bitRateTextView;
    private PLVMediaPlayerSpeedTextView speedTextView;
    private PLVMediaPlayerProgressSeekBar progressSeekBar;
    private PLVMediaPlayerAutoContinueHintFeedStyleLayout autoContinueHintLayout;
    private PLVMediaPlayerBrightnessVolumeHintLayout brightnessVolumeHintLayout;
    private PLVMediaPlayerLongPressSpeedHintLayout longPressSpeedHintLayout;
    private PLVMediaPlayerMoreActionLayoutLandscape moreActionLayout;
    private PLVMediaPlayerBitRateSelectLayoutLandscape bitRateSelectLayout;
    private PLVMediaPlayerSpeedSelectLayoutLandscape speedSelectLayout;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-属性-数据">
    // 当前视频尺寸
    protected Rect currentVideoSize = null;
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
        videoLayout = findViewById(R.id.plv_media_player_video_layout);
        videoViewContainer = findViewById(R.id.plv_media_player_video_view_container);
        audioModeCoverLandscape = findViewById(R.id.plv_media_player_audio_mode_cover_landscape);
        brightnessVolumeControlLayout = findViewById(R.id.plv_media_player_brightness_volume_control_layout);
        longPressSpeedControlLayout = findViewById(R.id.plv_media_player_long_press_speed_control_layout);
        horizontalDragControlLayout = findViewById(R.id.plv_media_player_horizontal_drag_control_layout);
        controllerGradientMaskLayout = findViewById(R.id.plv_media_player_controller_gradient_mask_layout);
        errorOverlayLayout = findViewById(R.id.plv_media_player_error_overlay_layout);
        backIv = findViewById(R.id.plv_media_player_back_iv);
        titleTv = findViewById(R.id.plv_media_player_title_tv);
        moreActionButton = findViewById(R.id.plv_media_player_more_action_button);
        lockControllerIv = findViewById(R.id.plv_media_player_lock_controller_iv);
        playButton = findViewById(R.id.plv_media_player_play_button);
        progressTextView = findViewById(R.id.plv_media_player_progress_text_view);
        bitRateTextView = findViewById(R.id.plv_media_player_bit_rate_text_view);
        speedTextView = findViewById(R.id.plv_media_player_speed_text_view);
        progressSeekBar = findViewById(R.id.plv_media_player_progress_seek_bar);
        autoContinueHintLayout = findViewById(R.id.plv_media_player_auto_continue_hint_layout);
        brightnessVolumeHintLayout = findViewById(R.id.plv_media_player_brightness_volume_hint_layout);
        longPressSpeedHintLayout = findViewById(R.id.plv_media_player_long_press_speed_hint_layout);
        moreActionLayout = findViewById(R.id.plv_media_player_more_action_layout);
        bitRateSelectLayout = findViewById(R.id.plv_media_player_bit_rate_select_layout);
        speedSelectLayout = findViewById(R.id.plv_media_player_speed_select_layout);
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
                        currentVideoSize = viewState.getVideoSize();
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangeVideoSize")
                .compareLastAndSet(currentVideoSize)
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

    private void setOnClickListener() {
        setOnClickListener(new PLVOnDoubleClickListener() {
            @Override
            protected void onSingleClick() {
                final DependScope dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(PLVMediaPlayerFeedLandscapeItemLayout.this).current();
                if (dependScope == null) {
                    return;
                }
                dependScope.get(PLVMPMediaControllerViewModel.class).changeControllerVisible();
            }

            @Override
            protected void onDoubleClick() {
                final DependScope dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(PLVMediaPlayerFeedLandscapeItemLayout.this).current();
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
            containerParam.leftMargin = containerParam.rightMargin = dp(72).px();
        } else {
            containerParam.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            containerParam.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            containerParam.leftMargin = containerParam.rightMargin = 0;
        }
        videoViewContainer.setLayoutParams(containerParam);
    }
    // </editor-fold>

}
