package com.example.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.api.NetworkClient
import com.example.data.api.VerbaApiService
import com.example.data.audio.AudioRecorderManager
import com.example.data.auth.AuthRepository
import com.example.data.auth.AuthRepositoryImpl
import com.example.data.db.VerbaDatabase
import com.example.data.notification.VerbaNotificationManager
import com.example.data.repository.VerbaRepository
import com.example.data.repository.VerbaRepositoryImpl
import com.example.ui.auth.AuthViewModel
import com.example.ui.chat.RagChatViewModel
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.hub.KnowledgeHubViewModel

object ServiceLocator {

    private var database: VerbaDatabase? = null
    private var apiService: VerbaApiService? = null
    private var repository: VerbaRepository? = null
    private var audioRecorderManager: AudioRecorderManager? = null
    private var authRepository: AuthRepository? = null
    private var notificationManager: VerbaNotificationManager? = null

    fun initialize(context: Context) {
        val appCtx = context.applicationContext
        if (database == null) {
            database = VerbaDatabase.getInstance(appCtx)
        }
        if (apiService == null) {
            apiService = NetworkClient.createApiService()
        }
        if (repository == null) {
            repository = VerbaRepositoryImpl(
                apiService = apiService!!,
                dao = database!!.verbaDao()
            )
        }
        if (audioRecorderManager == null) {
            audioRecorderManager = AudioRecorderManager(appCtx)
        }
        if (authRepository == null) {
            authRepository = AuthRepositoryImpl(appCtx)
        }
        if (notificationManager == null) {
            notificationManager = VerbaNotificationManager(appCtx)
        }
    }

    fun getRepository(): VerbaRepository {
        return checkNotNull(repository) { "ServiceLocator must be initialized before accessing repository" }
    }

    fun getAudioRecorderManager(): AudioRecorderManager {
        return checkNotNull(audioRecorderManager) { "ServiceLocator must be initialized before accessing audio recorder" }
    }

    fun getAuthRepository(): AuthRepository {
        return checkNotNull(authRepository) { "ServiceLocator must be initialized before accessing authRepository" }
    }

    fun getNotificationManager(): VerbaNotificationManager {
        return checkNotNull(notificationManager) { "ServiceLocator must be initialized before accessing notificationManager" }
    }

    fun provideViewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = getRepository()
                val recorder = getAudioRecorderManager()
                val auth = getAuthRepository()
                val notifications = getNotificationManager()
                return when {
                    modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                        AuthViewModel(auth) as T
                    }
                    modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                        DashboardViewModel(repo, recorder, auth, notifications) as T
                    }
                    modelClass.isAssignableFrom(KnowledgeHubViewModel::class.java) -> {
                        KnowledgeHubViewModel(repo) as T
                    }
                    modelClass.isAssignableFrom(RagChatViewModel::class.java) -> {
                        RagChatViewModel(repo) as T
                    }
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}
