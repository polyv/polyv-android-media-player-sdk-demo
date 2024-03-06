package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.common.ui.viewmodel.viewstate.PLVMediaPlayerControlViewState;
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerPlayingState;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerPlayButtonLandscape extends AppCompatImageView implements View.OnClickListener {

    protected PLVMediaPlayerPlayingState currentPlayingState = null;
    protected PLVMediaPlayerControlViewState currentControlViewState = null;

    public PLVMediaPlayerPlayButtonLandscape(Context context) {
        super(context);
    }

    public PLVMediaPlayerPlayButtonLandscape(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerPlayButtonLandscape(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        setOnClickListener(this);
        onViewStateChanged();
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
                requireNotNull(PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current())
                        .getControlViewStateLiveData(),
                this,
                new Observer<PLVMediaPlayerControlViewState>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVMediaPlayerControlViewState viewState) {
                        currentControlViewState = viewState;
                        onViewStateChanged();
                    }
                }
        );
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangePlayingState")
                .compareLastAndSet(currentPlayingState)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangePlayingState();
                    }
                });

        PLVRememberState.rememberStateOf(this, "onChangeVisibility")
                .compareLastAndSet(currentControlViewState)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeVisibility();
                    }
                });
    }

    protected void onChangePlayingState() {
        if (currentPlayingState == PLVMediaPlayerPlayingState.PLAYING) {
            setImageResource(R.drawable.plv_media_player_play_button_icon_to_pause_landscape);
        } else {
            setImageResource(R.drawable.plv_media_player_play_button_icon_to_play_landscape);
        }
    }

    protected void onChangeVisibility() {
        if (currentControlViewState == null) {
            return;
        }
        final boolean visible = currentControlViewState.controllerVisible
                && !currentControlViewState.isOverlayLayoutVisible()
                && !currentControlViewState.controllerLocking
                && !(currentControlViewState.isFloatActionPanelVisible() && ScreenUtils.isLandscape());
        setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        final IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current();
        final PLVMediaPlayerControlViewModel controlViewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current();
        if (mediaPlayer == null || controlViewModel == null) {
            return;
        }
        PLVMediaPlayerPlayingState playingState = mediaPlayer.getStateListenerRegistry().getPlayingState().getValue();
        if (playingState == PLVMediaPlayerPlayingState.PLAYING) {
            mediaPlayer.pause();
            controlViewModel.requestControl(PLVMediaPlayerControlAction.hintManualPauseVideo(true));
        } else {
            mediaPlayer.start();
            controlViewModel.requestControl(PLVMediaPlayerControlAction.hintManualPauseVideo(false));
        }
    }

}
