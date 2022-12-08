package io.github.itsflicker.itsbot.command

import io.github.itsflicker.itsbot.ItsBot
import io.github.itsflicker.itsbot.ItsBot.PERM_EXE_1
import net.mamoe.mirai.console.command.CommandSender

object ConsoleCommand : IBCommand(
    ItsBot,
    "itsbot",
    "ib",
    description = "ItsBot 控制台命令",
    parentPermission = PERM_EXE_1
) {

    @SubCommand
    fun CommandSender.reload() {
        ItsBot.reload()
    }

}