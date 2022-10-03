package io.github.itsflicker.itsbot.command

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandBody(
    val aliases: Array<String> = [],
    val usage: String = "",
    val description: String = ""
)
