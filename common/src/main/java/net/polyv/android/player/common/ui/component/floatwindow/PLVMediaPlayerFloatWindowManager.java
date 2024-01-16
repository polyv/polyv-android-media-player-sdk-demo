package net.polyv.android.player.common.ui.component.floatwindow;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.mutableStateLiveData;

import android.app.Activity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.graphics.Point;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import net.polyv.android.player.common.ui.component.floatwindow.layout.PLVMediaPlayerFloatWindowContentLayout;
import net.polyv.android.player.common.utils.floatwindow.PLVFloatingWindowManager;
import net.polyv.android.player.common.utils.floatwindow.enums.PLVFloatingEnums;
import net.polyv.android.player.common.utils.floatwindow.permission.PLVFloatPermissionUtils;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerFloatWindowManager {

    // <editor-fold defaultstate="collapsed" desc="单例">

    private static final PLVMediaPlayerFloatWindowManager INSTANCE = new PLVMediaPlayerFloatWindowManager();

    private PLVMediaPlayerFloatWindowManager() {

    }

    public static PLVMediaPlayerFloatWindowManager getInstance() {
        return INSTANCE;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="变量">

    public static final int SHOW_REASON_MANUAL = 100;
    public static final int SHOW_REASON_ENTER_BACKGROUND = 200;

    // 保存小窗视频播放数据 PLVMediaResource
    public static final String KEY_SAVE_MEDIA_RESOURCE = "key_save_media_resource";

    private final Queue<Runnable> onClosedTaskQueue = new ArrayDeque<>();

    private final MutableLiveData<Boolean> floatingViewShowState = mutableStateLiveData(false);

    private final Bundle saveDataBundle = new Bundle();
    @Nullable
    private PLVMediaPlayerFloatWindowContentLayout contentLayout = null;

    private int left = 0;
    private int top = 0;
    private int width = 0;
    private int height = 0;
    private PLVFloatingEnums.ShowType showType = PLVFloatingEnums.ShowType.SHOW_ALWAYS;

    @Nullable
    private IPLVMediaPlayerFloatWindowControlActionListener controlActionListener = null;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public PLVMediaPlayerFloatWindowManager setControlActionListener(@Nullable IPLVMediaPlayerFloatWindowControlActionListener controlActionListener) {
        this.controlActionListener = controlActionListener;
        return this;
    }

    public PLVMediaPlayerFloatWindowManager saveData(@NonNull PLVSugarUtil.Consumer<Bundle> consumer) {
        consumer.accept(saveDataBundle);
        return this;
    }

    public PLVMediaPlayerFloatWindowManager bindContentLayout(@Nullable PLVMediaPlayerFloatWindowContentLayout layout) {
        this.contentLayout = layout;
        return this;
    }

    public PLVMediaPlayerFloatWindowManager setFloatingPosition(int left, int top) {
        this.left = left;
        this.top = top;
        PLVFloatingWindowManager.getInstance().updateFloatLocation(left, top);
        return this;
    }

    public PLVMediaPlayerFloatWindowManager setFloatingSize(int width, int height) {
        this.width = width;
        this.height = height;
        PLVFloatingWindowManager.getInstance().updateFloatSize(width, height);
        return this;
    }

    public PLVMediaPlayerFloatWindowManager updateShowType(PLVFloatingEnums.ShowType showType) {
        this.showType = showType;
        PLVFloatingWindowManager.getInstance().setShowType(showType);
        return this;
    }

    public PLVMediaPlayerFloatWindowManager runOnFloatingWindowClosed(Runnable runnable) {
        if (!isFloatingWindowShowing()) {
            runnable.run();
            return this;
        }
        onClosedTaskQueue.add(runnable);
        return this;
    }

    public void show(final int reason) {
        if (contentLayout == null || isFloatingWindowShowing()) {
            return;
        }
        PLVFloatPermissionUtils.requestPermission(
                (Activity) contentLayout.getContext(),
                new PLVFloatPermissionUtils.IPLVOverlayPermissionListener() {
                    @Override
                    public void onResult(boolean isGrant) {
                        if (isGrant) {
                            showFloatingWindow(reason);
                            floatingViewShowState.setValue(true);
                        }
                    }
                }
        );
    }

    public void hide() {
        if (contentLayout == null) {
            return;
        }
        closeFloatingWindow();
        contentLayout = null;
        saveDataBundle.clear();

        runAllClosePendingTask();
        floatingViewShowState.setValue(false);
    }

    public void clear() {
        hide();
        contentLayout = null;
        controlActionListener = null;
    }

    @Nullable
    public Point getFloatWindowLocation() {
        return PLVFloatingWindowManager.getInstance().getFloatLocation();
    }

    public boolean isFloatingWindowShowing() {
        return Boolean.TRUE.equals(floatingViewShowState.getValue());
    }

    public LiveData<Boolean> getFloatingViewShowState() {
        return floatingViewShowState;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">

    private void showFloatingWindow(int reason) {
        if (contentLayout == null) {
            return;
        }
        bindListener();
        PLVFloatingWindowManager.getInstance().createNewWindow((Activity) contentLayout.getContext())
                .setIsSystemWindow(true)
                .setContentView(contentLayout)
                .setSize(width, height)
                .setFloatLocation(left, top)
                .setShowType(showType)
                .setAutoMoveToEdge(PLVFloatingEnums.AutoEdgeType.AUTO_MOVE_TO_NEAREST_EDGE)
                .setAutoEdgeMargin(ConvertUtils.dp2px(6))
                .build()
                .show((Activity) contentLayout.getContext());
        if (controlActionListener != null) {
            controlActionListener.onAfterFloatWindowShow(reason);
        }
    }

    private void closeFloatingWindow() {
        PLVFloatingWindowManager.getInstance().hide();
        PLVFloatingWindowManager.getInstance().destroy();
    }

    private void bindListener() {
        if (contentLayout == null) {
            return;
        }
        contentLayout
                .setOnClickCloseListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (controlActionListener != null) {
                            controlActionListener.onClickClose();
                        }
                    }
                })
                .setOnClickContentGoBackListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (controlActionListener != null) {
                            controlActionListener.onClickContentGoBack((Bundle) saveDataBundle.clone());
                        }
                    }
                });
    }

    private void runAllClosePendingTask() {
        while (!onClosedTaskQueue.isEmpty()) {
            final Runnable task = onClosedTaskQueue.poll();
            if (task != null) {
                task.run();
            }
        }
    }

    // </editor-fold>

}
