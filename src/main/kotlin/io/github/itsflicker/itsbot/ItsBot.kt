package io.github.itsflicker.itsbot

import io.github.itsflicker.itsbot.command.Commands
import io.github.itsflicker.itsbot.data.BindingData
import io.github.itsflicker.itsbot.data.IBConfig
import io.github.itsflicker.itsbot.listener.GroupEventListener
import io.github.itsflicker.itsbot.listener.GroupMessageListener
import io.github.itsflicker.itsbot.util.unsafeLazy
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.registerTo
import net.mamoe.mirai.utils.info

object ItsBot : KotlinPlugin(
    JvmPluginDescription(
        id = "io.github.itsflicker",
        name = "ItsBot",
        version = "0.2.0",
    ) {
        author("ItsFlicker")
    }
) {
    val PERM_ROOT by unsafeLazy { PermissionService.INSTANCE.register(permissionId("root"), "根权限") }
    val PERM_EXE_1 by unsafeLazy { PermissionService.INSTANCE.register(permissionId("execute_1"), "一级执行权限", parent = PERM_ROOT) }
    val PERM_EXE_2 by unsafeLazy { PermissionService.INSTANCE.register(permissionId("execute_2"), "二级执行权限", parent = PERM_EXE_1) }
    val PERM_EXE_3 by unsafeLazy { PermissionService.INSTANCE.register(permissionId("execute_3"), "三级执行权限", parent = PERM_EXE_2) }
    val PERM_EXE_MEMBER by unsafeLazy { PermissionService.INSTANCE.register(permissionId("execute_member"), "群成员执行权限", parent = PERM_EXE_3) }
    val PERM_EXE_USER by unsafeLazy { PermissionService.INSTANCE.register(permissionId("execute_user"), "所有用户执行权限", parent = PERM_EXE_MEMBER) }

    override fun PluginComponentStorage.onLoad() {
        logger.info { "ItsBot v${version} loaded." }
    }

    override fun onEnable() {
        init()
        Commands.init()
        GroupMessageListener.init()
        GroupEventListener.registerTo(GlobalEventChannel)
        Ktor.engine = Ktor.createServer(Ktor.module).start(false)
        logger.info { "ItsBot v${version} enabled." }
    }

    override fun onDisable() {
        Ktor.engine?.stop(5000, 10000)
    }

    fun reload() {
        IBConfig.reload()
        BindingData.reload()
    }

    private fun init() {
        AbstractPermitteeId.AnyMemberFromAnyGroup.permit(PERM_EXE_MEMBER)
        AbstractPermitteeId.AnyUser.permit(PERM_EXE_USER)
        reload()
    }

}