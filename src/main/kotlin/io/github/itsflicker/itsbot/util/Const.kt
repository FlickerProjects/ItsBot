package io.github.itsflicker.itsbot.util

import io.github.itsflicker.itsbot.ItsBot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.event.globalEventChannel

val ibScope = CoroutineScope(SupervisorJob())
val ibChannel = ibScope.globalEventChannel()

val PERM_ROOT by lazy { PermissionService.INSTANCE.register(ItsBot.permissionId("root"), "ItsBot管理权限") }

val reader = VariableReader()