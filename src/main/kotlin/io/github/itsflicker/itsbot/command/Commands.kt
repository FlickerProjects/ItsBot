package io.github.itsflicker.itsbot.command

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register

object Commands {

    val mapping = mutableMapOf(
        "help" to "itsserver",
        "h" to "itsserver"
    )

    private val commands = mapOf(
        ServerCommands to "itsserver",
        AdminCommands to "itsadmin"
    )

    fun init() {
        ConsoleCommand.register()
        commands.forEach { (command, prefix) ->
            command.register()
            command.getCommands().forEach { mapping[it] = prefix }
        }
    }

}