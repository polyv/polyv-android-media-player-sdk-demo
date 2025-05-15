package net.polyv.android.player.demo.activity

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory
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
import net.polyv.android.player.common.ui.router.RouterDestination.SceneFeed
import net.polyv.android.player.common.ui.router.RouterPayload.SceneFeedPayload
import net.polyv.android.player.common.ui.router.RouterPayloadStaticHolder
import net.polyv.android.player.common.utils.orientation.PLVActivityOrientationManager
import net.polyv.android.player.demo.mock.PLVMockFeedVideoDataViewModel
import net.polyv.android.player.scenes.feed.PLVMediaPlayerFeedVideoLayout
import net.polyv.android.player.scenes.feed.viewmodel.IPLVMediaPlayerFeedVideoDataViewModel
import net.polyv.android.player.sdk.PLVDeviceManager.hideNavigationBar
import net.polyv.android.player.sdk.PLVDeviceManager.hideStatusBar
import net.polyv.android.player.sdk.PLVDeviceManager.setTransparentStatusBar
import net.polyv.android.player.sdk.PLVDeviceManager.showNavigationBar
import net.polyv.android.player.sdk.foundation.collections.listOf
import net.polyv.android.player.sdk.foundation.graphics.isPortrait

/**
 * @author Hoshiiro
 */
private const val TAG = "PLVMediaPlayerFeedVideoActivity"

// 禁止录屏、截图
private const val FLAG_SECURE_WINDOW: Boolean = false

class PLVMediaPlayerFeedVideoActivity : AppCompatActivity() {
    // 实际的业务布局
    private var contentLayout: PLVMediaPlayerFeedVideoLayout? = null

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
        initFeedVideoLayout()

        // 设置屏幕方向监听
        setupOrientationManager()

        // 设置悬浮窗监听
        setFloatWindowListener()
    }

    private fun initActivity() {
        contentLayout = PLVMediaPlayerFeedVideoLayout(this)
        setContentView(contentLayout)
    }

    private fun initFeedVideoLayout() {
        // 模拟 生成Feed流容器的数据的请求实现类，客户集成式，需要替换的
        val feedVideoDataViewModel = mock()

        // 把 Feed流容器的数据的请求实现类 传入Feed流布局，以备后续Feed流容器获取更新的数据
        contentLayout!!.init(feedVideoDataViewModel)

        // 获取传入的第一页数据
        val bundle = intent.extras
        if (bundle != null) {
            val holderId = bundle.getInt(PLVMediaPlayerRouter.KEY_TARGET_MEDIA_RESOURCE_LIST_HOLDER_ID, 0)
            val holder = RouterPayloadStaticHolder.remove<List<PLVMediaResource>>(holderId)
            if (holder != null) {
                contentLayout!!.setTargetMediaResource(holder.value)
            }
        }
    }

    private fun mock(): IPLVMediaPlayerFeedVideoDataViewModel {
        return ViewModelProvider(this, NewInstanceFactory())
            .get(PLVMockFeedVideoDataViewModel::class.java)
    }

    // <editor-fold defaultstate="collapsed" desc="页面-横竖屏切换">
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateWindowInsets()
    }

    private fun updateWindowInsets() {
        if (isPortrait()) {
            setTransparentStatusBar(window)
            showNavigationBar(this)
        } else {
            hideStatusBar(this)
            hideNavigationBar(this)
        }
    }

    private fun setupOrientationManager() {
        PLVActivityOrientationManager.on(this).start()
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
                        SceneFeed(
                            SceneFeedPayload(
                                RouterPayloadStaticHolder.create(listOf(mediaResource))
                            )
                        )
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
