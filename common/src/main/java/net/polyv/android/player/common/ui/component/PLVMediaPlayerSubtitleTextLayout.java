package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.polyv.android.player.business.scene.vod.model.vo.PLVVodSubtitleText;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaInfoViewState;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaPlayViewState;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPSubtitleTextStyle;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerSubtitleTextLayout extends FrameLayout {

    private TextView subtitleTopTextTv;
    private TextView subtitleBottomTextTv;

    public PLVMediaPlayerSubtitleTextLayout(@NonNull Context context) {
        super(context);
    }

    public PLVMediaPlayerSubtitleTextLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerSubtitleTextLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_ui_component_subtitle_text_layout, this);

        subtitleTopTextTv = findViewById(R.id.plv_media_player_subtitle_top_text_tv);
        subtitleBottomTextTv = findViewById(R.id.plv_media_player_subtitle_bottom_text_tv);
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
                        List<PLVVodSubtitleText> subtitleTexts = viewState.getSubtitleTexts();
                        if (subtitleTexts.isEmpty()) {
                            subtitleTopTextTv.setVisibility(View.GONE);
                            subtitleBottomTextTv.setVisibility(View.GONE);
                        } else if (subtitleTexts.size() == 1) {
                            subtitleTopTextTv.setVisibility(View.VISIBLE);
                            subtitleBottomTextTv.setVisibility(View.GONE);
                            subtitleTopTextTv.setText(subtitleTexts.get(0).getText());
                        } else {
                            subtitleTopTextTv.setVisibility(View.VISIBLE);
                            subtitleBottomTextTv.setVisibility(View.VISIBLE);
                            subtitleTopTextTv.setText(subtitleTexts.get(0).getText());
                            subtitleBottomTextTv.setText(subtitleTexts.get(1).getText());
                        }
                        return null;
                    }
                });

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getMediaInfoViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaInfoViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaInfoViewState viewState) {
                        setSubtitleTextStyle(subtitleTopTextTv, viewState.getTopSubtitleTextStyle());
                        setSubtitleTextStyle(subtitleBottomTextTv, viewState.getBottomSubtitleTextStyle());
                        return null;
                    }
                });
    }

    private void setSubtitleTextStyle(TextView textView, PLVMPSubtitleTextStyle style) {
        textView.setTextColor(style.getFontColor());
        textView.setBackgroundColor(style.getBackgroundColor());
        final Typeface typeface = textView.getTypeface();
        if (style.isBold() && style.isItalic()) {
            textView.setTypeface(typeface, Typeface.BOLD_ITALIC);
        } else if (style.isBold()) {
            textView.setTypeface(typeface, Typeface.BOLD);
        } else if (style.isItalic()) {
            textView.setTypeface(typeface, Typeface.ITALIC);
        } else {
            textView.setTypeface(typeface, Typeface.NORMAL);
        }
    }

}
