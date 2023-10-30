package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.event.PLVEventKt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;

import java.text.DecimalFormat;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerLongPressSpeedHintLayout extends FrameLayout {

    private static final DecimalFormat SPEED_FORMAT = new DecimalFormat("#.#x");

    private TextView speedTv;
    private TextView speedHintTv;

    public PLVMediaPlayerLongPressSpeedHintLayout(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerLongPressSpeedHintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerLongPressSpeedHintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_long_press_speed_hint_layout, this);
        speedTv = findViewById(R.id.plv_media_player_speed_tv);
        speedHintTv = findViewById(R.id.plv_media_player_speed_hint_tv);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localControlViewModel.on(this).current())
                        .getControlActionEvent(),
                this,
                new PLVSugarUtil.Consumer<PLVMediaPlayerControlAction>() {
                    @Override
                    public void accept(PLVMediaPlayerControlAction action) {
                        if (action instanceof PLVMediaPlayerControlAction.HintLongPressSpeedControl) {
                            showLongPressControlHint((PLVMediaPlayerControlAction.HintLongPressSpeedControl) action);
                        }
                    }
                }
        );
    }

    protected void showLongPressControlHint(PLVMediaPlayerControlAction.HintLongPressSpeedControl action) {
        setVisibility(action.isLongPressing ? View.VISIBLE : View.GONE);
        if (action.isLongPressing) {
            speedTv.setText(SPEED_FORMAT.format(action.speed));
        }
    }

}
