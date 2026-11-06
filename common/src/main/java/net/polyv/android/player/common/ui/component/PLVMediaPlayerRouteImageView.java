package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.isLandscape;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaInfoViewState;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerFloatAction;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class PLVMediaPlayerRouteImageView extends AppCompatImageView implements View.OnClickListener {

    private int supportRouteCount;
    private PLVMPMediaControllerViewState controllerViewState;

    public PLVMediaPlayerRouteImageView(Context context) { super(context); init(); }
    public PLVMediaPlayerRouteImageView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public PLVMediaPlayerRouteImageView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() { setOnClickListener(this); }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class).getMediaInfoViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaInfoViewState, Unit>() {
                    @Override public Unit invoke(PLVMPMediaInfoViewState state) {
                        supportRouteCount = state.getSupportRoutes().size();
                        updateVisibility();
                        return Unit.INSTANCE;
                    }
                });
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class).getMediaControllerViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaControllerViewState, Unit>() {
                    @Override public Unit invoke(PLVMPMediaControllerViewState state) {
                        controllerViewState = state;
                        updateVisibility();
                        return Unit.INSTANCE;
                    }
                });
    }

    private void updateVisibility() {
        boolean visible = supportRouteCount > 0 && controllerViewState != null
                && controllerViewState.getControllerVisible()
                && !controllerViewState.isMediaStopOverlayVisible()
                && !controllerViewState.getProgressSeekBarDragging()
                && !controllerViewState.getControllerLocking()
                && !(controllerViewState.isFloatActionLayoutVisible() && isLandscape());
        setVisibility(visible ? VISIBLE : GONE);
    }

    @Override public void onClick(View v) {
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .pushFloatActionLayout(PLVMPMediaControllerFloatAction.ROUTE);
    }
}
