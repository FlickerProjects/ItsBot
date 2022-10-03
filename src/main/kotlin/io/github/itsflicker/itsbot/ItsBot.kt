package io.github.itsflicker.itsbot

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.itsflicker.itsbot.command.GroupCommands
import io.github.itsflicker.itsbot.command.ItsBotCommand
import io.github.itsflicker.itsbot.config.IBConfig
import io.github.itsflicker.itsbot.data.BindingData
import io.github.itsflicker.itsbot.ktor.Ktor
import io.github.itsflicker.itsbot.listener.BotMessageListener
import io.github.itsflicker.itsbot.listener.GroupMessageListener
import io.github.itsflicker.itsbot.rcon.ItsRcon
import io.github.itsflicker.itsbot.util.ibChannel
import io.ktor.server.engine.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.utils.info
import java.util.concurrent.TimeUnit

object ItsBot : KotlinPlugin(
    JvmPluginDescription(
        id = "io.github.itsflicker",
        name = "ItsBot",
        version = "0.1.0",
    ) {
        author("ItsFlicker")
    }
) {
    var engine: ApplicationEngine? = null

    val connects = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.SECONDS)
        .removalListener<String, ItsRcon> { _, value, _ ->
            value?.disconnect()
        }
        .buildAsync<String, ItsRcon> { key ->
            if (key.isEmpty()) {
                return@buildAsync null
            }
            val info = IBConfig.servers[key] ?: return@buildAsync null
            kotlin.runCatching { ItsRcon(info.address, info.port, info.password) }.getOrNull()
        }

    override fun PluginComponentStorage.onLoad() {
        logger.info { "ItsBot v${version} loaded." }
    }

    override fun onEnable() {
        reload()
        ItsBotCommand.register()
        GroupCommands.reg()
        GroupMessageListener.reg()

        ibChannel.registerListenerHost(BotMessageListener)

        engine = Ktor.createServer(Ktor.module).start(false)
        logger.info { "ItsBot v${version} enabled." }
    }

    override fun onDisable() {
        engine?.stop(5000, 10000)
    }

    fun reload() {
        IBConfig.reload()
        BindingData.reload()
    }

}