package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    var likes: Int = 0,
    var hidden: Boolean = false,
    val attachment: Attachment? = null,
)

data class Attachment(
    val url: String,
    val type: AttachmentType,
)
