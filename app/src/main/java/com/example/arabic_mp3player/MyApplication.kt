package com.example.arabic_mp3player

import android.app.Application
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the AppLovin SDK
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        applicationScope.launch {
            AppLovinSdk.getInstance(applicationContext).mediationProvider = AppLovinMediationProvider.MAX
            AppLovinSdk.getInstance(applicationContext).initializeSdk()
        }
    }
}