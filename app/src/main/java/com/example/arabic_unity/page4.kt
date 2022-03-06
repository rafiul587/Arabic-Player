package com.example.arabic_unity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.unity3d.ads.UnityAds

class page4 : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page4)
    }

    override fun onResume() {
        super.onResume()
        if (UnityAds.isInitialized()) {
            UnityAds.load(resources.getString(R.string.interstitial))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        showInterstitialAd()
    }

    private fun showInterstitialAd() {
        if (UnityAds.isReady(resources.getString(R.string.interstitial))) {
            UnityAds.show(this@page4, resources.getString(R.string.interstitial))
        }
    }
}