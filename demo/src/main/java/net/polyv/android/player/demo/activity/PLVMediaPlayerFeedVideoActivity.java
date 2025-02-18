package net.polyv.android.player.demo.activity;

import static net.polyv.android.player.sdk.foundation.collections.CollectionsKt.listOf;
import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.isPortrait;

import androidx.lifecycle.ViewModelProvider;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.WindowManager;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;
import net.polyv.android.player.common.ui.component.floatwindow.IPLVMediaPlayerFloatWindowControlActionListener;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager;
import net.polyv.android.player.common.ui.router.PLVMediaPlayerRouter;
import net.polyv.android.player.common.ui.router.RouterDestination;
import net.polyv.android.player.common.ui.router.RouterPayload;
import net.polyv.android.player.common.ui.router.RouterPayloadStaticHolder;
import net.polyv.android.player.common.utils.orientation.PLVActivityOrientationManager;
import net.polyv.android.player.demo.mock.PLVMockFeedVideoDataViewModel;
import net.polyv.android.player.scenes.feed.PLVMediaPlayerFeedVideoLayout;
import net.polyv.android.player.scenes.feed.viewmodel.IPLVMediaPlayerFeedVideoDataViewModel;
import net.polyv.android.player.sdk.PLVDeviceManager;

import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerFeedVideoActivity extends AppCompatActivity {

    // 禁止录屏、截图
    public static final boolean FLAG_SECURE_WINDOW = false;

    // 实际的业务布局
    private PLVMediaPlayerFeedVideoLayout contentLayout = null;

    // <editor-fold defaultstate="collapsed" desc="页面-初始化">
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLAG_SECURE_WINDOW) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // 全局状态更新
        PLVMediaPlayerFloatWindowManager.getInstance().clear(); // 如果是从小窗状态进入，需要先销毁小窗
        updateStatusBar(); // 更新状态栏的横竖屏状态

        // 初始化 页面 和 Layout
        initActivity();
        initFeedVideoLayout();

        // 设置屏幕方向监听
        setupOrientationManager();

        // 设置悬浮窗监听
        setFloatWindowListener();
    }

    private void initActivity() {
        contentLayout = new PLVMediaPlayerFeedVideoLayout(this);
        setContentView(contentLayout);
    }

    private void initFeedVideoLayout() {
        // 模拟 生成Feed流容器的数据的请求实现类，客户集成式，需要替换的
        IPLVMediaPlayerFeedVideoDataViewModel feedVideoDataViewModel = mock();

        // 把 Feed流容器的数据的请求实现类 传入Feed流布局，以备后续Feed流容器获取更新的数据
        contentLayout.init(feedVideoDataViewModel);

        // 获取传入的第一页数据
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int holderId = bundle.getInt(PLVMediaPlayerRouter.KEY_TARGET_MEDIA_RESOURCE_LIST_HOLDER_ID, 0);
            RouterPayloadStaticHolder<List<PLVMediaResource>> holder = RouterPayloadStaticHolder.remove(holderId);
            if (holder != null) {
                contentLayout.setTargetMediaResource(holder.getValue());
            }
        }
    }

    private IPLVMediaPlayerFeedVideoDataViewModel mock() {
        return new ViewModelProvider(
                this,
                new ViewModelProvider.NewInstanceFactory()
        ).get(PLVMockFeedVideoDataViewModel.class);
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
            PLVDeviceManager.setTransparentStatusBar(getWindow());
        } else {
            PLVDeviceManager.hideStatusBar(this);
        }
    }

    private void setupOrientationManager() {
        PLVActivityOrientationManager.on(this).start();
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

                        Intent intent = PLVMediaPlayerRouter.router(
                                PLVMediaPlayerFeedVideoActivity.this,
                                new RouterDestination.SceneFeed(new RouterPayload.SceneFeedPayload(
                                        RouterPayloadStaticHolder.create(listOf(mediaResource))
                                ))
                        );
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
