package net.polyv.android.player.common.ui.router

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.Intent
import android.os.Bundle
import net.polyv.android.player.business.scene.common.model.vo.PLVMediaResource
import net.polyv.android.player.sdk.foundation.app.PLVApplicationContext
import net.polyv.android.player.sdk.foundation.lang.proxyNoOp
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Hoshiiro
 */
object PLVMediaPlayerRouter {

    @JvmField
    val KEY_TARGET_MEDIA_RESOURCE: String = "key_target_media_resource"

    @JvmField
    val KEY_TARGET_MEDIA_RESOURCE_LIST_HOLDER_ID: String = "key_target_media_resource_list_holder_id"

    @JvmField
    val KEY_ENTER_FROM_FLOAT_WINDOW: String = "key_enter_from_float_window"

    @JvmField
    val KEY_ENTER_FROM_DOWNLOAD_CENTER: String = "key_enter_from_download_center"

    @JvmField
    val KEY_DOWNLOAD_CENTER_TAB_INDEX: String = "key_download_center_tab_index"

    private val routerMap: MutableMap<Class<out RouterDestination<*>>, Class<out Activity>> = mutableMapOf()
    private val activities: MutableMap<Class<out Activity>, MutableList<WeakReference<out Activity>>> = mutableMapOf()

    init {
        observeActivityLifecycle()
    }

    @JvmStatic
    fun Context.router(
        destination: RouterDestination<out RouterPayload>
    ): Intent {
        val clazz = routerMap[destination::class.java]
            ?: throw IllegalArgumentException("Unknown router class $destination")
        return when (destination) {
            is RouterDestination.DownloadCenter -> {
                Intent(this, clazz).run {
                    putExtra(KEY_DOWNLOAD_CENTER_TAB_INDEX, destination.payload.defaultTabIndex)
                }
            }

            is RouterDestination.SceneSingle -> {
                Intent(this, clazz).run {
                    putExtra(KEY_TARGET_MEDIA_RESOURCE, destination.payload.targetMediaResource)
                    putExtra(KEY_ENTER_FROM_FLOAT_WINDOW, destination.payload.enterFromFloatWindow)
                    putExtra(KEY_ENTER_FROM_DOWNLOAD_CENTER, destination.payload.enterFromDownloadCenter)
                }
            }

            is RouterDestination.SceneFeed -> {
                Intent(this, clazz).run {
                    putExtra(KEY_TARGET_MEDIA_RESOURCE_LIST_HOLDER_ID, destination.payload.mediaResources.id)
                }
            }

            else -> Intent(this, clazz)
        }
    }

    @JvmStatic
    fun Context.routerTo(destination: RouterDestination<out RouterPayload>) {
        startActivity(router(destination))
    }

    @SafeVarargs
    @JvmStatic
    fun finish(vararg destinationClass: Class<out RouterDestination<*>>) {
        destinationClass
            .mapNotNull { routerMap[it] }
            .flatMap { activities[it] ?: emptyList() }
            .forEach { it.get()?.finish() }
    }

    fun register(destination: Class<out RouterDestination<*>>, clazz: Class<out Activity>) {
        routerMap[destination] = clazz
    }

    inline fun <reified T : RouterDestination<*>> register(clazz: Class<out Activity>) {
        register(T::class.java, clazz)
    }

    private fun observeActivityLifecycle() {
        (PLVApplicationContext.applicationContext as? Application)?.registerActivityLifecycleCallbacks(
            object : ActivityLifecycleCallbacks by proxyNoOp() {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    val list = activities.getOrPut(activity.javaClass) { mutableListOf() }
                    list.add(WeakReference(activity))
                }

                override fun onActivityDestroyed(activity: Activity) {
                    val list = activities.getOrPut(activity.javaClass) { mutableListOf() }
                    list.removeIf { it.get() == null || it.get() == activity }
                }
            }
        )
    }

}

sealed class RouterDestination<T : RouterPayload>(
    val payload: T
) {

    object Entrance : RouterDestination<RouterPayload.EntrancePayload>(RouterPayload.EntrancePayload)

    class DownloadCenter(
        payload: RouterPayload.DownloadCenterPayload
    ) : RouterDestination<RouterPayload.DownloadCenterPayload>(payload)

    class SceneSingle(
        payload: RouterPayload.SceneSinglePayload
    ) : RouterDestination<RouterPayload.SceneSinglePayload>(payload)

    class SceneFeed(
        payload: RouterPayload.SceneFeedPayload
    ) : RouterDestination<RouterPayload.SceneFeedPayload>(payload)

}

sealed class RouterPayload {

    object EntrancePayload : RouterPayload()

    data class DownloadCenterPayload @JvmOverloads constructor(
        val defaultTabIndex: Int = 0
    ) : RouterPayload()

    data class SceneSinglePayload @JvmOverloads constructor(
        val targetMediaResource: PLVMediaResource,
        val enterFromFloatWindow: Boolean = false,
        val enterFromDownloadCenter: Boolean = false
    ) : RouterPayload()

    data class SceneFeedPayload(
        val mediaResources: RouterPayloadStaticHolder<List<PLVMediaResource>>
    ) : RouterPayload()

}

data class RouterPayloadStaticHolder<T>(
    val value: T,
    val id: Int
) {

    companion object {
        private val idGenerator = AtomicInteger(1)
        private val storage = mutableMapOf<Int, RouterPayloadStaticHolder<*>>()

        @JvmStatic
        fun <T> create(value: T): RouterPayloadStaticHolder<T> {
            return RouterPayloadStaticHolder(value, idGenerator.getAndIncrement())
                .also { storage[it.id] = it }
        }

        @JvmStatic
        fun <T> get(id: Int): RouterPayloadStaticHolder<T>? {
            return storage[id] as RouterPayloadStaticHolder<T>?
        }

        @JvmStatic
        fun <T> remove(id: Int): RouterPayloadStaticHolder<T>? {
            return storage.remove(id) as RouterPayloadStaticHolder<T>?
        }
    }

}