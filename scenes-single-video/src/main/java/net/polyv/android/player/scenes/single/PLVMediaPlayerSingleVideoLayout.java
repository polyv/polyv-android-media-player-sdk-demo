package net.polyv.android.player.scenes.single;

import static net.polyv.android.player.sdk.foundation.collections.CollectionsKt.listOf;
import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.isLandscape;
import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.isPortrait;
import static net.polyv.android.player.sdk.foundation.lang.NullablesKt.nullable;

import android.app.Activity;
import androidx.lifecycle.GenericLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;
import net.polyv.android.player.common.di.PLVMPCommonModuleKt;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.auxiliary.view.PLVMPAuxiliaryVideoView;
import net.polyv.android.player.common.modules.auxiliary.viewmodel.PLVMPAuxiliaryViewModel;
import net.polyv.android.player.common.modules.media.view.PLVMPVideoView;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.LockMediaControllerAction;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerHandleOnEnterBackgroundComponent;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerMoreActionLayoutPortrait;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowHelper;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager;
import net.polyv.android.player.common.ui.component.floatwindow.layout.PLVMediaPlayerFloatWindowContentLayout;
import net.polyv.android.player.common.utils.audiofocus.PLVMediaPlayerAudioFocusManager;
import net.polyv.android.player.common.utils.floatwindow.enums.PLVFloatWindowLaunchReason;
import net.polyv.android.player.common.utils.floatwindow.permission.PLVFloatPermissionUtils;
import net.polyv.android.player.common.utils.orientation.PLVActivityOrientationManager;
import net.polyv.android.player.common.utils.ui.PLVViewLifecycleObservable;
import net.polyv.android.player.core.api.option.PLVMediaPlayerOptionEnum;
import net.polyv.android.player.scenes.single.layout.PLVMediaPlayerSingleLandscapeItemLayout;
import net.polyv.android.player.scenes.single.layout.PLVMediaPlayerSinglePortraitItemLayout;
import net.polyv.android.player.sdk.foundation.di.DependScope;
import net.polyv.android.player.sdk.foundation.lang.Consumer;
import net.polyv.android.player.sdk.foundation.lang.Lazy;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerSingleVideoLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="Layout-属性">
    private final DependScope dependScope = new DependScope(PLVMPCommonModuleKt.commonItemModule);
    private final View videoView = new PLVMPVideoView(getContext(), dependScope);
    private final View auxiliaryVideoView = new PLVMPAuxiliaryVideoView(getContext(), dependScope);
    private final PLVMPMediaViewModel mediaViewModel = dependScope.get(PLVMPMediaViewModel.class);
    private final PLVMPMediaControllerViewModel mediaControllerViewModel = dependScope.get(PLVMPMediaControllerViewModel.class);
    private final PLVMPAuxiliaryViewModel auxiliaryViewModel = dependScope.get(PLVMPAuxiliaryViewModel.class);

    // 纵向-半屏 播放器皮肤 layout
    private final Lazy<PortraitLayout> portraitLayout = new Lazy<PortraitLayout>() {
        @Override
        public PortraitLayout onLazyInit() {
            return new PortraitLayout(getContext());
        }
    };

    // 横向-全屏 播放器皮肤 layout
    private final Lazy<LandscapeLayout> landscapeLayout = new Lazy<LandscapeLayout>() {
        @Override
        public LandscapeLayout onLazyInit() {
            return new LandscapeLayout(getContext());
        }
    };

    // App进入后台时自动唤起小窗
    private final PLVMediaPlayerHandleOnEnterBackgroundComponent handleOnEnterBackgroundComponent = new PLVMediaPlayerHandleOnEnterBackgroundComponent(getContext());
    // 音频焦点管理
    private final PLVMediaPlayerAudioFocusManager audioFocusManager = new PLVMediaPlayerAudioFocusManager(getContext());

    // 生命周期
    private final PLVViewLifecycleObservable viewLifecycleObservable = new PLVViewLifecycleObservable();

    @Nullable
    private PLVMediaResource mediaResource = null;

    // 横竖屏方向
    private int lastOrientation = -1;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-构造方法">
    public PLVMediaPlayerSingleVideoLayout(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerSingleVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerSingleVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-初始化">
    public void init() {
        initProvider();
        initLayout();
        initVideoView();
        observeLifecycle();
        observeLaunchFloatWindow();
        updateVideoLayout();
    }

    private void initLayout() {
        setKeepScreenOn(true);
        handleOnEnterBackgroundComponent.setUserVisibleHint(true);
    }

    private void initProvider() {
        PLVMediaPlayerLocalProvider.localLifecycleObservable.on(this).provide(viewLifecycleObservable);
        PLVMediaPlayerLocalProvider.localDependScope.on(this).provide(dependScope);
    }

    private void initVideoView() {
        mediaViewModel.setPlayerOption(listOf(
                PLVMediaPlayerOptionEnum.ENABLE_ACCURATE_SEEK.value("1")
        ));
        mediaViewModel.setAutoContinue(true);

        auxiliaryViewModel.bind();

        audioFocusManager.startFocus(mediaViewModel);
    }

    private void observeLifecycle() {
        ((LifecycleOwner) getContext()).getLifecycle().addObserver(new GenericLifecycleObserver() {
            @Override
            public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY && source.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
                    destroy();
                }
            }
        });
    }

    private void observeLaunchFloatWindow() {
        mediaControllerViewModel
                .getLaunchFloatWindowEvent()
                .observeUntilViewDetached(this, new Function1<PLVFloatWindowLaunchReason, Unit>() {
                    @Override
                    public Unit invoke(PLVFloatWindowLaunchReason reason) {
                        onLaunchFloatWindowEvent(reason.code);
                        return null;
                    }
                });

        PLVMediaPlayerFloatWindowManager.getInstance().getFloatingViewShowState()
                .observeUntilViewDetached(this, new Function1<Boolean, Unit>() {
                    @Override
                    public Unit invoke(Boolean showing) {
                        if (!showing) {
                            // 从小窗模式回到页面
                            updateVideoLayout();
                        }
                        return null;
                    }
                });
    }

    private void updateVideoLayout() {
        removeAllViews();
        addView(handleOnEnterBackgroundComponent);
        if (isPortrait()) {
            portraitLayout.get().setVideoView(videoView);
            portraitLayout.get().setAuxiliaryVideoView(auxiliaryVideoView);
            addView(portraitLayout.get());
        } else {
            landscapeLayout.get().setVideoView(videoView);
            landscapeLayout.get().setAuxiliaryVideoView(auxiliaryVideoView);
            addView(landscapeLayout.get());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-setter/getter">
    public void setMediaResource(PLVMediaResource mediaResource) {
        this.mediaResource = mediaResource;
        if (mediaResource != null) {
            mediaViewModel.setMediaResource(mediaResource);
        }
    }

    public void setEnterFromFloatWindow(boolean enterFromFloatWindow) {
        auxiliaryViewModel.setEnterFromFloatWindow(enterFromFloatWindow);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-小窗逻辑处理">
    private void onLaunchFloatWindowEvent(final int reason) {
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

    private void launchFloatWindow(int reason) {
        final Rect videoSize = nullable(new Function0<Rect>() {
            @Override
            public Rect invoke() {
                return mediaViewModel.getMediaInfoViewState().getValue().getVideoSize();
            }
        });
        Rect floatWindowPosition = PLVMediaPlayerFloatWindowHelper.calculateFloatWindowPosition(videoSize);
        if (floatWindowPosition == null) {
            return;
        }
        PLVMediaPlayerFloatWindowContentLayout contentLayout = new PLVMediaPlayerFloatWindowContentLayout(getContext());
        PLVMediaPlayerLocalProvider.localDependScope.on(contentLayout).provide(dependScope);
        if (videoView.getParent() != null) {
            ((ViewGroup) videoView.getParent()).removeView(videoView);
        }
        contentLayout.getContainer().addView(videoView);

        PLVMediaPlayerFloatWindowManager.getInstance()
                .bindContentLayout(contentLayout)
                .saveData(new Consumer<Bundle>() {
                    @Override
                    public void accept(Bundle bundle) {
                        bundle.putParcelable(
                                PLVMediaPlayerFloatWindowManager.KEY_SAVE_MEDIA_RESOURCE,
                                mediaResource
                        );
                    }
                })
                .setFloatingSize(floatWindowPosition.width(), floatWindowPosition.height())
                .setFloatingPosition(floatWindowPosition.left, floatWindowPosition.top)
                .show(reason);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-横竖屏切换">
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation != lastOrientation) {
            lastOrientation = newConfig.orientation;
            updateVideoLayout();
        }
        if (isPortrait()) {
            // 只有横屏有操作锁定，竖屏没有
            mediaControllerViewModel.lockMediaController(LockMediaControllerAction.UNLOCK);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-返回和销毁">
    public boolean onBackPressed() {
        if (isLandscape()) {
            PLVActivityOrientationManager.on((AppCompatActivity) getContext())
                    .requestOrientation(true)
                    .setLockOrientation(false);
            return true;
        }
        return false;
    }

    private void destroy() {
        viewLifecycleObservable.callObserver(new Consumer<PLVViewLifecycleObservable.IViewLifecycleObserver>() {
            @Override
            public void accept(PLVViewLifecycleObservable.IViewLifecycleObserver observer) {
                observer.onDestroy(viewLifecycleObservable);
            }
        });
        PLVMediaPlayerFloatWindowManager.getInstance()
                .runOnFloatingWindowClosed(new Runnable() {
                    @Override
                    public void run() {
                        audioFocusManager.stopFocus();
                        dependScope.destroy();
                    }
                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="竖屏布局">
    private static class PortraitLayout extends FrameLayout {

        private PLVMediaPlayerSinglePortraitItemLayout singlePortVideoLayout;
        private FrameLayout singlePortVideoDetailContainer;
        private PLVMediaPlayerMoreActionLayoutPortrait singlePortVideoMoreActionLayout;

        public PortraitLayout(@NonNull Context context) {
            super(context);
        }

        public PortraitLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public PortraitLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        {
            LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_single_video_layout_port, this);
            singlePortVideoLayout = findViewById(R.id.plv_media_player_single_port_video_layout);
            singlePortVideoDetailContainer = findViewById(R.id.plv_media_player_single_port_video_detail_container);
            singlePortVideoMoreActionLayout = findViewById(R.id.plv_media_player_more_action_layout_portrait);
        }

        public void setVideoView(View videoView) {
            singlePortVideoLayout.setVideoView(videoView);
        }

        public void setAuxiliaryVideoView(View auxiliaryVideoView) {
            singlePortVideoLayout.setAuxiliaryVideoView(auxiliaryVideoView);
        }

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="横屏布局">
    private static class LandscapeLayout extends FrameLayout {

        private final PLVMediaPlayerSingleLandscapeItemLayout landscapeLayout = new PLVMediaPlayerSingleLandscapeItemLayout(getContext());

        public LandscapeLayout(@NonNull Context context) {
            super(context);
        }

        public LandscapeLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public LandscapeLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        {
            addView(landscapeLayout);
        }

        public void setVideoView(View videoView) {
            landscapeLayout.setVideoView(videoView);
        }

        public void setAuxiliaryVideoView(View auxiliaryVideoView) {
            landscapeLayout.setAuxiliaryVideoView(auxiliaryVideoView);
        }

    }
    // </editor-fold>

}
