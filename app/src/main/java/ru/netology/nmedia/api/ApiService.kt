package ru.netology.nmedia.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nmedia.dto.LoginData
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PushToken

//private const val BASE_URL = "${BuildConfig.BASE_URL}/api/slow/"
//
//private val logging = HttpLoggingInterceptor().apply {
//    if (BuildConfig.DEBUG) {
//        level = HttpLoggingInterceptor.Level.BODY
//    }
//}
//
//private val okhttp = OkHttpClient.Builder()
//    .addInterceptor(logging)
//    .addInterceptor { chain ->
//        AppAuth.getInstance().authStateFlow.value.token?.let { token ->
//            val newRequest = chain.request().newBuilder()
//                .addHeader("Authorization", token)
//                .build()
//            return@addInterceptor chain.proceed(newRequest)
//        }
//        chain.proceed(chain.request())
//    }
//    .build()
//
//private val retrofit = Retrofit.Builder()
//    .addConverterFactory(GsonConverterFactory.create())
//    .baseUrl(BASE_URL)
//    .client(okhttp)
//    .build()

interface ApiService {
    @POST("users/push-tokens")
    suspend fun save(@Body pushToken: PushToken): Response<Unit>

    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    @GET("posts/{id}/newer-count")
    suspend fun getNewerCount(@Path("id") id: Long): Response<Long>

    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/before")
    suspend fun getBefore(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getAfter(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Long): Response<Post>

    @POST("posts")
    suspend fun save(@Body post: Post): Response<Post>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Response<Post>

    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun updateUser(@Field("login") login: String, @Field("pass") pass: String): Response<LoginData>
}
