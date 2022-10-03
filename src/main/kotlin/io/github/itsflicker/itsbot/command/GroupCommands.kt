package io.github.itsflicker.itsbot.command

import io.github.itsflicker.itsbot.ItsBot
import io.github.itsflicker.itsbot.config.IBConfig
import io.github.itsflicker.itsbot.listener.BotMessageListener
import io.github.itsflicker.itsbot.util.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeoutOrNull
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.event.events.GroupMessageEvent

private typealias CommandHandler = suspend GroupMessageEvent.(List<String>) -> Any?

object GroupCommands {

    val handlers = mutableMapOf<String, CommandHandler>()

    var helpMessage = "ItsBot v${ItsBot.version}\n"
        private set

    private val handler = CoroutineExceptionHandler { _, throwable ->
        ItsBot.logger.error(throwable)
    }

    @Suppress("UNCHECKED_CAST")
    fun reg() {
        handlers.clear()
        this::class.objectInstance!!.javaClass.declaredFields.filter { it.isAnnotationPresent(CommandBody::class.java) }.forEach {
            val annotation = it.getAnnotation(CommandBody::class.java)
            handlers[it.name] = it.get(this::class.objectInstance!!) as CommandHandler
            annotation.aliases.forEach { alias ->
                handlers[alias] = it.get(this::class.objectInstance!!) as CommandHandler
            }
            if (annotation.usage.isNotEmpty()) {
                helpMessage += annotation.usage + " "
                helpMessage += annotation.description + "\n"
            }
        }
    }

    @CommandBody(["h", "?"])
    val help: CommandHandler = { helpMessage.uploadAsImage(sender) }

    @CommandBody(["op"], "!admin <subcommand> [args]", "管理员命令")
    val admin: CommandHandler = reply@ {
        if (sender.permission.level >= 1) {
            when (it.getOrElse(1) { "help" }.lowercase()) {
                "command", "cmd" -> {
                    val server = it.getOrNull(2) ?: return@reply "Please specify server."
                    val rcon = getRconFromName(server) ?: return@reply "No such server."
                    val command = subList(it, 3).ifEmpty { return@reply "Please type command." }.joinToString(" ")
                    rcon.command(command)?.uploadAsImage(sender)
                }
                "recall", "ch" -> {
                    BotMessageListener.botMessageCache.removeLastOrNull()?.recall()
                }
                else -> Unit
            }
        } else {
            "You have no permission."
        }
    }

    @CommandBody(["online", "zx", "在线"], "!list", "列出所有在线玩家")
    val list: CommandHandler = reply@ {
        val jobs = mutableListOf<Deferred<Pair<String, Int>?>>()
        IBConfig.servers.keys.forEach {
            jobs += ItsBot.async(handler) { list(it) }
        }
        val builder = StringBuilder()
        var count = 0
        jobs.forEach { job ->
            kotlin.runCatching {
                job.await()?.let {
                    builder.appendLine(it.first)
                    count += it.second
                }
            }
        }
        builder.insert(0, "<c>共 $count 在线\n")
        builder.toString().uploadAsImage(sender, pt = 60, pl = 50)
    }

    @CommandBody(["balancetop"], "!baltop <server>", "列出指定服务器的经济排行榜")
    val baltop: CommandHandler = reply@ {
        val server = it.getOrNull(1) ?: return@reply "Please specify server."
        val rcon = getRconFromName(server) ?: return@reply "No such server."
        (rcon.command("baltop") + rcon.command("baltop 2")).uploadAsImage(sender)
    }

    @CommandBody(["msg", "sl"], "!tell <player> <message>", "向服内玩家发送消息")
    val tell: CommandHandler = reply@ {
        val player = it.getOrNull(1) ?: return@reply "Please specify player."
        val rcon = getRconFromPlayer(player) ?: return@reply "No such player."
        val message = subList(it, 2).ifEmpty { return@reply "Please type message." }.joinToString(" ")
        rcon.command("ibb send $player \"$senderName(${sender.id})\" $message")
        Unit
    }

//    @CommandBody(["cmd", "ml"], "!command <command>", "使用绑定玩家发送命令")
//    val command: CommandHandler = reply@ {
//        val player = BindingData.bindings[sender.id] ?: return@reply "Bind first."
//        val rcon = getRconFromPlayer(player) ?: return@reply "You are not online."
//        val command = subList(it, 1).joinToString(" ")
//        rcon.command("it simplekether -sender $player -source \"command \"$command\"\"")
//    }
//
//    @CommandBody(["dc"], "!logout", "将绑定玩家登出游戏")
//    val logout: CommandHandler = reply@ {
//        val player = BindingData.bindings[sender.id] ?: return@reply "Bind first."
//        val rcon = getRconFromPlayer(player) ?: return@reply "You are not online."
//        rcon.command("kick $player")
//    }

    private suspend fun list(id: String): Pair<String, Int>? {
        return withTimeoutOrNull(5000L) {
            val list = onlinePlayers(id)
            val count = list.count()
            val string = IBConfig.servers[id]!!.alias[0] + "($count)\n    "
            string + list.joinToString("\n    ") to count
        }
    }
}