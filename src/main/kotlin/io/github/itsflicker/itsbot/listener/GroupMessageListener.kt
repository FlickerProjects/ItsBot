package io.github.itsflicker.itsbot.listener

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.itsflicker.itsbot.command.GroupCommands
import io.github.itsflicker.itsbot.config.IBConfig
import io.github.itsflicker.itsbot.data.BindingData
import io.github.itsflicker.itsbot.util.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupEvent
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.buildForwardMessage
import net.mamoe.mirai.message.data.buildMessageChain
import java.util.concurrent.TimeUnit


object GroupMessageListener {

    private val regex = IBConfig.options.prefix.toRegex()
    private val groupChannel = ibChannel
        .filterIsInstance<GroupEvent>()
        .filter { it.bot.id in IBConfig.options.bots }
        .filter { it.group.id in IBConfig.options.groups }

    val captchaCache = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build<String, Pair<String, String>>()

    fun reg() {
        // Keywords
        groupChannel.subscribeGroupMessages {
            IBConfig.keywords.forEach { map ->
                map["keyword"]!!.toString().toRegex().findingReply {
                    if (sender is Bot) return@findingReply Unit
                    map["action"]?.asList()?.forEach { action ->
                        when {
                            action.startsWith("mute") -> {
                                sender.mute(action.substring(5).toIntOrNull() ?: 600)
                            }
                            action == "recall" -> {
                                message.recall()
                            }
                        }
                    }
                    map["reply"]?.asList()?.let { reply ->
                        when {
                            reply[0].startsWith("<image>") -> {
                                reply.joinToString("\n") { it.removePrefix("<image>") }.uploadAsImage(sender)
                            }
                            reply[0].startsWith("<forward>") ->{
                                buildForwardMessage {
                                    reply.forEach {
                                        sender says it.removePrefix("<forward>")
                                    }
                                }
                            }
                            else -> reply.joinToString("\n")
                        }
                    }
                }
            }
        }
        // Commands
        groupChannel.subscribeGroupMessages {
            regex.matchingReply { result ->
                if (sender is Bot) return@matchingReply Unit
                val commands = result.groupValues[1].split(' ')
                GroupCommands.handlers.getOrDefault(commands[0].lowercase()) {
                    listOf("Unknown Command.", "你是故意找茬是吧").random()
                }(this, commands) ?: Unit
            }
        }
        // Join message
        groupChannel.subscribeAlways<MemberJoinEvent> {
            val message = IBConfig.events["join"]?.asList() ?: return@subscribeAlways
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
        // Auto accept
        GlobalEventChannel.subscribeAlways<MemberJoinRequestEvent> {
            val keywords = IBConfig.events["request"]?.asList() ?: return@subscribeAlways
            val message = message.substringAfterLast("答案：").trimIndent()
            if (keywords.any { message.contains(it, ignoreCase = true) }) {
                accept()
            }
        }
        // Captcha
        groupChannel.subscribeGroupMessages {
            content { it.length == 4 && captchaCache.getIfPresent(it) != null }.invoke {
                val player = captchaCache.getIfPresent(it)!!
                val responses = getAllRcons().mapNotNull { rcon ->
                    rcon.command("ibb bind ${player.second} ${sender.id}")?.trimIndent()
                }
                if ("OK" in responses) {
                    group.sendMessage(message.quote() + "成功将${player.first}与${sender.id}绑定!".uploadAsImage(sender))
                    BindingData.bindings.computeIfAbsent(sender.id) { ArrayList() } += player.first
                    captchaCache.invalidate(it)
                } else {
                    group.sendMessage(message.quote() + "绑定失败, 请稍后再试!".uploadAsImage(sender))
                }
            }
        }
    }

}