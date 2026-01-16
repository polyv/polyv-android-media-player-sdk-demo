package net.polyv.android.player.common.modules.danmu.viewmodel

import android.graphics.Color
import androidx.annotation.ColorInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.polyv.android.common.libs.lang.Duration.Companion.millis
import net.polyv.android.common.libs.lang.di.LifecycleAwareDependComponent
import net.polyv.android.common.libs.lang.graphic.dp
import net.polyv.android.common.libs.lang.state.MutableObserver
import net.polyv.android.common.libs.lang.state.MutableObserver.Companion.disposeAll
import net.polyv.android.common.libs.lang.state.mutableStateOf
import net.polyv.android.player.business.scene.common.coroutine.PLVMediaPlayerGlobalCoroutineScope
import net.polyv.android.player.common.modules.danmu.model.PLVMPDanmuRepo
import net.polyv.android.player.common.modules.danmu.viewmodel.viewstate.PLVMPDanmuSize
import net.polyv.android.player.common.modules.danmu.viewmodel.viewstate.PLVMPDanmuStyleViewState
import net.polyv.android.player.sdk.addon.business.danmu.vo.PLVMediaPlayerDanmuData
import net.polyv.android.player.sdk.addon.business.danmu.vo.PLVMediaPlayerDanmuMode
import net.polyv.android.player.sdk.foundation.lang.MutableEvent

/**
 * @author Hoshiiro
 */
class PLVMPDanmuViewModel(
    private val repo: PLVMPDanmuRepo
) : LifecycleAwareDependComponent {

    val danmuStyle = mutableStateOf(PLVMPDanmuStyleViewState())
    val danmuList = mutableStateOf<List<PLVMediaPlayerDanmuData>>(emptyList())
    val onDanmuListRefresh = MutableEvent<List<PLVMediaPlayerDanmuData>>()

    private val observers = mutableListOf<MutableObserver<*>>()

    init {
        repo.mediaMediator.mediaResource.observe { mediaResource ->
            PLVMediaPlayerGlobalCoroutineScope.launch(Dispatchers.IO) {
                val newDanmus = loadDanmu()
                danmuList.setValue(newDanmus)
                onDanmuListRefresh.setValue(newDanmus)
            }
        }.addTo(observers)
    }

    private suspend fun loadDanmu(): List<PLVMediaPlayerDanmuData> {
        return repo.danmuManager.loadDanmu()
    }

    suspend fun sendDanmu(content: String) {
        val progress = repo.mediaMediator.mediaPlayViewState.value?.currentProgress ?: 0
        val danmuData = PLVMediaPlayerDanmuData(
            null,
            content,
            progress.millis(),
            danmuStyle.value?.color ?: Color.WHITE,
            (danmuStyle.value?.size ?: PLVMPDanmuSize.MEDIUM).value.dp(),
            danmuStyle.value?.mode ?: PLVMediaPlayerDanmuMode.ROLL_RIGHT_TO_LEFT
        )
        repo.danmuManager.sendDanmu(danmu = danmuData)
        val newDanmus = (danmuList.value ?: emptyList()).toMutableList().apply { add(danmuData) }
        danmuList.setValue(newDanmus)
    }

    fun setDanmuColor(@ColorInt color: Int) {
        danmuStyle.setValue(
            (danmuStyle.value ?: PLVMPDanmuStyleViewState()).copy(color = color)
        )
    }

    fun setDanmuSize(size: PLVMPDanmuSize) {
        danmuStyle.setValue(
            (danmuStyle.value ?: PLVMPDanmuStyleViewState()).copy(size = size)
        )
    }

    fun setDanmuMode(mode: PLVMediaPlayerDanmuMode) {
        danmuStyle.setValue(
            (danmuStyle.value ?: PLVMPDanmuStyleViewState()).copy(mode = mode)
        )
    }

    override fun onDestroy() {
        observers.disposeAll()
    }

}