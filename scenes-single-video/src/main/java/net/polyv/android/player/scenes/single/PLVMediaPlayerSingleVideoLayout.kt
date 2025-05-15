package net.polyv.android.player.scenes.single

import android.app.Activity
import androidx.lifecycle.GenericLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.di.commonItemModule
import net.polyv.android.player.common.modules.auxiliary.view.PLVMPAuxiliaryVideoView
import net.polyv.android.player.common.modules.auxiliary.viewmodel.PLVMPAuxiliaryViewModel
import net.polyv.android.player.common.modules.download.single.viewmodel.PLVMPDownloadItemViewModel
import net.polyv.android.player.common.modules.media.view.PLVMPVideoView
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.LockMediaControllerAction
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.ui.component.PLVMediaPlayerHandleOnEnterBackgroundComponent
import net.polyv.android.player.common.ui.component.PLVMediaPlayerMoreActionLayoutPortrait
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowHelper
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager
import net.polyv.android.player.common.ui.component.floatwindow.layout.PLVMediaPlayerFloatWindowContentLayout
import net.polyv.android.player.common.utils.audiofocus.PLVMediaPlayerAudioFocusManager
import net.polyv.android.player.common.utils.floatwindow.permission.PLVFloatPermissionUtils
import net.polyv.android.player.common.utils.orientation.PLVActivityOrientationManager
import net.polyv.android.player.common.utils.ui.PLVViewLifecycleObservable
import net.polyv.android.player.common.utils.ui.PLVViewLifecycleObservable.IViewLifecycleObserver
import net.polyv.android.player.core.api.option.PLVMediaPlayerOptionEnum
import net.polyv.android.player.scenes.single.layout.PLVMediaPlayerSingleLandscapeItemLayout
import net.polyv.android.player.scenes.single.layout.PLVMediaPlayerSinglePortraitItemLayout
import net.polyv.android.player.sdk.foundation.collections.listOf
import net.polyv.android.player.sdk.foundation.di.DependScope
import net.polyv.android.player.sdk.foundation.graphics.isLandscape
import net.polyv.android.player.sdk.foundation.graphics.isPortrait
import net.polyv.android.player.sdk.foundation.lang.Consumer
import net.polyv.android.player.sdk.foundation.lang.Duration.Companion.seconds

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerSingleVideoLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // <editor-fold defaultstate="collapsed" desc="Layout-属性">
    private val dependScope = DependScope(commonItemModule)
    private val videoView: View = PLVMPVideoView(context, dependScope)
    private val auxiliaryVideoView: View = PLVMPAuxiliaryVideoView(context, dependScope)
    private val mediaViewModel: PLVMPMediaViewModel by dependScope
    private val mediaControllerViewModel: PLVMPMediaControllerViewModel by dependScope
    private val auxiliaryViewModel: PLVMPAuxiliaryViewModel by dependScope
    private val downloadItemViewModel: PLVMPDownloadItemViewModel by dependScope

    // 纵向-半屏 播放器皮肤 layout
    private val portraitLayout by lazy { PortraitLayout(context) }

    // 横向-全屏 播放器皮肤 layout
    private val landscapeLayout by lazy { LandscapeLayout(context) }

    // App进入后台时自动唤起小窗
    private val handleOnEnterBackgroundComponent = PLVMediaPlayerHandleOnEnterBackgroundComponent(context)

    // 音频焦点管理
    private val audioFocusManager = PLVMediaPlayerAudioFocusManager(context)

    // 生命周期
    private val viewLifecycleObservable = PLVViewLifecycleObservable()

    private var mediaResource: PLVMediaResource? = null

    // 横竖屏方向
    private var lastOrientation = -1

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-初始化">
    fun init() {
        initProvider()
        initLayout()
        initVideoView()
        observeLifecycle()
        observeLaunchFloatWindow()
        updateVideoLayout()
    }

    private fun initLayout() {
        keepScreenOn = true
        handleOnEnterBackgroundComponent.setUserVisibleHint(true)
    }

    private fun initProvider() {
        PLVMediaPlayerLocalProvider.localLifecycleObservable.on(this).provide(viewLifecycleObservable)
        PLVMediaPlayerLocalProvider.localDependScope.on(this).provide(dependScope)
    }

    private fun initVideoView() {
        mediaViewModel.setPlayerOption(
            listOf(
                PLVMediaPlayerOptionEnum.ENABLE_ACCURATE_SEEK.value("1"),
                PLVMediaPlayerOptionEnum.SKIP_ACCURATE_SEEK_AT_START.value("1")
            )
        )
        mediaViewModel.setAutoContinue(true)

        auxiliaryViewModel.bind()

        audioFocusManager.startFocus(mediaViewModel)
    }

    private fun observeLifecycle() {
        (context as LifecycleOwner).lifecycle.addObserver(GenericLifecycleObserver { source, event ->
            if (event == Lifecycle.Event.ON_DESTROY && source.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                destroy()
            }
        })
    }

    private fun observeLaunchFloatWindow() {
        mediaControllerViewModel.launchFloatWindowEvent
            .observeUntilViewDetached(this) { reason ->
                onLaunchFloatWindowEvent(reason.code)
            }

        PLVMediaPlayerFloatWindowManager.getInstance().floatingViewShowState
            .observeUntilViewDetached(this) { showing ->
                if (!showing) {
                    // 从小窗模式回到页面
                    updateVideoLayout()
                }
            }
    }

    private fun updateVideoLayout() {
        removeAllViews()
        addView(handleOnEnterBackgroundComponent)
        if (isPortrait(context)) {
            portraitLayout.setVideoView(videoView)
            portraitLayout.setAuxiliaryVideoView(auxiliaryVideoView)
            addView(portraitLayout)
        } else {
            landscapeLayout.setVideoView(videoView)
            landscapeLayout.setAuxiliaryVideoView(auxiliaryVideoView)
            addView(landscapeLayout)
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-setter/getter">
    fun setMediaResource(mediaResource: PLVMediaResource?) {
        this.mediaResource = mediaResource
        if (mediaResource != null) {
            mediaViewModel.setMediaResource(mediaResource)
        }
    }

    fun setEnterFromFloatWindow(enterFromFloatWindow: Boolean) {
        auxiliaryViewModel.setEnterFromFloatWindow(enterFromFloatWindow)
    }

    fun setEnterFromDownloadCenter(enterFromDownloadCenter: Boolean) {
        downloadItemViewModel.setDownloadActionVisible(!enterFromDownloadCenter)
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-小窗逻辑处理">
    private fun onLaunchFloatWindowEvent(reason: Int) {
        PLVFloatPermissionUtils.requestPermission(context as Activity) { isGrant ->
            if (isGrant) {
                launchFloatWindow(reason)
            }
        }
    }

    private fun launchFloatWindow(reason: Int) {
        val videoSize = mediaViewModel.mediaInfoViewState.value?.videoSize
        val floatWindowPosition = PLVMediaPlayerFloatWindowHelper.calculateFloatWindowPosition(videoSize) ?: return
        val contentLayout = PLVMediaPlayerFloatWindowContentLayout(context)
        PLVMediaPlayerLocalProvider.localDependScope.on(contentLayout).provide(dependScope)

        (videoView.parent as? ViewGroup)?.removeView(videoView)
        contentLayout.container.addView(videoView)

        PLVMediaPlayerFloatWindowManager.getInstance()
            .bindContentLayout(contentLayout)
            .saveData(object : Consumer<Bundle> {
                override fun accept(bundle: Bundle) {
                    bundle.putParcelable(
                        PLVMediaPlayerFloatWindowManager.KEY_SAVE_MEDIA_RESOURCE,
                        mediaResource
                    )
                }
            })
            .setFloatingSize(floatWindowPosition.width(), floatWindowPosition.height())
            .setFloatingPosition(floatWindowPosition.left, floatWindowPosition.top)
            .show(reason)
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-横竖屏切换">
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation != lastOrientation) {
            lastOrientation = newConfig.orientation
            updateVideoLayout()
        }
        if (isPortrait()) {
            // 只有横屏有操作锁定，竖屏没有
            mediaControllerViewModel.lockMediaController(LockMediaControllerAction.UNLOCK)
        }
        mediaControllerViewModel.showControllerForDuration(5.seconds())
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout-返回和销毁">
    fun onBackPressed(): Boolean {
        if (isLandscape()) {
            PLVActivityOrientationManager.on((context as AppCompatActivity))
                .requestOrientation(true)
                .setLockOrientation(false)
            return true
        }
        return false
    }

    private fun destroy() {
        viewLifecycleObservable.callObserver(object : Consumer<IViewLifecycleObserver> {
            override fun accept(observer: IViewLifecycleObserver) {
                observer.onDestroy(viewLifecycleObservable)
            }
        })
        PLVMediaPlayerFloatWindowManager.getInstance()
            .runOnFloatingWindowClosed {
                audioFocusManager.stopFocus()
                dependScope.destroy()
            }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="竖屏布局">
    private class PortraitLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : FrameLayout(context, attrs, defStyleAttr) {

        private val singlePortVideoLayout by lazy { findViewById<PLVMediaPlayerSinglePortraitItemLayout>(R.id.plv_media_player_single_port_video_layout) }
        private val singlePortVideoDetailContainer by lazy { findViewById<FrameLayout>(R.id.plv_media_player_single_port_video_detail_container) }
        private val singlePortVideoMoreActionLayout by lazy { findViewById<PLVMediaPlayerMoreActionLayoutPortrait>(R.id.plv_media_player_more_action_layout_portrait) }

        init {
            LayoutInflater.from(context).inflate(R.layout.plv_media_player_single_video_layout_port, this)
        }

        fun setVideoView(videoView: View?) {
            singlePortVideoLayout.setVideoView(videoView)
        }

        fun setAuxiliaryVideoView(auxiliaryVideoView: View?) {
            singlePortVideoLayout.setAuxiliaryVideoView(auxiliaryVideoView)
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="横屏布局">
    private class LandscapeLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : FrameLayout(context, attrs, defStyleAttr) {
        private val landscapeLayout = PLVMediaPlayerSingleLandscapeItemLayout(context)

        init {
            addView(landscapeLayout)
        }

        fun setVideoView(videoView: View?) {
            landscapeLayout.setVideoView(videoView)
        }

        fun setAuxiliaryVideoView(auxiliaryVideoView: View?) {
            landscapeLayout.setAuxiliaryVideoView(auxiliaryVideoView)
        }
    }
    // </editor-fold>

}
