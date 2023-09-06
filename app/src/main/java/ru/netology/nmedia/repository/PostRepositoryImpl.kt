package ru.netology.nmedia.repository

import androidx.paging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: ApiService,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
    ) : PostRepository {
//    override val data = dao.getAll()
//        .map(List<PostEntity>::toDto)
//        .flowOn(Dispatchers.Default)

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { dao.getPagingSource() },
        remoteMediator = PostRemoteMediator(
            apiService = apiService,
            postDao = dao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb
            )
    ).flow.map {
        it.map {
            it.toDto()
        }
    }


    override suspend fun getAll() {
        try {
            val response = apiService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getNewer(id: Long) {
         try {
//            val pageSize // TODO Как получить pageSize из PostRemoteMediator ?
//            val count = apiService.getNewerCount(id).body()
//            val response = count?.let { apiService.getAfter(it, pageSize) }
             val response = apiService.getNewer(id)
             if (response != null) {
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                dao.insert(body.toEntity())
                postRemoteKeyDao.insert(PostRemoteKeyEntity(
                    PostRemoteKeyEntity.KeyType.AFTER,
                    body.last().id))
             }
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            val maxId = postRemoteKeyDao.max()
            delay(10_000L)
            val response = maxId?.let { apiService.getNewerCount(it) }
            if (response != null) {
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
            }
            val body = response?.body() // ?: throw ApiError(response.code(), response.message())
//            dao.insert(body.toEntity())
            if (body != null) {
                emit(body.toInt())
            }
        }
    }
        .catch {
                e -> {
            println("======================= Exception: " + e)
            throw AppError.from(e)
                }
        }.flowOn(Dispatchers.Default)

    override suspend fun save(post: Post) {
        try {
            val response = apiService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long): Boolean {
        var err = false
        try {
            dao.removeById(id)

            val response = apiService.removeById(id)
            if (!response.isSuccessful) {
                err = true
            }
        } catch (e: IOException) {
            err = true
        } catch (e: Exception) {
            err = true
        }
        return err
    }

    override suspend fun likeById(post: Post): Boolean {
        val response: retrofit2.Response<Post>
        val needToLike = !post.likedByMe
        var err = false
        try {
            if(needToLike) post.likes += 1
            else post.likes -= 1
            dao.insert(PostEntity.fromDto(post))

            response = if (needToLike) apiService.likeById(post.id)
            else apiService.dislikeById(post.id)
            if (!response.isSuccessful) {
                err = true
//                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            err = true
//            throw NetworkError
        } catch (e: Exception) {
            err = true
//            throw UnknownError
        }
        return err
    }

    override suspend fun showAll() {
        dao.showAll()
    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {
        try {
            val media = upload(upload)
            // TODO: add support for other types
            val postWithAttachment = post.copy(attachment = Attachment(media.id, AttachmentType.IMAGE))
            save(postWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = apiService.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun login(name: String, pass: String): LoginData {
        var result: LoginData = LoginData(-1L, "", "")
        try {
            val response = apiService.updateUser(name, pass)
            if (!response.isSuccessful) {
                result.token = "Server Error"
            } else {
                result = response.body() ?: throw ApiError(response.code(), response.message())
            }
        }
        catch (e: IOException) {
            result.token = "Connection Error"
        } catch (e: Exception) {
            result.token = "Unknown Error"
        }
        return result
    }
}
