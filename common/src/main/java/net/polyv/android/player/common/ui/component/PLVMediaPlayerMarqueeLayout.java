package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import androidx.lifecycle.Observer;
import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.common.ui.component.marquee.PLVMarqueeView;
import net.polyv.android.player.common.ui.component.marquee.model.PLVMarqueeAnimationVO;
import net.polyv.android.player.common.ui.component.marquee.model.PLVMarqueeModel;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.utils.ui.PLVViewLifecycleObservable;
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerPlayingState;

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

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getStateListenerRegistry()
                        .getPlayingState(),
                this,
                new Observer<PLVMediaPlayerPlayingState>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVMediaPlayerPlayingState mediaPlayerPlayingState) {
                        if (mediaPlayerPlayingState == null) {
                            return;
                        }
                        isPlaying = mediaPlayerPlayingState == PLVMediaPlayerPlayingState.PLAYING;
                        onViewStateChanged();
                    }
                }
        );
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onPlayingStateChanged")
                .compareLastAndSet(isPlaying)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult plvRememberStateCompareResult) {
                        onPlayingStateChanged();
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
