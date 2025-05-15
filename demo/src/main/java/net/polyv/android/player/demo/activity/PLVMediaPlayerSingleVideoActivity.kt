package net.polyv.android.player.demo.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.WindowManager
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource
import net.polyv.android.player.common.ui.component.floatwindow.IPLVMediaPlayerFloatWindowControlActionListener
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager
import net.polyv.android.player.common.ui.router.PLVMediaPlayerRouter
import net.polyv.android.player.common.ui.router.PLVMediaPlayerRouter.router
import net.polyv.android.player.common.ui.router.RouterDestination.SceneSingle
import net.polyv.android.player.common.ui.router.RouterPayload.SceneSinglePayload
import net.polyv.android.player.common.utils.orientation.PLVActivityOrientationManager
import net.polyv.android.player.scenes.single.PLVMediaPlayerSingleVideoLayout
import net.polyv.android.player.sdk.PLVDeviceManager.hideNavigationBar
import net.polyv.android.player.sdk.PLVDeviceManager.hideStatusBar
import net.polyv.android.player.sdk.PLVDeviceManager.showNavigationBar
import net.polyv.android.player.sdk.PLVDeviceManager.showStatusBar
import net.polyv.android.player.sdk.foundation.graphics.isPortrait

/**
 * @author Hoshiiro
 */
private const val TAG = "PLVMediaPlayerSingleVideoActivity"

// 禁止录屏、截图
private const val FLAG_SECURE_WINDOW: Boolean = false

class PLVMediaPlayerSingleVideoActivity : AppCompatActivity() {
    // 进入页面时初始的视频资源
    private var targetMediaResource: PLVMediaResource? = null
    private var enterFromFloatWindow = false
    private var enterFromDownloadCenter = false

    // 实际的业务布局
    private var contentLayout: PLVMediaPlayerSingleVideoLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FLAG_SECURE_WINDOW) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        // 全局状态更新
        PLVMediaPlayerFloatWindowManager.getInstance().clear() // 如果是从小窗状态进入，需要先销毁小窗
        updateWindowInsets() // 更新状态栏的横竖屏状态

        // 初始化 页面 和 Layout
        initActivity()
        initSingleVideoLayout()

        // 设置屏幕方向监听
        setupOrientationManager()

        // 设置悬浮窗监听
        setFloatWindowListener()
    }

    private fun initActivity() {
        contentLayout = PLVMediaPlayerSingleVideoLayout(this)
        setContentView(contentLayout)

        val bundle = intent.extras
        if (bundle != null) {
            targetMediaResource = bundle.getParcelable(PLVMediaPlayerRouter.KEY_TARGET_MEDIA_RESOURCE)
            enterFromFloatWindow = bundle.getBoolean(PLVMediaPlayerRouter.KEY_ENTER_FROM_FLOAT_WINDOW, false)
            enterFromDownloadCenter = bundle.getBoolean(PLVMediaPlayerRouter.KEY_ENTER_FROM_DOWNLOAD_CENTER, false)
        }
    }

    private fun initSingleVideoLayout() {
        contentLayout!!.init()
        if (targetMediaResource != null) {
            contentLayout!!.setMediaResource(targetMediaResource)
        }
        contentLayout!!.setEnterFromFloatWindow(enterFromFloatWindow)
        contentLayout!!.setEnterFromDownloadCenter(enterFromDownloadCenter)
    }

    // <editor-fold defaultstate="collapsed" desc="页面-横竖屏切换">
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateWindowInsets()
    }

    private fun updateWindowInsets() {
        if (isPortrait()) {
            showStatusBar(this)
            showNavigationBar(this)
        } else {
            hideStatusBar(this)
            hideNavigationBar(this)
        }
    }

    private fun setupOrientationManager() {
        PLVActivityOrientationManager.on(this)
            .setFollowSystemAutoRotate(true)
            .start()
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="页面-悬浮窗控制">

    private fun setFloatWindowListener() {
        PLVMediaPlayerFloatWindowManager.getInstance()
            .setControlActionListener(object : IPLVMediaPlayerFloatWindowControlActionListener {
                override fun onAfterFloatWindowShow(reason: Int) {
                    if (reason == PLVMediaPlayerFloatWindowManager.SHOW_REASON_MANUAL) {
                        finish()
                    }
                }

                override fun onClickContentGoBack(bundle: Bundle) {
                    val mediaResource = bundle.getParcelable<PLVMediaResource>(PLVMediaPlayerFloatWindowManager.KEY_SAVE_MEDIA_RESOURCE)

                    if (mediaResource == null) {
                        onClickClose()
                        return
                    }

                    val intent = router(
                        SceneSingle(SceneSinglePayload(mediaResource, true))
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)

                    PLVMediaPlayerFloatWindowManager.getInstance().hide()
                }

                override fun onClickClose() {
                    PLVMediaPlayerFloatWindowManager.getInstance().hide()
                }
            })
    }

    // </editor-fold>

    override fun onBackPressed() {
        if (contentLayout != null && contentLayout!!.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        PLVMediaPlayerFloatWindowManager.getInstance().runOnFloatingWindowClosed {
            PLVMediaPlayerFloatWindowManager.getInstance().clear()
        }
    }

}
