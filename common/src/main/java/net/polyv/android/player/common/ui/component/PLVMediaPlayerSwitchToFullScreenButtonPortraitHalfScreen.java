package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState;
import net.polyv.android.player.common.utils.orientation.PLVActivityOrientationManager;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerSwitchToFullScreenButtonPortraitHalfScreen extends AppCompatImageView implements View.OnClickListener {

    protected boolean isVisible = false;

    public PLVMediaPlayerSwitchToFullScreenButtonPortraitHalfScreen(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerSwitchToFullScreenButtonPortraitHalfScreen(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerSwitchToFullScreenButtonPortraitHalfScreen(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
                .get(PLVMPMediaControllerViewModel.class)
                .getMediaControllerViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaControllerViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaControllerViewState viewState) {
                        isVisible = viewState.getControllerVisible()
                                && !viewState.isMediaStopOverlayVisible();
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangeVisibility")
                .compareLastAndSet(isVisible)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onChangeVisibility();
                        return null;
                    }
                });
    }

    protected void onChangeVisibility() {
        setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        PLVActivityOrientationManager.on((AppCompatActivity) getContext()).requestOrientation(false);
    }

}
