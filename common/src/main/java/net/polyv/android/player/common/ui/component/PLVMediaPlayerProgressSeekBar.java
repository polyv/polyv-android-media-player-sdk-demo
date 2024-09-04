package net.polyv.android.player.common.ui.component;

import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.isLandscape;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;

import net.polyv.android.player.common.R;
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider;
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel;
import net.polyv.android.player.common.modules.media.viewmodel.viewstate.PLVMPMediaPlayViewState;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.DragSeekBarAction;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel;
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.viewstate.PLVMPMediaControllerViewState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState;
import net.polyv.android.player.sdk.foundation.lang.PLVRememberStateCompareResult;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

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

    protected long videoProgress = 0;
    protected long dragProgress = 0;
    protected long videoDuration = 0;
    protected boolean isDragging = false;
    protected boolean dragWaitSeekFinish = false;
    protected boolean isVisible = false;

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

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaViewModel.class)
                .getMediaPlayViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaPlayViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaPlayViewState viewState) {
                        videoProgress = viewState.getCurrentProgress();
                        videoDuration = viewState.getDuration();
                        onViewStateChanged();
                        return null;
                    }
                });

        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .getMediaControllerViewState()
                .observeUntilViewDetached(this, new Function1<PLVMPMediaControllerViewState, Unit>() {
                    @Override
                    public Unit invoke(PLVMPMediaControllerViewState viewState) {
                        dragProgress = viewState.getProgressSeekBarDragPosition();
                        isDragging = viewState.getProgressSeekBarDragging();
                        dragWaitSeekFinish = viewState.getProgressSeekBarWaitSeekFinish();
                        isVisible = viewState.getControllerVisible()
                                && !viewState.isMediaStopOverlayVisible()
                                && !viewState.getControllerLocking()
                                && !(viewState.isFloatActionLayoutVisible() && isLandscape());
                        onViewStateChanged();
                        return null;
                    }
                });
    }

    protected void onViewStateChanged() {
        PLVRememberState.rememberStateOf(this, "updateProgress")
                .compareLastAndSet(videoProgress, videoDuration, dragProgress, isDragging, dragWaitSeekFinish)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        updateProgress();
                        return null;
                    }
                });

        PLVRememberState.rememberStateOf(this, "updateVisible")
                .compareLastAndSet(isVisible)
                .ifNotEquals(new Function1<PLVRememberStateCompareResult, Unit>() {
                    @Override
                    public Unit invoke(PLVRememberStateCompareResult result) {
                        updateVisible();
                        return null;
                    }
                });
    }

    protected void updateProgress() {
        long showProgress = isDragging || dragWaitSeekFinish ? dragProgress : videoProgress;
        setMax((int) videoDuration);
        setProgress((int) showProgress);
    }

    protected void updateVisible() {
        setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    // <editor-fold defaultstate="collapsed" desc="OnSeekBarChangeListener">

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (isDragging && fromUser) {
            requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                    .get(PLVMPMediaControllerViewModel.class)
                    .handleDragSeekBar(DragSeekBarAction.DRAG, progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .handleDragSeekBar(DragSeekBarAction.DRAG, videoProgress);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        requireNotNull(PLVMediaPlayerLocalProvider.localDependScope.on(this).current())
                .get(PLVMPMediaControllerViewModel.class)
                .handleDragSeekBar(DragSeekBarAction.FINISH);

        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(false);
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
        if (!isDragging) {
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
        if (!isDragging) {
            return thumbDrawable;
        } else {
            return thumbDrawableOnDrag;
        }
    }

    private int getSeekBarWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int getSeekBarHeight() {
        if (!isDragging) {
            return (int) seekBarHeight;
        } else {
            return (int) seekBarHeightOnDrag;
        }
    }

    private double progressPercent() {
        return (double) getProgress() / getMax();
    }

    // </editor-fold>

}
