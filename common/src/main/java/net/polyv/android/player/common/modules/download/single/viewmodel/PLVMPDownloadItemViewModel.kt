package net.polyv.android.player.common.modules.download.single.viewmodel

import net.polyv.android.player.common.modules.download.single.model.PLVMPDownloadItemRepo
import net.polyv.android.player.common.modules.download.single.viewmodel.usecase.PLVMPDownloadItemUseCases
import net.polyv.android.player.sdk.addon.download.PLVMediaDownloaderManager

/**
 * @author Hoshiiro
 */
class PLVMPDownloadItemViewModel internal constructor(
    private val repo: PLVMPDownloadItemRepo,
    private val useCases: PLVMPDownloadItemUseCases
) {

    val downloadItem = repo.mediator.downloadItem

    fun startDownload() {
        val downloader = repo.mediator.downloadItem.value?.downloader ?: return
        PLVMediaDownloaderManager.startDownloader(downloader)
    }

    fun pauseDownload() {
        val downloader = repo.mediator.downloadItem.value?.downloader ?: return
        PLVMediaDownloaderManager.pauseDownloader(downloader)
    }

    fun setDownloadActionVisible(isVisible: Boolean) {
        useCases.downloadItemUpdateStateUseCase.setDownloadActionVisible(isVisible)
    }

}