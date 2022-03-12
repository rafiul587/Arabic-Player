package com.example.arabic_unity

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the AppLovin SDK
        AppLovinSdk.getInstance(this).mediationProvider = AppLovinMediationProvider.MAX
        AppLovinSdk.getInstance(this).initializeSdk()
    }
}