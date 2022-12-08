package io.github.itsflicker.itsbot.listener

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.itsflicker.itsbot.data.IBConfig
import io.github.itsflicker.itsbot.util.asList
import io.github.itsflicker.itsbot.util.exceptionHandler
import io.github.itsflicker.itsbot.util.getAvatar
import io.github.itsflicker.itsbot.util.reader
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.sourceIds

object GroupEventListener : SimpleListenerHost(exceptionHandler) {

    val botMessageCache = mutableMapOf<Long, MutableList<MessageReceipt<*>>>()
    val imageCachePool: Cache<Long, String> = Caffeine.newBuilder().maximumSize(50).build()

    @EventHandler
    suspend fun GroupMessageEvent.onEvent() {
        if (check(bot, group)) {
            return
        }
        cacheMessageImage(this)
    }

    @EventHandler
    suspend fun GroupMessagePostSendEvent.onEvent() {
        if (check(bot, target)) {
            return
        }
        val target = receipt?.target ?: return
        val list = botMessageCache.computeIfAbsent(target.id) { ArrayList() }
        if (list.size >= 100) {
            list.removeFirst()
        }
        list += receipt!!
        cacheMessageImage(this)
    }

    @EventHandler
    suspend fun MemberJoinRequestEvent.onEvent() {
        if (check(bot, group ?: return)) {
            return
        }
        val keywords = IBConfig.events["request"]?.asList() ?: return
        val message = message.substringAfterLast("答案：").trimIndent()
        if (keywords.any { message.contains(it, ignoreCase = true) }) {
            accept()
        }
    }

    @EventHandler
    suspend fun MemberJoinEvent.onEvent() {
        if (check(bot, group)) {
            return
        }
        val message = IBConfig.events["join"]?.asList() ?: return
        group.sendMessage(buildMessageChain {
            message.forEachIndexed { index, string ->
                reader.readToFlatten(string).forEach { part ->
                    if (part.isVariable) {
                        when (part.text) {
                            "at" -> + At(member)
                            "avatar" -> + member.getAvatar()
                        }
                    } else {
                        + part.text
                    }
                }
                if (index < message.lastIndex) {
                    + "\n"
                }
            }
        })
    }

    @EventHandler
    suspend fun MemberLeaveEvent.onEvent() {
        if (check(bot, group)) {
            return
        }
        group.sendMessage(buildMessageChain {
            + member.getAvatar()
            + "\n"
            + member.nick
            if (member.nameCard.isNotEmpty()) {
                + " | ${member.nameCard}"
            }
            + "(${member.id})离开了本群"
        })
    }

    private fun check(bot: Bot, group: Group): Boolean {
        return bot.id !in IBConfig.options.bots || group.id !in IBConfig.options.groups
    }

    private suspend fun cacheMessageImage(e: GroupMessageEvent) {
        e.message.findIsInstance<Image>()?.let {
            val id = e.group.id + e.message.ids[0]
            imageCachePool.put(id, it.queryUrl())
        }
    }

    private suspend fun cacheMessageImage(e: GroupMessagePostSendEvent) {
        e.message.findIsInstance<Image>()?.let {
            val id = e.target.id + e.receipt!!.sourceIds[0]
            imageCachePool.put(id, it.queryUrl())
        }
    }

}