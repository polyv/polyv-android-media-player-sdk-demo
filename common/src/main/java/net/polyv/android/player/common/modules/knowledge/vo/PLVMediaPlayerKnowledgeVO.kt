package net.polyv.android.player.common.modules.knowledge.vo

/**
 * @author suhongtao
 */
data class PLVMediaPlayerKnowledgeVO(
    val fullScreenStyle: Boolean?,
    val wordTypes: List<WordType?>?,
) {
    data class WordType(
        val name: String?,
        val wordKeys: List<WordKey?>?,
    ) {
        data class WordKey(
            val name: String?,
            val knowledgePoints: List<KnowledgePoint?>?,
        ) {
            data class KnowledgePoint(
                val name: String?,
                val time: Int?,
            )
        }
    }
}