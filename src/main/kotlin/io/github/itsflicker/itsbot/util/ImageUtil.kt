package io.github.itsflicker.itsbot.util

import io.ktor.client.request.*
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.io.ByteArrayInputStream
import java.io.InputStream

suspend fun ByteArray.uploadAsImage(contact: Contact) = ByteArrayInputStream(this).use { it.uploadAsImage(contact) }

suspend fun ByteArray.sendAsImage(contact: Contact) = contact.sendMessage(uploadAsImage(contact))

suspend fun Contact.getAvatar() = httpClient.get<InputStream>(avatarUrl).use { uploadImage(it) }