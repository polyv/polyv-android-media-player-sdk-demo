package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerState;

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

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getStateListenerRegistry()
                        .getPlayerState(),
                this,
                new Observer<PLVMediaPlayerState>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVMediaPlayerState playerState) {
                        currentPlayerState = playerState;
                        onViewStateChanged();
                    }
                }
        );
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onPlayerStateChanged")
                .compareLastAndSet(currentPlayerState)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onPlayerStateChanged();
                    }
                });
    }

    protected void onPlayerStateChanged() {
        boolean isCompleted = currentPlayerState == PLVMediaPlayerState.STATE_COMPLETED;
        setVisibility(isCompleted ? View.VISIBLE : View.GONE);
        PLVMediaPlayerControlViewModel controlViewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(PLVMediaPlayerPlayCompleteManualRestartOverlayLayout.this).current();
        if (controlViewModel != null) {
            if (isCompleted) {
                controlViewModel.requestControl(PLVMediaPlayerControlAction.lockMediaController(false));
            }
            controlViewModel.requestControl(PLVMediaPlayerControlAction.hintCompleteOverlayLayoutVisible(isCompleted));
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == completeRestartLayout.getId()) {
            IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(v).current();
            PLVMediaPlayerControlViewModel controlViewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(v).current();
            if (mediaPlayer != null) {
                mediaPlayer.restart();
            }
            setVisibility(View.GONE);
            if (controlViewModel != null) {
                controlViewModel.requestControl(PLVMediaPlayerControlAction.hintCompleteOverlayLayoutVisible(false));
            }
        }
    }

}
