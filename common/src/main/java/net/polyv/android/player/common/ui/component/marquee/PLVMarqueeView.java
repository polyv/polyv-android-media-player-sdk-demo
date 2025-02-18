package net.polyv.android.player.common.ui.component.marquee;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import net.polyv.android.player.common.ui.component.marquee.animation.PLVMarqueeAnimation;
import net.polyv.android.player.common.ui.component.marquee.animation.PLVMarqueeFlick15PercentAnimation;
import net.polyv.android.player.common.ui.component.marquee.animation.PLVMarqueeFlickAdvanceAnimation;
import net.polyv.android.player.common.ui.component.marquee.animation.PLVMarqueeFlickAnimation;
import net.polyv.android.player.common.ui.component.marquee.animation.PLVMarqueeMergeRollFlickAnimation;
import net.polyv.android.player.common.ui.component.marquee.animation.PLVMarqueeRoll15PercentAnimation;
import net.polyv.android.player.common.ui.component.marquee.animation.PLVMarqueeRollAdvanceAnimation;
import net.polyv.android.player.common.ui.component.marquee.animation.PLVMarqueeRollAnimation;
import net.polyv.android.player.common.ui.component.marquee.model.PLVMarqueeAnimationVO;
import net.polyv.android.player.common.ui.component.marquee.model.PLVMarqueeModel;
import net.polyv.android.player.common.ui.component.marquee.model.PLVMarqueeTextVO;
import net.polyv.android.player.sdk.foundation.log.PLVMediaPlayerLogger;

import java.util.HashMap;

/**
 * 跑马灯的实现类
 */
public class PLVMarqueeView extends RelativeLayout implements IPLVMarqueeView {
    private static final String TAG = "PLVMarqueeView";

    // <editor-fold desc="变量">

    private PLVMarqueeTextView mainTextView;
    private PLVMarqueeTextView secondTextView;
    private PLVMarqueeAnimation marqueeAnimation;
    private PLVMarqueeModel plvMarqueeModel;

    private boolean isResetMarqueeVO = false;
    // </editor-fold>

    // <editor-fold desc="构造方法">
    public PLVMarqueeView(Context context) {
        super(context);
        initView(context);
    }

    public PLVMarqueeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PLVMarqueeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }
    // </editor-fold>

    // <editor-fold desc="view 的初始化">
    private void initView(Context context) {
        mainTextView = new PLVMarqueeTextView(context);
        secondTextView = new PLVMarqueeTextView(context);
        addView(mainTextView);
        addView(secondTextView);
        secondTextView.setAlpha(0F);
    }
    // </editor-fold>

    // <editor-fold desc="对外调用">
    @Override
    public void setPLVMarqueeModel(final PLVMarqueeModel marqueeVO) {
        plvMarqueeModel = marqueeVO;
        isResetMarqueeVO = true;
    }

    @Override
    public void start() {
        if (isResetMarqueeVO) {
            updatePLVMarqueeModel(plvMarqueeModel);
            isResetMarqueeVO = false;
        } else {
            if (marqueeAnimation != null) {
                marqueeAnimation.start();
            } else {
                PLVMediaPlayerLogger.debug(TAG, "need to excute setPLVMarqueeModel before start");
            }
        }
    }

    @Override
    public void pause() {
        if (marqueeAnimation != null) {
            marqueeAnimation.pause();
        }
    }

    @Override
    public void stop() {
        if (marqueeAnimation != null) {
            marqueeAnimation.stop();
        }
    }
    // </editor-fold>

    // <editor-fold desc="跑马灯功能实现和样式设置">

    /**
     * 正式更新跑马灯样式的实现
     *
     * @param plvMarqueeVO
     */
    private void updatePLVMarqueeModel(final PLVMarqueeModel plvMarqueeVO) {
        plvMarqueeVO.setSecondDefaultTextVO();

        setMainTextVO(plvMarqueeVO.getMainTextVO());
        setSecondTextVO(plvMarqueeVO.getSecondTextVO());
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= 16) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                mainTextView.setWidth(mainTextView.getWidth() + 2 * mainTextView.getStrokeWidth());
                mainTextView.setHeight(mainTextView.getHeight() + 2 * mainTextView.getStrokeWidth());
                secondTextView.setWidth(secondTextView.getWidth() + 2 * secondTextView.getStrokeWidth());
                secondTextView.setHeight(secondTextView.getHeight() + 2 * secondTextView.getStrokeWidth());
                setMarqueeAnimationType(plvMarqueeVO.getAnimationVO());
            }
        });
    }

    /**
     * 设置 主跑马灯的样式
     */
    private void setMainTextVO(PLVMarqueeTextVO textVO) {
        this.mainTextView.setMarqueeTextModel(textVO);
    }

    /**
     * 设置 副跑马灯的样式
     */
    private void setSecondTextVO(PLVMarqueeTextVO textVO) {
        this.secondTextView.setMarqueeTextModel(
                textVO.setFontAlpha((int) (0.02F * 255))
                        .setFontColor(Color.BLACK)
                        .setFilter(true)
                        .setFilterStrength(1)
                        .setFilterBlurX(0)
                        .setFilterBlurY(0)
                        .setFilterAlpha(0.02F * 255)
                        .setFilterColor(Color.WHITE)
        );
    }

    /**
     * 设置 动画类型和参数
     */
    private void setMarqueeAnimationType(PLVMarqueeAnimationVO animationVO) {
        if (marqueeAnimation != null) {
            marqueeAnimation.stop();
        }
        mainTextView.clearAnimation();
        secondTextView.clearAnimation();

        int animationType = animationVO.getAnimationType();

        int viewHeight = mainTextView.getMeasuredHeight() + 2 * mainTextView.getStrokeWidth();
        int viewWidth = mainTextView.getMeasuredWidth() + 2 * mainTextView.getStrokeWidth();

        int secondViewHeight = secondTextView.getMeasuredHeight() + 2 * secondTextView.getStrokeWidth();
        int secondViewWidth = secondTextView.getMeasuredWidth() + 2 * secondTextView.getStrokeWidth();

        int screenWidth = getWidth();
        int screenHeight = getHeight();

        int duration = animationVO.getSpeed() / 10 * 1000;
        int lifeTime = animationVO.getLifeTime() * 1000;
        int interval = animationVO.getInterval() * 1000;
        int tweenTime = animationVO.getTweenTime() * 1000;

        boolean isAlwaysShowWhenRun = animationVO.isAlwaysShowWhenRun();
        boolean isHiddenWhenPause = animationVO.isHiddenWhenPause();

        HashMap<Integer, Integer> paramMap = new HashMap<>();
        HashMap<Integer, View> viewMap = new HashMap<>();

        switch (animationType) {
            case PLVMarqueeAnimationVO.ROLL:
                marqueeAnimation = new PLVMarqueeRollAnimation();

                viewMap.put(PLVMarqueeAnimation.VIEW_MAIN, mainTextView);

                paramMap.put(PLVMarqueeAnimation.PARAM_VIEW_WIDTH, viewWidth);
                paramMap.put(PLVMarqueeAnimation.PARAM_VIEW_HEIGHT, viewHeight);
                paramMap.put(PLVMarqueeAnimation.PARAM_SCREEN_WIDTH, screenWidth);
                paramMap.put(PLVMarqueeAnimation.PARAM_SCREEN_HEIGHT, screenHeight);
                paramMap.put(PLVMarqueeAnimation.PARAM_DURATION, duration);
                paramMap.put(PLVMarqueeAnimation.PARAM_INTERVAL, interval);
                paramMap.put(PLVMarqueeAnimation.PARAM_ISALWAYS_SHOW_WHEN_RUN, isAlwaysShowWhenRun ? 1 : 0);
                paramMap.put(PLVMarqueeAnimation.PARAM_HIDE_WHEN_PAUSE, isHiddenWhenPause ? 1 : 0);

                break;
            case PLVMarqueeAnimationVO.ROLL_15PERCENT:
                marqueeAnimation = new PLVMarqueeRoll15PercentAnimation();

                viewMap.put(PLVMarqueeAnimation.VIEW_MAIN, mainTextView);

                paramMap.put(PLVMarqueeAnimation.PARAM_VIEW_WIDTH, viewWidth);
                paramMap.put(PLVMarqueeAnimation.PARAM_VIEW_HEIGHT, viewHeight);
                paramMap.put(PLVMarqueeAnimation.PARAM_SCREEN_WIDTH, screenWidth);
                paramMap.put(PLVMarqueeAnimation.PARAM_SCREEN_HEIGHT, screenHeight);
                paramMap.put(PLVMarqueeAnimation.PARAM_DURATION, duration);
                paramMap.put(PLVMarqueeAnimation.PARAM_INTERVAL, interval);
                paramMap.put(PLVMarqueeAnimation.PARAM_ISALWAYS_SHOW_WHEN_RUN, isAlwaysShowWhenRun ? 1 : 0);
                paramMap.put(PLVMarqueeAnimation.PARAM_HIDE_WHEN_PAUSE, isHiddenWhenPause ? 1 : 0);

                break;
            case PLVMarqueeAnimationVO.FLICK:
                marqueeAnimation = new PLVMarqueeFlickAnimation();

                viewMap.put(PLVMarqueeAnimation.VIEW_MAIN, mainTextView);

                paramMap.put(PLVMarqueeAnimation.PARAM_VIEW_WIDTH, viewWidth);
                paramMap.put(PLVMarqueeAnimation.PARAM_VIEW_HEIGHT, viewHeight);
                paramMap.put(PLVMarqueeAnimation.PARAM_SCREEN_WIDTH, screenWidth);
                paramMap.put(PLVMarqueeAnimation.PARAM_SCREEN_HEIGHT, screenHeight);
                paramMap.put(PLVMarqueeAnimation.PARAM_LIFE_TIME, lifeTime);
                paramMap.put(PLVMarqueeAnimation.PARAM_INTERVAL, interval);
                paramMap.put(PLVMarqueeAnimation.PARAM_TWEEN_TIME, tweenTime);
                paramMap.put(PLVMarqueeAnimation.PARAM_ISALWAYS_SHOW_WHEN_RUN, isAlwaysShowWhenRun ? 1 : 0);
                paramMap.put(PLVMarqueeAnimation.PARAM_HIDE_WHEN_PAUSE, isHiddenWhenPause ? 1 : 0);
                break;
            case PLVMarqueeAnimationVO.FLICK_15PERCENT:
                marqueeAnimation = new PLVMarqueeFlick15PercentAnimation();

                viewMap.put(PLVMarqueeAnimation.VIEW_MAIN, mainTextView);

                paramMap.put(PLVMarqueeAnimation.PARAM_VIEW_WIDTH, viewWidth);
                paramMap.put(PLVMarqueeAnimation.PARAM_VIEW_HEIGHT, viewHeight);
                paramMap.put(PLVMarqueeAnimation.PARAM_SCREEN_WIDTH, screenWidth);
                paramMap.put(PLVMarqueeAnimation.PARAM_SCREEN_HEIGHT, screenHeight);
                paramMap.put(PLVMarqueeAnimation.PARAM_LIFE_TIME, lifeTime);
                paramMap.put(PLVMarqueeAnimation.PARAM_INTERVAL, interval);
                paramMap.put(PLVMarqueeAnimation.PARAM_TWEEN_TIME, tweenTime);
                paramMap.put(PLVMarqueeAnimation.PARAM_ISALWAYS_SHOW_WHEN_RUN, isAlwaysShowWhenRun ? 1 : 0);
                paramMap.put(PLVMarqueeAnimation.PARAM_HIDE_WHEN_PAUSE, isHiddenWhenPause ? 1 : 0);
                break;
            case PLVMarqueeAnimationVO.MERGE_ROLL_FLICK:
                marqueeAnimation = new PLVMarqueeMergeRollFlickAnimation();

                viewMap.put(PLVMarqueeAnimation.VIEW_MAIN, mainTextView);

                paramMap.put(PLVMarqueeAnimation.PARAM_VIEW_WIDTH, viewWidth);
                paramMap.put(PLVMarqueeAnimation.PARAM_VIEW_HEIGHT, viewHeight);
                paramMap.put(PLVMarqueeAnimation.PARAM_SCREEN_WIDTH, screenWidth);
                paramMap.put(PLVMarqueeAnimation.PARAM_SCREEN_HEIGHT, screenHeight);
                paramMap.put(PLVMarqueeAnimation.PARAM_DURATION, duration);
                paramMap.put(PLVMarqueeAnimation.PARAM_INTERVAL, interval);
                paramMap.put(PLVMarqueeAnimation.PARAM_TWEEN_TIME, tweenTime);
                paramMap.put(PLVMarqueeAnimation.PARAM_ISALWAYS_SHOW_WHEN_RUN, isAlwaysShowWhenRun ? 1 : 0);
                paramMap.put(PLVMarqueeAnimation.PARAM_HIDE_WHEN_PAUSE, isHiddenWhenPause ? 1 : 0);
                break;
            case PLVMarqueeAnimationVO.ROLL_ADVANCE:
                marqueeAnimation = new PLVMarqueeRollAdvanceAnimation();

                viewMap.put(PLVMarqueeAnimation.VIEW_MAIN, mainTextView);
                viewMap.put(PLVMarqueeAnimation.VIEW_SECOND, secondTextView);

                paramMap.put(PLVMarqueeAnimation.PARAM_VIEW_WIDTH, viewWidth);
                paramMap.put(PLVMarqueeAnimation.PARAM_VIEW_HEIGHT, viewHeight);
                paramMap.put(PLVMarqueeAnimation.PARAM_SCREEN_WIDTH, screenWidth);
                paramMap.put(PLVMarqueeAnimation.PARAM_SCREEN_HEIGHT, screenHeight);
                paramMap.put(PLVMarqueeAnimation.PARAM_DURATION, duration);
                paramMap.put(PLVMarqueeAnimation.PARAM_INTERVAL, interval);
                paramMap.put(PLVMarqueeAnimation.PARAM_SECOND_VIEW_WIDTH, secondViewWidth);
                paramMap.put(PLVMarqueeAnimation.PARAM_SECOND_VIEW_HEIGHT, secondViewHeight);
                paramMap.put(PLVMarqueeAnimation.PARAM_ISALWAYS_SHOW_WHEN_RUN, isAlwaysShowWhenRun ? 1 : 0);
                paramMap.put(PLVMarqueeAnimation.PARAM_HIDE_WHEN_PAUSE, isHiddenWhenPause ? 1 : 0);
                break;
            case PLVMarqueeAnimationVO.FLICK_ADVANCE:
                marqueeAnimation = new PLVMarqueeFlickAdvanceAnimation();

                viewMap.put(PLVMarqueeAnimation.VIEW_MAIN, mainTextView);
                viewMap.put(PLVMarqueeAnimation.VIEW_SECOND, secondTextView);

                paramMap.put(PLVMarqueeAnimation.PARAM_VIEW_WIDTH, viewWidth);
                paramMap.put(PLVMarqueeAnimation.PARAM_VIEW_HEIGHT, viewHeight);
                paramMap.put(PLVMarqueeAnimation.PARAM_SCREEN_WIDTH, screenWidth);
                paramMap.put(PLVMarqueeAnimation.PARAM_SCREEN_HEIGHT, screenHeight);
                paramMap.put(PLVMarqueeAnimation.PARAM_LIFE_TIME, lifeTime);
                paramMap.put(PLVMarqueeAnimation.PARAM_INTERVAL, interval);
                paramMap.put(PLVMarqueeAnimation.PARAM_TWEEN_TIME, tweenTime);
                paramMap.put(PLVMarqueeAnimation.PARAM_SECOND_VIEW_WIDTH, secondViewWidth);
                paramMap.put(PLVMarqueeAnimation.PARAM_SECOND_VIEW_HEIGHT, secondViewHeight);
                paramMap.put(PLVMarqueeAnimation.PARAM_ISALWAYS_SHOW_WHEN_RUN, isAlwaysShowWhenRun ? 1 : 0);
                paramMap.put(PLVMarqueeAnimation.PARAM_HIDE_WHEN_PAUSE, isHiddenWhenPause ? 1 : 0);
                break;
            default:
                break;
        }
        if (marqueeAnimation == null) {
            mainTextView.setVisibility(GONE);
            secondTextView.setVisibility(GONE);
            return;
        }
        marqueeAnimation.setViews(viewMap);
        marqueeAnimation.setParams(paramMap);
        if (marqueeAnimation != null) {
            marqueeAnimation.start();
        }
    }

    // </editor-fold>

    // <editor-fold desc="生命周期">
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (marqueeAnimation != null) {
            marqueeAnimation.onParentSizeChanged(PLVMarqueeView.this);
        }
    }
    // </editor-fold>
}
