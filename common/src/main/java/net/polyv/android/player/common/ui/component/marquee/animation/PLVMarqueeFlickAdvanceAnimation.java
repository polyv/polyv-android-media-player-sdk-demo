package net.polyv.android.player.common.ui.component.marquee.animation;

import static net.polyv.android.player.sdk.foundation.lang.Duration.seconds;
import static net.polyv.android.player.sdk.foundation.lang.PLVTimerKt.timer;

import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver;

import net.polyv.android.player.sdk.foundation.lang.PLVTimer;

import java.util.HashMap;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * 跑马灯 闪烁增强动画类
 */
public class PLVMarqueeFlickAdvanceAnimation extends PLVMarqueeFlickAnimation {

    // <editor-fold desc="变量">
    @Nullable
    private View secondView;

    private PLVTimer viewPositionChangeTimer;
    private volatile boolean isStarted = false;

    // </editor-fold>

    // <editor-fold desc="对外API - 参数设置">
    @Override
    public void setViews(HashMap<Integer, View> viewMap) {
        super.setViews(viewMap);
        secondView = viewMap.get(VIEW_SECOND);
        if (secondView == null) {
            return;
        }
        secondView.setAlpha(0);
    }
    // </editor-fold>

    // <editor-fold desc="对外API - 生命周期控制">
    @Override
    public void start() {
        super.start();
        isStarted = true;
        if (secondView != null) {
            secondView.setAlpha(1);
        }
        if (viewPositionChangeTimer != null) {
            viewPositionChangeTimer.cancel();
        }
        viewPositionChangeTimer = timer(seconds(5), new Function1<Long, Unit>() {
            @Override
            public Unit invoke(Long aLong) {
                if (isStarted) {
                    setSecondActiveRect();
                }
                return null;
            }
        });
    }

    @Override
    public void pause() {
        super.pause();
        isStarted = false;
        if (secondView != null) {
            secondView.setAlpha(0);
        }
    }

    @Override
    public void stop() {
        super.stop();
        isStarted = false;
        if (secondView != null) {
            secondView.setAlpha(0);
        }
        if (viewPositionChangeTimer != null) {
            viewPositionChangeTimer.cancel();
            viewPositionChangeTimer = null;
        }
    }

    @Override
    public void onParentSizeChanged(final View parentView) {
        super.onParentSizeChanged(parentView);
        if (secondView == null) {
            return;
        }
        secondView.clearAnimation();
        secondView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                screenWidth = parentView.getWidth();
                screenHeight = parentView.getHeight();
                secondView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setSecondActiveRect();
            }
        });
    }
    // </editor-fold>

    // <editor-fold desc="功能模块 - 设置位置">

    // 设置活动区域
    protected void setSecondActiveRect() {
        if (secondView == null) {
            return;
        }
        MarginLayoutParams lp = (MarginLayoutParams) secondView.getLayoutParams();
        lp.topMargin = (int) (Math.random() * (screenHeight - Math.min(screenHeight, secondView.getHeight())));
        lp.leftMargin = (int) (Math.random() * (screenWidth - Math.min(screenWidth, secondView.getWidth())));
        secondView.setLayoutParams(lp);
    }

    // </editor-fold>

}
