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
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerState;

/**
 * App进入后台时自动唤起小窗
 * <p>
 * 该组件不会自动申请悬浮窗权限，需要提前获取权限后才会自动唤起
 *
 * @author Hoshiiro
 */
public class PLVMediaPlayerAutoFloatWindowOnBackgroundComponent extends View implements GenericLifecycleObserver {

    private boolean isVisibleToUser = false;
    private boolean isAttach = false;

    public PLVMediaPlayerAutoFloatWindowOnBackgroundComponent(Context context) {
        super(context);
        init();
    }

    public PLVMediaPlayerAutoFloatWindowOnBackgroundComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PLVMediaPlayerAutoFloatWindowOnBackgroundComponent(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
                        launchFloatWindow();
                    }
                }
            });
        } else if (event == Lifecycle.Event.ON_START) {
            hideFloatWindow();
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
            ((LifecycleOwner) getContext()).getLifecycle().removeObserver(PLVMediaPlayerAutoFloatWindowOnBackgroundComponent.this);
        }

    };

    public void setUserVisibleHint(boolean isVisibleToUser) {
        this.isVisibleToUser = isVisibleToUser;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        // do nothing
    }

    private void launchFloatWindow() {
        if (!isHandleAutoFloatWindow() || !PLVFloatPermissionUtils.checkPermission((Activity) getContext())) {
            return;
        }
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
        if (!isHandleAutoFloatWindow() || !PLVFloatPermissionUtils.checkPermission((Activity) getContext())) {
            return;
        }
        if (PLVMediaPlayerFloatWindowManager.getInstance().isFloatingWindowShowing()) {
            PLVMediaPlayerFloatWindowManager.getInstance().hide();
        }
    }

    private boolean isHandleAutoFloatWindow() {
        return isAttach && isVisibleToUser;
    }

}
