package net.polyv.android.player.demo.activity;

import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.isPortrait;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;
import net.polyv.android.player.common.ui.component.floatwindow.IPLVMediaPlayerFloatWindowControlActionListener;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager;
import net.polyv.android.player.common.utils.orientation.PLVActivityOrientationManager;
import net.polyv.android.player.scenes.single.PLVMediaPlayerSingleVideoLayout;
import net.polyv.android.player.sdk.PLVDeviceManager;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerSingleVideoActivity extends AppCompatActivity {

    // 进入页面时的视频资源传参Key
    public static final String KEY_TARGET_MEDIA_RESOURCE = "key_target_media_resource";
    public static final String KEY_ENTER_FROM_FLOAT_WINDOW = "key_enter_from_float_window";

    // 进入页面时初始的视频资源
    @Nullable
    private PLVMediaResource targetMediaResource = null;
    private boolean enterFromFloatWindow = false;

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
            enterFromFloatWindow = bundle.getBoolean(KEY_ENTER_FROM_FLOAT_WINDOW, false);
        }
    }

    private void initSingleVideoLayout() {
        contentLayout.init();
        if (targetMediaResource != null) {
            contentLayout.setMediaResource(targetMediaResource);
        }
        contentLayout.setEnterFromFloatWindow(enterFromFloatWindow);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="页面-横竖屏切换">
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateStatusBar();
    }

    private void updateStatusBar() {
        if (isPortrait()) {
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
                    public void onAfterFloatWindowShow(int reason) {
                        if (reason == PLVMediaPlayerFloatWindowManager.SHOW_REASON_MANUAL) {
                            finish();
                        }
                    }

                    @Override
                    public void onClickContentGoBack(@NonNull Bundle bundle) {
                        PLVMediaResource mediaResource = bundle.getParcelable(PLVMediaPlayerFloatWindowManager.KEY_SAVE_MEDIA_RESOURCE);

                        if (mediaResource == null) {
                            onClickClose();
                            return;
                        }

                        Intent intent = new Intent(PLVMediaPlayerSingleVideoActivity.this, PLVMediaPlayerSingleVideoActivity.class);
                        intent.putExtra(PLVMediaPlayerSingleVideoActivity.KEY_TARGET_MEDIA_RESOURCE, mediaResource);
                        intent.putExtra(PLVMediaPlayerSingleVideoActivity.KEY_ENTER_FROM_FLOAT_WINDOW, true);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);

                        PLVMediaPlayerFloatWindowManager.getInstance().hide();
                    }

                    @Override
                    public void onClickClose() {
                        PLVMediaPlayerFloatWindowManager.getInstance().hide();
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
