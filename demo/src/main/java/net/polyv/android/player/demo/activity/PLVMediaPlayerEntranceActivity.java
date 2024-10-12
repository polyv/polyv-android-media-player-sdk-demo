package net.polyv.android.player.demo.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;
import net.polyv.android.player.common.ui.router.PLVMediaPlayerRouter;
import net.polyv.android.player.common.ui.router.RouterDestination;
import net.polyv.android.player.common.ui.router.RouterPayload;
import net.polyv.android.player.common.ui.router.RouterPayloadStaticHolder;
import net.polyv.android.player.common.utils.ui.PLVDebounceClicker;
import net.polyv.android.player.demo.R;
import net.polyv.android.player.demo.mock.PLVMockMediaResourceData;
import net.polyv.android.player.sdk.PLVDeviceManager;

import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerEntranceActivity extends AppCompatActivity implements View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private Button entranceFeedVideoBtn;
    private Button entranceSingleVideoBtn;
    private Button entranceDownloadCenterBtn;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PLVDeviceManager.setTransparentStatusBar(getWindow());
        setContentView(R.layout.plv_media_player_entrance_layout);

        // 提前初始化视频列表数据
        PLVMockMediaResourceData.getInstance();

        entranceFeedVideoBtn = findViewById(R.id.plv_media_player_entrance_feed_video_btn);
        entranceSingleVideoBtn = findViewById(R.id.plv_media_player_entrance_single_video_btn);
        entranceDownloadCenterBtn = findViewById(R.id.plv_media_player_entrance_download_center_btn);

        entranceFeedVideoBtn.setOnClickListener(this);
        entranceSingleVideoBtn.setOnClickListener(this);
        entranceDownloadCenterBtn.setOnClickListener(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">

    @Override
    public void onClick(View v) {
        if (!PLVDebounceClicker.tryClick(this)) {
            return;
        }
        int id = v.getId();
        if (id == entranceFeedVideoBtn.getId()) {
            gotoFeedVideoActivity();
        } else if (id == entranceSingleVideoBtn.getId()) {
            gotoSingleVideoActivity();
        } else if (id == entranceDownloadCenterBtn.getId()) {
            gotoDownloadCenter();
        }
    }

    // </editor-fold>

    // <editor-fold desc="核心代码-页面跳转">

    private void gotoFeedVideoActivity() {
        // mock data
        List<PLVMediaResource> sourceList = PLVMockMediaResourceData.getInstance().getMediaResources();
        if (sourceList == null || sourceList.isEmpty()) {
            Toast.makeText(this, "视频数据未初始化", Toast.LENGTH_SHORT).show();
            return;
        }
        List<PLVMediaResource> mediaResourceList = sourceList.subList(0, Math.min(10, sourceList.size()));

        // goto Feed Video Activity
        PLVMediaPlayerRouter.routerTo(
                PLVMediaPlayerEntranceActivity.this,
                new RouterDestination.SceneFeed(new RouterPayload.SceneFeedPayload(
                        RouterPayloadStaticHolder.create(mediaResourceList)
                ))
        );
    }

    private void gotoSingleVideoActivity() {
        // mock data
        List<PLVMediaResource> mediaResources = PLVMockMediaResourceData.getInstance().getMediaResources();
        if (mediaResources == null || mediaResources.isEmpty()) {
            Toast.makeText(this, "视频数据未初始化", Toast.LENGTH_SHORT).show();
            return;
        }

        // goto Single Video Activity
        PLVMediaPlayerRouter.routerTo(
                PLVMediaPlayerEntranceActivity.this,
                new RouterDestination.SceneSingle(new RouterPayload.SceneSinglePayload(mediaResources.get(0)))
        );
    }

    private void gotoDownloadCenter() {
        PLVMediaPlayerRouter.routerTo(this, new RouterDestination.DownloadCenter(new RouterPayload.DownloadCenterPayload()));
    }

    // </editor-fold>

}
