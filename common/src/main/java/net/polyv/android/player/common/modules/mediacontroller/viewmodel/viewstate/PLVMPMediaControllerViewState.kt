package net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate

/**
 * @author Hoshiiro
 */
data class PLVMPMediaControllerViewState(
    val controllerVisible: Boolean = false,
    val controllerLocking: Boolean = false,
    val progressSeekBarDragging: Boolean = false,
    val progressSeekBarDragPosition: Long = 0,
    val progressSeekBarWaitSeekFinish: Boolean = false,
    val mediaViewTranslation: PLVMPMediaViewTranslation = PLVMPMediaViewTranslation(),
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

data class PLVMPMediaViewTranslation(
    val offsetX: Float = 0F,
    val offsetY: Float = 0F,
    val rotateAngle: Float = 0F,
    val scale: Float = 1F,
) {
    companion object {
        val IDENTITY = PLVMPMediaViewTranslation()
    }

    fun axisAngle(): Float {
        val normalizedAngle = (rotateAngle % 360F + 360F) % 360F
        return when {
            normalizedAngle < 45 -> 0F
            normalizedAngle < 135 -> 90F
            normalizedAngle < 225 -> 180F
            normalizedAngle < 315 -> 270F
            else -> 0F
        }
    }
}

enum class PLVMPMediaControllerFloatAction {
    MORE,
    BITRATE,
    SPEED,
    SUBTITLE,
    KNOWLEDGE,
}