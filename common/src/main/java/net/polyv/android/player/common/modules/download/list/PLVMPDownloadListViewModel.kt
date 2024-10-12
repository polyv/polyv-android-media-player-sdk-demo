package net.polyv.android.player.common.modules.download.list

import net.polyv.android.player.common.modules.download.list.di.internalDownloadListModule
import net.polyv.android.player.common.modules.download.list.model.PLVMPDownloadListRepo
import net.polyv.android.player.common.modules.download.list.usecase.PLVMPDownloadListUseCases
import net.polyv.android.player.sdk.foundation.di.DependScope

/**
 * @author Hoshiiro
 */
object PLVMPDownloadListViewModel {

    private val dependScope = DependScope(internalDownloadListModule)
    private val repo = dependScope.get<PLVMPDownloadListRepo>()
    private val useCases = dependScope.get<PLVMPDownloadListUseCases>()

    val downloadingList = repo.mediator.downloadingList
    val downloadedList = repo.mediator.downloadedList

}