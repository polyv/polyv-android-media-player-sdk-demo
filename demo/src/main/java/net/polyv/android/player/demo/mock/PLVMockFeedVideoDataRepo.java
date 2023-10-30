package net.polyv.android.player.demo.mock;

import com.plv.thirdpart.blankj.utilcode.util.NetworkUtils;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;

import java.util.ArrayList;
import java.util.List;

public class PLVMockFeedVideoDataRepo {

    public List<PLVMediaResource> getNewMediaResource(int fromIndex, int size) throws Exception {
        // Simulate network delay
        Thread.sleep(1000);

        // Simulate network connected
        if (!NetworkUtils.isConnected()) {
            throw new Exception("Network disconnected");
        }

        List<PLVMediaResource> source = PLVMockMediaResourceData.getInstance().getMediaResources();
        List<PLVMediaResource> mediaResources = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int index = fromIndex + i;
            if (index < source.size()) {
                mediaResources.add(source.get(index));
            }
        }
        return mediaResources;
    }

}
