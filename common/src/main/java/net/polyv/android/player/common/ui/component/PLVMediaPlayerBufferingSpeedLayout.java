package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaPlayViewState;
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import java.util.Locale;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerBufferingSpeedLayout extends FrameLayout {

    private TextView bufferingSpeedTv;

    protected boolean currentIsPreparing = false;
    protected boolean currentIsBuffering = false;
    protected long trafficBytesPerSecond = 0;

    public PLVMediaPlayerBufferingSpeedLayout(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerBufferingSpeedLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerBufferingSpeedLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_buffering_speed_layout, this);

        bufferingSpeedTv = findViewById(R.id.plv_media_player_buffering_speed_tv);
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
                        currentIsBuffering = viewState.isBuffering();
                        currentIsPreparing = viewState.getPlayerState() == PLVMediaPlayerState.STATE_PREPARING
                                || viewState.getPlayerState() == PLVMediaPlayerState.STATE_PREPARED;
                        trafficBytesPerSecond = viewState.getBufferingSpeed();
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        currentIsPreparing = false;
        currentIsBuffering = false;
        onViewStateChanged();
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onTrafficSpeedChanged")
                .compareLastAndSet(currentIsBuffering, currentIsPreparing, trafficBytesPerSecond)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onTrafficSpeedChanged();
                        return null;
                    }
                });
    }

    protected void onTrafficSpeedChanged() {
        final boolean isLoading = currentIsBuffering || currentIsPreparing;
        final boolean showBufferingSpeed = isLoading && trafficBytesPerSecond >= 0;
        if (!showBufferingSpeed) {
            setVisibility(GONE);
            return;
        }
        setVisibility(VISIBLE);
        bufferingSpeedTv.setText(getSpeedText());
    }

    private String getSpeedText() {
        final double bytesPerSecond = (double) trafficBytesPerSecond;
        if (bytesPerSecond < 1 << 10) {
            return String.format(Locale.getDefault(), "%.0fB/S", bytesPerSecond);
        } else if (bytesPerSecond < 1 << 20) {
            return String.format(Locale.getDefault(), "%.1fKB/S", bytesPerSecond / (1 << 10));
        } else {
            return String.format(Locale.getDefault(), "%.1fMB/S", bytesPerSecond / (1 << 20));
        }
    }


}
