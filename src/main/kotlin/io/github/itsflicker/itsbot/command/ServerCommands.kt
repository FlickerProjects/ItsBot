package io.github.itsflicker.itsbot.command

import io.github.itsflicker.itsbot.ItsBot
import io.github.itsflicker.itsbot.ItsBot.PERM_EXE_USER
import io.github.itsflicker.itsbot.data.IBConfig
import io.github.itsflicker.itsbot.image.list.ListCreator
import io.github.itsflicker.itsbot.rcon.getRconFromName
import io.github.itsflicker.itsbot.rcon.getRconFromPlayer
import io.github.itsflicker.itsbot.rcon.onlinePlayers
import io.github.itsflicker.itsbot.util.exceptionHandler
import io.github.itsflicker.itsbot.util.sendAsImage
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeoutOrNull
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.nameCardOrNick
import top.e404.skiko.util.toImage

@OptIn(ConsoleExperimentalApi::class)
object ServerCommands : IBCommand(
    ItsBot,
    "itsserver",
    "is",
    description = "ItsBot 服务器指令",
    parentPermission = PERM_EXE_USER
) {

    @SubCommand("zx", "list")
    @Description("列出在线玩家")
    suspend fun UserCommandSender.list() {
        val jobs = mutableListOf<Deferred<Pair<String, List<String>>?>>()
        IBConfig.servers.keys.forEach {
            jobs += ItsBot.async(exceptionHandler) { list(it) }
        }
        val servers = mutableMapOf<String, List<String>>()
        var count = 0
        jobs.forEach { job ->
            kotlin.runCatching {
                job.await()?.let {
                    servers[it.first] = it.second
                    count += it.second.size
                }
            }
        }
        ListCreator.generate(mapOf(
            "title" to "服务器维修，即将开服，敬请期待", // 共 $count 在线
            "servers" to servers
        )).sendAsImage(subject)
    }

    @SubCommand("baltop", "balancetop")
    @Description("查看经济排行榜")
    suspend fun UserCommandSender.balancetop(@Name("服务器") server: String) {
        val rcon = getRconFromName(server)
        if (rcon == null) {
            sendMessage("No such server.")
        } else {
            (rcon.command("baltop") + rcon.command("baltop 2")).toImage().sendAsImage(subject)
        }
    }

    @SubCommand("tell")
    @Description("向服内玩家发送消息")
    suspend fun UserCommandSender.tell(@Name("玩家") player: String, @Name("消息") message: String) {
        val rcon = getRconFromPlayer(player)
        if (rcon == null) {
            sendMessage("No such player.")
        } else {
            rcon.command("ibb send $player \"${user.nameCardOrNick}(${user.id})\" $message")
        }
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

    private suspend fun list(id: String): Pair<String, List<String>>? {
        return withTimeoutOrNull(5000L) {
            val list = onlinePlayers(id)
            val count = list.count()
            val name = IBConfig.servers[id]!!.alias[0] + "($count)"
            name to list
        }
    }

}