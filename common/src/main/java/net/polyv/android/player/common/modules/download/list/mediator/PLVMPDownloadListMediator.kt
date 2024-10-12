package net.polyv.android.player.common.modules.download.list.mediator

import net.polyv.android.player.common.modules.download.list.viewstate.PLVMPDownloadListViewState
import net.polyv.android.player.sdk.foundation.lang.MutableState

/**
 * @author Hoshiiro
 */
internal class PLVMPDownloadListMediator {

    val downloadedList = MutableState<PLVMPDownloadListViewState>()
    val downloadingList = MutableState<PLVMPDownloadListViewState>()

}