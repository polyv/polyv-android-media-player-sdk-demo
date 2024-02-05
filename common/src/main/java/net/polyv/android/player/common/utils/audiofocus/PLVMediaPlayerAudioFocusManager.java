package net.polyv.android.player.common.utils.audiofocus;

import androidx.lifecycle.Observer;
import android.content.Context;
import android.media.AudioManager;
import androidx.annotation.NonNull;

import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerPlayingState;

import javax.annotation.Nullable;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerAudioFocusManager implements AudioManager.OnAudioFocusChangeListener {

    private static final int MEDIA_PLAYER_AUDIO_FOCUS_LEVEL = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;

    private final AudioManager audioManager;

    @Nullable
    private IPLVMediaPlayer mediaPlayer;
    @Nullable
    private Observer<PLVMediaPlayerPlayingState> playingStateObserver;

    private AudioFocusState lastAudioFocusState = AudioFocusState.NO_AUDIO_FOCUS;
    private AudioFocusState audioFocusState = AudioFocusState.NO_AUDIO_FOCUS;

    public PLVMediaPlayerAudioFocusManager(@NonNull Context context) {
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void startFocus(IPLVMediaPlayer mediaPlayer) {
        stopFocus();
        this.mediaPlayer = mediaPlayer;
        startObserveMediaState();
    }

    public void stopFocus() {
        stopObserveMediaState();
        this.mediaPlayer = null;
        this.lastAudioFocusState = AudioFocusState.NO_AUDIO_FOCUS;
        this.audioFocusState = AudioFocusState.NO_AUDIO_FOCUS;
    }

    private void startObserveMediaState() {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.getStateListenerRegistry().getPlayingState()
                .observeForever(
                        playingStateObserver = new Observer<PLVMediaPlayerPlayingState>() {
                            @Override
                            public void onChanged(@androidx.annotation.Nullable @org.jetbrains.annotations.Nullable PLVMediaPlayerPlayingState mediaPlayerPlayingState) {
                                boolean isPlaying = mediaPlayerPlayingState == PLVMediaPlayerPlayingState.PLAYING;
                                if (isPlaying) {
                                    audioFocusState = audioFocusState.onStartPlaying();
                                } else {
                                    audioFocusState = audioFocusState.onPausePlaying();
                                }
                                updateAudioFocusRequest();
                            }
                        });
    }

    private void stopObserveMediaState() {
        if (mediaPlayer == null) {
            return;
        }
        if (playingStateObserver != null) {
            mediaPlayer.getStateListenerRegistry().getPlayingState().removeObserver(playingStateObserver);
            playingStateObserver = null;
        }
    }

    @Override
    public final void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                audioFocusState = audioFocusState.onRecoverAudioFocus();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                audioFocusState = audioFocusState.onLossAudioFocus();
                break;
            default:
        }
        updateAudioFocusRequest();
    }

    private void updateAudioFocusRequest() {
        if (lastAudioFocusState == audioFocusState) {
            return;
        }
        switch (audioFocusState) {
            case NO_AUDIO_FOCUS:
                audioManager.abandonAudioFocus(this);
                break;
            case PLAYING:
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
                audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, MEDIA_PLAYER_AUDIO_FOCUS_LEVEL);
                break;
            case LOSS_AUDIO_FOCUS:
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                }
                break;
            default:
                break;
        }
        lastAudioFocusState = audioFocusState;
    }

    private enum AudioFocusState {
        NO_AUDIO_FOCUS {
            @Override
            AudioFocusState onStartPlaying() {
                return PLAYING;
            }
        },

        PLAYING {
            @Override
            AudioFocusState onPausePlaying() {
                return NO_AUDIO_FOCUS;
            }

            @Override
            AudioFocusState onLossAudioFocus() {
                return LOSS_AUDIO_FOCUS;
            }
        },

        LOSS_AUDIO_FOCUS {
            @Override
            AudioFocusState onStartPlaying() {
                return PLAYING;
            }

            @Override
            AudioFocusState onRecoverAudioFocus() {
                return PLAYING;
            }
        },
        ;

        AudioFocusState onStartPlaying() {
            return this;
        }

        AudioFocusState onPausePlaying() {
            return this;
        }

        AudioFocusState onLossAudioFocus() {
            return this;
        }

        AudioFocusState onRecoverAudioFocus() {
            return this;
        }
    }

}
