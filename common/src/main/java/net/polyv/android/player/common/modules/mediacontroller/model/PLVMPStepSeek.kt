package net.polyv.android.player.common.modules.mediacontroller.model

/** 使用上一次尚未完成的目标累加，避免快速连点总是从旧播放进度计算。 */
internal fun stepSeekTarget(current: Long, pending: Long?, duration: Long, seconds: Int, forward: Boolean): Long {
    val step = (if (seconds in 5..60) seconds else 15) * 1000L
    val end = duration.coerceAtLeast(0L)
    val base = (pending ?: current).coerceIn(0L, end)
    return if (forward) base + minOf(step, end - base) else (base - step).coerceAtLeast(0L)
}
