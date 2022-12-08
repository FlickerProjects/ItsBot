package io.github.itsflicker.itsbot.image.list

import io.github.itsflicker.itsbot.image.ImageCreator
import io.github.itsflicker.itsbot.util.asList
import top.e404.skiko.FontType
import top.e404.skiko.draw.element.Text
import top.e404.skiko.draw.element.TextList
import top.e404.skiko.draw.element.TextWithIcon
import top.e404.skiko.draw.toImage

object ListCreator : ImageCreator {

    private val font = FontType.LW_BOLD.getSkiaFont(30F)

    override suspend fun generate(args: Map<String, Any>): ByteArray {
        val title = args["title"]!!.toString()
        val servers = (args["servers"]!! as Map<*, *>).map { (name, players) ->
            listOf(
                TextWithIcon(
                    content = name!!.toString(),
                    font = font,
                    udPadding = 15
                ),
                TextList(
                    contents = players!!.asList(),
                    font = font,
                    udPadding = 15,
                    index = ""
                )
            )
        }
        val list = listOf(
            Text(
                content = title,
                font = font,
                udPadding = 15,
                center = true
            )
        ) + servers.flatten()
        return list.toImage(
            imagePadding = 50
        )
    }

}