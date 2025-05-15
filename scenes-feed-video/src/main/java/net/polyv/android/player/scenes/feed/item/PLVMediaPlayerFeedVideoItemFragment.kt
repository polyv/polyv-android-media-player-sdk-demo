package net.polyv.android.player.scenes.feed.item

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.di.commonItemModule
import net.polyv.android.player.common.modules.media.view.PLVMPVideoView
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.LockMediaControllerAction
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.common.ui.component.PLVMediaPlayerHandleOnEnterBackgroundComponent
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowHelper
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager
import net.polyv.android.player.common.ui.component.floatwindow.layout.PLVMediaPlayerFloatWindowContentLayout
import net.polyv.android.player.common.utils.floatwindow.permission.PLVFloatPermissionUtils
import net.polyv.android.player.common.utils.ui.PLVViewLifecycleObservable
import net.polyv.android.player.common.utils.ui.PLVViewLifecycleObservable.IViewLifecycleObserver
import net.polyv.android.player.scenes.feed.item.layout.PLVMediaPlayerFeedLandscapeItemLayout
import net.polyv.android.player.scenes.feed.item.layout.PLVMediaPlayerFeedPortraitItemLayout
import net.polyv.android.player.sdk.foundation.di.DependScope
import net.polyv.android.player.sdk.foundation.graphics.isPortrait
import net.polyv.android.player.sdk.foundation.lang.Consumer
import net.polyv.android.player.sdk.foundation.lang.Duration.Companion.seconds
import net.polyv.android.player.sdk.foundation.lang.requireNotNull

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerFeedVideoItemFragment : Fragment() {

    // <editor-fold defaultstate="collapsed" desc="Fragment-属性">
    // 装载皮肤布局的容器，用于 addView 皮肤布局（ 竖屏-全屏 皮肤布局 或者 横屏-全屏 皮肤布局 ）
    private var rootContainer: FrameLayout? = null

    // 竖屏-全屏 皮肤布局
    private var portraitVideoLayout: PLVMediaPlayerFeedPortraitItemLayout? = null

    // 横屏-全屏 皮肤布局
    private var landscapeVideoLayout: PLVMediaPlayerFeedLandscapeItemLayout? = null

    private var dependScope: DependScope? = null
    private var videoView: View? = null
    private var mediaViewModel: PLVMPMediaViewModel? = null
    private var mediaControllerViewModel: PLVMPMediaControllerViewModel? = null

    // App进入后台时自动唤起小窗
    private var handleOnEnterBackgroundComponent: PLVMediaPlayerHandleOnEnterBackgroundComponent? = null

    // 裸播放器状态 的数据处理
    private val videoStateHandler = PLVMediaPlayerFeedVideoStateHandler()

    // 生命周期
    private val viewLifecycleObservable = PLVViewLifecycleObservable()

    private var mediaResource: PLVMediaResource? = null

    // 记录上一次的屏幕方向
    private var lastOrientation = -1

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fragment-初始化和生命周期方法">
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = inflater.context
        rootContainer = FrameLayout(context)
        portraitVideoLayout = PLVMediaPlayerFeedPortraitItemLayout(context)
        landscapeVideoLayout = PLVMediaPlayerFeedLandscapeItemLayout(context)
        dependScope = DependScope(commonItemModule)
        this.videoView = PLVMPVideoView(context, dependScope!!)
        this.mediaViewModel = dependScope!!.get<PLVMPMediaViewModel>()
        this.mediaControllerViewModel = dependScope!!.get<PLVMPMediaControllerViewModel>()
        handleOnEnterBackgroundComponent = PLVMediaPlayerHandleOnEnterBackgroundComponent(context)

        PLVMediaPlayerLocalProvider.localDependScope.on(rootContainer!!).provide(dependScope)
        PLVMediaPlayerLocalProvider.localLifecycleObservable.on(rootContainer!!).provide(viewLifecycleObservable)
        observeLaunchFloatWindow()

        // 竖屏控制栏长显
        mediaControllerViewModel!!.changeControllerVisible(true)

        videoStateHandler.onCreateView(context, dependScope!!)
        return rootContainer
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateVideoLayout()
        videoStateHandler.onActivityCreated()
        handleOnEnterBackgroundComponent?.setUserVisibleHint(userVisibleHint)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        videoStateHandler.setUserVisibleHint(isVisibleToUser)
        handleOnEnterBackgroundComponent?.setUserVisibleHint(isVisibleToUser)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        videoStateHandler.onDestroyView()
        viewLifecycleObservable.callObserver(object : Consumer<IViewLifecycleObserver> {
            override fun accept(observer: IViewLifecycleObserver) {
                observer.onDestroy(viewLifecycleObservable)
            }
        })
        PLVMediaPlayerFloatWindowManager.getInstance()
            .runOnFloatingWindowClosed {
                portraitVideoLayout?.setVideoView(null)
                portraitVideoLayout = null
                landscapeVideoLayout?.setVideoView(null)
                landscapeVideoLayout = null
                rootContainer?.removeAllViews()
                rootContainer = null
                dependScope?.destroy()
            }
    }

    // </editor-fold>

    private fun observeLaunchFloatWindow() {
        requireNotNull(dependScope).get(PLVMPMediaControllerViewModel::class.java)
            .launchFloatWindowEvent
            .observeUntilViewDetached(rootContainer!!) { reason ->
                onLaunchFloatWindowEvent(reason.code)
            }

        PLVMediaPlayerFloatWindowManager.getInstance().floatingViewShowState
            .observeUntilViewDetached(rootContainer!!) { showing ->
                if (!showing) {
                    // 从小窗模式回到页面
                    updateVideoLayout()
                }
            }
    }

    // <editor-fold defaultstate="collapsed" desc="Layout-方法-浮窗逻辑-响应普通模式切换到浮窗模式的处理逻辑">
    private fun onLaunchFloatWindowEvent(reason: Int) {
        PLVFloatPermissionUtils.requestPermission(context as Activity) { isGrant ->
            if (isGrant) {
                launchFloatWindow(reason)
            }
        }
    }

    private fun launchFloatWindow(reason: Int) {
        val videoSize = mediaViewModel?.mediaInfoViewState?.value?.videoSize
        val floatWindowPosition = PLVMediaPlayerFloatWindowHelper.calculateFloatWindowPosition(videoSize) ?: return
        if (videoView == null) {
            return
        }
        val contentLayout = PLVMediaPlayerFloatWindowContentLayout(context!!)
        PLVMediaPlayerLocalProvider.localDependScope.on(contentLayout).provide(dependScope)
        if (videoView!!.parent != null) {
            (videoView!!.parent as ViewGroup).removeView(videoView)
        }
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

    // <editor-fold defaultstate="collapsed" desc="Fragment-更新UI-用于 初始化、横竖屏切换 时">
    private fun updateVideoLayout() {
        if (rootContainer == null || portraitVideoLayout == null || landscapeVideoLayout == null) {
            return
        }

        rootContainer!!.removeAllViews()
        if (handleOnEnterBackgroundComponent != null) {
            rootContainer!!.addView(handleOnEnterBackgroundComponent)
        }

        // 根据屏幕方向，切换对应的横屏或者竖屏皮肤，并设置裸播放器进对应的皮肤
        if (isPortrait(rootContainer!!.context)) {
            portraitVideoLayout!!.setVideoView(videoView)
            rootContainer!!.addView(portraitVideoLayout!!)
        } else {
            landscapeVideoLayout!!.setVideoView(videoView)
            rootContainer!!.addView(landscapeVideoLayout!!)
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fragment-监听和处理设备的横竖屏切换事件">
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation != lastOrientation) {
            lastOrientation = newConfig.orientation
            updateVideoLayout()
        }
        if (isPortrait()) {
            // 只有横屏有操作锁定，竖屏没有
            mediaControllerViewModel?.lockMediaController(LockMediaControllerAction.UNLOCK)
            // 竖屏控制栏长显
            mediaControllerViewModel?.changeControllerVisible(true)
        } else {
            mediaControllerViewModel?.showControllerForDuration(5.seconds())
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fragment-从外部设置视频资源信息">
    fun setMediaResource(mediaResource: PLVMediaResource?) {
        this.mediaResource = mediaResource
        videoStateHandler.setMediaResource(mediaResource)
    }
    // </editor-fold>
}
