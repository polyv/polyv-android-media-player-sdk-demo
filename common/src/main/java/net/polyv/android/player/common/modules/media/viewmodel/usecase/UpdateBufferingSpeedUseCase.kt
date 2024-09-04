package net.polyv.android.player.common.modules.media.viewmodel.usecase

import net.polyv.android.player.common.modules.media.model.PLVMPMediaRepo
import net.polyv.android.player.sdk.foundation.di.LifecycleAwareDependComponent
import net.polyv.android.player.sdk.foundation.lang.Duration.Companion.millis
import net.polyv.android.player.sdk.foundation.lang.PLVTimer
import net.polyv.android.player.sdk.foundation.lang.timer

/**
 * @author Hoshiiro
 */
class UpdateBufferingSpeedUseCase internal constructor(
    private val repo: PLVMPMediaRepo
) : LifecycleAwareDependComponent {

    private var trafficSpeedTimer: PLVTimer? = null
    private var lastTrafficCount: Long = 0
    private var lastTrafficTimestamp: Long = 0

    init {
        observePlayerTrafficSpeed()
    }

    private fun observePlayerTrafficSpeed() {
        trafficSpeedTimer?.cancel()
        trafficSpeedTimer = timer(interval = 500.millis()) {
            if (lastTrafficTimestamp == 0L) {
                lastTrafficCount = this.repo.player.getTrafficStatisticByteCount() ?: 0L
                lastTrafficTimestamp = System.currentTimeMillis()
                return@timer
            }
            val newTrafficCount = this.repo.player.getTrafficStatisticByteCount() ?: 0L
            val newTrafficTimestamp = System.currentTimeMillis()
            val diffCount = newTrafficCount - lastTrafficCount
            val duration = newTrafficTimestamp - lastTrafficTimestamp
            val speed = diffCount.toDouble() / duration * 1000

            this.lastTrafficCount = newTrafficCount
            this.lastTrafficTimestamp = newTrafficTimestamp
            this.repo.mediator.bufferingSpeed.setValue(speed)
        }
    }

    override fun onDestroy() {
        trafficSpeedTimer?.cancel()
    }

}