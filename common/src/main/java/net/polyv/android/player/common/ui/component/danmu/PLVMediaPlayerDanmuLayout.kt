package net.polyv.android.player.common.ui.component.danmu

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import net.polyv.android.common.libs.lang.graphic.dp
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.danmu.viewmodel.PLVMPDanmuViewModel
import net.polyv.android.player.common.modules.danmu.viewmodel.viewstate.PLVMPDanmuSize
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.sdk.addon.business.danmu.vo.PLVMediaPlayerDanmuData
import net.polyv.android.player.sdk.addon.business.danmu.vo.PLVMediaPlayerDanmuMode
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState.Companion.rememberStateOf
import net.polyv.danmaku.controller.DrawHandler
import net.polyv.danmaku.danmaku.model.BaseDanmaku
import net.polyv.danmaku.danmaku.model.DanmakuTimer
import net.polyv.danmaku.danmaku.model.IDanmakus
import net.polyv.danmaku.danmaku.model.IDisplayer
import net.polyv.danmaku.danmaku.model.android.DanmakuContext
import net.polyv.danmaku.danmaku.model.android.Danmakus
import net.polyv.danmaku.danmaku.parser.BaseDanmakuParser
import net.polyv.danmaku.ui.widget.DanmakuView

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerDanmuLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val danmuView = DanmakuView(context)
    private val danmuContext = DanmakuContext.create()

    private var danmuList = emptyList<PLVMediaPlayerDanmuData>()
    private var lastDanmuProgress: Long? = null

    init {
        addView(danmuView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        danmuContext.apply {
            setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 2F)
            setDuplicateMergingEnabled(false)
            setScrollSpeedFactor(1.6F)
            setScaleTextSize(1.3F)
            setMaximumLines(
                mapOf(
                    BaseDanmaku.TYPE_SCROLL_RL to 6,
                    BaseDanmaku.TYPE_FIX_TOP to 6,
                    BaseDanmaku.TYPE_FIX_BOTTOM to 2,
                )
            )
            preventOverlapping(
                mapOf(
                    BaseDanmaku.TYPE_SCROLL_RL to true,
                    BaseDanmaku.TYPE_FIX_TOP to true,
                    BaseDanmaku.TYPE_FIX_BOTTOM to true,
                )
            )
            val frameRate = (context as Activity).windowManager.defaultDisplay.refreshRate
            setFrameUpateRate((1000 / frameRate).toInt())
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        prepareDanmuView()

        val dependScope = PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
        val mediaViewModel = dependScope.get<PLVMPMediaViewModel>()
        val danmuViewModel = dependScope.get<PLVMPDanmuViewModel>()

        danmuViewModel.danmuList
            .observe { danmuList ->
                this.danmuList = danmuList
            }
            .disposeOnDetached(this)

        danmuViewModel.onDanmuListRefresh
            .observe { danmuList ->
                danmuView.removeAllDanmakus(true)
                this.danmuList = danmuList
            }
            .disposeOnDetached(this)

        mediaViewModel.mediaPlayViewState
            .observe { viewState ->
                val progress = viewState.currentProgress
                val lastProgress = lastDanmuProgress
                if (lastProgress != null) {
                    addDanmuInRange(lastProgress.coerceAtLeast(progress - 2000), progress)
                }
                lastDanmuProgress = progress
            }
            .disposeOnDetached(this)

        mediaViewModel.mediaPlayViewState
            .observe { viewState ->
                val isPlaying = viewState.isPlaying
                rememberStateOf("updateDanmuViewPlaying")
                    .compareLastAndSet(isPlaying)
                    .ifNotEquals {
                        if (isPlaying) {
                            danmuView.resume()
                        } else {
                            danmuView.pause()
                        }
                    }
            }
            .disposeOnDetached(this)

        mediaViewModel.onSeekCompleteEvent
            .observe {
                danmuView.removeAllDanmakus(true)
                lastDanmuProgress = null
            }
            .disposeOnDetached(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        releaseDanmuView()
        danmuList = emptyList()
        lastDanmuProgress = null
    }

    private fun prepareDanmuView() {
        danmuView.apply {
            setCallback(object : DrawHandler.Callback {
                override fun prepared() {
                    danmuView.start()
                }

                override fun updateTimer(timer: DanmakuTimer?) {
                }

                override fun danmakuShown(danmaku: BaseDanmaku?) {
                }

                override fun drawingFinished() {
                }
            })

            prepare(object : BaseDanmakuParser() {
                override fun parse(): IDanmakus {
                    return Danmakus()
                }
            }, danmuContext)
        }
    }

    private fun releaseDanmuView() {
        danmuView.release()
    }

    private fun addDanmuInRange(min: Long, max: Long) {
        if (min >= max) return
        danmuList
            .filter {
                val time = it.timestamp?.toMillis() ?: 0L
                time in min until max
            }
            .sortedBy { it.timestamp }
            .forEach { addDanmu(it) }
    }

    private fun addDanmu(danmu: PLVMediaPlayerDanmuData) {
        val item = danmu.toDanmakuItem()
        item.priority = 0
        item.time = danmuView.currentTime
        danmuView.addDanmaku(item)
    }

    private fun PLVMediaPlayerDanmuData.toDanmakuItem(): BaseDanmaku {
        val type = when (mode) {
            PLVMediaPlayerDanmuMode.ROLL_RIGHT_TO_LEFT -> 1
            PLVMediaPlayerDanmuMode.FIX_TOP -> 5
            PLVMediaPlayerDanmuMode.FIX_BOTTOM -> 4
            else -> 1
        }
        val item = danmuContext.mDanmakuFactory.createDanmaku(type, danmuContext).apply {
            flags = danmuContext.mGlobalFlagValues
            text = content
            time = timestamp?.toMillis() ?: 0
            textSize = (size?.px() ?: PLVMPDanmuSize.MEDIUM.value.dp().px()).toFloat()
            textColor = color ?: Color.WHITE
            textShadowColor = if (color == Color.BLACK) Color.WHITE else Color.BLACK
        }
        return item
    }

}