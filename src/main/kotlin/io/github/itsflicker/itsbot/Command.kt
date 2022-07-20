package io.github.itsflicker.itsbot

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission

object Command : SimpleCommand(
    ItsBot,
    "itsbot"
) {

    @Handler
    suspend fun CommandSender.handle(vararg args: String) {
        if (hasPermission(ItsBot.PERM)) {
            when (args[0].lowercase()) {
                "reload" -> { }
            }
        }
    }

}