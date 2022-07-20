package io.github.itsflicker.itsbot

import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent

object Listener {

    val channel = GlobalEventChannel.filter { it is GroupMessageEvent }

}