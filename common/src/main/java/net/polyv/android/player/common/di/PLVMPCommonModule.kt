package net.polyv.android.player.common.di

import net.polyv.android.player.common.modules.auxiliary.di.auxiliaryModule
import net.polyv.android.player.common.modules.download.single.di.downloadItemModule
import net.polyv.android.player.common.modules.media.di.mediaModule
import net.polyv.android.player.common.modules.mediacontroller.di.mediaControllerModule
import net.polyv.android.player.sdk.foundation.di.dependModule

/**
 * @author Hoshiiro
 */
@JvmField
val commonItemModule = dependModule {
    include(mediaModule)
    include(mediaControllerModule)
    include(auxiliaryModule)
    include(downloadItemModule)
}