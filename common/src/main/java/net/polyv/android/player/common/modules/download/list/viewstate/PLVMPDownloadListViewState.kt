package net.polyv.android.player.common.modules.download.list.viewstate

import androidx.annotation.FloatRange
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate
import net.polyv.android.player.sdk.addon.download.PLVMediaDownloaderManager
import net.polyv.android.player.sdk.addon.download.common.PLVMediaDownloader
import net.polyv.android.player.sdk.addon.download.common.model.vo.PLVMediaDownloadStatus
import net.polyv.android.player.sdk.foundation.lang.Duration
import net.polyv.android.player.sdk.foundation.lang.State

/**
 * @author Hoshiiro
 */
data class PLVMPDownloadListViewState(
    val list: List<State<PLVMPDownloadListItemViewState>> = emptyList()
)

data class PLVMPDownloadListItemViewState(
    val downloader: PLVMediaDownloader,
    val title: String,
    val coverImage: String?,
    val bitRate: PLVMediaBitRate,
    val duration: Duration,
    @FloatRange(from = 0.0, to = 1.0)
    val progress: Float,
    // bytes
    val fileSize: Long,
    val downloadStatus: PLVMediaDownloadStatus,
    val downloadBytesPerSecond: Long
) {
    fun startDownload() {
        PLVMediaDownloaderManager.startDownloader(downloader)
    }

    fun pauseDownload() {
        PLVMediaDownloaderManager.pauseDownloader(downloader)
    }

    fun deleteDownload() {
        PLVMediaDownloaderManager.deleteDownloadContent(downloader)
    }
}
