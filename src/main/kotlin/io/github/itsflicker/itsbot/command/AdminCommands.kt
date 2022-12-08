package io.github.itsflicker.itsbot.command

import io.github.itsflicker.itsbot.ItsBot
import io.github.itsflicker.itsbot.ItsBot.PERM_EXE_1
import io.github.itsflicker.itsbot.listener.GroupEventListener
import io.github.itsflicker.itsbot.rcon.getRconFromName
import io.github.itsflicker.itsbot.util.*
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import top.e404.skiko.util.toImage

@OptIn(ConsoleExperimentalApi::class)
object AdminCommands : IBCommand(
    ItsBot,
    "itsadmin",
    "ia",
    description = "ItsBot 管理指令",
    parentPermission = PERM_EXE_1
) {

    @SubCommand("command", "cmd")
    @Description("在指定服务器执行命令")
    suspend fun UserCommandSender.cmd(@Name("服务器") server: String, @Name("命令") command: String) {
        val rcon = getRconFromName(server)
        if (rcon == null) {
            sendMessage("No such server.")
        } else {
            rcon.command(command)?.toImage()?.sendAsImage(subject)
        }
    }

    @SubCommand("recall", "ch")
    @Description("撤回ItsBot发送的上条消息")
    suspend fun MemberCommandSender.recall() {
        GroupEventListener.botMessageCache[group.id]?.removeLastOrNull()?.recall()
    }

    @OptIn(MiraiExperimentalApi::class)
    @SubCommand("test")
    suspend fun UserCommandSender.test(@Name("id") id: Int, @Name("args") args: PlainText) {
        when (id) {
            1 -> {
                val text = args.contentToString()

                val message = buildXmlMessage(1) {
                    item(layout = 2) {
                        title("[2c2t]猫窝")
                        summary(text)
                        picture("http://2c2t.cn/assets/img/backgrounds/about.jpg")
                    }

                    source("2c2t")

                    serviceId = 1
                    action = "web"
                    url = "https://http://2c2t.cn/docs/"
                    brief = "测试"
                }
                sendMessage(message)
            }
            2 -> {
                val message = RichMessage.share(
                    "https://http://2c2t.cn/docs/",
                    "测试",
                    "2c2t",
                    "http://2c2t.cn/assets/img/backgrounds/about.jpg"
                )
                sendMessage(message)
            }
            3 -> {
                val text = args.contentToString().toInt()

                val message = SimpleServiceMessage(text, """
                    <?xml version='1.0' encoding='utf-8' standalone='yes'?>
                    <!-- msg 卡片的基本属性 只可以有1个  -->
                    <msg flag='1' serviceID='$text' brief='foobar' templateID='1' action='plugin' >
                     <!-- item 卡片的内容 可重复使用  -->
                     <item layout="0">
                      <title>layout="0"</title>
                            <summary>title,summary,picture各占一行均可重复使用，按照Element顺序显示</summary>
                            <picture cover="http://placekitten.com/250/100"/>
                     </item>
                     <!-- source 卡片的来源，即卡片的角标 只可以有1个  -->
                     <source name="koukuko" icon="https://avatars2.githubusercontent.com/u/3916013?v=3&s=40" url="" action="plugin" appid="-1"/>
                    </msg>
                """.trimIndent())
                sendMessage(message)
            }
        }
    }

}