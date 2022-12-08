package io.github.itsflicker.itsbot.listener

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.itsflicker.itsbot.command.Commands
import io.github.itsflicker.itsbot.data.BindingData
import io.github.itsflicker.itsbot.data.IBConfig
import io.github.itsflicker.itsbot.rcon.getAllRcons
import io.github.itsflicker.itsbot.util.*
import io.ktor.client.request.*
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupEvent
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import top.e404.skiko.frame.encodeToBytes
import top.e404.skiko.handler.handlers
import top.e404.skiko.util.toImage
import java.util.concurrent.TimeUnit

object GroupMessageListener {

    private val regex by unsafeLazy { IBConfig.options.prefix.toRegex() }
    private val groupChannel = GlobalEventChannel
        .filterIsInstance<GroupEvent>()
        .filter { it.bot.id in IBConfig.options.bots }
        .filter { it.group.id in IBConfig.options.groups }

    val captchaCache: Cache<String, Pair<String, String>> = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build()

    @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
    fun init() {
        // Commands
        groupChannel.subscribeGroupMessages {
            regex.matchingReply { matchResult ->
                val commands = matchResult.groupValues[1].split(' ')
                val prefix = Commands.mapping[commands[0].lowercase()]
                    ?: return@matchingReply listOf("Unknown Command.", "你是故意找茬是吧").random()
                val command = buildMessageChain {
                    append(CommandManager.commandPrefix)
                    append(prefix)
                    append(' ')
                    append(matchResult.groupValues[1])
                }
                parseCommand(toCommandSender(), command)
            }
        }
        // Handlers
        groupChannel.subscribeGroupMessages {
            always {
                val pt = message.findIsInstance<PlainText>() ?: return@always
                val text = pt.content.trimIndent()
                val hasArgs = text.indexOf(' ') != -1
                for (handler in handlers) {
                    if (handler.regex.matches(text.substringBefore(' '))) {
                        val qr = message.findIsInstance<QuoteReply>()
                        val at = message.findIsInstance<At>()
                        var url: String? = null
                        if (qr != null) {
                            url = GroupEventListener.imageCachePool.getIfPresent(group.id + qr.source.ids[0])
                        }
                        if (url == null) {
                            url = (at?.let { group[it.target] } ?: sender).avatarUrl
                        }
                        val args = if (hasArgs) {
                            val source = text.substringAfter(' ').let {
                                if (it.startsWith('-')) "0 $it" else it
                            }
                            val de = Demand(source)
                            de.dataMap.apply {
                                putAll(de.tags.associateWith { "true" })
                                put("text", de.namespace)
                            }
                        } else {
                            mutableMapOf()
                        }
                        val result = handler.handleBytes(httpClient.get(url), args)
                        result.result?.encodeToBytes()?.sendAsImage(subject)
                        break
                    }
                }
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
                    group.sendMessage(message.quote() + "成功将${player.first}与${sender.id}绑定!".toImage().uploadAsImage(sender))
                    BindingData.bindings.computeIfAbsent(sender.id) { ArrayList() } += player.first
                    captchaCache.invalidate(it)
                } else {
                    group.sendMessage(message.quote() + "绑定失败, 请稍后再试!".toImage().uploadAsImage(sender))
                }
            }
        }
        // Keywords
        groupChannel.subscribeGroupMessages {
            content { sender.id !in IBConfig.options.bots }.invoke {
                val text = message.contentToString()
                for (map in IBConfig.keywords) {
                    if (text.contains(map["keyword"]!!.toString().toRegex())) {
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
                                    reply.joinToString("\n") { text ->
                                        text.removePrefix("<image>")
                                    }.toImage().uploadAsImage(sender)
                                }
                                reply[0].startsWith("<forward>") ->{
                                    buildForwardMessage {
                                        reply.forEach { text ->
                                            sender says text.removePrefix("<forward>")
                                        }
                                    }
                                }
                                else -> PlainText(reply.joinToString("\n"))
                            }
                        }?.let { result -> group.sendMessage(result) }
                        break
                    }
                }
            }
        }
    }

}