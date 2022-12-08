package io.github.itsflicker.itsbot.rcon

import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.itsflicker.itsbot.data.IBConfig
import io.github.itsflicker.itsbot.util.getOrNull
import java.util.concurrent.TimeUnit

object RconManager {

    val connects: AsyncLoadingCache<String, CustomRcon> = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.SECONDS)
        .removalListener<String, CustomRcon> { _, value, _ ->
            value?.disconnect()
        }
        .buildAsync { key ->
            if (key.isEmpty()) {
                return@buildAsync null
            }
            val info = IBConfig.servers[key] ?: return@buildAsync null
            kotlin.runCatching { CustomRcon(info.address, info.port, info.password) }.getOrNull()
        }

}

fun getAllRcons(timeout: Long = 5000L): Collection<CustomRcon> =
    IBConfig.servers.keys.map { RconManager.connects.get(it) }.mapNotNull { it.getOrNull(timeout) }

fun onlinePlayers(server: String): List<String> {
    return RconManager.connects[server].get().command("minecraft:list")?.substringAfter(": ", "")?.let {
        if (it.isBlank()) emptyList()
        else it.split(", ")
    } ?: emptyList()
}

fun getRconFromName(name: String): CustomRcon? {
    return RconManager.connects[name].get()
        ?: RconManager.connects[IBConfig.servers.entries.firstOrNull { name in it.value.alias }?.key ?: return null].getOrNull(3000)
}

fun getRconFromPlayer(name: String): CustomRcon? =
    RconManager.connects[IBConfig.servers.keys.firstOrNull { onlinePlayers(it).contains(name) } ?: ""].getOrNull(3000)