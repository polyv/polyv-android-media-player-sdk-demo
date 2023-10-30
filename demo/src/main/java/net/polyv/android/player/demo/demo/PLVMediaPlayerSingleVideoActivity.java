package net.polyv.android.player.demo.demo;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;
import net.polyv.android.player.common.ui.component.floatwindow.IPLVMediaPlayerFloatWindowControlActionListener;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager;
import net.polyv.android.player.common.utils.orientation.PLVActivityOrientationManager;
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerState;
import net.polyv.android.player.demo.scene.single.PLVMediaPlayerSingleVideoLayout;
import net.polyv.android.player.sdk.PLVDeviceManager;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerSingleVideoActivity extends AppCompatActivity {

    // 进入页面时的视频资源传参Key
    public static final String KEY_TARGET_MEDIA_RESOURCE = "key_target_media_resource";

    // 进入页面时初始的视频资源
    @Nullable
    private PLVMediaResource targetMediaResource = null;

    // 实际的业务布局
    private PLVMediaPlayerSingleVideoLayout contentLayout;

    // <editor-fold defaultstate="collapsed" desc="页面-初始化和监听">
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 全局状态更新
        PLVMediaPlayerFloatWindowManager.getInstance().clear(); // 如果是从小窗状态进入，需要先销毁小窗
        updateStatusBar(); // 更新状态栏的横竖屏状态

        // 初始化 页面 和 Layout
        initActivity();
        initSingleVideoLayout();

        // 设置屏幕方向监听
        setupOrientationManager();

        // 设置悬浮窗监听
        setFloatWindowListener();
    }

    private void initActivity() {
        contentLayout = new PLVMediaPlayerSingleVideoLayout(this);
        setContentView(contentLayout);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            targetMediaResource = bundle.getParcelable(KEY_TARGET_MEDIA_RESOURCE);
        }
    }

    private void initSingleVideoLayout() {
        contentLayout.init();
        if (targetMediaResource != null) {
            contentLayout.setMediaResource(targetMediaResource);
        }

        onSingleVideoLayoutStatus();
    }

    // 初始化数据 - 从 intent 中读取视频资源数据 PLVMediaResource，并赋值给 contentLayout
    private void initLayoutData() {

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="页面-Layout监听">
    // 初始化监听器 - 监听播放状态
    private void onSingleVideoLayoutStatus() {
        // 监听播放状态
        contentLayout.getVideoView().getStateListenerRegistry().getPlayerState().observe(this, new Observer<PLVMediaPlayerState>() {
            @Override
            public void onChanged(@Nullable PLVMediaPlayerState plvMediaPlayerState) {
                switch (plvMediaPlayerState) {
                    case STATE_PLAYING:
                        break;
                    case STATE_PAUSED:
                        break;
                    case STATE_COMPLETED:
                        //  播放下一个视频
//                        PLVMediaResource anotherMediaResource = 获取下一个视频;
//                        contentLayout.setMediaResource(anotherMediaResource);
                        break;
                    case STATE_ERROR:
                        break;
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="页面-横竖屏切换">
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateStatusBar();
    }

    private void updateStatusBar() {
        if (ScreenUtils.isPortrait()) {
            PLVDeviceManager.showStatusBar(this);
        } else {
            PLVDeviceManager.hideStatusBar(this);
        }
    }

    private void setupOrientationManager() {
        PLVActivityOrientationManager.on(this)
                .setFollowSystemAutoRotate(true)
                .start();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="页面-悬浮窗控制">
    private void setFloatWindowListener() {
        PLVMediaPlayerFloatWindowManager.getInstance()
                .setControlActionListener(new IPLVMediaPlayerFloatWindowControlActionListener() {

                    @Override
                    public void onAfterFloatWindowShow() {
                        finish();
                    }

                    @Override
                    public void onClickContentGoBack(@Nullable PLVMediaResource saveMediaResource) {
                        Intent intent = new Intent(PLVMediaPlayerSingleVideoActivity.this, PLVMediaPlayerSingleVideoActivity.class);
                        intent.putExtra(PLVMediaPlayerSingleVideoActivity.KEY_TARGET_MEDIA_RESOURCE, saveMediaResource);
                        startActivity(intent);
                        PLVMediaPlayerFloatWindowManager.getInstance().hide();
                    }

                    @Override
                    public void onClickClose() {
                        PLVMediaPlayerFloatWindowManager.getInstance().clear();
                    }

                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="页面-返回和销毁">
    @Override
    public void onBackPressed() {
        if (contentLayout != null && contentLayout.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PLVMediaPlayerFloatWindowManager.getInstance()
                .runOnFloatingWindowClosed(new Runnable() {
                    @Override
                    public void run() {
                        PLVMediaPlayerFloatWindowManager.getInstance().clear();
                    }
                });
    }
    // </editor-fold>
}
