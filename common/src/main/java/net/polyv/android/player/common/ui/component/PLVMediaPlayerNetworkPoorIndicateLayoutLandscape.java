package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.event.PLVEventKt.observeUntilViewDetached;
import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVAppUtils.getString;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.plv.foundationsdk.component.collection.PLVSequenceWrapper;
import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.business.scene.common.model.vo.PLVMediaBitRate;
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaOutputMode;
import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.common.ui.viewmodel.viewstate.PLVMediaPlayerControlViewState;
import net.polyv.android.player.core.api.listener.event.PLVMediaPlayerOnInfoEvent;
import net.polyv.android.player.core.api.listener.event.PLVMediaPlayerOnPreparedEvent;
import net.polyv.android.player.core.api.listener.event.PLVMediaPlayerOnSeekStartEvent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerNetworkPoorIndicateLayoutLandscape extends FrameLayout implements View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="常量">

    // 计时：5秒卡顿进行一次提示
    private static final long INDICATE_BUFFERING_TIMEOUT = 5 * 1000;
    // 计次：计算10秒内卡顿次数
    private static final long INDICATE_COUNT_BUFFERING_DURATION = 10 * 1000;
    // 计次：2次卡顿进行一次提示
    private static final int INDICATE_BUFFERING_COUNT_TOO_MORE_THRESHOLD = 2;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="变量">

    private TextView networkPoorIndicateTv;
    private ImageView networkPoorIndicateCloseIv;

    private final LinkedList<BufferingCacheVO> bufferingCacheList = new LinkedList<>();
    private long lastSeekStartTimestamp = 0;
    private boolean isIndicateShown = false;

    protected PLVMediaPlayerControlViewState currentViewState = null;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造函数">

    public PLVMediaPlayerNetworkPoorIndicateLayoutLandscape(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerNetworkPoorIndicateLayoutLandscape(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerNetworkPoorIndicateLayoutLandscape(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_network_poor_indicate_layout_landscape, this);

        networkPoorIndicateTv = findViewById(R.id.plv_media_player_network_poor_indicate_tv);
        networkPoorIndicateCloseIv = findViewById(R.id.plv_media_player_network_poor_indicate_close_iv);

        networkPoorIndicateCloseIv.setOnClickListener(this);

        showNetworkPoorIndicate(false);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="onAttach - 事件状态监听">

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getEventListenerRegistry()
                        .getOnInfo(),
                this,
                new PLVSugarUtil.Consumer<PLVMediaPlayerOnInfoEvent>() {
                    @Override
                    public void accept(PLVMediaPlayerOnInfoEvent onInfoEvent) {
                        if (onInfoEvent == null) {
                            return;
                        }
                        switch (onInfoEvent.getWhat()) {
                            case PLVMediaPlayerOnInfoEvent.MEDIA_INFO_BUFFERING_START:
                                onBufferingStart();
                                break;
                            case PLVMediaPlayerOnInfoEvent.MEDIA_INFO_BUFFERING_END:
                                onBufferingEnd();
                                break;
                            default:
                        }
                    }
                }
        );

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getEventListenerRegistry()
                        .getOnSeekStartEvent(),
                this,
                new PLVSugarUtil.Consumer<PLVMediaPlayerOnSeekStartEvent>() {
                    @Override
                    public void accept(PLVMediaPlayerOnSeekStartEvent onSeekStartEvent) {
                        if (onSeekStartEvent != null) {
                            onSeekStart();
                        }
                    }
                }
        );

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getEventListenerRegistry()
                        .getOnPrepared(),
                this,
                new PLVSugarUtil.Consumer<PLVMediaPlayerOnPreparedEvent>() {
                    @Override
                    public void accept(PLVMediaPlayerOnPreparedEvent onPreparedEvent) {
                        if (onPreparedEvent != null) {
                            reset();
                        }
                    }
                }
        );

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current())
                        .getControlViewStateLiveData(),
                this,
                new Observer<PLVMediaPlayerControlViewState>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVMediaPlayerControlViewState viewState) {
                        currentViewState = viewState;
                        onViewStateChanged();
                    }
                }
        );
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        reset();
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangeBitRateSelectPanelVisible")
                .compareLastAndSet(currentViewState)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeBitRateSelectPanelVisible();
                    }
                });
    }

    protected void onChangeBitRateSelectPanelVisible() {
        if (currentViewState == null) {
            return;
        }
        boolean isBitRateSelectPanelVisible = currentViewState.bitRateSelectLayoutVisible;
        if (isBitRateSelectPanelVisible && getVisibility() == View.VISIBLE) {
            showNetworkPoorIndicate(false);
        }
    }

    protected void onBufferingStart() {
        boolean isBufferingBySeek = System.currentTimeMillis() - lastSeekStartTimestamp < 500;
        bufferingCacheList.add(new BufferingCacheVO(System.currentTimeMillis(), isBufferingBySeek));

        checkToShowIndicate();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                checkToShowIndicate();
            }
        }, INDICATE_BUFFERING_TIMEOUT);
    }

    protected void onBufferingEnd() {
        if (bufferingCacheList.isEmpty()) {
            return;
        }
        BufferingCacheVO vo = bufferingCacheList.getLast();
        vo.endTimestamp = System.currentTimeMillis();
    }

    protected void onSeekStart() {
        lastSeekStartTimestamp = System.currentTimeMillis();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="UI更新">

    protected void checkToShowIndicate() {
        dropExpireBufferingCache();
        if (checkBufferTooLong()) {
            tryShowNetworkPoorIndicate();
            return;
        }
        if (checkBufferTooMore()) {
            tryShowNetworkPoorIndicate();
        }
    }

    private void dropExpireBufferingCache() {
        Iterator<BufferingCacheVO> voIterator = bufferingCacheList.iterator();
        while (voIterator.hasNext()) {
            BufferingCacheVO vo = voIterator.next();
            if (vo.startTimestamp < System.currentTimeMillis() - INDICATE_COUNT_BUFFERING_DURATION && vo.endTimestamp > 0) {
                voIterator.remove();
            }
        }
    }

    private boolean checkBufferTooLong() {
        if (bufferingCacheList.isEmpty()) {
            return false;
        }
        BufferingCacheVO vo = bufferingCacheList.getLast();
        if (vo.startTimestamp < System.currentTimeMillis() - INDICATE_BUFFERING_TIMEOUT && vo.endTimestamp < 0) {
            return true;
        } else if (vo.endTimestamp - vo.startTimestamp > INDICATE_BUFFERING_TIMEOUT) {
            return true;
        }
        return false;
    }

    private boolean checkBufferTooMore() {
        int countBuffer = 0;
        for (BufferingCacheVO vo : bufferingCacheList) {
            if (!vo.bySeek) {
                countBuffer++;
            }
        }
        return countBuffer >= INDICATE_BUFFERING_COUNT_TOO_MORE_THRESHOLD;
    }

    private void tryShowNetworkPoorIndicate() {
        if (isIndicateShown) {
            return;
        }
        isIndicateShown = true;
        SwitchBitrateAction action = getSwitchBitrateAction();
        if (action == null) {
            return;
        }
        updateNetworkPoorIndicate(action);
        showNetworkPoorIndicate(true);
    }

    @Nullable
    private SwitchBitrateAction getSwitchBitrateAction() {
        final IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current();
        final PLVMediaPlayerControlViewModel viewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current();
        if (mediaPlayer == null || viewModel == null) {
            return null;
        }
        final PLVMediaBitRate currentBitRate = mediaPlayer.getBusinessListenerRegistry().getCurrentMediaBitRate().getValue();
        final PLVMediaOutputMode mediaOutputMode = mediaPlayer.getBusinessListenerRegistry().getCurrentMediaOutputMode().getValue();
        final List<PLVMediaBitRate> supportBitRates = mediaPlayer.getBusinessListenerRegistry().getSupportMediaBitRates().getValue();
        if (currentBitRate == null || supportBitRates == null || supportBitRates.isEmpty() || mediaOutputMode != PLVMediaOutputMode.AUDIO_VIDEO) {
            return null;
        }

        final PLVMediaBitRate nextDowngradeBitRate = PLVSequenceWrapper.wrap(supportBitRates)
                .filter(new Function1<PLVMediaBitRate, Boolean>() {
                    @Override
                    public Boolean invoke(PLVMediaBitRate mediaBitRate) {
                        return mediaBitRate.getIndex() < currentBitRate.getIndex();
                    }
                })
                .maxBy(new Function1<PLVMediaBitRate, Integer>() {
                    @Override
                    public Integer invoke(PLVMediaBitRate mediaBitRate) {
                        return mediaBitRate.getIndex();
                    }
                });
        if (nextDowngradeBitRate == null) {
            return null;
        }

        final String hintText = PLVSugarUtil.format(getString(R.string.plv_media_player_ui_component_network_poor_switch_bitrate_action_text), nextDowngradeBitRate.getName());
        final SwitchBitrateAction action = new SwitchBitrateAction();
        action.hint = hintText;
        action.onClick = new PLVSugarUtil.Callback() {
            @Override
            public void onCallback() {
                mediaPlayer.changeBitRate(nextDowngradeBitRate);
                viewModel.requestControl(PLVMediaPlayerControlAction.hintBitRateChanged(nextDowngradeBitRate));
            }
        };
        return action;
    }

    private void updateNetworkPoorIndicate(final SwitchBitrateAction action) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append(getString(R.string.plv_media_player_ui_component_network_poor_hint_text));
        sb.append(
                action.hint,
                new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        action.onClick.onCallback();
                        reset();
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(PLVFormatUtils.parseColor("#3F76FC"));
                        ds.setUnderlineText(false);
                    }
                },
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        networkPoorIndicateTv.setText(sb);
        networkPoorIndicateTv.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void showNetworkPoorIndicate(boolean toShow) {
        setVisibility(toShow ? View.VISIBLE : View.GONE);

        PLVMediaPlayerControlViewModel viewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current();
        if (viewModel != null) {
            viewModel.requestControl(PLVMediaPlayerControlAction.hintNetworkPoorIndicateVisible(toShow));
        }
    }

    private void reset() {
        showNetworkPoorIndicate(false);
        bufferingCacheList.clear();
        lastSeekStartTimestamp = 0;
        isIndicateShown = false;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == networkPoorIndicateCloseIv.getId()) {
            showNetworkPoorIndicate(false);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类">

    private static class BufferingCacheVO {
        private long startTimestamp;
        private long endTimestamp = -1;
        private boolean bySeek;

        public BufferingCacheVO(long timestamp, boolean bySeek) {
            this.startTimestamp = timestamp;
            this.bySeek = bySeek;
        }
    }

    private static class SwitchBitrateAction {
        private String hint;
        private PLVSugarUtil.Callback onClick;
    }

    // </editor-fold>

}
