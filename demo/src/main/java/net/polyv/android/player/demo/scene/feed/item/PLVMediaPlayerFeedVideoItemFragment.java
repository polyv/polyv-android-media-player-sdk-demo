package net.polyv.android.player.demo.scene.feed.item;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.plv.foundationsdk.component.exts.Lazy;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerHandleOnEnterBackgroundComponent;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.common.utils.ui.PLVViewLifecycleObservable;
import net.polyv.android.player.demo.scene.feed.item.layout.PLVMediaPlayerFeedLandscapeItemLayout;
import net.polyv.android.player.demo.scene.feed.item.layout.PLVMediaPlayerFeedPortraitItemLayout;
import net.polyv.android.player.sdk.PLVVideoView;

import org.jetbrains.annotations.NotNull;

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

    // 裸播放器，会被 addView 到 皮肤布局
    @Nullable
    private PLVVideoView videoView;

    // App进入后台时自动唤起小窗
    @Nullable
    private PLVMediaPlayerHandleOnEnterBackgroundComponent handleOnEnterBackgroundComponent;

    // 皮肤状态 的数据监听中心，用于监听播放器状态的变化
    private final PLVMediaPlayerControlViewModel controlViewModel = new PLVMediaPlayerControlViewModel();

    // 裸播放器状态 的数据处理
    private final PLVMediaPlayerFeedVideoStateHandler videoStateHandler = new PLVMediaPlayerFeedVideoStateHandler();

    // 生命周期
    private final PLVViewLifecycleObservable viewLifecycleObservable = new PLVViewLifecycleObservable();

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
        videoView = new PLVVideoView(context);
        handleOnEnterBackgroundComponent = new PLVMediaPlayerHandleOnEnterBackgroundComponent(context);

        PLVMediaPlayerLocalProvider.localMediaPlayer.on(rootContainer).provide(videoView);
        PLVMediaPlayerLocalProvider.localControlViewModel.on(rootContainer).provide(controlViewModel);
        PLVMediaPlayerLocalProvider.localLifecycleObservable.on(rootContainer).provide(viewLifecycleObservable);

        videoStateHandler.onCreateView(videoView, controlViewModel);
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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fragment-销毁">
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        videoStateHandler.onDestroyView();
        viewLifecycleObservable.callObserver(new PLVSugarUtil.Consumer<PLVViewLifecycleObservable.IViewLifecycleObserver>() {
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
                        videoView = null;
                        if (rootContainer != null) {
                            rootContainer.removeAllViews();
                        }
                        rootContainer = null;
                    }
                });
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
        if (ScreenUtils.isPortrait()) {
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
        if (ScreenUtils.isPortrait()) {
            // 只有横屏有操作锁定，竖屏没有
            controlViewModel.requestControl(PLVMediaPlayerControlAction.lockMediaController(false));
            // 竖屏控制栏长显
            controlViewModel.requestControl(PLVMediaPlayerControlAction.showMediaController());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fragment-从外部设置视频资源信息">
    public void setMediaResource(PLVMediaResource mediaResource) {
        videoStateHandler.setMediaResource(mediaResource);
    }
    // </editor-fold>

}
