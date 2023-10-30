package net.polyv.android.player.demo.scene.single;

import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.plv.foundationsdk.component.exts.Lazy;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;
import net.polyv.android.player.common.ui.component.PLVMediaPlayerMoreActionLayoutPortrait;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.common.utils.orientation.PLVActivityOrientationManager;
import net.polyv.android.player.core.api.option.PLVMediaPlayerOptionEnum;
import net.polyv.android.player.demo.R;
import net.polyv.android.player.demo.scene.single.layout.PLVMediaPlayerSingleLandscapeItemLayout;
import net.polyv.android.player.demo.scene.single.layout.PLVMediaPlayerSinglePortraitItemLayout;
import net.polyv.android.player.sdk.PLVVideoView;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerSingleVideoLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="Layout-属性">
    // 裸播放器 view
    private final PLVVideoView videoView = new PLVVideoView(getContext());

    // 播放器皮肤 对应的 数据模型，控制 播放器皮肤 的显示状态
    private final PLVMediaPlayerControlViewModel controlViewModel = new PLVMediaPlayerControlViewModel();

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
        initLayout();  // 初始化播放器皮肤 - 设置UI可见的布局
        initProvider();  // 初始化本地共享数据的监听
        initVideoView();   // 初始化裸播放器 - 配置UI不可见的播放器底层参数
        observeLifecycle();  // 监听播放器生命周期 - 销毁处理
        updateVideoLayout();   // 更新布局
    }

    private void initLayout() {
        setKeepScreenOn(true); // 缺省设置屏幕常亮
    }

    private void initProvider() {
        // 监听裸播放器的状态变化
        PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).provide(videoView);

        // 监听皮肤的状态变化
        PLVMediaPlayerLocalProvider.localControlViewModel.on(this).provide(controlViewModel);
    }

    private void initVideoView() {
        videoView.setPlayerOption(listOf(
                PLVMediaPlayerOptionEnum.ENABLE_ACCURATE_SEEK.value("1")
        ));
        videoView.setAutoContinue(true);
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

    private void updateVideoLayout() {
        removeAllViews();
        if (ScreenUtils.isPortrait()) {
            portraitLayout.get().setVideoView(videoView);
            addView(portraitLayout.get());
        } else {
            landscapeLayout.get().setVideoView(videoView);
            addView(landscapeLayout.get());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-setter/getter">
    public void setMediaResource(PLVMediaResource mediaResource) {
        if (mediaResource != null && videoView != null) {
            videoView.setMediaResource(mediaResource);
        }
    }

    public PLVMediaResource getMediaResource() {
        LiveData<PLVMediaResource> mediaResource = null;
        if (videoView != null) {
            mediaResource = videoView.getBusinessListenerRegistry().getCurrentMediaResource();
        }
        return mediaResource.getValue();
    }

    public PLVVideoView getVideoView() {
        return videoView;
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
        if (ScreenUtils.isPortrait()) {
            // 只有横屏有操作锁定，竖屏没有
            controlViewModel.requestControl(PLVMediaPlayerControlAction.lockMediaController(false));
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-返回和销毁">
    public boolean onBackPressed() {
        if (ScreenUtils.isLandscape()) {
            PLVActivityOrientationManager.on((AppCompatActivity) getContext())
                    .requestOrientation(true)
                    .setLockOrientation(false);
            return true;
        }
        return false;
    }

    private void destroy() {
        PLVMediaPlayerFloatWindowManager.getInstance()
                .runOnFloatingWindowClosed(new Runnable() {
                    @Override
                    public void run() {
                        videoView.destroy();
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

        public void setVideoView(PLVVideoView videoView) {
            singlePortVideoLayout.setVideoView(videoView);
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

        public void setVideoView(PLVVideoView videoView) {
            landscapeLayout.setVideoView(videoView);
        }

    }
    // </editor-fold>

}
