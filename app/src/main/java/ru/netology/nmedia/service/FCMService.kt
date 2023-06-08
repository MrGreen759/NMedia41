package ru.netology.nmedia.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.*
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import java.lang.reflect.Type


class FCMService : FirebaseMessagingService() {
//    private val action = "action"
//    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()
    private lateinit var receivedPush: ReceivedPush

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
        println(message.data["content"])
        println("----------------message: " + message.data)
        var sss = message.data["content"]
//        sss = """$sss""".trimIndent()
//        val jjj = gson.toJsonTree(sss)
//        println("<<<< jjj: " + jjj)
//        val j2 = jjj.asJsonObject
//        println("............. j2: " + j2)
        println("------------sss: " + sss)
//        receivedPush = gson.fromJson(message.data["content"], ReceivedPush::class.java)

        val gson1 = GsonBuilder().registerTypeAdapter(ReceivedPush::class.java, PushDeserializer()).create()
        receivedPush = gson1.fromJson(sss, ReceivedPush::class.java)

        println("---------- recipientId: " + receivedPush.recId)
        println(">>>>>>>>>>>pushMessage: " + receivedPush.pushMessage)
//        message.data[action]?.let {
//           when (Action.valueOf(it)) {
//              Action.LIKE -> handleLike(gson.fromJson(message.data[content], Like::class.java))
//           }
//        }
    }

    override fun onNewToken(token: String) {
        AppAuth.getInstance().sendPushToken(token)
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
        var rid: Long? = -1L
        var pm: String? = null

        try {
            rid = json.get("recipientId").asLong
            pm = json.get("content").asString
        } catch (e: Exception) {
            rid = null
            println("rid=" + rid + ", pm: " + pm)
        }
        return ReceivedPush(rid, pm)
    }
}
