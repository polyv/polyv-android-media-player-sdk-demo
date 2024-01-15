package net.polyv.android.player.common.ui.component;

import net.polyv.android.player.business.scene.auxiliary.listener.callback.IPLVAuxiliaryOnBeforeAdvertListener;
import net.polyv.android.player.business.scene.auxiliary.model.vo.PLVAdvertMediaDataSource;
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaPlayStage;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerAuxiliaryBeforePlayListener implements IPLVAuxiliaryOnBeforeAdvertListener {

    private final Set<String> playedAdvertIds = new HashSet<>();
    private boolean isEnterFromFloatWindow = false;

    @Override
    public boolean onBeforeAdvert(@NotNull PLVAdvertMediaDataSource dataSource, @NotNull PLVMediaPlayStage stage) {
        boolean playAdvert = true;

        // 已播放过的片头片尾广告，不再播放
        if (stage == PLVMediaPlayStage.HEAD_ADVERT || stage == PLVMediaPlayStage.TAIL_ADVERT) {
            if (playedAdvertIds.contains(dataSource.getAdvertId())) {
                playAdvert = false;
            } else {
                playedAdvertIds.add(dataSource.getAdvertId());
            }
        }

        if (isEnterFromFloatWindow) {
            // 从小窗进入，不播放片头
            if (stage.ordinal() < PLVMediaPlayStage.MAIN_CONTENT.ordinal()) {
                playAdvert = false;
            }
            // 重播时需要播放片头
            if (stage == PLVMediaPlayStage.PLAYER_OPENING || stage.ordinal() > PLVMediaPlayStage.MAIN_CONTENT.ordinal()) {
                isEnterFromFloatWindow = false;
            }
        }

        return playAdvert;
    }

    public void setEnterFromFloatWindow(boolean isEnterFromFloatWindow) {
        this.isEnterFromFloatWindow = isEnterFromFloatWindow;
    }

}
