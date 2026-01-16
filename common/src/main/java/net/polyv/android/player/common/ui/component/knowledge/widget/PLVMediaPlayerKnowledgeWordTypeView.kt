package net.polyv.android.player.common.ui.component.knowledge.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.widget.AppCompatTextView
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import net.polyv.android.common.libs.lang.graphic.dp

/**
 * @author suhongtao
 */
class PLVMediaPlayerKnowledgeWordTypeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var wordTypeTextView: TextView
    private var selectedPaint: Paint

    init {
        setLayoutParams(ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT))
        val paddingHorizontal = 20.dp().px()
        setPadding(paddingHorizontal, 0, paddingHorizontal, 0)
        setWillNotDraw(false)

        wordTypeTextView = AppCompatTextView(context).apply {
            setTextColor(Color.WHITE)
            textSize = 16f
        }
        addView(wordTypeTextView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            addRule(CENTER_IN_PARENT, 1)
        })

        selectedPaint = Paint().apply {
            setColor(Color.parseColor("#3990FF"))
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isSelected) {
            canvas.drawRect(
                wordTypeTextView.left.toFloat(),
                (bottom - 2.dp().px()).toFloat(),
                wordTypeTextView.right.toFloat(),
                bottom.toFloat(),
                selectedPaint
            )
        }
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (selected) {
            wordTypeTextView.setAlpha(1f)
        } else {
            wordTypeTextView.setAlpha(0.6f)
        }
    }

    fun setWordTypeName(wordTypeName: String?) {
        wordTypeTextView.text = wordTypeName
    }
}
