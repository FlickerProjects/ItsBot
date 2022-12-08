package io.github.itsflicker.itsbot.command

import net.mamoe.mirai.console.command.CommandOwner
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.permission.Permission

abstract class IBCommand(
    owner: CommandOwner,
    primaryName: String,
    vararg secondaryNames: String,
    description: String = "no description available",
    parentPermission: Permission = owner.parentPermission
) : CompositeCommand(owner, primaryName, *secondaryNames, description = description, parentPermission = parentPermission) {

    fun getCommands(): List<String> {
        return this::class.objectInstance!!.javaClass.declaredMethods.filter { it.isAnnotationPresent(SubCommand::class.java) }.map {
            val annotation = it.getAnnotation(SubCommand::class.java)
            annotation.value.toMutableList().ifEmpty { listOf(it.name) }
        }.flatten()
    }

}