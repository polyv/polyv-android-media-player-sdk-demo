package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.event.PLVEventKt.observeUntilViewDetached;
import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.format;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.business.scene.auxiliary.listener.event.PLVAuxiliaryAdvertTimeLeftCountDownEvent;
import net.polyv.android.player.business.scene.auxiliary.listener.event.PLVAuxiliaryOnShowAdvertEvent;
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaPlayStage;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerAuxiliaryCountDownTextView extends AppCompatTextView {

    protected boolean isAdvertShowing = false;
    protected PLVMediaPlayStage currentPlayStage = null;
    protected int countDownTimeLeft = Integer.MAX_VALUE;

    public PLVMediaPlayerAuxiliaryCountDownTextView(Context context) {
        super(context);
    }

    public PLVMediaPlayerAuxiliaryCountDownTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerAuxiliaryCountDownTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localAuxiliaryMediaPlayer.on(this).current())
                        .getAuxiliaryListenerRegistry()
                        .getAdvertShowingState(),
                this,
                new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable Boolean showing) {
                        if (showing == null) {
                            return;
                        }
                        isAdvertShowing = showing;
                        onViewStateChanged();
                    }
                }
        );

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getBusinessListenerRegistry()
                        .getCurrentPlayStage(),
                this,
                new Observer<PLVMediaPlayStage>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVMediaPlayStage playStage) {
                        currentPlayStage = playStage;
                        onViewStateChanged();
                    }
                }
        );

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localAuxiliaryMediaPlayer.on(this).current())
                        .getAuxiliaryListenerRegistry()
                        .getOnShowAdvertEvent(),
                this,
                new PLVSugarUtil.Consumer<PLVAuxiliaryOnShowAdvertEvent>() {
                    @Override
                    public void accept(PLVAuxiliaryOnShowAdvertEvent onShowAdvertEvent) {
                        countDownTimeLeft = (int) onShowAdvertEvent.getDataSource().getDuration().toSeconds();
                        onViewStateChanged();
                    }
                }
        );

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localAuxiliaryMediaPlayer.on(this).current())
                        .getAuxiliaryListenerRegistry()
                        .getOnTimeLeftCountDownEvent(),
                this,
                new PLVSugarUtil.Consumer<PLVAuxiliaryAdvertTimeLeftCountDownEvent>() {
                    @Override
                    public void accept(PLVAuxiliaryAdvertTimeLeftCountDownEvent countDownEvent) {
                        if (countDownEvent == null) {
                            return;
                        }
                        countDownTimeLeft = countDownEvent.getTimeLeftInSeconds();
                        onViewStateChanged();
                    }
                }
        );
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangeVisibility")
                .compareLastAndSet(isAdvertShowing, currentPlayStage)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult plvRememberStateCompareResult) {
                        onChangeVisibility();
                    }
                });

        PLVRememberState.rememberStateOf(this, "onChangeTimeLeft")
                .compareLastAndSet(countDownTimeLeft)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult plvRememberStateCompareResult) {
                        onChangeTimeLeft();
                    }
                });
    }

    protected void onChangeVisibility() {
        final boolean isAdvertStage = currentPlayStage == PLVMediaPlayStage.HEAD_ADVERT || currentPlayStage == PLVMediaPlayStage.TAIL_ADVERT;
        final boolean showCountDown = isAdvertShowing && isAdvertStage;
        setVisibility(showCountDown ? VISIBLE : GONE);
    }

    protected void onChangeTimeLeft() {
        setText(format(getContext().getString(R.string.plv_media_player_ui_component_auxiliary_time_left_text), countDownTimeLeft));
    }

}
