package net.polyv.android.player.common.modules.media.model

import android.graphics.Bitmap
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaSubtitle
import net.polyv.android.player.core.api.option.PLVMediaPlayerOption
import net.polyv.android.player.core.api.render.IPLVMediaPlayerRenderView

/**
 * @author Hoshiiro
 */
interface IPLVMPMediaPlayer {

    fun setMediaResource(mediaResource: PLVMediaResource)

    fun setRenderView(renderView: IPLVMediaPlayerRenderView)

    fun setAutoContinue(autoContinue: Boolean)

    fun setPlayerOption(options: List<PLVMediaPlayerOption>)

    fun start()

    fun pause()

    fun seekTo(position: Long)

    fun restart()

    fun setSpeed(speed: Float)

    fun setVolume(volume: Int)

    fun changeBitRate(bitRate: PLVMediaBitRate)

    fun changeMediaOutputMode(outputMode: PLVMediaOutputMode)

    fun setShowSubtitles(subtitles: List<PLVMediaSubtitle>)

    fun screenshot(): Bitmap?

}