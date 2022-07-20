package io.github.itsflicker.itsbot.config

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object Config : AutoSavePluginConfig("config.yml") {
    val groups: List<Long> by value()
}