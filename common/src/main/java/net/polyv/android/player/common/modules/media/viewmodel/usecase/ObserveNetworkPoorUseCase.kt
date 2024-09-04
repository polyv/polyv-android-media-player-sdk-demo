package net.polyv.android.player.common.modules.media.viewmodel.usecase

import net.polyv.android.player.common.modules.media.model.PLVMPMediaRepo
import net.polyv.android.player.core.api.listener.event.PLVMediaPlayerOnInfoEvent
import net.polyv.android.player.sdk.foundation.di.LifecycleAwareDependComponent
import net.polyv.android.player.sdk.foundation.lang.Duration.Companion.seconds
import net.polyv.android.player.sdk.foundation.lang.MutableObserver
import net.polyv.android.player.sdk.foundation.lang.MutableObserver.Companion.disposeAll
import net.polyv.android.player.sdk.foundation.lang.postToMainThread

/**
 * @author Hoshiiro
 */

// <editor-fold defaultstate="collapsed" desc="常量">

// 计时：5秒卡顿进行一次提示
private val INDICATE_BUFFERING_TIMEOUT = 5.seconds()

// 计次：计算10秒内卡顿次数
private val INDICATE_COUNT_BUFFERING_DURATION = 10.seconds()

// 计次：2次卡顿进行一次提示
private const val INDICATE_BUFFERING_COUNT_TOO_MORE_THRESHOLD = 2

// </editor-fold>

class ObserveNetworkPoorUseCase internal constructor(
    private val repo: PLVMPMediaRepo
) : LifecycleAwareDependComponent {

    private var bufferingEvents: MutableList<BufferingEventVO> = mutableListOf()
    private var lastSeekTimestamp: Long = 0
    private var isIndicatedNetworkPoor: Boolean = false
    private val observers = mutableListOf<MutableObserver<*>>()

    init {
        observerPlayerEvent()
    }

    private fun observerPlayerEvent() {
        repo.player.getEventListenerRegistry().onInfo.observe {
            when (it.what) {
                PLVMediaPlayerOnInfoEvent.MEDIA_INFO_BUFFERING_START -> onBufferingStart()
                PLVMediaPlayerOnInfoEvent.MEDIA_INFO_BUFFERING_END -> onBufferingEnd()
                else -> {}
            }
        }.addTo(this.observers)
        repo.player.getEventListenerRegistry().onSeekStartEvent.observe {
            onSeekStart()
        }.addTo(this.observers)
        repo.player.getEventListenerRegistry().onPrepared.observe {
            reset()
        }.addTo(this.observers)
    }

    private fun onBufferingStart() {
        bufferingEvents.add(
            BufferingEventVO(
                bySeek = System.currentTimeMillis() - lastSeekTimestamp < 500
            )
        )

        checkToShowIndicate()
        postToMainThread(delay = INDICATE_BUFFERING_TIMEOUT) {
            checkToShowIndicate()
        }
    }

    private fun onBufferingEnd() {
        val event = bufferingEvents.lastOrNull() ?: return
        event.endTimestamp = System.currentTimeMillis()
    }

    private fun onSeekStart() {
        lastSeekTimestamp = System.currentTimeMillis()
    }

    private fun reset() {
        bufferingEvents.clear()
        lastSeekTimestamp = 0
        isIndicatedNetworkPoor = false
    }

    private fun checkToShowIndicate() {
        dropExpireBufferingCache()
        val isNetworkPoor = checkBufferTooLong() || checkBufferTooMore()
        if (isNetworkPoor && !isIndicatedNetworkPoor) {
            repo.mediator.networkPoorEvent.setValue(System.currentTimeMillis())
            isIndicatedNetworkPoor = true
        }
    }

    private fun dropExpireBufferingCache() {
        this.bufferingEvents = bufferingEvents
            .filter {
                it.endTimestamp < 0 || it.startTimestamp > System.currentTimeMillis() - INDICATE_COUNT_BUFFERING_DURATION.toMillis()
            }
            .toMutableList()
    }

    private fun checkBufferTooLong(): Boolean {
        val event = bufferingEvents.lastOrNull() ?: return false
        return if (event.endTimestamp < 0) {
            System.currentTimeMillis() - event.startTimestamp > INDICATE_BUFFERING_TIMEOUT.toMillis()
        } else {
            event.endTimestamp - event.startTimestamp > INDICATE_BUFFERING_TIMEOUT.toMillis()
        }
    }

    private fun checkBufferTooMore(): Boolean {
        return bufferingEvents
            .filter { !it.bySeek }
            .size >= INDICATE_BUFFERING_COUNT_TOO_MORE_THRESHOLD
    }

    override fun onDestroy() {
        this.observers.disposeAll()
        this.observers.clear()
    }

}

private data class BufferingEventVO(
    var startTimestamp: Long = System.currentTimeMillis(),
    var endTimestamp: Long = -1,
    var bySeek: Boolean = false
)