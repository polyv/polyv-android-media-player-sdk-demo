package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.isLandscape;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate;
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaInfoViewState;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerFloatAction;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerBitRateTextView extends AppCompatTextView implements View.OnClickListener {

    protected PLVMediaBitRate currentMediaBitRate = null;
    protected PLVMediaOutputMode currentMediaOutputMode = null;
    protected PLVMPMediaControllerViewState controlViewState = null;

    public PLVMediaPlayerBitRateTextView(Context context) {
        super(context);
    }

    public PLVMediaPlayerBitRateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerBitRateTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        setOnClickListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getMediaInfoViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaInfoViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaInfoViewState viewState) {
                        currentMediaBitRate = viewState.getBitRate();
                        currentMediaOutputMode = viewState.getOutputMode();
                        onViewStateChanged();
                        return null;
                    }
                });

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .getMediaControllerViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaControllerViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaControllerViewState viewState) {
                        controlViewState = viewState;
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangeVisibility")
                .compareLastAndSet(controlViewState, currentMediaBitRate, currentMediaOutputMode)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onChangeVisibility();
                        return null;
                    }
                });

        PLVRememberState.rememberStateOf(this, "onChangeBitRateText")
                .compareLastAndSet(currentMediaBitRate)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onChangeBitRateText();
                        return null;
                    }
                });
    }

    protected void onChangeVisibility() {
        boolean isVisible = controlViewState != null
                && controlViewState.getControllerVisible()
                && !controlViewState.isMediaStopOverlayVisible()
                && !controlViewState.getProgressSeekBarDragging()
                && !controlViewState.getControllerLocking()
                && !(controlViewState.isFloatActionLayoutVisible() && isLandscape())
                && currentMediaBitRate != null
                && currentMediaOutputMode != PLVMediaOutputMode.AUDIO_ONLY;
        setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    protected void onChangeBitRateText() {
        if (currentMediaBitRate != null) {
            setText(currentMediaBitRate.getName());
        }
    }

    @Override
    public void onClick(View v) {
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .pushFloatActionLayout(PLVMPMediaControllerFloatAction.BITRATE);
    }
}
