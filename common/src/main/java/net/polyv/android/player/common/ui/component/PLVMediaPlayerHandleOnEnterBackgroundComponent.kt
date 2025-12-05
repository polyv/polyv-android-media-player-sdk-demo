package net.polyv.android.player.common.ui.component

import android.app.Activity
import androidx.lifecycle.GenericLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager
import net.polyv.android.player.common.utils.floatwindow.enums.PLVFloatWindowLaunchReason
import net.polyv.android.player.common.utils.floatwindow.permission.PLVFloatPermissionUtils
import net.polyv.android.player.common.utils.ui.PLVViewLifecycleObservable
import net.polyv.android.player.common.utils.ui.PLVViewLifecycleObservable.AbsViewLifecycleObserver
import net.polyv.android.player.common.utils.ui.PLVViewLifecycleObservable.IViewLifecycleObserver
import net.polyv.android.player.core.api.listener.state.PLVMediaPlayerState
import net.polyv.android.player.sdk.foundation.app.PLVApplicationContext
import net.polyv.android.player.sdk.foundation.lang.postToMainThread

/**
 * App进入后台时自动处理逻辑
 *
 * @author Hoshiiro
 */
private const val TAG = "PLVMediaPlayerHandleOnEnterBackgroundComponent"

/**
 * 是否自动唤起小窗，不会自动申请悬浮窗权限，需要提前获取权限后才会自动唤起
 */
private const val AUTO_FLOAT_WINDOW_ON_BACKGROUND = true

/**
 * 是否自动暂停播放，当未唤起小窗时自动暂停
 */
private const val AUTO_PAUSE_ON_BACKGROUND = true

class PLVMediaPlayerHandleOnEnterBackgroundComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GenericLifecycleObserver {
    private var isVisibleToUser = false
    private var isAttach = false

    private var isAutoPausedOnEnterBackground = false

    init {
        (context as LifecycleOwner).lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner?, event: Lifecycle.Event?) {
        if (event == Lifecycle.Event.ON_STOP) {
            postToMainThread {
                onEnterBackground()
            }
        } else if (event == Lifecycle.Event.ON_START) {
            postToMainThread {
                onResumeFromBackground()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAttach = true

        PLVMediaPlayerLocalProvider.localLifecycleObservable.on(this).current()!!
            .addObserver(viewLifecycleObserver)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isAttach = false
    }

    private val viewLifecycleObserver: IViewLifecycleObserver = object : AbsViewLifecycleObserver() {
        override fun onDestroy(observable: PLVViewLifecycleObservable) {
            observable.removeObserver(this)
            (context as LifecycleOwner).lifecycle.removeObserver(this@PLVMediaPlayerHandleOnEnterBackgroundComponent)
        }
    }

    fun setUserVisibleHint(isVisibleToUser: Boolean) {
        this.isVisibleToUser = isVisibleToUser
    }

    override fun onDraw(canvas: Canvas) {
        // do nothing
    }

    private fun onEnterBackground() {
        if (PLVApplicationContext.startedActivitiesCount <= 0 && isHandleAutoFloatWindow()) {
            if (launchFloatWindow()) {
                return
            }
        }
        if (isHandleAutoPause()) {
            autoPauseOnEnterBackground()
        }
    }

    private fun onResumeFromBackground() {
        if (isHandleAutoFloatWindow()) {
            if (hideFloatWindow()) {
                return
            }
        }
        if (isHandleAutoPause()) {
            recoverPlayOnEnterBackground()
        }
    }

    private fun isHandleAutoFloatWindow(): Boolean {
        return AUTO_FLOAT_WINDOW_ON_BACKGROUND && isAttach && isVisibleToUser
    }

    private fun launchFloatWindow(): Boolean {
        if (!PLVFloatPermissionUtils.checkPermission(context as Activity?)) {
            return false
        }
        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
        if (dependScope == null) {
            return false
        }
        val mediaViewModel: PLVMPMediaViewModel = dependScope.get()
        val controllerViewModel: PLVMPMediaControllerViewModel = dependScope.get()
        val playerState = mediaViewModel.mediaPlayViewState.value?.playerState
        val mediaOutputMode = mediaViewModel.mediaInfoViewState.value?.outputMode
        if (playerState != PLVMediaPlayerState.STATE_PLAYING || mediaOutputMode != PLVMediaOutputMode.AUDIO_VIDEO) {
            return false
        }
        controllerViewModel.launchFloatWindow(PLVFloatWindowLaunchReason.BACKGROUND_STATE_CHANGED)
        return true
    }

    private fun hideFloatWindow(): Boolean {
        if (!PLVMediaPlayerFloatWindowManager.getInstance().isFloatingWindowShowing) {
            return false
        }
        PLVMediaPlayerFloatWindowManager.getInstance().hide()
        return true
    }

    private fun isHandleAutoPause(): Boolean {
        return AUTO_PAUSE_ON_BACKGROUND && isAttach && isVisibleToUser
    }

    private fun autoPauseOnEnterBackground() {
        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
        if (dependScope == null) {
            return
        }
        val mediaViewModel: PLVMPMediaViewModel = dependScope.get()
        val isPlaying: Boolean = mediaViewModel.mediaPlayViewState.value?.isPlaying ?: false
        if (isPlaying) {
            mediaViewModel.pause()
            isAutoPausedOnEnterBackground = true
        }
    }

    private fun recoverPlayOnEnterBackground() {
        if (!isAutoPausedOnEnterBackground) {
            return
        }
        isAutoPausedOnEnterBackground = false
        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()
        if (dependScope == null) {
            return
        }
        val mediaViewModel: PLVMPMediaViewModel = dependScope.get()
        mediaViewModel.start()
    }

}
