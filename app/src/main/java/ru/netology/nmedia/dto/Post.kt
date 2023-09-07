package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType

sealed interface FeedItem {
    val id: Long
}

data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    var likes: Int = 0,
    var hidden: Boolean = false,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
): java.io.Serializable, FeedItem

data class Ad(
    override val id: Long,
    val image: String,
): FeedItem

data class Attachment(
    val url: String,
    val type: AttachmentType,
)
