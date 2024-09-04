package net.polyv.android.player.scenes.feed.item;

import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.isPortrait;
import static net.polyv.android.player.sdk.foundation.lang.NullablesKt.nullable;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;
import net.polyv.android.player.common.di.PLVMPCommonModuleKt;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.view.PLVMPVideoView;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.LockMediaControllerAction;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerHandleOnEnterBackgroundComponent;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowHelper;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager;
import net.polyv.android.player.common.ui.component.floatwindow.layout.PLVMediaPlayerFloatWindowContentLayout;
import net.polyv.android.player.common.utils.floatwindow.enums.PLVFloatWindowLaunchReason;
import net.polyv.android.player.common.utils.floatwindow.permission.PLVFloatPermissionUtils;
import net.polyv.android.player.common.utils.ui.PLVViewLifecycleObservable;
import net.polyv.android.player.scenes.feed.item.layout.PLVMediaPlayerFeedLandscapeItemLayout;
import net.polyv.android.player.scenes.feed.item.layout.PLVMediaPlayerFeedPortraitItemLayout;
import net.polyv.android.player.sdk.foundation.di.DependScope;
import net.polyv.android.player.sdk.foundation.lang.Consumer;
import net.polyv.android.player.sdk.foundation.lang.Lazy;

import org.jetbrains.annotations.NotNull;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerFeedVideoItemFragment extends Fragment {

    // <editor-fold defaultstate="collapsed" desc="Fragment-属性">
    // 装载皮肤布局的容器，用于 addView 皮肤布局（ 竖屏-全屏 皮肤布局 或者 横屏-全屏 皮肤布局 ）
    @Nullable
    private FrameLayout rootContainer;

    // 竖屏-全屏 皮肤布局
    @Nullable
    private Lazy<PLVMediaPlayerFeedPortraitItemLayout> portraitVideoLayout;

    // 横屏-全屏 皮肤布局
    @Nullable
    private Lazy<PLVMediaPlayerFeedLandscapeItemLayout> landscapeVideoLayout;

    @Nullable
    private DependScope dependScope = null;
    @Nullable
    private View videoView = null;
    @Nullable
    private PLVMPMediaViewModel mediaViewModel = null;
    @Nullable
    private PLVMPMediaControllerViewModel mediaControllerViewModel = null;

    // App进入后台时自动唤起小窗
    @Nullable
    private PLVMediaPlayerHandleOnEnterBackgroundComponent handleOnEnterBackgroundComponent;

    // 裸播放器状态 的数据处理
    private final PLVMediaPlayerFeedVideoStateHandler videoStateHandler = new PLVMediaPlayerFeedVideoStateHandler();

    // 生命周期
    private final PLVViewLifecycleObservable viewLifecycleObservable = new PLVViewLifecycleObservable();

    @Nullable
    private PLVMediaResource mediaResource = null;

    // 记录上一次的屏幕方向
    private int lastOrientation = -1;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fragment-初始化和生命周期方法">
    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        final Context context = inflater.getContext();
        rootContainer = new FrameLayout(context);
        portraitVideoLayout = new Lazy<PLVMediaPlayerFeedPortraitItemLayout>() {
            @Override
            public PLVMediaPlayerFeedPortraitItemLayout onLazyInit() {
                return new PLVMediaPlayerFeedPortraitItemLayout(context);
            }
        };
        landscapeVideoLayout = new Lazy<PLVMediaPlayerFeedLandscapeItemLayout>() {
            @Override
            public PLVMediaPlayerFeedLandscapeItemLayout onLazyInit() {
                return new PLVMediaPlayerFeedLandscapeItemLayout(context);
            }
        };
        dependScope = new DependScope(PLVMPCommonModuleKt.commonItemModule);
        this.videoView = new PLVMPVideoView(context, dependScope);
        this.mediaViewModel = dependScope.get(PLVMPMediaViewModel.class);
        this.mediaControllerViewModel = dependScope.get(PLVMPMediaControllerViewModel.class);
        handleOnEnterBackgroundComponent = new PLVMediaPlayerHandleOnEnterBackgroundComponent(context);

        PLVMediaPlayerLocalProvider.localDependScope.on(rootContainer).provide(dependScope);
        PLVMediaPlayerLocalProvider.localLifecycleObservable.on(rootContainer).provide(viewLifecycleObservable);
        observeLaunchFloatWindow();

        videoStateHandler.onCreateView(context, dependScope);
        return rootContainer;
    }

    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateVideoLayout();
        videoStateHandler.onActivityCreated();
        if (handleOnEnterBackgroundComponent != null) {
            handleOnEnterBackgroundComponent.setUserVisibleHint(getUserVisibleHint());
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        videoStateHandler.setUserVisibleHint(isVisibleToUser);
        if (handleOnEnterBackgroundComponent != null) {
            handleOnEnterBackgroundComponent.setUserVisibleHint(isVisibleToUser);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        videoStateHandler.onDestroyView();
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
                        if (portraitVideoLayout != null && portraitVideoLayout.isInitialized()) {
                            portraitVideoLayout.get().setVideoView(null);
                        }
                        portraitVideoLayout = null;
                        if (landscapeVideoLayout != null && landscapeVideoLayout.isInitialized()) {
                            landscapeVideoLayout.get().setVideoView(null);
                        }
                        landscapeVideoLayout = null;
                        if (rootContainer != null) {
                            rootContainer.removeAllViews();
                        }
                        rootContainer = null;
                        if (dependScope != null) {
                            dependScope.destroy();
                        }
                    }
                });
    }
    // </editor-fold>

    private void observeLaunchFloatWindow() {
        requireNotNull(dependScope).get(PLVMPMediaControllerViewModel.class)
                .getLaunchFloatWindowEvent()
                .observeUntilViewDetached(rootContainer, new Function1<PLVFloatWindowLaunchReason, Unit>() {
                    @Override
                    public Unit invoke(PLVFloatWindowLaunchReason reason) {
                        onLaunchFloatWindowEvent(reason.code);
                        return null;
                    }
                });

        PLVMediaPlayerFloatWindowManager.getInstance().getFloatingViewShowState()
                .observeUntilViewDetached(rootContainer, new Function1<Boolean, Unit>() {
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
        final Rect videoSize = nullable(new Function0<Rect>() {
            @Override
            public Rect invoke() {
                return mediaViewModel.getMediaInfoViewState().getValue().getVideoSize();
            }
        });
        Rect floatWindowPosition = PLVMediaPlayerFloatWindowHelper.calculateFloatWindowPosition(videoSize);
        if (videoView == null || floatWindowPosition == null) {
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

    // <editor-fold defaultstate="collapsed" desc="Fragment-更新UI-用于 初始化、横竖屏切换 时">
    private void updateVideoLayout() {
        if (rootContainer == null || portraitVideoLayout == null || landscapeVideoLayout == null) {
            return;
        }

        rootContainer.removeAllViews();
        if (handleOnEnterBackgroundComponent != null) {
            rootContainer.addView(handleOnEnterBackgroundComponent);
        }

        // 根据屏幕方向，切换对应的横屏或者竖屏皮肤，并设置裸播放器进对应的皮肤
        if (isPortrait()) {
            portraitVideoLayout.get().setVideoView(videoView);
            rootContainer.addView(portraitVideoLayout.get());
        } else {
            landscapeVideoLayout.get().setVideoView(videoView);
            rootContainer.addView(landscapeVideoLayout.get());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fragment-监听和处理设备的横竖屏切换事件">
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation != lastOrientation) {
            lastOrientation = newConfig.orientation;
            updateVideoLayout();
        }
        if (isPortrait() && mediaControllerViewModel != null) {
            // 只有横屏有操作锁定，竖屏没有
            mediaControllerViewModel.lockMediaController(LockMediaControllerAction.UNLOCK);
            // 竖屏控制栏长显
            mediaControllerViewModel.changeControllerVisible(true);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fragment-从外部设置视频资源信息">
    public void setMediaResource(PLVMediaResource mediaResource) {
        this.mediaResource = mediaResource;
        videoStateHandler.setMediaResource(mediaResource);
    }
    // </editor-fold>

}
