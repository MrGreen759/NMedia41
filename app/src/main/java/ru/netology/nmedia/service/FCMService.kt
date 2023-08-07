package ru.netology.nmedia.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.*
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import java.lang.reflect.Type
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {
    private val channelId = "remote"
    private lateinit var receivedPush: ReceivedPush

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val pushData = message.data["content"]
        println("-------------pushData: " + pushData)
//        receivedPush = gson.fromJson(message.data["content"], ReceivedPush::class.java) - не работает

        val gson1 = GsonBuilder().registerTypeAdapter(ReceivedPush::class.java, PushDeserializer()).create()
        receivedPush = gson1.fromJson(pushData, ReceivedPush::class.java)
        val recipId = receivedPush.recId

        val authId = appAuth.authStateFlow.value.id
        println("============= AuthID: " + authId)

        if ((recipId == 0L) || (recipId != authId)) {
            appAuth.sendPushToken()
        }

        if ((recipId == authId) || (recipId == null)) {
            val handler = Handler(Looper.getMainLooper())
            handler.post { Toast.makeText(applicationContext, receivedPush.pushMessage, Toast.LENGTH_LONG).show() }
        }

//        message.data[action]?.let {
//           when (Action.valueOf(it)) {
//              Action.LIKE -> handleLike(gson.fromJson(message.data[content], Like::class.java))
//           }
//        }
    }

    override fun onNewToken(token: String) {
        appAuth.sendPushToken(token)
        println(token)
    }

//    private fun handleLike(content: Like) {
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle(
//                getString(
//                    R.string.notification_user_liked,
//                    content.userName,
//                    content.postAuthor,
//                )
//            )
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//
//        if (PermissionChecker.checkSelfPermission(
//                this,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) == PermissionChecker.PERMISSION_GRANTED
//        ) {
//            NotificationManagerCompat.from(this)
//                .notify(Random.nextInt(100_000), notification)
//        }
//    }
}

//enum class Action {
//    LIKE,
//}
//
//data class Like(
//    val userId: Long,
//    val userName: String,
//    val postId: Long,
//    val postAuthor: String,
//)

data class ReceivedPush(
    var recId: Long?,
    var pushMessage: String?
    )

class PushDeserializer : JsonDeserializer<ReceivedPush> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ReceivedPush {
        json as JsonObject
        var rid: Long?
        var pm: String?

        try {
            rid = json.get("recipientId").asLong
            pm = json.get("content").asString
        } catch (e: Exception) {
            rid = null
            pm = "Mass mailing"
            println("rid=" + rid + ", pm: " + pm)
        }
        return ReceivedPush(rid, pm)
    }
}
