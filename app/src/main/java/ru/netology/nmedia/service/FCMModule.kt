package ru.netology.nmedia.service

import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class FCMModule {

    @Provides
    @Singleton
    fun provideFCM(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Singleton
    fun provideChecker(): GoogleApiAvailability = GoogleApiAvailability.getInstance()


//    @Provides
//    @Singleton
//    fun provideChecker(@ActivityContext
//                       context: Context
//    ): GoogleApiAvailability = GoogleApiAvailability().apply {
//        val code = isGooglePlayServicesAvailable(context)
//        if (code == ConnectionResult.SUCCESS) {
//            return@apply
//        }
//        if (isUserResolvableError(code)) {
//            Toast.makeText(context, "Error. Code: $code", Toast.LENGTH_LONG)
//                .show()
//        }
//        Toast.makeText(context, R.string.google_play_unavailable, Toast.LENGTH_LONG)
//            .show()
//    }

}
