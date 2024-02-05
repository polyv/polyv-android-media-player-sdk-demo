package net.polyv.android.player.demo.scene.feed.item;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.mutableLiveData;
import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;
import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager;
import net.polyv.android.player.common.utils.audiofocus.PLVMediaPlayerAudioFocusManager;
import net.polyv.android.player.common.utils.orientation.PLVActivityOrientationManager;
import net.polyv.android.player.core.api.listener.event.PLVMediaPlayerOnPreparedEvent;
import net.polyv.android.player.core.api.option.PLVMediaPlayerOptionEnum;
import net.polyv.android.player.sdk.PLVVideoView;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerFeedVideoStateHandler {

    private static final MutableLiveData<PLVMediaResource> priorPrepareMediaResource = mutableLiveData(null);

    @Nullable
    private Observer<PLVMediaResource> priorPrepareMediaResourceObserver;

    @Nullable
    private IPLVMediaPlayer mediaPlayer;
    // 音频焦点管理
    @Nullable
    private PLVMediaPlayerAudioFocusManager audioFocusManager;

    @Nullable
    private AppCompatActivity activity;

    @Nullable
    private PLVMediaResource mediaResource;

    private VideoState currentVideoState = new InitializedState();
    private boolean onActivityCreated = false;
    private boolean isVisibleToUser = false;
    private boolean isCalledPrepare = false;
    private boolean isPrepared = false;

    public void onCreateView(PLVVideoView videoView) {
        this.mediaPlayer = videoView;
        if (videoView != null) {
            activity = (AppCompatActivity) videoView.getContext();
            audioFocusManager = new PLVMediaPlayerAudioFocusManager(activity);
        }
    }

    public void onActivityCreated() {
        onActivityCreated = true;
        initMediaPlayer();
        updateVideoState();
        observeMediaPlayer();
        observePriorMediaResource();
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        this.isVisibleToUser = isVisibleToUser;
        updateVideoState();
    }

    public void onDestroyView() {
        stopPriorMediaResourceObserver();
        onActivityCreated = false;
        isVisibleToUser = false;
        PLVMediaPlayerFloatWindowManager.getInstance()
                .runOnFloatingWindowClosed(new Runnable() {
                    @Override
                    public void run() {
                        isCalledPrepare = false;
                        isPrepared = false;
                        if (audioFocusManager != null) {
                            audioFocusManager.stopFocus();
                        }
                        if (mediaPlayer != null) {
                            mediaPlayer.destroy();
                        }
                        mediaPlayer = null;
                        changeToVideoState(new InitializedState());
                    }
                });
    }

    public void setMediaResource(@Nullable PLVMediaResource mediaResource) {
        if (this.mediaResource == mediaResource) {
            return;
        }
        this.mediaResource = mediaResource;
        changeToVideoState(new InitializedState());
        updateVideoState();
    }

    private void initMediaPlayer() {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.setPlayerOption(listOf(
                PLVMediaPlayerOptionEnum.ENABLE_ACCURATE_SEEK.value("1"),
                PLVMediaPlayerOptionEnum.START_ON_PREPARED.value("0"),
                PLVMediaPlayerOptionEnum.RENDER_ON_PREPARED.value("1")
        ));
        mediaPlayer.setAutoContinue(true);
    }

    private void observeMediaPlayer() {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.getEventListenerRegistry().getOnPrepared()
                .observe(new PLVSugarUtil.Consumer<PLVMediaPlayerOnPreparedEvent>() {
                    @Override
                    public void accept(PLVMediaPlayerOnPreparedEvent onPreparedEvent) {
                        isPrepared = true;
                        updateVideoState();
                    }
                });
    }

    private void observePriorMediaResource() {
        stopPriorMediaResourceObserver();
        priorPrepareMediaResource.observeForever(
                priorPrepareMediaResourceObserver = new Observer<PLVMediaResource>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVMediaResource mediaResource) {
                        updateVideoState();
                    }
                }
        );
    }

    private void stopPriorMediaResourceObserver() {
        if (priorPrepareMediaResourceObserver != null) {
            priorPrepareMediaResource.removeObserver(priorPrepareMediaResourceObserver);
            priorPrepareMediaResourceObserver = null;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="预加载状态管理">

    private void updateVideoState() {
        changeToVideoState(currentVideoState.nextState());
    }

    private void changeToVideoState(VideoState nextState) {
        if (nextState == currentVideoState) {
            return;
        }
        currentVideoState.onLeave();
        currentVideoState = nextState;
        currentVideoState.onEnter();
    }

    private abstract class VideoState {
        public void onEnter() {
        }

        public VideoState nextState() {
            return this;
        }

        public void onLeave() {
        }
    }

    private class InitializedState extends VideoState {
        @Override
        public void onEnter() {
            isCalledPrepare = false;
            isPrepared = false;
        }

        @Override
        public VideoState nextState() {
            if (!onActivityCreated) {
                return this;
            }
            if (isVisibleToUser) {
                if (mediaResource != null) {
                    return new VisibleCallPrepareState();
                } else {
                    return new VisibleNotPrepareState();
                }
            } else {
                if (priorPrepareMediaResource.getValue() == null && mediaResource != null) {
                    return new InvisibleCallPrepareState();
                } else {
                    return new InvisibleNotPrepareState();
                }
            }
        }
    }

    private class InvisibleNotPrepareState extends VideoState {
        @Override
        public VideoState nextState() {
            if (isVisibleToUser) {
                if (mediaResource != null) {
                    return new VisibleCallPrepareState();
                } else {
                    return new VisibleNotPrepareState();
                }
            }
            if (priorPrepareMediaResource.getValue() == null && mediaResource != null) {
                return new InvisibleCallPrepareState();
            }
            return this;
        }
    }

    private class VisibleNotPrepareState extends VideoState {
        @Override
        public VideoState nextState() {
            if (!isVisibleToUser) {
                return new InvisibleNotPrepareState();
            }
            if (mediaResource != null) {
                return new VisibleCallPrepareState();
            }
            return this;
        }
    }

    private class InvisibleCallPrepareState extends VideoState {
        @Override
        public void onEnter() {
            if (isCalledPrepare) {
                return;
            }
            isCalledPrepare = true;
            if (mediaPlayer != null && mediaResource != null) {
                mediaPlayer.setMediaResource(mediaResource);
            }
        }

        @Override
        public VideoState nextState() {
            if (isVisibleToUser) {
                if (isPrepared) {
                    return new VisiblePreparedState();
                } else {
                    return new VisibleCallPrepareState();
                }
            } else {
                if (isPrepared) {
                    return new InvisiblePreparedState();
                }
            }
            return this;
        }
    }

    private class VisibleCallPrepareState extends VideoState {
        @Override
        public void onEnter() {
            if (isCalledPrepare) {
                return;
            }
            isCalledPrepare = true;
            if (mediaPlayer != null && mediaResource != null) {
                mediaPlayer.setMediaResource(mediaResource);
            }
            priorPrepareMediaResource.setValue(mediaResource);
        }

        @Override
        public VideoState nextState() {
            if (isVisibleToUser) {
                if (isPrepared) {
                    return new VisiblePreparedState();
                }
            } else {
                if (!isPrepared) {
                    return new InvisibleCallPrepareState();
                } else {
                    return new InvisiblePreparedState();
                }
            }
            return this;
        }

        @Override
        public void onLeave() {
            if (priorPrepareMediaResource.getValue() == mediaResource && mediaResource != null) {
                priorPrepareMediaResource.setValue(null);
            }
        }
    }

    private class InvisiblePreparedState extends VideoState {
        @Override
        public void onEnter() {
            if (audioFocusManager != null) {
                audioFocusManager.stopFocus();
            }
            if (mediaPlayer != null) {
                mediaPlayer.pause();
                mediaPlayer.setPlayerOption(listOf(
                        PLVMediaPlayerOptionEnum.START_ON_PREPARED.value("0")
                ));
            }
        }

        @Override
        public VideoState nextState() {
            if (isVisibleToUser) {
                return new VisiblePreparedState();
            }
            return this;
        }
    }

    private class VisiblePreparedState extends VideoState {
        @Override
        public void onEnter() {
            if (audioFocusManager != null) {
                audioFocusManager.startFocus(mediaPlayer);
            }
            if (mediaPlayer != null) {
                mediaPlayer.start();
                mediaPlayer.setPlayerOption(listOf(
                        PLVMediaPlayerOptionEnum.START_ON_PREPARED.value("1")
                ));
            }
            setAutoRotate();
        }

        @Override
        public VideoState nextState() {
            if (!isVisibleToUser) {
                return new InvisiblePreparedState();
            }
            return this;
        }

        private void setAutoRotate() {
            if (mediaPlayer == null || activity == null) {
                return;
            }
            Rect videoSize = mediaPlayer.getStateListenerRegistry().getVideoSize().getValue();
            if (videoSize == null) {
                return;
            }
            boolean isPortraitVideo = videoSize.width() < videoSize.height();
            PLVActivityOrientationManager.on(activity).setFollowSystemAutoRotate(!isPortraitVideo);
            if (isPortraitVideo) {
                PLVActivityOrientationManager.on(activity).requestOrientation(true);
            }
        }
    }

    // </editor-fold>

}
