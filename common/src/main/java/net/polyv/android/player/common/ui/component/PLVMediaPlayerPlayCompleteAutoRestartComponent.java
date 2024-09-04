package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import android.graphics.Canvas;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.core.api.listener.event.PLVMediaPlayerOnCompletedEvent;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

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

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getOnCompleteEvent()
                .observeUntilViewDetached(this, new Function1<PLVMediaPlayerOnCompletedEvent, Unit>() {
                    @Override
                    public Unit invoke(PLVMediaPlayerOnCompletedEvent onCompletedEvent) {
                        onCompleteAutoRestart(onCompletedEvent);
                        return null;
                    }
                });
    }

    protected void onCompleteAutoRestart(PLVMediaPlayerOnCompletedEvent onCompletedEvent) {
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .restart();
    }

}
