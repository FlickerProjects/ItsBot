package io.github.itsflicker.itsbot.util

import io.github.itsflicker.itsbot.ItsBot
import io.github.itsflicker.itsbot.config.IBConfig
import io.github.itsflicker.itsbot.rcon.ItsRcon
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

fun <T> subList(list: List<T>, start: Int = 0, end: Int = list.size): List<T> {
    return list.filterIndexed { index, _ -> index in start until end }
}

fun Any.asList(): List<String> {
    return if (this is Collection<*>) map { it.toString() } else listOf(toString())
}

fun <T> CompletableFuture<T>.getOrNull(millis: Long): T? {
    return kotlin.runCatching { get(millis, TimeUnit.MILLISECONDS) }.getOrDefault(null)
}

fun getAllRcons(): Collection<ItsRcon> =
    IBConfig.servers.keys.map { ItsBot.connects.get(it) }.mapNotNull { it.getOrNull(5000L) }

fun getRconFromName(name: String): ItsRcon? {
    return ItsBot.connects[name].get()
        ?: ItsBot.connects[IBConfig.servers.entries.firstOrNull { name in it.value.alias }?.key ?: ""].getOrNull(3000)
}

fun onlinePlayers(server: String): List<String> {
    return ItsBot.connects[server].get().command("minecraft:list")?.substringAfter(": ", "")?.let {
        if (it.isBlank()) emptyList()
        else it.split(", ")
    } ?: emptyList()
}

fun getRconFromPlayer(name: String): ItsRcon? =
    ItsBot.connects[IBConfig.servers.keys.firstOrNull { onlinePlayers(it).contains(name) } ?: ""].getOrNull(3000)