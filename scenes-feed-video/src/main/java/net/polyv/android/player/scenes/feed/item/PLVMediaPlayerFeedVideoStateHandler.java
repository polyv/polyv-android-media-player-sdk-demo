package net.polyv.android.player.scenes.feed.item;

import static net.polyv.android.player.sdk.foundation.collections.CollectionsKt.listOf;
import static net.polyv.android.player.sdk.foundation.lang.NullablesKt.nullable;

import android.content.Context;
import android.graphics.Rect;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager;
import net.polyv.android.player.common.utils.audiofocus.PLVMediaPlayerAudioFocusManager;
import net.polyv.android.player.common.utils.orientation.PLVActivityOrientationManager;
import net.polyv.android.player.core.api.listener.event.PLVMediaPlayerOnPreparedEvent;
import net.polyv.android.player.core.api.option.PLVMediaPlayerOptionEnum;
import net.polyv.android.player.sdk.foundation.di.DependScope;
import net.polyv.android.player.sdk.foundation.lang.MutableObserver;
import net.polyv.android.player.sdk.foundation.lang.MutableState;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerFeedVideoStateHandler {

    private static final MutableState<PLVMediaResource> priorPrepareMediaResource = new MutableState<>(null);

    @Nullable
    private MutableObserver<PLVMediaResource> priorPrepareMediaResourceObserver = null;

    @Nullable
    private DependScope dependScope;
    @Nullable
    private PLVMPMediaViewModel mediaViewModel;
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

    public void onCreateView(Context context, DependScope dependScope) {
        this.dependScope = dependScope;
        if (dependScope != null) {
            activity = (AppCompatActivity) context;
            mediaViewModel = dependScope.get(PLVMPMediaViewModel.class);
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
                        if (dependScope != null) {
                            dependScope.destroy();
                        }
                        dependScope = null;
                        mediaViewModel = null;
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
        if (mediaViewModel == null) {
            return;
        }
        mediaViewModel.setPlayerOption(listOf(
                PLVMediaPlayerOptionEnum.ENABLE_ACCURATE_SEEK.value("1"),
                PLVMediaPlayerOptionEnum.START_ON_PREPARED.value("0"),
                PLVMediaPlayerOptionEnum.RENDER_ON_PREPARED.value("1")
        ));
        mediaViewModel.setAutoContinue(true);
    }

    private void observeMediaPlayer() {
        if (mediaViewModel == null) {
            return;
        }
        mediaViewModel.getOnPreparedEvent()
                .observe(new Function1<PLVMediaPlayerOnPreparedEvent, Unit>() {
                    @Override
                    public Unit invoke(PLVMediaPlayerOnPreparedEvent onPreparedEvent) {
                        isPrepared = true;
                        updateVideoState();
                        return null;
                    }
                });
    }

    private void observePriorMediaResource() {
        stopPriorMediaResourceObserver();
        priorPrepareMediaResourceObserver = priorPrepareMediaResource.observe(new Function1<PLVMediaResource, Unit>() {
            @Override
            public Unit invoke(PLVMediaResource plvMediaResource) {
                updateVideoState();
                return null;
            }
        });
    }

    private void stopPriorMediaResourceObserver() {
        if (priorPrepareMediaResourceObserver != null) {
            priorPrepareMediaResourceObserver.dispose();
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
            if (mediaViewModel != null && mediaResource != null) {
                mediaViewModel.setMediaResource(mediaResource);
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
            if (mediaViewModel != null && mediaResource != null) {
                mediaViewModel.setMediaResource(mediaResource);
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
            if (mediaViewModel != null) {
                mediaViewModel.pause();
                mediaViewModel.setPlayerOption(listOf(
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
                audioFocusManager.startFocus(mediaViewModel);
            }
            if (mediaViewModel != null) {
                mediaViewModel.start();
                mediaViewModel.setPlayerOption(listOf(
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
            if (mediaViewModel == null || activity == null) {
                return;
            }
            Rect videoSize = nullable(new Function0<Rect>() {
                @Override
                public Rect invoke() {
                    return mediaViewModel.getMediaInfoViewState().getValue().getVideoSize();
                }
            });
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
