package net.polyv.android.player.common.modules.download.single.di

import net.polyv.android.player.common.modules.download.single.mediator.PLVMPDownloadItemMediator
import net.polyv.android.player.common.modules.download.single.model.PLVMPDownloadItemRepo
import net.polyv.android.player.common.modules.download.single.viewmodel.PLVMPDownloadItemViewModel
import net.polyv.android.player.common.modules.download.single.viewmodel.usecase.DownloadItemUpdateStateUseCase
import net.polyv.android.player.common.modules.download.single.viewmodel.usecase.PLVMPDownloadItemUseCases
import net.polyv.android.player.sdk.foundation.di.dependModule
import net.polyv.android.player.sdk.foundation.di.get

/**
 * @author Hoshiiro
 */
val downloadItemModule = dependModule {
    provide { PLVMPDownloadItemMediator() }

    provide { PLVMPDownloadItemRepo(get(), get()) }

    provide { DownloadItemUpdateStateUseCase(get()) }
    provide { PLVMPDownloadItemUseCases(get()) }

    provide { PLVMPDownloadItemViewModel(get(), get()) }
}