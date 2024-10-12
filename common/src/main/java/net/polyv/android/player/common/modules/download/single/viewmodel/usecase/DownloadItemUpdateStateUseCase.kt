package net.polyv.android.player.common.modules.download.single.viewmodel.usecase

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate
import net.polyv.android.player.common.modules.download.single.model.PLVMPDownloadItemRepo
import net.polyv.android.player.common.modules.download.single.viewmodel.viewstate.PLVMPDownloadItemViewState
import net.polyv.android.player.sdk.addon.download.PLVMediaDownloaderManager
import net.polyv.android.player.sdk.addon.download.common.model.vo.PLVMediaDownloadStatus
import net.polyv.android.player.sdk.foundation.di.LifecycleAwareDependComponent
import net.polyv.android.player.sdk.foundation.lang.DerivedState
import net.polyv.android.player.sdk.foundation.lang.MutableObserver
import net.polyv.android.player.sdk.foundation.lang.MutableObserver.Companion.disposeAll
import net.polyv.android.player.sdk.foundation.lang.MutableState

/**
 * @author Hoshiiro
 */
internal class DownloadItemUpdateStateUseCase(
    private val repo: PLVMPDownloadItemRepo
) : LifecycleAwareDependComponent {

    private val observers = mutableListOf<MutableObserver<*>>()

    private val downloadActionVisibleState = MutableState<Boolean>(true)

    init {
        DerivedState {
            val mediaResource = repo.mediaMediator.mediaResource.value ?: return@DerivedState null
            val bitRate = repo.mediaMediator.mediaInfoViewState.value?.bitRate ?: PLVMediaBitRate.BITRATE_AUTO
            val downloader = PLVMediaDownloaderManager.getDownloader(mediaResource, bitRate)
            PLVMPDownloadItemViewState(
                downloader,
                downloader.listenerRegistry.progress.value ?: 0F,
                downloader.listenerRegistry.fileSize.value ?: 0,
                downloader.listenerRegistry.status.value ?: PLVMediaDownloadStatus.NOT_STARTED,
                downloadActionVisibleState.value ?: true
            )
        }.relayTo(repo.mediator.downloadItem)
            .addTo(observers)
    }

    fun setDownloadActionVisible(isVisible: Boolean) {
        downloadActionVisibleState.setValue(isVisible)
    }

    override fun onDestroy() {
        observers.disposeAll()
        observers.clear()
    }

}