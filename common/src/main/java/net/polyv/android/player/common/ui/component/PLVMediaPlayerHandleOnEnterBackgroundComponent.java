package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.utils.PLVAppUtils.postToMainThread;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.app.Activity;
import androidx.lifecycle.GenericLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.graphics.Canvas;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.plv.thirdpart.blankj.utilcode.util.Utils;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode;
import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.common.utils.floatwindow.permission.PLVFloatPermissionUtils;
import net.polyv.android.player.common.utils.ui.PLVViewLifecycleObservable;
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerPlayingState;
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerState;

/**
 * App进入后台时自动处理逻辑
 *
 * @author Hoshiiro
 */
public class PLVMediaPlayerHandleOnEnterBackgroundComponent extends View implements GenericLifecycleObserver {

    /**
     * 是否自动唤起小窗，不会自动申请悬浮窗权限，需要提前获取权限后才会自动唤起
     */
    private static final boolean AUTO_FLOAT_WINDOW_ON_BACKGROUND = true;
    /**
     * 是否自动暂停播放，当未唤起小窗时自动暂停
     */
    private static final boolean AUTO_PAUSE_ON_BACKGROUND = true;

    private boolean isVisibleToUser = false;
    private boolean isAttach = false;

    private boolean isAutoPausedOnEnterBackground = false;

    public PLVMediaPlayerHandleOnEnterBackgroundComponent(Context context) {
        super(context);
        init();
    }

    public PLVMediaPlayerHandleOnEnterBackgroundComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PLVMediaPlayerHandleOnEnterBackgroundComponent(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        ((LifecycleOwner) getContext()).getLifecycle().addObserver(this);
    }

    @Override
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_STOP) {
            postToMainThread(new Runnable() {
                @Override
                public void run() {
                    if (Utils.getStartedActivityCount() <= 0) {
                        onEnterBackground();
                    }
                }
            });
        } else if (event == Lifecycle.Event.ON_START) {
            onResumeFromBackground();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttach = true;

        requireNotNull(PLVMediaPlayerLocalProvider.localLifecycleObservable.on(this).current())
                .addObserver(viewLifecycleObserver);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttach = false;
    }

    private final PLVViewLifecycleObservable.IViewLifecycleObserver viewLifecycleObserver = new PLVViewLifecycleObservable.AbsViewLifecycleObserver() {

        @Override
        public void onDestroy(PLVViewLifecycleObservable observable) {
            observable.removeObserver(this);
            ((LifecycleOwner) getContext()).getLifecycle().removeObserver(PLVMediaPlayerHandleOnEnterBackgroundComponent.this);
        }

    };

    public void setUserVisibleHint(boolean isVisibleToUser) {
        this.isVisibleToUser = isVisibleToUser;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        // do nothing
    }

    private void onEnterBackground() {
        if (isHandleAutoFloatWindow()) {
            launchFloatWindow();
            return;
        }
        if (isHandleAutoPause()) {
            autoPauseOnEnterBackground();
        }
    }

    private void onResumeFromBackground() {
        if (isHandleAutoFloatWindow()) {
            hideFloatWindow();
            return;
        }
        if (isHandleAutoPause()) {
            recoverPlayOnEnterBackground();
        }
    }

    private boolean isHandleAutoFloatWindow() {
        return AUTO_FLOAT_WINDOW_ON_BACKGROUND && isAttach && isVisibleToUser && PLVFloatPermissionUtils.checkPermission((Activity) getContext());
    }

    private void launchFloatWindow() {
        final PLVMediaPlayerControlViewModel controlViewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current();
        final IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current();
        if (controlViewModel == null || mediaPlayer == null) {
            return;
        }
        final PLVMediaPlayerState playerState = mediaPlayer.getStateListenerRegistry().getPlayerState().getValue();
        final PLVMediaOutputMode mediaOutputMode = mediaPlayer.getBusinessListenerRegistry().getCurrentMediaOutputMode().getValue();
        if (playerState != PLVMediaPlayerState.STATE_PLAYING || mediaOutputMode != PLVMediaOutputMode.AUDIO_VIDEO) {
            return;
        }
        controlViewModel.requestControl(PLVMediaPlayerControlAction.launchFloatWindow(PLVMediaPlayerFloatWindowManager.SHOW_REASON_ENTER_BACKGROUND));
    }

    private void hideFloatWindow() {
        if (PLVMediaPlayerFloatWindowManager.getInstance().isFloatingWindowShowing()) {
            PLVMediaPlayerFloatWindowManager.getInstance().hide();
        }
    }

    private boolean isHandleAutoPause() {
        return AUTO_PAUSE_ON_BACKGROUND && isAttach && isVisibleToUser;
    }

    private void autoPauseOnEnterBackground() {
        final IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current();
        if (mediaPlayer == null) {
            return;
        }
        final boolean isPlaying = mediaPlayer.getStateListenerRegistry().getPlayingState().getValue() == PLVMediaPlayerPlayingState.PLAYING;
        if (isPlaying) {
            mediaPlayer.pause();
            isAutoPausedOnEnterBackground = true;
        }
    }

    private void recoverPlayOnEnterBackground() {
        if (!isAutoPausedOnEnterBackground) {
            return;
        }
        isAutoPausedOnEnterBackground = false;
        final IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current();
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

}
