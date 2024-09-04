package net.polyv.android.player.common.modules.media.view

import android.content.Context
import net.polyv.android.player.common.modules.media.model.PLVMPMediaRepo
import net.polyv.android.player.sdk.PLVVideoView
import net.polyv.android.player.sdk.foundation.di.DependScope

/**
 * @author Hoshiiro
 */
class PLVMPVideoView(
    context: Context,
    dependScope: DependScope
) : PLVVideoView(
    context = context,
    mediaPlayer = dependScope.get<PLVMPMediaRepo>().player
)