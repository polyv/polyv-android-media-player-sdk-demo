package net.polyv.android.player.scenes.feed.viewmodel;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;
import net.polyv.android.player.common.utils.data.PLVStatefulData;
import net.polyv.android.player.sdk.foundation.lang.MutableEvent;

import java.util.List;

/**
 * @author Hoshiiro
 */
public interface IPLVMediaPlayerFeedVideoDataViewModel {

    /**
     * 获取更多视频数据
     *
     * @param fromIndex 如果是0，代表是Feed流下拉的刷新操作，获得的视频数据会替换本地Feed流中已有的数据；
     *                  如果不是0，代表是Feed流上拉的获取更多操作，获得的视频数据会追加到本地Feed流中已有的数据后面；
     */
    void requireMoreMediaResource(int fromIndex);

    MutableEvent<PLVStatefulData<List<PLVMediaResource>>> getOnReceiveMediaResourceEvent();

}
