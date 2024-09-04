package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaPlayViewState;
import net.polyv.android.player.common.ui.component.marquee.PLVMarqueeView;
import net.polyv.android.player.common.ui.component.marquee.model.PLVMarqueeAnimationVO;
import net.polyv.android.player.common.ui.component.marquee.model.PLVMarqueeModel;
import net.polyv.android.player.common.utils.ui.PLVViewLifecycleObservable;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerMarqueeLayout extends FrameLayout {

    private final PLVMarqueeView marqueeView = new PLVMarqueeView(getContext());

    private boolean isPlaying = false;

    public PLVMediaPlayerMarqueeLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public PLVMediaPlayerMarqueeLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVMediaPlayerMarqueeLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        addView(marqueeView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setupMarquee();
    }

    private void setupMarquee() {
        marqueeView.setPLVMarqueeModel(
                new PLVMarqueeModel()
                        .setUserName("播放器跑马灯 MediaPlayerMarquee")
                        .setFontAlpha(255)
                        .setFontSize(40)
                        .setFontColor(Color.RED)
                        .setFilter(false)
                        .setFilterAlpha(255)
                        .setFilterColor(Color.BLACK)
                        .setFilterBlurX(2)
                        .setFilterBlurY(2)
                        .setFilterStrength(4)
                        .setSetting(PLVMarqueeAnimationVO.ROLL)
                        .setInterval(3)
                        .setTweenTime(1)
                        .setLifeTime(2)
                        .setSpeed(200)
                        .setAlwaysShowWhenRun(true)
                        .setHiddenWhenPause(false)
        );
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        requireNotNull(PLVMediaPlayerLocalProvider.localLifecycleObservable.on(this).current())
                .addObserver(viewLifecycleObserver);

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getMediaPlayViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaPlayViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaPlayViewState playViewState) {
                        isPlaying = playViewState.isPlaying();
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onPlayingStateChanged")
                .compareLastAndSet(isPlaying)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onPlayingStateChanged();
                        return null;
                    }
                });
    }

    protected void onPlayingStateChanged() {
        if (isPlaying) {
            marqueeView.start();
        } else {
            marqueeView.pause();
        }
    }

    private final PLVViewLifecycleObservable.IViewLifecycleObserver viewLifecycleObserver = new PLVViewLifecycleObservable.AbsViewLifecycleObserver() {

        @Override
        public void onDestroy(PLVViewLifecycleObservable observable) {
            observable.removeObserver(this);
            marqueeView.stop();
        }

    };

}
