package net.polyv.android.player.common.ui.component.floatwindow.layout;

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

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerFloatWindowPlayButton extends AppCompatImageView implements View.OnClickListener {

    public PLVMediaPlayerFloatWindowPlayButton(Context context) {
        super(context);
    }

    public PLVMediaPlayerFloatWindowPlayButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerFloatWindowPlayButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        setImageResource(R.drawable.plv_media_player_float_window_play_button_to_play);
        setOnClickListener(this);
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
                        if (viewState.isPlaying()) {
                            setImageResource(R.drawable.plv_media_player_float_window_play_button_to_pause);
                        } else {
                            setImageResource(R.drawable.plv_media_player_float_window_play_button_to_play);
                        }
                        return null;
                    }
                });
    }

    @Override
    public void onClick(View v) {
        final PLVMPMediaViewModel mediaViewModel = requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class);
        if (mediaViewModel.getMediaPlayViewState().getValue().isPlaying()) {
            mediaViewModel.pause();
        } else {
            mediaViewModel.start();
        }
    }

}
