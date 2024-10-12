package net.polyv.android.player.common.modules.download.list.usecase

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate
import net.polyv.android.player.common.modules.download.list.model.PLVMPDownloadListRepo
import net.polyv.android.player.common.modules.download.list.viewstate.PLVMPDownloadListItemViewState
import net.polyv.android.player.common.modules.download.list.viewstate.PLVMPDownloadListViewState
import net.polyv.android.player.sdk.addon.download.PLVMediaDownloaderManager
import net.polyv.android.player.sdk.addon.download.common.PLVMediaDownloader
import net.polyv.android.player.sdk.addon.download.common.model.vo.PLVMediaDownloadStatus
import net.polyv.android.player.sdk.foundation.lang.DerivedState
import net.polyv.android.player.sdk.foundation.lang.Duration.Companion.seconds
import net.polyv.android.player.sdk.foundation.lang.MutableObserver
import net.polyv.android.player.sdk.foundation.lang.MutableObserver.Companion.disposeAll
import net.polyv.android.player.sdk.foundation.lang.State

/**
 * @author Hoshiiro
 */
internal class DownloadUpdateListUseCase(
    private val repo: PLVMPDownloadListRepo
) {

    private val observersAnyDownloaderStatusChanged = mutableListOf<MutableObserver<*>>()

    init {
        PLVMediaDownloaderManager.downloaderList.observe {
            updateDownloadList()
        }
    }

    private fun updateDownloadList() {
        val list = PLVMediaDownloaderManager.downloaderList.value ?: emptyList()

        observersAnyDownloaderStatusChanged.disposeAll()
        observersAnyDownloaderStatusChanged.clear()

        list.forEach { downloader ->
            val downloading = downloader.isDownloading()
            val downloadCompleted = downloader.isDownloadCompleted()
            downloader.listenerRegistry.status.observe {
                if (downloading != downloader.isDownloading() || downloadCompleted != downloader.isDownloadCompleted()) {
                    updateDownloadList()
                }
            }.addTo(observersAnyDownloaderStatusChanged)
        }

        val downloading = list
            .filter { it.isDownloading() }
            .map { it.asDownloadItemState() }

        val downloaded = list
            .filter { it.isDownloadCompleted() }
            .map { it.asDownloadItemState() }

        repo.mediator.downloadingList.setValue(PLVMPDownloadListViewState(downloading))
        repo.mediator.downloadedList.setValue(PLVMPDownloadListViewState(downloaded))
    }

    private fun PLVMediaDownloader.isDownloading(): Boolean {
        val status = this.listenerRegistry.status.value ?: PLVMediaDownloadStatus.NOT_STARTED
        return status in listOf(
            PLVMediaDownloadStatus.PAUSED,
            PLVMediaDownloadStatus.WAITING,
            PLVMediaDownloadStatus.DOWNLOADING
        ) || status is PLVMediaDownloadStatus.ERROR
    }

    private fun PLVMediaDownloader.isDownloadCompleted(): Boolean {
        val status = this.listenerRegistry.status.value ?: PLVMediaDownloadStatus.NOT_STARTED
        return status == PLVMediaDownloadStatus.COMPLETED
    }

    private fun PLVMediaDownloader.asDownloadItemState(): State<PLVMPDownloadListItemViewState> = DerivedState {
        PLVMPDownloadListItemViewState(
            this,
            this.listenerRegistry.vodVideoJson.value?.title ?: "",
            this.listenerRegistry.coverImage.value,
            this.listenerRegistry.downloadBitRate.value ?: PLVMediaBitRate.BITRATE_UNKNOWN,
            this.listenerRegistry.duration.value ?: 0.seconds(),
            this.listenerRegistry.progress.value ?: 0F,
            this.listenerRegistry.fileSize.value ?: 0,
            this.listenerRegistry.status.value ?: PLVMediaDownloadStatus.PAUSED,
            this.listenerRegistry.downloadBytesPerSecond.value ?: 0
        )
    }

}