package io.github.itsflicker.itsbot.data

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object BindingData : AutoSavePluginData("bindings") {
    val bindings: MutableMap<Long, MutableList<String>> by value()
}