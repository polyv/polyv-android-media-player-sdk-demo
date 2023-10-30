package net.polyv.android.player.common.ui.component.floatwindow;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.mutableLiveData;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.view.View;

import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;
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

    private final Queue<Runnable> onClosedTaskQueue = new ArrayDeque<>();

    private final MutableLiveData<Boolean> floatingViewShowState = mutableLiveData(false);

    @Nullable
    private PLVMediaResource saveMediaResource = null;
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

    public PLVMediaPlayerFloatWindowManager saveMediaResource(@Nullable PLVMediaResource mediaResource) {
        this.saveMediaResource = mediaResource;
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

    public void show() {
        if (contentLayout == null || isFloatingWindowShowing()) {
            return;
        }
        PLVFloatPermissionUtils.requestPermission(
                (Activity) contentLayout.getContext(),
                new PLVFloatPermissionUtils.IPLVOverlayPermissionListener() {
                    @Override
                    public void onResult(boolean isGrant) {
                        if (isGrant) {
                            showFloatingWindow();
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
        saveMediaResource = null;

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

    private void showFloatingWindow() {
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
            controlActionListener.onAfterFloatWindowShow();
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
                            controlActionListener.onClickContentGoBack(saveMediaResource);
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
