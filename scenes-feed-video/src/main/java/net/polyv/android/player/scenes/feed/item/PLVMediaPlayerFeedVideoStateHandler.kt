package net.polyv.android.player.scenes.feed.item

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager
import net.polyv.android.player.common.utils.audiofocus.PLVMediaPlayerAudioFocusManager
import net.polyv.android.player.common.utils.orientation.PLVActivityOrientationManager
import net.polyv.android.player.core.api.option.PLVMediaPlayerOptionEnum
import net.polyv.android.player.sdk.foundation.collections.listOf
import net.polyv.android.player.sdk.foundation.di.DependScope
import net.polyv.android.player.sdk.foundation.lang.MutableObserver
import net.polyv.android.player.sdk.foundation.lang.MutableState

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerFeedVideoStateHandler {

    companion object {
        private val priorPrepareMediaResource = MutableState<PLVMediaResource?>(null)
    }

    private var priorPrepareMediaResourceObserver: MutableObserver<PLVMediaResource?>? = null

    private var dependScope: DependScope? = null
    private var mediaViewModel: PLVMPMediaViewModel? = null

    // 音频焦点管理
    private var audioFocusManager: PLVMediaPlayerAudioFocusManager? = null

    private var activity: AppCompatActivity? = null

    private var mediaResource: PLVMediaResource? = null

    private var currentVideoState: VideoState = InitializedState()
    private var onActivityCreated = false
    private var isVisibleToUser = false
    private var isCalledPrepare = false
    private var isPrepared = false

    fun onCreateView(context: Context, dependScope: DependScope) {
        this.dependScope = dependScope
        activity = context as AppCompatActivity
        mediaViewModel = dependScope.get<PLVMPMediaViewModel>()
        audioFocusManager = PLVMediaPlayerAudioFocusManager(activity!!)
    }

    fun onActivityCreated() {
        onActivityCreated = true
        initMediaPlayer()
        updateVideoState()
        observeMediaPlayer()
        observePriorMediaResource()
    }

    fun setUserVisibleHint(isVisibleToUser: Boolean) {
        this.isVisibleToUser = isVisibleToUser
        updateVideoState()
    }

    fun onDestroyView() {
        stopPriorMediaResourceObserver()
        onActivityCreated = false
        isVisibleToUser = false
        PLVMediaPlayerFloatWindowManager.getInstance().runOnFloatingWindowClosed {
            isCalledPrepare = false
            isPrepared = false
            audioFocusManager?.stopFocus()
            dependScope?.destroy()
            dependScope = null
            mediaViewModel = null
            changeToVideoState(InitializedState())
        }
    }

    fun setMediaResource(mediaResource: PLVMediaResource?) {
        if (this.mediaResource === mediaResource) {
            return
        }
        this.mediaResource = mediaResource
        changeToVideoState(InitializedState())
        updateVideoState()
    }

    private fun initMediaPlayer() {
        if (mediaViewModel == null) {
            return
        }
        mediaViewModel!!.setPlayerOption(
            listOf(
                PLVMediaPlayerOptionEnum.ENABLE_ACCURATE_SEEK.value("1"),
                PLVMediaPlayerOptionEnum.SKIP_ACCURATE_SEEK_AT_START.value("1"),
                PLVMediaPlayerOptionEnum.START_ON_PREPARED.value("0"),
                PLVMediaPlayerOptionEnum.RENDER_ON_PREPARED.value("1")
            )
        )
        mediaViewModel!!.setAutoContinue(true)
    }

    private fun observeMediaPlayer() {
        if (mediaViewModel == null) {
            return
        }
        mediaViewModel!!.onPreparedEvent
            .observe {
                isPrepared = true
                updateVideoState()
            }
    }

    private fun observePriorMediaResource() {
        stopPriorMediaResourceObserver()
        priorPrepareMediaResourceObserver = priorPrepareMediaResource.observe {
            updateVideoState()
        }
    }

    private fun stopPriorMediaResourceObserver() {
        if (priorPrepareMediaResourceObserver != null) {
            priorPrepareMediaResourceObserver!!.dispose()
            priorPrepareMediaResourceObserver = null
        }
    }

    // <editor-fold defaultstate="collapsed" desc="预加载状态管理">
    private fun updateVideoState() {
        changeToVideoState(currentVideoState.nextState())
    }

    private fun changeToVideoState(nextState: VideoState) {
        if (nextState === currentVideoState) {
            return
        }
        currentVideoState.onLeave()
        currentVideoState = nextState
        currentVideoState.onEnter()
    }

    private abstract inner class VideoState {
        open fun onEnter() {
        }

        open fun nextState(): VideoState {
            return this
        }

        open fun onLeave() {
        }
    }

    private inner class InitializedState : VideoState() {
        override fun onEnter() {
            isCalledPrepare = false
            isPrepared = false
        }

        override fun nextState(): VideoState {
            if (!onActivityCreated) {
                return this
            }
            return if (isVisibleToUser) {
                if (mediaResource != null) {
                    VisibleCallPrepareState()
                } else {
                    VisibleNotPrepareState()
                }
            } else {
                if (priorPrepareMediaResource.value == null && mediaResource != null) {
                    InvisibleCallPrepareState()
                } else {
                    InvisibleNotPrepareState()
                }
            }
        }
    }

    private inner class InvisibleNotPrepareState : VideoState() {
        override fun nextState(): VideoState {
            if (isVisibleToUser) {
                return if (mediaResource != null) {
                    VisibleCallPrepareState()
                } else {
                    VisibleNotPrepareState()
                }
            }
            if (priorPrepareMediaResource.value == null && mediaResource != null) {
                return InvisibleCallPrepareState()
            }
            return this
        }
    }

    private inner class VisibleNotPrepareState : VideoState() {
        override fun nextState(): VideoState {
            if (!isVisibleToUser) {
                return InvisibleNotPrepareState()
            }
            if (mediaResource != null) {
                return VisibleCallPrepareState()
            }
            return this
        }
    }

    private inner class InvisibleCallPrepareState : VideoState() {
        override fun onEnter() {
            if (isCalledPrepare) {
                return
            }
            isCalledPrepare = true
            if (mediaViewModel != null && mediaResource != null) {
                mediaViewModel!!.setMediaResource(mediaResource!!)
            }
        }

        override fun nextState(): VideoState {
            if (isVisibleToUser) {
                return if (isPrepared) {
                    VisiblePreparedState()
                } else {
                    VisibleCallPrepareState()
                }
            } else {
                if (isPrepared) {
                    return InvisiblePreparedState()
                }
            }
            return this
        }
    }

    private inner class VisibleCallPrepareState : VideoState() {
        override fun onEnter() {
            if (isCalledPrepare) {
                return
            }
            isCalledPrepare = true
            if (mediaViewModel != null && mediaResource != null) {
                mediaViewModel!!.setMediaResource(mediaResource!!)
            }
            priorPrepareMediaResource.setValue(mediaResource)
        }

        override fun nextState(): VideoState {
            if (isVisibleToUser) {
                if (isPrepared) {
                    return VisiblePreparedState()
                }
            } else {
                return if (!isPrepared) {
                    InvisibleCallPrepareState()
                } else {
                    InvisiblePreparedState()
                }
            }
            return this
        }

        override fun onLeave() {
            if (priorPrepareMediaResource.value === mediaResource && mediaResource != null) {
                priorPrepareMediaResource.setValue(null)
            }
        }
    }

    private inner class InvisiblePreparedState : VideoState() {
        override fun onEnter() {
            if (audioFocusManager != null) {
                audioFocusManager!!.stopFocus()
            }
            if (mediaViewModel != null) {
                mediaViewModel!!.pause()
                mediaViewModel!!.setPlayerOption(
                    listOf(
                        PLVMediaPlayerOptionEnum.START_ON_PREPARED.value("0")
                    )
                )
            }
        }

        override fun nextState(): VideoState {
            if (isVisibleToUser) {
                return VisiblePreparedState()
            }
            return this
        }
    }

    private inner class VisiblePreparedState : VideoState() {
        override fun onEnter() {
            if (audioFocusManager != null) {
                audioFocusManager!!.startFocus(mediaViewModel)
            }
            if (mediaViewModel != null) {
                mediaViewModel!!.start()
                mediaViewModel!!.setPlayerOption(
                    listOf(
                        PLVMediaPlayerOptionEnum.START_ON_PREPARED.value("1")
                    )
                )
            }
            setAutoRotate()
        }

        override fun nextState(): VideoState {
            if (!isVisibleToUser) {
                return InvisiblePreparedState()
            }
            return this
        }

        fun setAutoRotate() {
            if (mediaViewModel == null || activity == null) {
                return
            }
            val videoSize = mediaViewModel?.mediaInfoViewState?.value?.videoSize ?: return
            val isPortraitVideo = videoSize.width() < videoSize.height()
            PLVActivityOrientationManager.on(activity!!).setFollowSystemAutoRotate(!isPortraitVideo)
            if (isPortraitVideo) {
                PLVActivityOrientationManager.on(activity!!).requestOrientation(true)
            }
        }
    }
    // </editor-fold>
}
