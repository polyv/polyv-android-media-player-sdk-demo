package net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate

/**
 * @author Hoshiiro
 */
data class PLVMPMediaControllerViewState(
    val controllerVisible: Boolean = true,
    val controllerLocking: Boolean = false,
    val progressSeekBarDragging: Boolean = false,
    val progressSeekBarDragPosition: Long = 0,
    val progressSeekBarWaitSeekFinish: Boolean = false,
    val longPressSpeeding: Boolean = false,
    val speedBeforeLongPress: Float = 1F,
    val floatActionLayouts: List<PLVMPMediaControllerFloatAction> = emptyList(),
    val errorOverlayLayoutVisible: Boolean = false,
    val completeOverlayLayoutVisible: Boolean = false
) {
    val isFloatActionLayoutVisible: Boolean
        get() = floatActionLayouts.isNotEmpty()

    val isMediaStopOverlayVisible: Boolean
        get() = errorOverlayLayoutVisible || completeOverlayLayoutVisible

    val lastFloatActionLayout: PLVMPMediaControllerFloatAction? = floatActionLayouts.lastOrNull()
}

enum class PLVMPMediaControllerFloatAction {
    MORE,
    BITRATE,
    SPEED,
    SUBTITLE
}