package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.*

interface PostRepository {
//    val data: Flow<List<Post>>
    val data: Flow<PagingData<FeedItem>>
    suspend fun getAll()
    suspend fun getNewer(id: Long)
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun removeById(id: Long): Boolean
    suspend fun likeById(post: Post): Boolean
    suspend fun showAll()
    suspend fun upload(upload: MediaUpload): Media
    suspend fun login(name: String, pass: String): LoginData
}

