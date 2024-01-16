package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.business.scene.common.player.error.PLVMediaPlayerBusinessError;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerPlayingState;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerPlayErrorOverlayLayout extends FrameLayout implements View.OnClickListener {

    private LinearLayout errorRestartLayout;

    protected PLVMediaPlayerPlayingState currentPlayingState = null;
    protected PLVMediaPlayerBusinessError currentBusinessError = null;

    public PLVMediaPlayerPlayErrorOverlayLayout(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerPlayErrorOverlayLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerPlayErrorOverlayLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_play_error_overlay_layout, this);
        errorRestartLayout = findViewById(R.id.plv_media_player_error_restart_layout);

        errorRestartLayout.setOnClickListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getStateListenerRegistry()
                        .getPlayingState(),
                this,
                new Observer<PLVMediaPlayerPlayingState>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVMediaPlayerPlayingState playingState) {
                        currentPlayingState = playingState;
                        onViewStateChanged();
                    }
                }
        );

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getBusinessListenerRegistry()
                        .getBusinessErrorState(),
                this,
                new Observer<PLVMediaPlayerBusinessError>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVMediaPlayerBusinessError businessError) {
                        currentBusinessError = businessError;
                        onViewStateChanged();
                    }
                }
        );
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "updateBusinessError")
                .compareLastAndSet(currentPlayingState, currentBusinessError)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        updateBusinessError();
                    }
                });
    }

    protected void updateBusinessError() {
        boolean isPlaying = currentPlayingState == PLVMediaPlayerPlayingState.PLAYING;
        boolean toShow = currentBusinessError != null && !isPlaying;
        setVisibility(toShow ? View.VISIBLE : View.GONE);
        PLVMediaPlayerControlViewModel controlViewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(PLVMediaPlayerPlayErrorOverlayLayout.this).current();
        if (controlViewModel != null) {
            if (toShow) {
                controlViewModel.requestControl(PLVMediaPlayerControlAction.lockMediaController(false));
            }
            controlViewModel.requestControl(PLVMediaPlayerControlAction.hintErrorOverlayLayoutVisible(toShow));
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == errorRestartLayout.getId()) {
            IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(v).current();
            if (mediaPlayer != null) {
                mediaPlayer.restart();
            }
            currentBusinessError = null;
            onViewStateChanged();
        }
    }

}
