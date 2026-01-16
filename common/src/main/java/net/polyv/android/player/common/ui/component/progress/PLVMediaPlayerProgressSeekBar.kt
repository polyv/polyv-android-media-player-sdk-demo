package net.polyv.android.player.common.ui.component.progress

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.appcompat.widget.AppCompatSeekBar
import android.util.AttributeSet
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import net.polyv.android.player.common.R
import net.polyv.android.player.common.di.PLVMediaPlayerLocalProvider
import net.polyv.android.player.common.modules.media.viewmodel.PLVMPMediaViewModel
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.DragSeekBarAction
import net.polyv.android.player.common.modules.mediacontroller.viewmodel.PLVMPMediaControllerViewModel
import net.polyv.android.player.sdk.foundation.graphics.isLandscape
import net.polyv.android.player.sdk.foundation.lang.PLVRememberState.Companion.rememberStateOf

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerProgressSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatSeekBar(context, attrs, defStyle), OnSeekBarChangeListener {
    private var seekBarHeight = 0f
    private var seekBarHeightOnDrag = 0f
    private var progressDrawableOnDrag: Drawable? = null
    private var thumbDrawableOnDrag: Drawable? = null

    private var videoProgress: Long = 0
    private var dragProgress: Long = 0
    private var videoDuration: Long = 0
    private var isDragging: Boolean = false
    private var dragWaitSeekFinish: Boolean = false
    private var isVisible: Boolean = false

    init {
        parseAttrs(attrs)
        setOnSeekBarChangeListener(this)
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        attrs ?: return
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PLVMediaPlayerProgressSeekBar)
        seekBarHeight = typedArray.getDimension(
            R.styleable.PLVMediaPlayerProgressSeekBar_plv_seek_bar_height,
            seekBarHeight
        )
        seekBarHeightOnDrag = typedArray.getDimension(
            R.styleable.PLVMediaPlayerProgressSeekBar_plv_seek_bar_height_on_drag,
            seekBarHeight
        )
        progressDrawableOnDrag = typedArray.getDrawable(R.styleable.PLVMediaPlayerProgressSeekBar_plv_progress_drawable_on_drag)
        thumbDrawableOnDrag = typedArray.getDrawable(R.styleable.PLVMediaPlayerProgressSeekBar_plv_thumb_drawable_on_drag)
        typedArray.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaViewModel>()
            .mediaPlayViewState
            .observe { viewState ->
                videoProgress = viewState.currentProgress
                videoDuration = viewState.duration
                onViewStateChanged()
            }
            .disposeOnDetached(this)

        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaControllerViewModel>()
            .mediaControllerViewState
            .observe { viewState ->
                dragProgress = viewState.progressSeekBarDragPosition
                isDragging = viewState.progressSeekBarDragging
                dragWaitSeekFinish = viewState.progressSeekBarWaitSeekFinish
                isVisible = viewState.controllerVisible
                        && !viewState.isMediaStopOverlayVisible
                        && !viewState.controllerLocking
                        && !(viewState.isFloatActionLayoutVisible && isLandscape())
                onViewStateChanged()
            }
            .disposeOnDetached(this)
    }

    private fun onViewStateChanged() {
        rememberStateOf("updateProgress")
            .compareLastAndSet(videoProgress, videoDuration, dragProgress, isDragging, dragWaitSeekFinish)
            .ifNotEquals {
                updateProgress()
            }

        rememberStateOf("updateVisible")
            .compareLastAndSet(isVisible)
            .ifNotEquals {
                updateVisible()
            }
    }

    private fun updateProgress() {
        if (videoDuration == 0L) {
            return
        }
        if (!isDragging && dragWaitSeekFinish) {
            return
        }
        val showProgress = if (isDragging) dragProgress else videoProgress
        setMax(videoDuration.toInt())
        progress = showProgress.toInt()
    }

    private fun updateVisible() {
        visibility = if (isVisible) VISIBLE else GONE
    }

    // <editor-fold defaultstate="collapsed" desc="OnSeekBarChangeListener">

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
                .get<PLVMPMediaControllerViewModel>()
                .handleDragSeekBar(DragSeekBarAction.DRAG, progress.toLong())
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaControllerViewModel>()
            .handleDragSeekBar(DragSeekBarAction.DRAG, videoProgress)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        PLVMediaPlayerLocalProvider.localDependScope.on(this).current()!!
            .get<PLVMPMediaControllerViewModel>()
            .handleDragSeekBar(DragSeekBarAction.FINISH)

        parent?.requestDisallowInterceptTouchEvent(false)
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="onDraw">

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        val saveCount = canvas.save()
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        drawBackground(canvas)
        drawProgress(canvas)
        drawThumb(canvas)
        canvas.restoreToCount(saveCount)
    }

    private fun drawBackground(canvas: Canvas?) {
        val drawable = getProgressDrawableById(android.R.id.background) ?: return
        val height = getSeekBarHeight()
        val top = (getHeight() - height) / 2
        drawable.setBounds(0, top, this.getSeekBarWidth(), top + height)
        drawable.draw(canvas!!)
    }

    private fun drawProgress(canvas: Canvas?) {
        val drawable = getProgressDrawableById(android.R.id.progress) ?: return
        val height = getSeekBarHeight()
        val top = (getHeight() - height) / 2
        drawable.setLevel((progressPercent() * 10000).toInt())
        drawable.setBounds(0, top, this.getSeekBarWidth(), top + height)
        drawable.draw(canvas!!)
    }

    private fun drawThumb(canvas: Canvas?) {
        val drawable = getThumbDrawable() ?: return
        val thumbWidth = drawable.intrinsicWidth
        val thumbHeight = drawable.intrinsicHeight
        val left = (progressPercent() * this.getSeekBarWidth() - thumbWidth / 2).toInt()
        val top = (height - thumbHeight) / 2
        drawable.setBounds(left, top, left + thumbWidth, top + thumbHeight)
        drawable.draw(canvas!!)
    }

    private fun getProgressDrawableById(id: Int): Drawable? {
        var drawable = if (!isDragging) {
            progressDrawable
        } else {
            progressDrawableOnDrag
        }
        if (drawable is LayerDrawable) {
            drawable = drawable.findDrawableByLayerId(id)
        }
        return drawable
    }

    private fun getThumbDrawable(): Drawable? {
        return if (!isDragging) {
            thumb
        } else {
            thumbDrawableOnDrag
        }
    }

    private fun getSeekBarWidth(): Int {
        return width - getPaddingLeft() - getPaddingRight()
    }

    private fun getSeekBarHeight(): Int {
        return if (!isDragging) {
            seekBarHeight.toInt()
        } else {
            seekBarHeightOnDrag.toInt()
        }
    }

    private fun progressPercent(): Double {
        return progress.toDouble() / max
    }

    // </editor-fold>
}
