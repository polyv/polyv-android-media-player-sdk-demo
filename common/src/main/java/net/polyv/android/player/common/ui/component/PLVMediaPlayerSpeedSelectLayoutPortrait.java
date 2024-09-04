package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.collections.CollectionsKt.listOf;
import static net.polyv.android.player.sdk.foundation.graphics.ColorsKt.parseColor;
import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.dp;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;
import static net.polyv.android.player.sdk.foundation.ui.ViewGroupsKt.children;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaPlayViewState;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.sdk.foundation.collections.PLVSequences;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import java.util.List;
import java.util.Locale;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerSpeedSelectLayoutPortrait extends LinearLayout {

    private static final List<Float> SUPPORT_SPEED_LIST = listOf(0.5F, 1.0F, 1.5F, 2.0F);
    private static final int TEXT_COLOR_SELECTED = parseColor("#3F76FC");
    private static final int TEXT_COLOR_NORMAL = parseColor("#333333");

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
        PLVSequences.wrap(SUPPORT_SPEED_LIST)
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
                                onSelectSpeed(speed);
                            }
                        });
                        return tv;
                    }
                })
                .forEach(new Function1<TextView, Unit>() {
                    @Override
                    public Unit invoke(TextView textView) {
                        MarginLayoutParams layoutParams = new MarginLayoutParams(dp(40).px(), dp(17).px());
                        layoutParams.setMarginEnd(dp(28).px());
                        addView(textView, layoutParams);
                        return null;
                    }
                });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getMediaPlayViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaPlayViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaPlayViewState viewState) {
                        currentSpeed = viewState.getSpeed();
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onUpdateCurrentSelectedSpeed")
                .compareLastAndSet(currentSpeed)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onUpdateCurrentSelectedSpeed();
                        return null;
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

    private void onSelectSpeed(float speed) {
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .setSpeed(speed);
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .popFloatActionLayout();
    }

}
