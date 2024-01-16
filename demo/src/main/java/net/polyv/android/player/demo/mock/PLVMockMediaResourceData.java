package net.polyv.android.player.demo.mock;

import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;

import androidx.annotation.Nullable;

import com.plv.foundationsdk.component.collection.PLVSequenceWrapper;
import com.plv.foundationsdk.log.PLVCommonLog;

import net.polyv.android.player.business.scene.common.model.api.vo.PLVVodVideoListResponseVO;
import net.polyv.android.player.business.scene.common.model.datasource.PLVGeneralRxApiManager;
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource;
import net.polyv.android.player.business.scene.common.model.vo.PLVViewerParam;
import net.polyv.android.player.business.scene.common.model.vo.PLVVodAuthentication;
import net.polyv.android.player.business.scene.common.model.vo.PLVVodMainAccountAuthentication;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMockMediaResourceData {

    private static final PLVVodAuthentication mockAuthentication = new PLVVodMainAccountAuthentication("e97dbe3e64", "zMV29c519P", null, null);
    private static final PLVViewerParam mockViewerParam = new PLVViewerParam("123", "123", null, null, null, null, "param3", "param4", "param5");

    private static final PLVMockMediaResourceData INSTANCE = new PLVMockMediaResourceData();

    public static PLVMockMediaResourceData getInstance() {
        return INSTANCE;
    }

    @Nullable
    private List<PLVMediaResource> mediaResources;

    private PLVMockMediaResourceData() {
        setup();
    }

    private void setup() {
        setupMediaResourcesLocal();
//        setupMediaResourcesFromVodServer();
    }

    /**
     * 本地配置视频列表
     */
    private void setupMediaResourcesLocal() {
        mediaResources = listOf(
                vod("e97dbe3e648aefc2eb6f68b96db9db6c_e"),
                vod("e97dbe3e6401ea8f76617bafe32f57e9_e"),
                vod("e97dbe3e64ed6e0aac558e43787df1b4_e"),
                vod("e97dbe3e646f8f565c015f361025c51c_e"),
                vod("e97dbe3e64755eda79bbda0c8c9a939e_e"),
                vod("e97dbe3e6492596e7e680c4c7b99ca1b_e"),
                vod("e97dbe3e641a81a1e87750a2522b22c9_e"),
                vod("e97dbe3e64f6f6d1f75aa6d16a2d128e_e"),
                vod("e97dbe3e64b6dd24f868c16335570343_e"),
                vod("e97dbe3e64b2ab3301c3289a5731cbb0_e"),
                vod("e97dbe3e649c4f6743ca640bda94230c_e"),
                vod("e97dbe3e64ae88c87769c9dba0aad552_e"),
                vod("e97dbe3e640cd100431e12a8f8313c7d_e"),
                vod("e97dbe3e6423671524f4601cb652bd0d_e"),
                vod("e97dbe3e645083078cb42da5aac89b7f_e"),
                vod("e97dbe3e64414e6dad17196f652c021f_e"),
                vod("e97dbe3e64fba07447f7c37b283fd76d_e"),
                vod("e97dbe3e643ea0a62166780aeab7c43c_e"),
                vod("e97dbe3e64436506a71c7cbeecf001de_e"),
                vod("e97dbe3e64b564d0daac43002effcb48_e"),
                vod("e97dbe3e64e4f1bd6c28c395703fc4d8_e"),
                vod("e97dbe3e6400b7c243ecacf0a45e3fbb_e"),
                vod("e97dbe3e648ca60cad8017983a07ecc9_e"),
                vod("e97dbe3e646a24f4c61a45dcbf9354ce_e"),
                vod("e97dbe3e641b079d268330cc274fe3b4_e"),
                vod("e97dbe3e642eb6b15f9983949ffbdea7_e")
        );
    }

    /**
     * 从点播后台获取视频列表（异步）
     */
    private void setupMediaResourcesFromVodServer() {
        final PLVVodAuthentication authentication = mockAuthentication;
        final PLVViewerParam viewerParam = mockViewerParam;
        Disposable disposable = PLVGeneralRxApiManager.INSTANCE.getVodVideoList(authentication, 1, 20)
                .subscribeOn(Schedulers.io())
                .map(new Function<PLVVodVideoListResponseVO, List<PLVMediaResource>>() {
                    @Override
                    public List<PLVMediaResource> apply(@NotNull PLVVodVideoListResponseVO responseVO) throws Exception {
                        return PLVSequenceWrapper.wrap(responseVO.getData())
                                .map(new Function1<PLVVodVideoListResponseVO.VodVideo, PLVMediaResource>() {
                                    @Override
                                    public PLVMediaResource invoke(PLVVodVideoListResponseVO.VodVideo vodVideo) {
                                        return PLVMediaResource.vod(
                                                vodVideo.getVid(),
                                                authentication,
                                                viewerParam
                                        );
                                    }
                                })
                                .toMutableList();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<PLVMediaResource>>() {
                    @Override
                    public void accept(List<PLVMediaResource> plvMediaResources) throws Exception {
                        mediaResources = plvMediaResources;
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }

    @Nullable
    public List<PLVMediaResource> getMediaResources() {
        return this.mediaResources;
    }

    @Nullable
    public PLVMediaResource getRandomMediaResource() {
        if (mediaResources == null || mediaResources.isEmpty()) {
            return null;
        }
        return mediaResources.get((int) (Math.random() * mediaResources.size()));
    }

    private static PLVMediaResource vod(String videoId) {
        return PLVMediaResource.vod(videoId, mockAuthentication, mockViewerParam);
    }

    private static PLVMediaResource url(String url) {
        return PLVMediaResource.url(url);
    }

}
