package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.ext.PLVViewGroupExt.children;
import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.plv.foundationsdk.component.collection.PLVSequenceWrapper;
import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;

import java.util.List;
import java.util.Locale;

import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerSpeedSelectLayoutPortrait extends LinearLayout {

    private static final List<Float> SUPPORT_SPEED_LIST = listOf(0.5F, 1.0F, 1.5F, 2.0F);
    private static final int TEXT_COLOR_SELECTED = PLVFormatUtils.parseColor("#3F76FC");
    private static final int TEXT_COLOR_NORMAL = PLVFormatUtils.parseColor("#333333");

    protected Float currentSpeed = null;

    public PLVMediaPlayerSpeedSelectLayoutPortrait(Context context) {
        super(context);
    }

    public PLVMediaPlayerSpeedSelectLayoutPortrait(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerSpeedSelectLayoutPortrait(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        setOrientation(HORIZONTAL);
        initSpeedTextView();
    }

    private void initSpeedTextView() {
        PLVSequenceWrapper.wrap(SUPPORT_SPEED_LIST)
                .map(new Function1<Float, TextView>() {
                    @Override
                    public TextView invoke(final Float speed) {
                        TextView tv = new TextView(getContext());
                        tv.setText(String.format(Locale.getDefault(), "%.1fx", speed));
                        tv.setTextColor(TEXT_COLOR_NORMAL);
                        tv.setTextSize(12);
                        tv.setTag(speed);
                        tv.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(v).current();
                                PLVMediaPlayerControlViewModel viewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(v).current();
                                if (mediaPlayer != null) {
                                    mediaPlayer.setSpeed(speed);
                                }
                                if (viewModel != null) {
                                    viewModel.requestControl(PLVMediaPlayerControlAction.closeFloatMenuLayout());
                                }
                            }
                        });
                        return tv;
                    }
                })
                .forEach(new PLVSugarUtil.Consumer<TextView>() {
                    @Override
                    public void accept(TextView textView) {
                        MarginLayoutParams layoutParams = new MarginLayoutParams(ConvertUtils.dp2px(40), ConvertUtils.dp2px(17));
                        layoutParams.setMarginEnd(ConvertUtils.dp2px(28));
                        addView(textView, layoutParams);
                    }
                });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getStateListenerRegistry()
                        .getSpeed(),
                this,
                new Observer<Float>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable Float speed) {
                        currentSpeed = speed;
                        onViewStateChanged();
                    }
                }
        );
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onUpdateCurrentSelectedSpeed")
                .compareLastAndSet(currentSpeed)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onUpdateCurrentSelectedSpeed();
                    }
                });
    }

    protected void onUpdateCurrentSelectedSpeed() {
        if (currentSpeed == null) {
            return;
        }
        for (View child : children(this)) {
            if (!(child instanceof TextView) || !(child.getTag() instanceof Float)) {
                continue;
            }
            TextView tv = (TextView) child;
            if (((float) tv.getTag()) == currentSpeed) {
                tv.setTextColor(TEXT_COLOR_SELECTED);
            } else {
                tv.setTextColor(TEXT_COLOR_NORMAL);
            }
        }
    }

}
