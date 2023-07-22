package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.LoginData
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import javax.inject.Inject

//class LoginViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
class LoginViewModel @Inject constructor(private val repository: PostRepository, appAuth: AppAuth) : ViewModel() {

//    private val repository: PostRepository =
//        PostRepositoryImpl(AppDb.getInstance(context = application).postDao())

    var authResult = MutableLiveData("nothing")

    fun login(name: String, pass: String, appAuth: AppAuth) = viewModelScope.launch {
        var authData: LoginData? = null
        launch { authData = repository.login(name, pass)  }.join()
        println("========= ID: " + authData?.id)
        println("========= TOKEN: " + authData?.token)
        if (authData?.id == -1L) {
            authResult.postValue(authData!!.token)
        } else {
            authResult.postValue("OK")
            authData?.let { appAuth.setAuth(it.id, it.token) }
        }
    }

}
