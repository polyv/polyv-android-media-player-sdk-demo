package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaInfoViewState;
import net.polyv.android.player.common.utils.ui.image.glide.transform.BlurTransformation;
import net.polyv.android.player.core.api.listener.event.PLVMediaPlayerOnInfoEvent;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerVideoFirstImageView extends AppCompatImageView {

    protected String firstImageUrl = null;
    protected boolean isFirstFrameRendered = false;

    public PLVMediaPlayerVideoFirstImageView(Context context) {
        super(context);
    }

    public PLVMediaPlayerVideoFirstImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVMediaPlayerVideoFirstImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getMediaInfoViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaInfoViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaInfoViewState viewState) {
                        firstImageUrl = viewState.getAudioModeCoverImage();
                        onViewStateChanged();
                        return null;
                    }
                });

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getOnInfoEvent()
                .observeUntilViewDetached(this, new Function1<PLVMediaPlayerOnInfoEvent, Unit>() {
                    @Override
                    public Unit invoke(PLVMediaPlayerOnInfoEvent onInfoEvent) {
                        if (onInfoEvent.getWhat() == PLVMediaPlayerOnInfoEvent.MEDIA_INFO_VIDEO_RENDERING_START) {
                            isFirstFrameRendered = true;
                            onViewStateChanged();
                        }
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangeFirstImage")
                .compareLastAndSet(firstImageUrl, isFirstFrameRendered)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        onChangeFirstImage();
                        return null;
                    }
                });
    }

    protected void onChangeFirstImage() {
        if (isFirstFrameRendered || TextUtils.isEmpty(firstImageUrl)) {
            setVisibility(View.GONE);
            return;
        }
        setImageDrawable(null);
        setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(firstImageUrl)
                .apply(
                        new RequestOptions()
                                .transform(new BlurTransformation(40))
                )
                .into(this);
    }

}
