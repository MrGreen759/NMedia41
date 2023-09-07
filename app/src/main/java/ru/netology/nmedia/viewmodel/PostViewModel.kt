package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import androidx.paging.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject
import kotlin.random.Random

private val empty = Post(
    id = 0,
    authorId = 0,
    author = "",
    authorAvatar = "",
    content = "",
    published = 0,
    likedByMe = false,
    likes = 0,
    hidden = false,
    attachment = null
)

private val noPhoto = PhotoModel()

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
//class PostViewModel @Inject constructor(application: Application, appAuth: AppAuth) : AndroidViewModel(application) {
class PostViewModel @Inject constructor(
    private val repository: PostRepositoryImpl,
    appAuth: AppAuth,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    ) : ViewModel() {
    // упрощённый вариант
//    private val repository: PostRepository =
//        PostRepositoryImpl(AppDb.getInstance(context = application).postDao())

//    val data: LiveData<FeedModel> = repository.data
//        .map(::FeedModel)
//        .asLiveData(Dispatchers.Default)

//    val data: LiveData<FeedModel> = appAuth
//        .authStateFlow
//        .flatMapLatest { (myId, _) ->
//            repository.data
//                .map { posts ->
//                    FeedModel(
//                        posts.map { it.copy(ownedByMe = it.authorId == myId) },
//                        posts.isEmpty()
//                    )
//                }
//        }.asLiveData(Dispatchers.Default)

    private val cached = repository.data.cachedIn(viewModelScope)

    val data: Flow<PagingData<FeedItem>> = appAuth.authStateFlow
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.map { post ->
                    if (post is Post) {
                        post.copy(ownedByMe = post.authorId == myId)
                    } else {
                        post
                    }
                }
            }
        }


    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    // TODO Восстановить!!!
//    val newerCount: LiveData<Int> = data.switchMap {
//        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
//            .catch { e -> e.printStackTrace() }
//            .asLiveData(Dispatchers.Default)
//    }

//    val newerCount: LiveData<Int> ?= viewModelScope.launch {
//        postRemoteKeyDao.max()?.let {
//            repository.getNewerCount(it)
//                .catch { e -> e.printStackTrace() }
//                .asLiveData(Dispatchers.Default)
//
//        }
//    }

    val newerCount: Flow<Int> = repository.getNewerCount(0L) // TODO Разобраться с id

    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    var errorOperation = MutableLiveData(0) // номер функции, в которой произошла ошибка
    var errorPostId = MutableLiveData(-1L)  // id поста, при обработке которого произошла ошибка

//    init {
//        loadPosts()
//    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshFromId() {
        _dataState.value = FeedModelState(refreshing = true)
        viewModelScope.launch {
            val id = postRemoteKeyDao.max()
            try {
                println("--------------- MYLOG. postRemoteKeyDao.max() = " + id)
                if (id != null) {
                    repository.getNewer(id)
                }
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun save() {
        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    when(_photo.value) {
                        noPhoto -> repository.save(it)
                        else -> _photo.value?.file?.let { file ->
                            repository.saveWithAttachment(it, MediaUpload(file))
                        }
                    }
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
        _photo.value = noPhoto
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun showAll() = viewModelScope.launch {
        repository.showAll()
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    fun likeById(post: Post) = viewModelScope.launch {
        if (post != null) {
            val err = repository.likeById(post)
            if (err) {
                errorOperation.postValue(1)
                errorPostId.postValue((post.id))
            }
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        val err = repository.removeById(id)
        if (err) {
            errorOperation.postValue(2)
            errorPostId.postValue(id)
        }
    }
}
