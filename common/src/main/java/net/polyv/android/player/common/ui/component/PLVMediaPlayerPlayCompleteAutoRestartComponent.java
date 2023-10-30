package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.event.PLVEventKt.observeUntilViewDetached;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.core.api.listener.event.PLVMediaPlayerOnCompletedEvent;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerPlayCompleteAutoRestartComponent extends View {

    public PLVMediaPlayerPlayCompleteAutoRestartComponent(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerPlayCompleteAutoRestartComponent(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerPlayCompleteAutoRestartComponent(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        setVisibility(View.GONE);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(View.GONE);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void draw(Canvas canvas) {

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current();
        if (mediaPlayer != null) {
            // 播放器自带的循环播放
            mediaPlayer.setLoopCount(-1);

            // 播放器未自动重播时，通过 onComplete 事件实现自动重播
            observeUntilViewDetached(
                    mediaPlayer.getEventListenerRegistry().getOnCompleted(),
                    this,
                    new PLVSugarUtil.Consumer<PLVMediaPlayerOnCompletedEvent>() {
                        @Override
                        public void accept(PLVMediaPlayerOnCompletedEvent onCompletedEvent) {
                            if (onCompletedEvent != null) {
                                onCompleteAutoRestart(onCompletedEvent);
                            }
                        }
                    }
            );
        }
    }

    protected void onCompleteAutoRestart(PLVMediaPlayerOnCompletedEvent onCompletedEvent) {
        IPLVMediaPlayer mp = PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current();
        if (mp != null) {
            mp.restart();
        }
    }

}
