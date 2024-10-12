package net.polyv.android.player.common.modules.download.single.mediator

import net.polyv.android.player.common.modules.download.single.viewmodel.viewstate.PLVMPDownloadItemViewState
import net.polyv.android.player.sdk.foundation.di.LifecycleAwareDependComponent
import net.polyv.android.player.sdk.foundation.lang.MutableSource
import net.polyv.android.player.sdk.foundation.lang.MutableState

/**
 * @author Hoshiiro
 */
internal class PLVMPDownloadItemMediator : LifecycleAwareDependComponent {

    val downloadItem = MutableState<PLVMPDownloadItemViewState?>(null)

    override fun onDestroy() {
        MutableSource.disposeAllSource(this)
    }

}