package net.polyv.android.player.common.ui.component.knowledge.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import net.polyv.android.player.common.R
import net.polyv.android.player.common.utils.data.PLVTimeUtils

/**
 * @author suhongtao
 */
class PLVMediaPlayerKnowledgePointView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val knowledgePointIv by lazy { findViewById<ImageView>(R.id.plv_media_player_knowledge_point_iv) }
    private val knowledgePointDescTv by lazy { findViewById<TextView>(R.id.plv_media_player_knowledge_point_desc_tv) }
    private val knowledgePointTimeTv by lazy { findViewById<TextView>(R.id.plv_media_player_knowledge_point_time_tv) }

    init {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_media_player_knowledge_point_item, this)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (selected) {
            knowledgePointIv.setImageResource(R.drawable.plv_media_player_knowledge_selected_icon)
            knowledgePointDescTv.setTextColor(Color.parseColor("#3990FF"))
            knowledgePointTimeTv.setTextColor(Color.parseColor("#3990FF"))
        } else {
            knowledgePointIv.setImageResource(R.drawable.plv_media_player_knowledge_unselected_icon)
            knowledgePointDescTv.setTextColor(Color.parseColor("#CCFFFFFF"))
            knowledgePointTimeTv.setTextColor(Color.parseColor("#99FFFFFF"))
        }
    }

    fun setDescription(name: String?) {
        knowledgePointDescTv.text = name
    }

    fun setTime(timeInSecond: Int) {
        val timeText = PLVTimeUtils.formatTime(timeInSecond * 1000L)
        knowledgePointTimeTv.text = timeText
    }

    fun showDescription(show: Boolean) {
        knowledgePointDescTv.visibility = if (show) VISIBLE else GONE
    }

}
