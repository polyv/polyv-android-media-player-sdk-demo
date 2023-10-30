package net.polyv.android.player.common.ui.component.floatwindow;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeForeverUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerPlayingState;

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

        observeForeverUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getStateListenerRegistry()
                        .getPlayingState(),
                this,
                new Observer<PLVMediaPlayerPlayingState>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVMediaPlayerPlayingState playingState) {
                        if (playingState == null) {
                            return;
                        }
                        if (playingState == PLVMediaPlayerPlayingState.PLAYING) {
                            setImageResource(R.drawable.plv_media_player_float_window_play_button_to_pause);
                        } else {
                            setImageResource(R.drawable.plv_media_player_float_window_play_button_to_play);
                        }
                    }
                }
        );
    }

    @Override
    public void onClick(View v) {
        final IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current();
        if (mediaPlayer == null) {
            return;
        }
        PLVMediaPlayerPlayingState playingState = mediaPlayer.getStateListenerRegistry().getPlayingState().getValue();
        if (playingState == PLVMediaPlayerPlayingState.PLAYING) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

}
