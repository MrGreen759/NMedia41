package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.LoginData
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(context = application).postDao())

    fun login(name: String, pass: String) {
        viewModelScope.launch {
            var authData: LoginData? = null
            launch { authData = repository.login(name, pass)  }.join()
            println("========= ID: " + authData?.id)
            println("========= TOKEN: " + authData?.token)

            authData?.let { AppAuth.getInstance().setAuth(it.id, it.token) }
        }
    }

}
