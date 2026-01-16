package net.polyv.android.player.common.modules.danmu.viewmodel.viewstate

import android.graphics.Color
import net.polyv.android.player.sdk.addon.business.danmu.vo.PLVMediaPlayerDanmuMode

/**
 * @author Hoshiiro
 */
data class PLVMPDanmuStyleViewState(
    val color: Int = Color.WHITE,
    val size: PLVMPDanmuSize = PLVMPDanmuSize.MEDIUM,
    val mode: PLVMediaPlayerDanmuMode = PLVMediaPlayerDanmuMode.ROLL_RIGHT_TO_LEFT,
)

enum class PLVMPDanmuSize(
    val value: Int
) {
    SMALL(16),
    MEDIUM(18),
    LARGE(24)
}