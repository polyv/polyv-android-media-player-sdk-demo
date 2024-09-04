package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaPlayViewState;
import net.polyv.android.player.core.api.listener.event.PLVMediaPlayerOnInfoEvent;
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerPlayButtonPortraitFullScreen extends AppCompatImageView {

    protected boolean isPaused = false;
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

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getMediaPlayViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaPlayViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaPlayViewState viewState) {
                        isPaused = viewState.getPlayerState() == PLVMediaPlayerState.STATE_PAUSED;
                        onViewStateChanged();
                        return null;
                    }
                });

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getOnInfoEvent()
                .observeUntilViewDetached(this, new Function1<PLVMediaPlayerOnInfoEvent, Unit>() {
                    @Override
                    public Unit invoke(PLVMediaPlayerOnInfoEvent onInfoEvent) {
                        if (onInfoEvent.getWhat() == PLVMediaPlayerOnInfoEvent.MEDIA_INFO_VIDEO_RENDERING_START) {
                            isFirstFrameRendered = true;
                            onViewStateChanged();
                        }
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangeVisibility")
                .compareLastAndSet(isPaused, isFirstFrameRendered)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onChangeVisibility();
                        return null;
                    }
                });
    }

    protected void onChangeVisibility() {
        final boolean visible = isPaused && isFirstFrameRendered;
        setVisibility(visible ? View.VISIBLE : View.GONE);
    }

}
