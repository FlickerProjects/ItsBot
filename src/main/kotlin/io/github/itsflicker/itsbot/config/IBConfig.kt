package io.github.itsflicker.itsbot.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object IBConfig : AutoSavePluginConfig("config") {
    override val serializersModule = SerializersModule {
        contextual(Options::class, Options.serializer())
        contextual(RconInfo::class, RconInfo.serializer())
    }

    val options: Options by value(Options())
    val servers: Map<String, RconInfo> by value()
    val keywords: List<Map<String, Any>> by value()
    val events: Map<String, Any> by value()
}

@Serializable
data class Options(
    val host: String = "localhost",
    val port: Int = 2000,
    val prefix: String = "^[!！] ?(.*)",
    val bots: List<Long> = emptyList(),
    val groups: List<Long> = emptyList()
)

@Serializable
data class RconInfo(
    val alias: List<String>,
    val address: String,
    val port: Int,
    val password: String
)