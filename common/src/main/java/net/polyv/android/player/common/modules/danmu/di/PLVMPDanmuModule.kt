package net.polyv.android.player.common.modules.danmu.di

import net.polyv.android.common.libs.lang.di.get
import net.polyv.android.player.common.modules.danmu.model.PLVMPDanmuRepo
import net.polyv.android.player.common.modules.danmu.viewmodel.PLVMPDanmuViewModel
import net.polyv.android.player.sdk.foundation.di.dependModule

/**
 * @author Hoshiiro
 */
internal val danmuModule = dependModule {
    provide { PLVMPDanmuRepo(get()) }
    provide { PLVMPDanmuViewModel(get()) }
}