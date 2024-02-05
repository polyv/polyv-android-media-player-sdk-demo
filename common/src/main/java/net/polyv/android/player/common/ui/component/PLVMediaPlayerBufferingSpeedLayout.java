package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerState;

import java.util.Locale;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerBufferingSpeedLayout extends FrameLayout {

    private TextView bufferingSpeedTv;

    @Nullable
    private Disposable networkTrafficDisposable = null;

    protected boolean currentIsPreparing = false;
    protected boolean currentIsBuffering = false;

    private long lastTrafficByteCount = -1;
    private long lastTrafficTimestamp = -1;
    private long trafficSpeedByteCount = -1;
    private long trafficSpeedDuration = -1;

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

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getStateListenerRegistry()
                        .isBuffering(),
                this,
                new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable Boolean isBuffering) {
                        currentIsBuffering = Boolean.TRUE.equals(isBuffering);
                        onViewStateChanged();
                    }
                }
        );

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getStateListenerRegistry()
                        .getPlayerState(),
                this,
                new Observer<PLVMediaPlayerState>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVMediaPlayerState playerState) {
                        currentIsPreparing = playerState == PLVMediaPlayerState.STATE_PREPARING || playerState == PLVMediaPlayerState.STATE_PREPARED;
                        onViewStateChanged();
                    }
                }
        );
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        currentIsPreparing = false;
        currentIsBuffering = false;
        onViewStateChanged();
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onLoadingStateChanged")
                .compareLastAndSet(currentIsBuffering, currentIsPreparing)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult plvRememberStateCompareResult) {
                        onLoadingStateChanged();
                    }
                });

        PLVRememberState.rememberStateOf(this, "onTrafficSpeedChanged")
                .compareLastAndSet(currentIsBuffering, currentIsPreparing, trafficSpeedByteCount, trafficSpeedDuration)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult plvRememberStateCompareResult) {
                        onTrafficSpeedChanged();
                    }
                });
    }

    protected void onLoadingStateChanged() {
        final boolean isLoading = currentIsBuffering || currentIsPreparing;
        if (isLoading) {
            startObserveNetworkTraffic();
        } else {
            stopObserveNetworkTraffic();
        }
    }

    private void startObserveNetworkTraffic() {
        if (networkTrafficDisposable != null) {
            return;
        }
        networkTrafficDisposable = PLVRxTimer.timer(500, new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                final IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(PLVMediaPlayerBufferingSpeedLayout.this).current();
                if (mediaPlayer == null) {
                    return;
                }
                final long trafficByteCount = mediaPlayer.getTrafficStatisticByteCount();
                if (lastTrafficByteCount > 0) {
                    trafficSpeedByteCount = trafficByteCount - lastTrafficByteCount;
                    trafficSpeedDuration = System.currentTimeMillis() - lastTrafficTimestamp;
                }
                lastTrafficByteCount = trafficByteCount;
                lastTrafficTimestamp = System.currentTimeMillis();
                onViewStateChanged();
            }
        });
    }

    private void stopObserveNetworkTraffic() {
        if (networkTrafficDisposable != null) {
            networkTrafficDisposable.dispose();
            networkTrafficDisposable = null;
        }
        lastTrafficByteCount = -1;
        lastTrafficTimestamp = -1;
        trafficSpeedByteCount = -1;
        trafficSpeedDuration = -1;
        onViewStateChanged();
    }

    protected void onTrafficSpeedChanged() {
        final boolean isLoading = currentIsBuffering || currentIsPreparing;
        final boolean showBufferingSpeed = isLoading && trafficSpeedByteCount >= 0 && trafficSpeedDuration >= 0;
        if (!showBufferingSpeed) {
            setVisibility(GONE);
            return;
        }
        setVisibility(VISIBLE);
        bufferingSpeedTv.setText(getSpeedText());
    }

    private String getSpeedText() {
        final double bytesPerSecond = (double) trafficSpeedByteCount / trafficSpeedDuration * 1000;
        if (bytesPerSecond < 1 << 10) {
            return String.format(Locale.getDefault(), "%.0fB/S", bytesPerSecond);
        } else if (bytesPerSecond < 1 << 20) {
            return String.format(Locale.getDefault(), "%.1fKB/S", bytesPerSecond / (1 << 10));
        } else {
            return String.format(Locale.getDefault(), "%.1fMB/S", bytesPerSecond / (1 << 20));
        }
    }


}
