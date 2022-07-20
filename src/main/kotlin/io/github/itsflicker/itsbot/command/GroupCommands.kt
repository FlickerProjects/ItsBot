package io.github.itsflicker.itsbot.command

import io.github.itsflicker.itsbot.*
import io.github.itsflicker.itsbot.config.IBConfig
import io.github.itsflicker.itsbot.data.BindingData
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeoutOrNull
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent

private typealias CommandHandler = suspend MessageEvent.(List<String>) -> Any

object Commands {

    val handlers = mutableMapOf<String, CommandHandler>()

    var helpMessage = "ItsBot v${ItsBot.version}\n"
        private set

    private val handler = CoroutineExceptionHandler { _, throwable ->
        ItsBot.logger.debug(throwable)
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

    @CommandBody
    val admin: CommandHandler = reply@ {
        if (sender.permission.level >= 1) {
            if ()
        }
    }

    @CommandBody(["bd", "绑定"], "!bind <player> <key>", "绑定QQ和玩家")
    val bind: CommandHandler = reply@ {
        val player = it.getOrNull(1) ?: return@reply "Type player name."
        if (BindingData.bindings.values.any { it.equals(player, ignoreCase = true) }) {
            return@reply "Another member has bound this player."
        }
        val key = it.getOrNull(2) ?: return@reply "Type key."
        if (!player.reversed().md5().substring(20, 24).reversed().equals(key, ignoreCase = true)) {
            return@reply "Wrong key."
        }
        if (ItsBot.connects.values.any { rcon ->
                rcon.command("ft simplekether -source \"tell \$ server.getOfflinePlayer(\"$player\").hasPlayedBefore()\"").trimIndent().toBoolean()
            }) {
            BindingData.bindings[sender.id] = player
            "Bind successfully."
        } else {
            "No such player."
        }
    }

    @CommandBody(["online", "zx", "在线"], "!list", "列出所有在线玩家")
    val list: CommandHandler = reply@ {
        val jobs = mutableListOf<Deferred<Pair<String, Int>?>>()
        ItsBot.connects.keys.forEach {
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
        builder.insert(0, "<center>共 $count 在线\n")
        builder.toString().uploadAsImage(sender)
    }

    @CommandBody(["balancetop"], "!baltop <server>", "列出指定服务器的经济排行榜")
    val baltop: CommandHandler = reply@ {
        val server = it.getOrNull(1) ?: return@reply "Please specify server."
        val rcon = getRcon(server) ?: return@reply "No such server."
        (rcon.command("baltop") + rcon.command("baltop 2")).uploadAsImage(sender)
    }

    @CommandBody(["msg", "sl"], "!tell <player> <message>", "向服内玩家发送消息")
    val tell: CommandHandler = reply@ {
        val player = it.getOrNull(1) ?: return@reply "Please specify player."
        val rcon = player.rcon ?: return@reply "No such player."
        val message = subList(it, 2).ifEmpty { return@reply "Please type message." }.joinToString(" ")
        rcon.command("tellraw $player [{\"text\":\"$senderName(${sender.id})\",\"color\":\"gold\"},{\"text\":\" 对你说: \",\"color\":\"gray\"},{\"text\":\"$message\"}]")
        Unit
    }

    @CommandBody(["cmd", "ml"], "!command <command>", "使用绑定玩家发送命令")
    val command: CommandHandler = reply@ {
        val player = BindingData.bindings[sender.id] ?: return@reply "Bind first."
        val rcon = player.rcon ?: return@reply "You are not online."
        val command = subList(it, 1).joinToString(" ")
        rcon.command("ft simplekether -sender $player -source \"command \"$command\"\"")
    }

    @CommandBody(["dc"], "!logout", "将绑定玩家登出游戏")
    val logout: CommandHandler = reply@ {
        val player = BindingData.bindings[sender.id] ?: return@reply "Bind first."
        val rcon = player.rcon ?: return@reply "You are not online."
        rcon.command("kick $player")
    }

    private suspend fun list(id: String): Pair<String, Int>? {
        return withTimeoutOrNull(5000L) {
            val list = onlinePlayers(id)
            val count = list.count()
            val string = IBConfig.servers.first { it["id"] == id }["alias"].toString() + "($count)\n    "
            string + list.joinToString("\n    ") to count
        }
    }
}