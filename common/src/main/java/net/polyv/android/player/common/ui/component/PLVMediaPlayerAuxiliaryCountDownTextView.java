package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;
import static net.polyv.android.player.sdk.foundation.lang.StringsKt.format;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaPlayStage;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.auxiliary.viewmodel.PLVMPAuxiliaryViewModel;
import net.polyv.android.player.common.modules.auxiliary.viewmodel.viewstate.PLVMPAuxiliaryInfoViewState;
import net.polyv.android.player.common.modules.auxiliary.viewmodel.viewstate.PLVMPAuxiliaryPlayViewState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

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

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPAuxiliaryViewModel.class)
                .getAuxiliaryInfoViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPAuxiliaryInfoViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPAuxiliaryInfoViewState viewState) {
                        if (viewState != null) {
                            isAdvertShowing = viewState.getStage().isAuxiliaryStage();
                            currentPlayStage = viewState.getStage();
                        } else {
                            isAdvertShowing = false;
                        }
                        onViewStateChanged();
                        return null;
                    }
                });

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPAuxiliaryViewModel.class)
                .getAuxiliaryPlayViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPAuxiliaryPlayViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPAuxiliaryPlayViewState viewState) {
                        countDownTimeLeft = viewState.getTimeLeftInSeconds();
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangeVisibility")
                .compareLastAndSet(isAdvertShowing, currentPlayStage)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onChangeVisibility();
                        return null;
                    }
                });

        PLVRememberState.rememberStateOf(this, "onChangeTimeLeft")
                .compareLastAndSet(countDownTimeLeft)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onChangeTimeLeft();
                        return null;
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
