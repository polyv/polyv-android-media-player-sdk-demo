package net.polyv.android.player.common.modules.auxiliary.viewmodel.usecase

import net.polyv.android.player.business.scene.auxiliary.listener.callback.IPLVAuxiliaryOnBeforeAdvertListener
import net.polyv.android.player.business.scene.auxiliary.model.vo.PLVAdvertMediaDataSource
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaPlayStage
import net.polyv.android.player.common.modules.auxiliary.model.PLVMPAuxiliaryRepo

/**
 * @author Hoshiiro
 */
class AuxiliaryBeforePlayListener internal constructor(
    private val repo: PLVMPAuxiliaryRepo
) : IPLVAuxiliaryOnBeforeAdvertListener {

    private val playedAdvertIds = mutableSetOf<String>()
    private var isEnterFromFloatWindow = false

    init {
        repo.auxiliaryPlayer.getAuxiliaryListenerRegistry().onBeforeAdvertListener = this
    }

    override fun onBeforeAdvert(dataSource: PLVAdvertMediaDataSource, stage: PLVMediaPlayStage): Boolean {
        var playAdvert = true

        // 已播放过的片头片尾广告，不再播放
        if (stage == PLVMediaPlayStage.HEAD_ADVERT || stage == PLVMediaPlayStage.TAIL_ADVERT) {
            if (playedAdvertIds.contains(dataSource.advertId)) {
                playAdvert = false
            } else {
                playedAdvertIds.add(dataSource.advertId)
            }
        }

        if (isEnterFromFloatWindow) {
            // 从小窗进入，不播放片头
            if (stage < PLVMediaPlayStage.MAIN_CONTENT) {
                playAdvert = false
            }
            // 重播时需要播放片头
            if (stage == PLVMediaPlayStage.PLAYER_OPENING || stage > PLVMediaPlayStage.MAIN_CONTENT) {
                isEnterFromFloatWindow = false
            }
        }

        return playAdvert
    }

    fun setEnterFromFloatWindow(isEnterFromFloatWindow: Boolean) {
        this.isEnterFromFloatWindow = isEnterFromFloatWindow
    }

}