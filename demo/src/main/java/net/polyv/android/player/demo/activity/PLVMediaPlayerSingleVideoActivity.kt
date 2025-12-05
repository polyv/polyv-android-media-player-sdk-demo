package net.polyv.android.player.demo.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.ViewGroup
import android.view.WindowManager
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource
import net.polyv.android.player.common.ui.component.floatwindow.IPLVMediaPlayerFloatWindowControlActionListener
import net.polyv.android.player.common.ui.component.floatwindow.PLVMediaPlayerFloatWindowManager
import net.polyv.android.player.common.ui.router.PLVMediaPlayerRouter
import net.polyv.android.player.common.ui.router.PLVMediaPlayerRouter.router
import net.polyv.android.player.common.ui.router.RouterDestination.SceneFeed
import net.polyv.android.player.common.ui.router.RouterDestination.SceneSingle
import net.polyv.android.player.common.ui.router.RouterPayload.SceneSinglePayload
import net.polyv.android.player.common.utils.orientation.PLVActivityOrientationManager
import net.polyv.android.player.demo.mock.PLVMockMediaResourceData
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
    // 实际的业务布局
    private val contentLayout by lazy { PLVMediaPlayerSingleVideoLayout(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FLAG_SECURE_WINDOW) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        // 如果是从小窗状态进入，需要先销毁小窗
        PLVMediaPlayerFloatWindowManager.getInstance().clear()

        // 初始化 页面 和 Layout
        setContentView(contentLayout)
        updateWindowInsets()
        initSingleVideoLayout()

        // 设置屏幕方向监听
        setupOrientationManager()

        // 设置悬浮窗监听
        setFloatWindowListener()
    }

    private fun initSingleVideoLayout() {
        contentLayout.init()

        val bundle = intent.extras ?: return
        val targetMediaResource: PLVMediaResource? = bundle.getParcelable(PLVMediaPlayerRouter.KEY_TARGET_MEDIA_RESOURCE)
        val enterFromFloatWindow = bundle.getBoolean(PLVMediaPlayerRouter.KEY_ENTER_FROM_FLOAT_WINDOW, false)
        val enterFromDownloadCenter = bundle.getBoolean(PLVMediaPlayerRouter.KEY_ENTER_FROM_DOWNLOAD_CENTER, false)
        val recommendVideos = (PLVMockMediaResourceData.getInstance().mediaResources ?: emptyList())
            .filter { it != targetMediaResource }
            .shuffled()
            .take(10)

        if (targetMediaResource != null) {
            contentLayout.setMediaResource(targetMediaResource)
        }
        contentLayout.setRecommendVideos(recommendVideos)
        contentLayout.setEnterFromFloatWindow(enterFromFloatWindow)
        contentLayout.setEnterFromDownloadCenter(enterFromDownloadCenter)
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
        updateWindowInsetsAPI35()
    }

    // Android 15: force edge to edge
    private fun updateWindowInsetsAPI35() {
        val rootContent = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        if (isPortrait()) {
            ViewCompat.setOnApplyWindowInsetsListener(rootContent) { v, insets ->
                v.setPadding(
                    insets.systemWindowInsetLeft,
                    insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight,
                    insets.systemWindowInsetBottom
                )
                insets
            }
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(rootContent) { v, insets ->
                v.setPadding(0, 0, 0, 0)
                insets
            }
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
                        PLVMediaPlayerRouter.finish(SceneSingle::class.java, SceneFeed::class.java)
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
        if (contentLayout.onBackPressed()) {
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
