package net.polyv.android.player.common.ui.component;

import static com.plv.foundationsdk.component.event.PLVEventKt.observeUntilViewDetached;
import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.observeUntilViewDetached;
import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;

import com.plv.foundationsdk.component.remember.PLVRememberState;
import com.plv.foundationsdk.component.remember.PLVRememberStateCompareResult;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;
import net.polyv.android.player.common.R;
import net.polyv.android.player.common.ui.localprovider.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.ui.viewmodel.PLVMediaPlayerControlViewModel;
import net.polyv.android.player.common.ui.viewmodel.action.PLVMediaPlayerControlAction;
import net.polyv.android.player.common.ui.viewmodel.viewstate.PLVMediaPlayerControlViewState;
import net.polyv.android.player.core.api.listener.event.PLVMediaPlayerOnInfoEvent;

/**
 * @author Hoshiiro
 */
public class PLVMediaPlayerProgressSeekBar extends AppCompatSeekBar implements SeekBar.OnSeekBarChangeListener {

    private float seekBarHeight;
    private float seekBarHeightOnDrag;
    private Drawable progressDrawable;
    private Drawable progressDrawableOnDrag;
    private Drawable thumbDrawable;
    private Drawable thumbDrawableOnDrag;

    protected boolean isSeekBarDragging = false;
    protected boolean waitSeekFinish = false;
    protected int progressOnDrag = 0;
    protected long currentPosition = 0;
    protected long currentDuration = 0;
    protected PLVMediaPlayerControlViewState currentControlViewState = null;

    public PLVMediaPlayerProgressSeekBar(Context context) {
        super(context);
        init(null);
    }

    public PLVMediaPlayerProgressSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PLVMediaPlayerProgressSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        parseAttrs(attrs);
        setOnSeekBarChangeListener(this);
    }

    private void parseAttrs(AttributeSet attrs) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PLVMediaPlayerProgressSeekBar);
        seekBarHeight = typedArray.getDimension(R.styleable.PLVMediaPlayerProgressSeekBar_plvSeekBarHeight, seekBarHeight);
        seekBarHeightOnDrag = typedArray.getDimension(R.styleable.PLVMediaPlayerProgressSeekBar_plvSeekBarHeightOnDrag, seekBarHeight);
        progressDrawable = getProgressDrawable();
        progressDrawableOnDrag = typedArray.getDrawable(R.styleable.PLVMediaPlayerProgressSeekBar_plvProgressDrawableOnDrag);
        thumbDrawable = getThumb();
        thumbDrawableOnDrag = typedArray.getDrawable(R.styleable.PLVMediaPlayerProgressSeekBar_plvThumbDrawableOnDrag);
        typedArray.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getStateListenerRegistry()
                        .getProgressState(),
                this,
                new Observer<Long>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable Long progress) {
                        currentPosition = progress == null ? 0 : progress;
                        onViewStateChanged();
                    }
                }
        );

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getStateListenerRegistry()
                        .getDurationState(),
                this,
                new Observer<Long>() {
                    @Override
                    public void onChanged(@Nullable @org.jetbrains.annotations.Nullable Long duration) {
                        currentDuration = duration == null ? 0 : duration;
                        onViewStateChanged();
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
                        currentControlViewState = viewState;
                        onViewStateChanged();
                    }
                }
        );

        observeUntilViewDetached(
                requireNotNull(PLVMediaPlayerLocalProvider.localMediaPlayer.on(this).current())
                        .getEventListenerRegistry()
                        .getOnInfo(),
                this,
                new PLVSugarUtil.Consumer<PLVMediaPlayerOnInfoEvent>() {
                    @Override
                    public void accept(PLVMediaPlayerOnInfoEvent onInfoEvent) {
                        if (onInfoEvent != null) {
                            if (onInfoEvent.getWhat() == PLVMediaPlayerOnInfoEvent.MEDIA_INFO_AUDIO_SEEK_RENDERING_START
                                    || onInfoEvent.getWhat() == PLVMediaPlayerOnInfoEvent.MEDIA_INFO_VIDEO_SEEK_RENDERING_START
                                    || onInfoEvent.getWhat() == PLVMediaPlayerOnInfoEvent.MEDIA_INFO_AUDIO_RENDERING_START
                                    || onInfoEvent.getWhat() == PLVMediaPlayerOnInfoEvent.MEDIA_INFO_VIDEO_RENDERING_START) {
                                waitSeekFinish = false;
                                onViewStateChanged();
                            }
                        }
                    }
                }
        );
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "onProgressUpdated")
                .compareLastAndSet(currentPosition, currentDuration, currentControlViewState, waitSeekFinish)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onProgressUpdated();
                    }
                });

        PLVRememberState.rememberStateOf(this, "onChangeVisibility")
                .compareLastAndSet(currentControlViewState)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        onChangeVisibility();
                    }
                });

        PLVRememberState.rememberStateOf(this, "updateWaitBufferEndAfterSeek")
                .compareLastAndSet(currentControlViewState)
                .ifNotEquals(new PLVSugarUtil.Consumer<PLVRememberStateCompareResult>() {
                    @Override
                    public void accept(PLVRememberStateCompareResult result) {
                        updateWaitSeekFinishAfterSeek();
                    }
                });
    }

    protected void onProgressUpdated() {
        if (currentControlViewState == null) {
            return;
        }
        if (!currentControlViewState.progressSeekBarDragging && waitSeekFinish) {
            invalidate();
            return;
        }

        long position = currentPosition;
        if (currentControlViewState.progressSeekBarDragging) {
            position = currentControlViewState.progressSeekBarDragPosition;
        }
        int max = getMax();
        int progress = (int) (((double) position) / currentDuration * max);
        setProgress(progress);
        invalidate();
    }

    protected void onChangeVisibility() {
        if (currentControlViewState == null) {
            return;
        }
        final boolean visible = currentControlViewState.controllerVisible
                && !currentControlViewState.isOverlayLayoutVisible()
                && !currentControlViewState.controllerLocking
                && !(currentControlViewState.isFloatActionPanelVisible() && ScreenUtils.isLandscape());
        setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    protected void updateWaitSeekFinishAfterSeek() {
        if (currentControlViewState == null) {
            return;
        }
        if (currentControlViewState.progressSeekBarDragging) {
            waitSeekFinish = true;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="OnSeekBarChangeListener">

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (isSeekBarDragging && fromUser) {
            progressOnDrag = progress;
            postDraggingAction();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isSeekBarDragging = true;
        progressOnDrag = getProgress();
        waitSeekFinish = true;
        postDraggingAction();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isSeekBarDragging = false;
        postDraggingAction();
        seekOnStopDrag();
    }

    protected void postDraggingAction() {
        PLVMediaPlayerControlViewModel viewModel = PLVMediaPlayerLocalProvider.localControlViewModel.on(PLVMediaPlayerProgressSeekBar.this).current();
        if (viewModel != null) {
            viewModel.requestControl(PLVMediaPlayerControlAction.progressSeekBarDrag((long) (((double) progressOnDrag) / getMax() * currentDuration), isSeekBarDragging));
        }
    }

    protected void seekOnStopDrag() {
        IPLVMediaPlayer mediaPlayer = PLVMediaPlayerLocalProvider.localMediaPlayer.on(PLVMediaPlayerProgressSeekBar.this).current();
        if (mediaPlayer != null) {
            mediaPlayer.seek((long) (((double) progressOnDrag) / getMax() * currentDuration));
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="onDraw">

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        final int saveCount = canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        drawBackground(canvas);
        drawProgress(canvas);
        drawThumb(canvas);
        canvas.restoreToCount(saveCount);
    }

    private void drawBackground(Canvas canvas) {
        Drawable drawable = getProgressDrawableById(android.R.id.background);
        if (drawable == null) {
            return;
        }
        int height = getSeekBarHeight();
        int top = (getHeight() - height) / 2;
        drawable.setBounds(0, top, getSeekBarWidth(), top + height);
        drawable.draw(canvas);
    }

    private void drawProgress(Canvas canvas) {
        Drawable drawable = getProgressDrawableById(android.R.id.progress);
        if (drawable == null) {
            return;
        }
        int height = getSeekBarHeight();
        int top = (getHeight() - height) / 2;
        drawable.setLevel((int) (progressPercent() * 10000));
        drawable.setBounds(0, top, getSeekBarWidth(), top + height);
        drawable.draw(canvas);
    }

    private void drawThumb(Canvas canvas) {
        Drawable drawable = getThumbDrawable();
        if (drawable == null) {
            return;
        }
        int thumbWidth = drawable.getIntrinsicWidth();
        int thumbHeight = drawable.getIntrinsicHeight();
        int left = (int) (progressPercent() * getSeekBarWidth() - thumbWidth / 2);
        int top = (getHeight() - thumbHeight) / 2;
        drawable.setBounds(left, top, left + thumbWidth, top + thumbHeight);
        drawable.draw(canvas);
    }

    @Nullable
    private Drawable getProgressDrawableById(int id) {
        Drawable drawable;
        if (!isSeekBarOrScreenDragging()) {
            drawable = progressDrawable;
        } else {
            drawable = progressDrawableOnDrag;
        }
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof LayerDrawable) {
            drawable = ((LayerDrawable) drawable).findDrawableByLayerId(id);
        }
        return drawable;
    }

    @Nullable
    private Drawable getThumbDrawable() {
        if (!isSeekBarOrScreenDragging()) {
            return thumbDrawable;
        } else {
            return thumbDrawableOnDrag;
        }
    }

    private int getSeekBarWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int getSeekBarHeight() {
        if (!isSeekBarOrScreenDragging()) {
            return (int) seekBarHeight;
        } else {
            return (int) seekBarHeightOnDrag;
        }
    }

    private double progressPercent() {
        return (double) getProgress() / getMax();
    }

    private boolean isSeekBarOrScreenDragging() {
        if (isSeekBarDragging) {
            return true;
        }
        if (currentControlViewState != null && currentControlViewState.progressSeekBarDragging) {
            return true;
        }
        return false;
    }

    // </editor-fold>

}
