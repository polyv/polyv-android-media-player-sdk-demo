package net.polyv.android.player.common.modules.auxiliary.viewmodel.viewstate

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaPlayStage
import net.polyv.android.player.sdk.foundation.lang.Duration
import net.polyv.android.player.sdk.foundation.lang.Duration.Companion.seconds

/**
 * @author Hoshiiro
 */
data class PLVMPAuxiliaryInfoViewState(
    val url: String = "",
    val isImage: Boolean = false,
    val clickNavigationUrl: String? = null,
    val showDuration: Duration = 0.seconds(),
    val canSkip: Boolean = false,
    val beforeSkipDuration: Duration? = null,
    val stage: PLVMediaPlayStage = PLVMediaPlayStage.HEAD_ADVERT
)