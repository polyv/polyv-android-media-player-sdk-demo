package net.polyv.android.player.demo.mock;


import static com.plv.foundationsdk.component.event.PLVEventKt.mutableEvent;

import android.arch.lifecycle.ViewModel;
import com.plv.foundationsdk.component.event.PLVEvent;
import com.plv.foundationsdk.component.event.PLVMutableEvent;
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;
import net.polyv.android.player.common.utils.data.PLVStatefulData;
import net.polyv.android.player.demo.scene.feed.viewmodel.IPLVMediaPlayerFeedVideoDataViewModel;

import java.util.List;

public class PLVMockFeedVideoDataViewModel extends ViewModel implements IPLVMediaPlayerFeedVideoDataViewModel {

    private final PLVMockFeedVideoDataRepo repo = new PLVMockFeedVideoDataRepo();

    private final PLVMutableEvent<PLVStatefulData<List<PLVMediaResource>>> onReceiveMediaResource = mutableEvent();

    @Override
    public void requireMoreMediaResource(final int fromIndex) {
        // Simulate launching a coroutine
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<PLVMediaResource> mediaResourceList = repo.getNewMediaResource(fromIndex, 10);
                    onReceiveMediaResource.post(PLVStatefulData.success(mediaResourceList));
                } catch (Exception e) {
                    onReceiveMediaResource.post(PLVStatefulData.<List<PLVMediaResource>>error(e.getMessage(), e));
                }
            }
        }).start();
    }

    @Override
    public PLVEvent<PLVStatefulData<List<PLVMediaResource>>> getOnReceiveMediaResourceEvent() {
        return onReceiveMediaResource;
    }

}
