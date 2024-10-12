package net.polyv.android.player.common.modules.download.list.di

import net.polyv.android.player.common.modules.download.list.mediator.PLVMPDownloadListMediator
import net.polyv.android.player.common.modules.download.list.model.PLVMPDownloadListRepo
import net.polyv.android.player.common.modules.download.list.usecase.DownloadUpdateListUseCase
import net.polyv.android.player.common.modules.download.list.usecase.PLVMPDownloadListUseCases
import net.polyv.android.player.sdk.foundation.di.dependModule
import net.polyv.android.player.sdk.foundation.di.get

/**
 * @author Hoshiiro
 */
internal val internalDownloadListModule = dependModule {
    provide { PLVMPDownloadListMediator() }

    provide { PLVMPDownloadListRepo(get()) }

    provide { DownloadUpdateListUseCase(get()) }
    provide { PLVMPDownloadListUseCases(get()) }
}