package io.github.itsflicker.itsbot.listener

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.MessagePostSendEvent
import net.mamoe.mirai.message.MessageReceipt

object BotMessageListener : ListenerHost {

    val botMessageCache = mutableListOf<MessageReceipt<*>>()

    @EventHandler
    fun MessagePostSendEvent<out Contact>.onSend() {
        if (receipt?.target is Group) {
            if (botMessageCache.size >= 100) {
                botMessageCache.removeFirst()
            }
            botMessageCache += receipt!!
        }
    }

}