package net.polyv.android.player.demo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.Toast
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource
import net.polyv.android.player.common.ui.router.PLVMediaPlayerRouter.routerTo
import net.polyv.android.player.common.ui.router.RouterDestination.DownloadCenter
import net.polyv.android.player.common.ui.router.RouterDestination.SceneFeed
import net.polyv.android.player.common.ui.router.RouterDestination.SceneSingle
import net.polyv.android.player.common.ui.router.RouterPayload.DownloadCenterPayload
import net.polyv.android.player.common.ui.router.RouterPayload.SceneFeedPayload
import net.polyv.android.player.common.ui.router.RouterPayload.SceneSinglePayload
import net.polyv.android.player.common.ui.router.RouterPayloadStaticHolder.Companion.create
import net.polyv.android.player.common.utils.ui.PLVDebounceClicker
import net.polyv.android.player.demo.R
import net.polyv.android.player.demo.mock.PLVMockMediaResourceData
import net.polyv.android.player.sdk.PLVDeviceManager.setTransparentStatusBar
import kotlin.math.min

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerEntranceActivity : AppCompatActivity(), View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private val entranceFeedVideoBtn by lazy { findViewById<Button>(R.id.plv_media_player_entrance_feed_video_btn) }
    private val entranceSingleVideoBtn by lazy { findViewById<Button>(R.id.plv_media_player_entrance_single_video_btn) }
    private val entranceDownloadCenterBtn by lazy { findViewById<Button>(R.id.plv_media_player_entrance_download_center_btn) }
    // </editor-fold>

    init {
        // 提前初始化视频列表数据
        PLVMockMediaResourceData.getInstance()
    }

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTransparentStatusBar(window)
        setContentView(R.layout.plv_media_player_entrance_layout)

        entranceFeedVideoBtn.setOnClickListener(this)
        entranceSingleVideoBtn.setOnClickListener(this)
        entranceDownloadCenterBtn.setOnClickListener(this)
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    override fun onClick(v: View) {
        if (!PLVDebounceClicker.tryClick(this)) {
            return
        }
        when (v.id) {
            entranceFeedVideoBtn.id -> gotoFeedVideoActivity()
            entranceSingleVideoBtn.id -> gotoSingleVideoActivity()
            entranceDownloadCenterBtn.id -> gotoDownloadCenter()
            else -> {}
        }
    }
    // </editor-fold>

    private fun gotoFeedVideoActivity() {
        // mock data
        val sourceList = PLVMockMediaResourceData.getInstance().mediaResources
        if (sourceList.isNullOrEmpty()) {
            Toast.makeText(this, "视频数据未初始化", Toast.LENGTH_SHORT).show()
            return
        }
        val mediaResourceList: List<PLVMediaResource> = sourceList.subList(
            0,
            min(10.0, sourceList.size.toDouble()).toInt()
        )

        // goto Feed Video Activity
        routerTo(
            SceneFeed(
                SceneFeedPayload(
                    create(mediaResourceList)
                )
            )
        )
    }

    private fun gotoSingleVideoActivity() {
        // mock data
        val mediaResources = PLVMockMediaResourceData.getInstance().mediaResources
        if (mediaResources.isNullOrEmpty()) {
            Toast.makeText(this, "视频数据未初始化", Toast.LENGTH_SHORT).show()
            return
        }

        // goto Single Video Activity
        routerTo(
            SceneSingle(SceneSinglePayload(mediaResources[0]))
        )
    }

    private fun gotoDownloadCenter() {
        routerTo(DownloadCenter(DownloadCenterPayload()))
    }

}
