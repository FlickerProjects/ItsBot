package io.github.itsflicker.itsbot

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info

object ItsBot : KotlinPlugin(
    JvmPluginDescription(
        id = "io.github.itsflicker",
        name = "ItsBot",
        version = "0.0.1",
    ) {
        author("ItsFlicker")
    }
) {

    val PERM by lazy { PermissionService.INSTANCE.register(permissionId("itsbot"), "ItsBot管理权限") }

    override fun onEnable() {
        Command.register()

        logger.info { "ItsBot loaded" }
    }
}