package net.polyv.android.player.common.ui.component.knowledge.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import net.polyv.android.player.common.modules.knowledge.vo.PLVMediaPlayerKnowledgeVO.WordType.WordKey.KnowledgePoint
import net.polyv.android.player.common.ui.component.knowledge.adapter.PLVMediaPlayerKnowledgePointAdapter.KnowledgePointViewHolder
import net.polyv.android.player.common.ui.component.knowledge.widget.PLVMediaPlayerKnowledgePointView

/**
 * @author suhongtao
 */
class PLVMediaPlayerKnowledgePointAdapter : RecyclerView.Adapter<KnowledgePointViewHolder?>() {

    private val knowledgePoints = mutableListOf<KnowledgePoint>()
    var selectedKnowledgePoint: KnowledgePoint? = null
    private var showKnowledgePointDescription = false

    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): KnowledgePointViewHolder {
        return KnowledgePointViewHolder(PLVMediaPlayerKnowledgePointView(viewGroup.context))
    }

    override fun onBindViewHolder(knowledgePointViewHolder: KnowledgePointViewHolder, i: Int) {
        val knowledgePoint = knowledgePoints[i]
        knowledgePointViewHolder.bind(knowledgePoint)
        knowledgePointViewHolder.knowledgePointView.showDescription(showKnowledgePointDescription)
        knowledgePointViewHolder.knowledgePointView.setSelected(knowledgePoint == selectedKnowledgePoint)
        knowledgePointViewHolder.knowledgePointView.setOnClickListener {
            selectedKnowledgePoint = knowledgePoint
            onItemClickListener?.onClick(knowledgePoint)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return knowledgePoints.size
    }

    fun setKnowledgePoints(list: List<KnowledgePoint>) {
        this.knowledgePoints.clear()
        knowledgePoints.addAll(list)
        notifyDataSetChanged()
    }

    fun setShowKnowledgePointDescription(show: Boolean) {
        this.showKnowledgePointDescription = show
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    class KnowledgePointViewHolder(
        val knowledgePointView: PLVMediaPlayerKnowledgePointView
    ) : RecyclerView.ViewHolder(knowledgePointView) {
        fun bind(knowledgePoint: KnowledgePoint) {
            knowledgePointView.setDescription(knowledgePoint.name)
            knowledgePointView.setTime(knowledgePoint.time ?: 0)
        }
    }

    fun interface OnItemClickListener {
        fun onClick(knowledgePoint: KnowledgePoint)
    }
}
