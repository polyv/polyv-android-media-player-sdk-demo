package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaPlayViewState;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.LockMediaControllerAction;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerPlayCompleteManualRestartOverlayLayout extends FrameLayout implements View.OnClickListener {

    private LinearLayout completeRestartLayout;

    protected PLVMediaPlayerState currentPlayerState = null;

    public PLVMediaPlayerPlayCompleteManualRestartOverlayLayout(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerPlayCompleteManualRestartOverlayLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerPlayCompleteManualRestartOverlayLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_play_complete_overlay_layout, this);
        completeRestartLayout = findViewById(R.id.plv_media_player_complete_restart_layout);

        completeRestartLayout.setOnClickListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getMediaPlayViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaPlayViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaPlayViewState viewState) {
                        currentPlayerState = viewState.getPlayerState();
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onPlayerStateChanged")
                .compareLastAndSet(currentPlayerState)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onPlayerStateChanged();
                        return null;
                    }
                });
    }

    protected void onPlayerStateChanged() {
        boolean isCompleted = currentPlayerState == PLVMediaPlayerState.STATE_COMPLETED;
        setVisibility(isCompleted ? View.VISIBLE : View.GONE);
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .lockMediaController(LockMediaControllerAction.UNLOCK);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == completeRestartLayout.getId()) {
            requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                    .get(PLVMPMediaViewModel.class)
                    .restart();
        }
    }

}
