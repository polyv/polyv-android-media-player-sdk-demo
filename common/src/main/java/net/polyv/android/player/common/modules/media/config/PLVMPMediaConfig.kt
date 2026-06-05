package net.polyv.android.player.common.modules.media.config

import android.widget.Toast
import net.polyv.android.player.business.scene.common.model.vo.PLVVodMainAccountAuthentication
import net.polyv.android.player.business.scene.common.model.vo.PLVVodMediaResource
import net.polyv.android.player.business.scene.common.player.listener.callback.DefaultVodMediaTokenRequestListener
import net.polyv.android.player.business.scene.common.player.listener.callback.IPLVVodMediaTokenRequestListener
import net.polyv.android.player.business.scene.vod.model.vo.PLVVodVideoTokenVO
import net.polyv.android.player.sdk.foundation.app.PLVApplicationContext
import net.polyv.android.player.sdk.foundation.lang.postToMainThread

/**
 * 媒体播放全局配置类
 * 用于外部（如 demo 代码）设置全局配置
 */
object PLVMPMediaConfig {

    /**
     * 是否使用自定义Token播放，参考：https://help.polyv.net/#/vod/android_player_sdk/4-加密视频版权保护
     * true: 使用自定义token，不需要在PLVVodMainAccountAuthentication中配置secretKey，需要设置vodTokenRequestListener
     * false: 使用默认认证方式，需要在PLVVodMainAccountAuthentication中配置secretKey
     */
    var useCustomToken = false
}

const val demoUserId = "e97dbe3e64"
const val demoSecretKey = "zMV29c519P"

val customTokenRequestListener = object : IPLVVodMediaTokenRequestListener {
    override fun onRequestToken(
        mediaResource: PLVVodMediaResource,
        callback: (PLVVodVideoTokenVO?) -> Unit
    ) {
        // 如果是demo测试账号
        val isDemoUser = demoUserId == mediaResource.authentication.userIdOrAppId
        if (isDemoUser) {
            val authentication = PLVVodMainAccountAuthentication(demoUserId, demoSecretKey, null, null)
            DefaultVodMediaTokenRequestListener.onRequestToken(mediaResource.copy(authentication = authentication), callback)
        } else {
            // 切到主线程toast，如果返回了token，请注释toast代码
            postToMainThread {
                Toast.makeText(
                    PLVApplicationContext.applicationContext,
                    "请参考 https://help.polyv.net/#/vod/android_player_sdk/4-加密视频版权保护 获取播放视频凭证",
                    Toast.LENGTH_LONG
                ).show()
            }
            // 参考：https://help.polyv.net/#/vod/android_player_sdk/4-加密视频版权保护
            // 通过网络请求，向您的服务器请求视频播放token
            val token: PLVVodVideoTokenVO? = null
            // 将token返回给播放器
            callback(token)
        }
    }
}