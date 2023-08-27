package ru.netology.nmedia.repository

import androidx.paging.*
import androidx.room.withTransaction
import kotlinx.coroutines.flow.single
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.error.ApiError
import java.util.concurrent.Flow

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator (
    private val apiService:ApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
    ): RemoteMediator<Int, PostEntity>() {
//    override fun getRefreshKey(state: PagingState<Long, Post>): Long? {
//        return null
//    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, PostEntity>): MediatorResult {
        try {
            val response = when (loadType) {
//                LoadType.REFRESH -> apiService.getLatest(state.config.pageSize)
                LoadType.REFRESH -> {
                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(endOfPaginationReached = false)
                    apiService.getNewer(id)
                }
                LoadType.PREPEND -> {
//                    val id = state.firstItemOrNull()?.id ?: return MediatorResult.Success(endOfPaginationReached = false)
                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(endOfPaginationReached = false)
                    apiService.getAfter(id, state.config.pageSize)
                }
                LoadType.APPEND -> {
//                    val id = state.lastItemOrNull()?.id ?: return MediatorResult.Success(endOfPaginationReached = false)
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(endOfPaginationReached = false)
                    apiService.getBefore(id, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message(),
            )

//            val nextKey = if (body.isEmpty()) null else body.last().id

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
//                        postDao.clear()
                        postRemoteKeyDao.insert(
                            listOf(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.AFTER,
                                    body.first().id
                                ),
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id
                                )
                            )
                        )
                    }
                    LoadType.PREPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                body.first().id
                            )
                        )
                    }
                    LoadType.APPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id
                            )
                        )
                    }
                }

                postDao.insert(body.map { PostEntity.fromDto(it) })
            }

            return MediatorResult.Success(body.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}
