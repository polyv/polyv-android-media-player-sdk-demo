package net.polyv.android.player.common.utils.audiofocus;

import android.content.Context;
import android.media.AudioManager;
import androidx.annotation.NonNull;

import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaPlayViewState;
import net.polyv.android.player.sdk.foundation.lang.MutableObserver;

import javax.annotation.Nullable;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerAudioFocusManager implements AudioManager.OnAudioFocusChangeListener {

    private static final int MEDIA_PLAYER_AUDIO_FOCUS_LEVEL = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;

    private final AudioManager audioManager;

    @Nullable
    private PLVMPMediaViewModel mediaViewModel;
    @Nullable
    private MutableObserver<PLVMPMediaPlayViewState> playingStateObserver;

    private AudioFocusState lastAudioFocusState = AudioFocusState.NO_AUDIO_FOCUS;
    private AudioFocusState audioFocusState = AudioFocusState.NO_AUDIO_FOCUS;

    public PLVMediaPlayerAudioFocusManager(@NonNull Context context) {
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void startFocus(PLVMPMediaViewModel mediaViewModel) {
        stopFocus();
        this.mediaViewModel = mediaViewModel;
        startObserveMediaState();
    }

    public void stopFocus() {
        stopObserveMediaState();
        this.mediaViewModel = null;
        this.lastAudioFocusState = AudioFocusState.NO_AUDIO_FOCUS;
        this.audioFocusState = AudioFocusState.NO_AUDIO_FOCUS;
    }

    private void startObserveMediaState() {
        if (mediaViewModel == null) {
            return;
        }
        playingStateObserver = mediaViewModel.getMediaPlayViewState()
                .observe(new Function1<PLVMPMediaPlayViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaPlayViewState playViewState) {
                        if (playViewState.isPlaying()) {
                            audioFocusState = audioFocusState.onStartPlaying();
                        } else {
                            audioFocusState = audioFocusState.onPausePlaying();
                        }
                        updateAudioFocusRequest();
                        return null;
                    }
                });
    }

    private void stopObserveMediaState() {
        if (playingStateObserver != null) {
            playingStateObserver.dispose();
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
                if (mediaViewModel != null) {
                    mediaViewModel.start();
                }
                audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, MEDIA_PLAYER_AUDIO_FOCUS_LEVEL);
                break;
            case LOSS_AUDIO_FOCUS:
                if (mediaViewModel != null) {
                    mediaViewModel.pause();
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
