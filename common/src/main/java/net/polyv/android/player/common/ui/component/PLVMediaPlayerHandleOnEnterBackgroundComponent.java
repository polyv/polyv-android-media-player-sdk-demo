package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.lang.NullablesKt.nullable;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;
import static net.polyv.android.player.sdk.foundation.lang.ThreadsKt.postToMainThread;

import android.app.Activity;
import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager;
import net.polyv.android.player.common.utils.floatwindow.enums.PLVFloatWindowLaunchReason;
import net.polyv.android.player.common.utils.floatwindow.permission.PLVFloatPermissionUtils;
import net.polyv.android.player.common.utils.ui.PLVViewLifecycleObservable;
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerState;
import net.polyv.android.player.sdk.foundation.app.PLVApplicationContext;
import net.polyv.android.player.sdk.foundation.di.DependScope;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

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
            postToMainThread(new Function0<Unit>() {
                @Override
                public Unit invoke() {
                    if (PLVApplicationContext.getStartedActivitiesCount() <= 0) {
                        onEnterBackground();
                    }
                    return null;
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
        final DependScope dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current();
        if (dependScope == null) {
            return;
        }
        final PLVMPMediaViewModel mediaViewModel = dependScope.get(PLVMPMediaViewModel.class);
        final PLVMPMediaControllerViewModel controllerViewModel = dependScope.get(PLVMPMediaControllerViewModel.class);
        final PLVMediaPlayerState playerState = nullable(new Function0<PLVMediaPlayerState>() {
            @Override
            public PLVMediaPlayerState invoke() {
                return mediaViewModel.getMediaPlayViewState().getValue().getPlayerState();
            }
        });
        final PLVMediaOutputMode mediaOutputMode = nullable(new Function0<PLVMediaOutputMode>() {
            @Override
            public PLVMediaOutputMode invoke() {
                return mediaViewModel.getMediaInfoViewState().getValue().getOutputMode();
            }
        });
        if (playerState != PLVMediaPlayerState.STATE_PLAYING || mediaOutputMode != PLVMediaOutputMode.AUDIO_VIDEO) {
            return;
        }
        controllerViewModel.launchFloatWindow(PLVFloatWindowLaunchReason.BACKGROUND_STATE_CHANGED);
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
        final DependScope dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current();
        if (dependScope == null) {
            return;
        }
        final PLVMPMediaViewModel mediaViewModel = dependScope.get(PLVMPMediaViewModel.class);
        final boolean isPlaying = nullable(new Function0<Boolean>() {
            @Override
            public Boolean invoke() {
                return mediaViewModel.getMediaPlayViewState().getValue().isPlaying();
            }
        });
        if (isPlaying) {
            mediaViewModel.pause();
            isAutoPausedOnEnterBackground = true;
        }
    }

    private void recoverPlayOnEnterBackground() {
        if (!isAutoPausedOnEnterBackground) {
            return;
        }
        isAutoPausedOnEnterBackground = false;
        final DependScope dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current();
        if (dependScope == null) {
            return;
        }
        final PLVMPMediaViewModel mediaViewModel = dependScope.get(PLVMPMediaViewModel.class);
        mediaViewModel.start();
    }

}
