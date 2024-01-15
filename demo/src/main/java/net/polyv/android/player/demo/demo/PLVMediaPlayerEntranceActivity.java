package net.polyv.android.player.demo.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;
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

        entranceFeedVideoBtn.setOnClickListener(this);
        entranceSingleVideoBtn.setOnClickListener(this);
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
        }
    }

    // </editor-fold>

    // <editor-fold desc="核心代码-页面跳转">

    private void gotoFeedVideoActivity() {
        // mock data
        List<PLVMediaResource> sourceList = PLVMockMediaResourceData.getInstance().getMediaResources();
        if (sourceList == null || sourceList.isEmpty()) {
            ToastUtils.showShort("视频数据未初始化");
            return;
        }
        List<PLVMediaResource> mediaResourceList = sourceList.subList(0, Math.min(10, sourceList.size()));

        // goto Feed Video Activity
        Intent intent = PLVMediaPlayerFeedVideoActivity.launchActivity(PLVMediaPlayerEntranceActivity.this, mediaResourceList);
        startActivity(intent);
    }

    private void gotoSingleVideoActivity() {
        // mock data
        PLVMediaResource mediaResource = PLVMockMediaResourceData.getInstance().getRandomMediaResource();
        if (mediaResource == null) {
            ToastUtils.showShort("视频数据未初始化");
            return;
        }

        // goto Single Video Activity
        Intent intent = new Intent(PLVMediaPlayerEntranceActivity.this, PLVMediaPlayerSingleVideoActivity.class);
        intent.putExtra(PLVMediaPlayerSingleVideoActivity.KEY_TARGET_MEDIA_RESOURCE, mediaResource);
        startActivity(intent);
    }
    // </editor-fold>

}
