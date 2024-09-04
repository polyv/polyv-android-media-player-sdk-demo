package net.polyv.android.player.common.modules.media.viewmodel.viewstate

import android.graphics.Color
import android.graphics.Rect
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaSubtitle
import net.polyv.android.player.sdk.foundation.lang.Duration

/**
 * @author Hoshiiro
 */
data class PLVMPMediaInfoViewState(
    val title: String = "",
    val videoSize: Rect = Rect(),
    val bitRate: PLVMediaBitRate? = null,
    val supportBitRates: List<PLVMediaBitRate> = emptyList(),
    val outputMode: PLVMediaOutputMode = PLVMediaOutputMode.AUDIO_VIDEO,
    val supportOutputModes: List<PLVMediaOutputMode> = emptyList(),
    val currentSubtitle: List<PLVMediaSubtitle>? = null,
    val supportSubtitles: List<List<PLVMediaSubtitle>> = emptyList(),
    val progressPreviewImage: String? = null,
    val progressPreviewImageInterval: Duration? = null,
    val audioModeCoverImage: String? = null,
    val topSubtitleTextStyle: PLVMPSubtitleTextStyle = PLVMPSubtitleTextStyle(),
    val bottomSubtitleTextStyle: PLVMPSubtitleTextStyle = PLVMPSubtitleTextStyle()
)

data class PLVMPSubtitleTextStyle(
    val fontColor: Int = Color.WHITE,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val backgroundColor: Int = Color.BLACK
)