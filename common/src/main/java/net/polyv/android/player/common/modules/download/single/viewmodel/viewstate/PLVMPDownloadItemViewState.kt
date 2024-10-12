package net.polyv.android.player.common.modules.download.single.viewmodel.viewstate

import net.polyv.android.player.sdk.addon.download.common.PLVMediaDownloader
import net.polyv.android.player.sdk.addon.download.common.model.vo.PLVMediaDownloadStatus

/**
 * @author Hoshiiro
 */
data class PLVMPDownloadItemViewState(
    val downloader: PLVMediaDownloader,
    val progress: Float,
    val fileSize: Long,
    val status: PLVMediaDownloadStatus,
    val isVisible: Boolean
)