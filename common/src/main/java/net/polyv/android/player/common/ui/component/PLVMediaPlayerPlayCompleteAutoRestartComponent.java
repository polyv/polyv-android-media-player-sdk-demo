package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.event.PLVEventKt.observeUntilViewDetached;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager;
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

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        // do nothing
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        final IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current();
        if (mediaPlayer != null) {
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
        final IPLVMediaPlayer mp = PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current();
        final boolean shouldAutoRestart = !PLVMediaPlayerFloatWindowManager.getInstance().isFloatingWindowShowing();
        if (mp != null && shouldAutoRestart) {
            mp.restart();
        }
    }

}
