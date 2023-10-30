package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;

import net.polyv.android.player.business.scene.common.model.api.vo.PLVVodVideoJsonVO;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.utils.ui.image.glide.transform.BlurTransformation;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerVideoFirstImageView extends AppCompatImageView {

    protected PLVVodVideoJsonVO currentVodVideoJson = null;

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

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getBusinessListenerRegistry()
                        .getVodVideoJson(),
                this,
                new Observer<PLVVodVideoJsonVO>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable PLVVodVideoJsonVO vodVideoJson) {
                        currentVodVideoJson = vodVideoJson;
                        onViewStateChanged();
                    }
                }
        );
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onChangeFirstImage")
                .compareLastAndSet(currentVodVideoJson)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeFirstImage();
                    }
                });
    }

    protected void onChangeFirstImage() {
        String url = nullable(new PLVSugarUtil.Supplier<String>() {
            @Override
            public String get() {
                return currentVodVideoJson.getFirst_image();
            }
        });
        if (TextUtils.isEmpty(url)) {
            setVisibility(View.GONE);
            return;
        }
        setImageDrawable(null);
        setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(url)
                .apply(
                        new RequestOptions()
                                .transform(new BlurTransformation(40))
                )
                .into(this);
    }

}
