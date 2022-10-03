package io.github.itsflicker.itsbot.util

import io.github.itsflicker.itsbot.ItsBot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.awt.Color
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL
import javax.imageio.ImageIO

val font: Font =
    Font.createFont(Font.TRUETYPE_FONT, ItsBot.getResourceAsStream("font/LXGWWenKai-Bold.ttf")).deriveFont(Font.BOLD, 30F)
val frc =
    FontRenderContext(AffineTransform(), true, true)
val stringHeight =
    font.getStringBounds("字", frc).bounds.height + 15

fun createImage(text: String, pt: Int = 40, pl: Int = 30): InputStream {
    reader.readToFlatten(text).map {
        
    }
    val list = text.split("\n").filter { it.isNotBlank() }

    val height = list.size * stringHeight + pt * 2
    val width = list.maxOf { it.width() } + pl * 2

    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_BGR)

    val g = image.createGraphics()
    g.color = Color(31, 27, 29) // 背景色
    g.fillRect(0, 0, width, height)
    g.color = Color.WHITE // 字体颜色
    g.font = font // 设置画笔字体

    list.forEachIndexed { index, string ->
        val i = index + 1
        when {
            string.startsWith("<c>") -> {
                val str = string.substring(3)
                g.drawString(str, width / 2 - str.width() / 2, pt / 2 + stringHeight * i)
            }
            else -> g.drawString(string, pl, pt / 2 + stringHeight * i)
        }
    }

    g.dispose()

    return ByteArrayOutputStream().apply { use {
        ImageIO.write(image, "png", it)
    } }.let { ByteArrayInputStream(it.toByteArray()) }
}

private fun String.width() =
    font.getStringBounds(this, frc).bounds.width

suspend fun String.uploadAsImage(contact: Contact, pt: Int = 20, pl: Int = 10) =
    createImage(this, pt, pl).uploadAsImage(contact)

suspend fun Contact.getAvatar(): Image {
    val input = withContext(Dispatchers.IO) {
        URL(avatarUrl).openStream()
    }
    return uploadImage(input)
}