package net.polyv.android.player.common.ui.component.knowledge.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import net.polyv.android.player.common.R

/**
 * @author suhongtao
 */
class PLVMediaPlayerKnowledgeWordKeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val knowledgeWordKeyLl by lazy { findViewById<LinearLayout>(R.id.plv_media_player_knowledge_word_key_ll) }
    private val knowledgeWordKeyTv by lazy { findViewById<TextView>(R.id.plv_media_player_knowledge_word_key_tv) }
    private val knowledgePointCountTv by lazy { findViewById<TextView>(R.id.plv_media_player_knowledge_point_count_tv) }
    private val knowledgeWordKeySeparatorView by lazy { findViewById<View>(R.id.plv_media_player_knowledge_word_key_separator_view) }

    init {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_knowledge_word_key_item, this)
    }

    fun setWordKey(wordKey: String?) {
        knowledgeWordKeyTv.text = wordKey
    }

    fun setKnowledgePointCount(count: Int) {
        knowledgePointCountTv.text = "($count)"
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (selected) {
            setBackgroundColor(Color.parseColor("#14FFFFFF"))
            knowledgeWordKeySeparatorView.visibility = GONE
            knowledgeWordKeyTv.setAlpha(1f)
            knowledgePointCountTv.setAlpha(1f)
        } else {
            setBackgroundColor(Color.TRANSPARENT)
            knowledgeWordKeySeparatorView.visibility = VISIBLE
            knowledgeWordKeyTv.setAlpha(0.6f)
            knowledgePointCountTv.setAlpha(0.6f)
        }
    }

}
