package net.polyv.android.player.demo.mock;


import android.arch.lifecycle.ViewModel;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;
import net.polyv.android.player.common.utils.data.PLVStatefulData;
import net.polyv.android.player.scenes.feed.viewmodel.IPLVMediaPlayerFeedVideoDataViewModel;
import net.polyv.android.player.sdk.foundation.lang.MutableEvent;

import java.util.List;

public class PLVMockFeedVideoDataViewModel extends ViewModel implements IPLVMediaPlayerFeedVideoDataViewModel {

    private final PLVMockFeedVideoDataRepo repo = new PLVMockFeedVideoDataRepo();

    private final MutableEvent<PLVStatefulData<List<PLVMediaResource>>> onReceiveMediaResource = new MutableEvent<>();

    @Override
    public void requireMoreMediaResource(final int fromIndex) {
        // Simulate launching a coroutine
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<PLVMediaResource> mediaResourceList = repo.getNewMediaResource(fromIndex, 10);
                    onReceiveMediaResource.setValue(PLVStatefulData.success(mediaResourceList));
                } catch (Exception e) {
                    onReceiveMediaResource.setValue(PLVStatefulData.<List<PLVMediaResource>>error(e.getMessage(), e));
                }
            }
        }).start();
    }

    @Override
    public MutableEvent<PLVStatefulData<List<PLVMediaResource>>> getOnReceiveMediaResourceEvent() {
        return onReceiveMediaResource;
    }

}
