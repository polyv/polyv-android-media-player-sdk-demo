package net.polyv.android.player.common.modules.knowledge

import net.polyv.android.common.libs.lang.state.mutableStateOf
import net.polyv.android.player.common.modules.knowledge.vo.PLVMediaPlayerKnowledgeVO

/**
 * @author Hoshiiro
 */
class PLVMediaPlayerKnowledgeViewModel {

    val knowledgeData = mutableStateOf<PLVMediaPlayerKnowledgeVO>()

    fun setKnowledgeData(data: PLVMediaPlayerKnowledgeVO) {
        knowledgeData.setValue(data)
    }

}