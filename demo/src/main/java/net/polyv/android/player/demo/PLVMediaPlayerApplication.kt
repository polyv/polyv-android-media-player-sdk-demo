package net.polyv.android.player.demo

import android.app.Application
import net.polyv.android.player.common.ui.router.PLVMediaPlayerRouter
import net.polyv.android.player.common.ui.router.RouterDestination
import net.polyv.android.player.demo.activity.PLVMediaPlayerEntranceActivity
import net.polyv.android.player.demo.activity.PLVMediaPlayerFeedVideoActivity
import net.polyv.android.player.demo.activity.PLVMediaPlayerSingleVideoActivity
import net.polyv.android.player.scenes.download.PLVMediaPlayerDownloadCenterActivity
import net.polyv.android.player.sdk.addon.download.PLVMediaDownloaderManager
import net.polyv.android.player.sdk.addon.download.common.model.vo.PLVMediaDownloadSetting
import net.polyv.android.player.sdk.addon.download.migrate.vod.PLVMediaDownloaderVodMigrate
import net.polyv.android.player.sdk.foundation.network.httpdns.PLVMediaPlayerHttpDns

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        PLVMediaPlayerHttpDns.enable = true

        // 初始化下载模块
        PLVMediaDownloaderManager.init(PLVMediaDownloadSetting.defaultSetting(applicationContext))
        // 如果在点播 SDK 下载的视频需要通过播放器 SDK 播放，需要调用 migrate 方法
        PLVMediaDownloaderVodMigrate.migrate(
            listOf(applicationContext.getExternalFilesDir(null)!!.resolve("polyvdownload").absolutePath)
        )

        PLVMediaPlayerRouter.apply {
            register<RouterDestination.Entrance>(PLVMediaPlayerEntranceActivity::class.java)
            register<RouterDestination.DownloadCenter>(PLVMediaPlayerDownloadCenterActivity::class.java)
            register<RouterDestination.SceneSingle>(PLVMediaPlayerSingleVideoActivity::class.java)
            register<RouterDestination.SceneFeed>(PLVMediaPlayerFeedVideoActivity::class.java)
        }
    }
}
