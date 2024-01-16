package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.event.PLVEventKt.observeUntilViewDetached;
import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.viewstate.PLVMediaPlayerControlViewState;
import net.polyv.android.player.core.api.listener.event.PLVMediaPlayerOnInfoEvent;
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerState;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerPlayButtonPortraitFullScreen extends AppCompatImageView {

    protected PLVMediaPlayerState currentPlayerState = null;
    protected PLVMediaPlayerControlViewState currentControlViewState = null;
    protected boolean isFirstFrameRendered = false;

    public PLVMediaPlayerPlayButtonPortraitFullScreen(Context context) {
        super(context);
    }

    public PLVMediaPlayerPlayButtonPortraitFullScreen(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerPlayButtonPortraitFullScreen(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        setImageResource(R.drawable.plv_media_player_play_button_icon_to_play_portrait_full_screen);
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

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getEventListenerRegistry()
                        .getOnInfo(),
                this,
                new PLVSugarUtil.Consumer<PLVMediaPlayerOnInfoEvent>() {
                    @Override
                    public void accept(PLVMediaPlayerOnInfoEvent onInfoEvent) {
                        if (onInfoEvent != null && onInfoEvent.getWhat() == PLVMediaPlayerOnInfoEvent.MEDIA_INFO_VIDEO_RENDERING_START) {
                            isFirstFrameRendered = true;
                            onViewStateChanged();
                        }
                    }
                }
        );
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangeVisibility")
                .compareLastAndSet(currentPlayerState, currentControlViewState, isFirstFrameRendered)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeVisibility();
                    }
                });
    }

    protected void onChangeVisibility() {
        final boolean visible = currentPlayerState == PLVMediaPlayerState.STATE_PAUSED
                && isFirstFrameRendered
                && currentControlViewState != null
                && !currentControlViewState.isOverlayLayoutVisible();
        setVisibility(visible ? View.VISIBLE : View.GONE);
    }

}
