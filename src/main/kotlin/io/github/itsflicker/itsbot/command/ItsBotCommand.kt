package io.github.itsflicker.itsbot.command

import io.github.itsflicker.itsbot.ItsBot
import io.github.itsflicker.itsbot.ItsBot.reload
import io.github.itsflicker.itsbot.util.PERM_ROOT
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission

object ItsBotCommand : SimpleCommand(
    ItsBot,
    "itsbot",
    "ib",
    description = "ItsBot 命令"
) {

    @Handler
    fun CommandSender.handle(vararg args: String) {
        if (hasPermission(PERM_ROOT)) {
            when (args[0].lowercase()) {
                "reload" -> reload()
            }
        }
    }

}