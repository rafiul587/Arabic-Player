package com.example.arabic_unity.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.example.arabic_unity.R
import com.example.arabic_unity.component.CommonButton
import com.example.arabic_unity.utils.Constants
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.math.pow


class SplashActivity : ComponentActivity(), MaxAdListener {

    private lateinit var snackBar: Snackbar
    private var interstitialAd: MaxInterstitialAd? = null
    private var int1 = mutableStateOf("")
    private lateinit var int2: String
    private lateinit var int3: String
    private lateinit var ban1: String
    private lateinit var ban2: String
    private var retryAttempt = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Green
                ) {

                    SplashScreen(int1.value, { showAd() },
                        { readJsonFromRaw() }) {
                        createInterstitialAd(it)
                    }
                }
            }
        }

        snackBar = Snackbar.make(
            this.findViewById(android.R.id.content),
            getString(R.string.ad_coming_msg),
            Snackbar.LENGTH_INDEFINITE
        )
    }

    private fun createInterstitialAd(int1: String) {
        try {
            interstitialAd = MaxInterstitialAd(int1, this)
            interstitialAd?.setListener(this)
            interstitialAd?.loadAd()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun readJsonFromRaw() {
        lifecycleScope.launch(Dispatchers.IO) {
            val text = resources.openRawResource(R.raw.ad_id)
                .bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(text)
            val interstitial = jsonObject.get(Constants.INTERSTITIAL)
            int1.value = (interstitial as JSONObject).get("id_1").toString()
            int2 = interstitial.get("id_2").toString()
            int3 = interstitial.get("id_3").toString()
            val banner = jsonObject.get(Constants.BANNER)
            ban1 = (banner as JSONObject).get("id_1").toString()
            ban2 = banner.get("id_2").toString()
        }
    }

    // MAX Ad Listener
    override fun onAdLoaded(maxAd: MaxAd) {
        retryAttempt = 0
    }

    override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
        retryAttempt++
        val delayMillis: Long = TimeUnit.SECONDS.toMillis(
            2.0.pow(min(6, retryAttempt).toDouble()).toLong()
        )
        Handler(Looper.getMainLooper()).postDelayed({ interstitialAd?.loadAd() }, delayMillis)
    }

    override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
        if (snackBar.isShown) {
            snackBar.setText(getString(R.string.ad_failed_msg))
            snackBar.dismiss()
        }
        interstitialAd?.loadAd()
    }

    override fun onAdDisplayed(maxAd: MaxAd) {
        if (snackBar.isShown) snackBar.dismiss()
    }

    override fun onAdClicked(maxAd: MaxAd) {
    }

    override fun onAdHidden(maxAd: MaxAd) {
        navigateToMain()
    }


    private fun showAd() {
        snackBar.setText(getString(R.string.ad_coming_msg))
        snackBar.show()
        if (interstitialAd?.isReady == true) {
            interstitialAd?.showAd()
        } else {
            snackBar.dismiss()
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Constants.INT_2, int2)
            putExtra(Constants.INT_3, int3)
            putExtra(Constants.BAN_1, ban1)
            putExtra(Constants.BAN_2, ban2)
        })
        finish()
    }
}

@Composable
fun SplashScreen(
    int1: String,
    showAd: () -> Unit,
    readJsonFromRaw: () -> Unit,
    createInterstitialAd: (String) -> Unit
) {

    LaunchedEffect(key1 = Unit) {
        readJsonFromRaw()
    }
    LaunchedEffect(int1) {
        if (int1.isNotEmpty()) createInterstitialAd(int1)
    }
    val scale = remember {
        Animatable(.5f)
    }
    var buttonVisibility by remember {
        mutableStateOf(0f)
    }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = repeatable(
                animation = tween(
                    durationMillis = 700,
                    easing = {
                        OvershootInterpolator(2f).getInterpolation(it)
                    }
                ), repeatMode = RepeatMode.Reverse, iterations = 7
            )
        )
        delay(200)
        buttonVisibility = 1f
    }
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentScale = ContentScale.FillBounds,
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = ""
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                color = Color.White,
                fontSize = 25.sp
            )
            Spacer(modifier = Modifier.height(50.dp))
            Box(
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .aspectRatio(1f)
                        .scale(scale.value),
                    contentScale = ContentScale.FillBounds,
                    painter = painterResource(id = R.drawable.ic_splash_icon),
                    contentDescription = "Icon"
                )
                if (LocalLayoutDirection.current == LayoutDirection.Rtl) {
                    Image(
                        modifier = Modifier
                            .padding(top = 20.dp, end = 10.dp)
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        painter = painterResource(id = R.drawable.ic_play_icon_splash),
                        contentDescription = "App_Logo",
                        colorFilter = ColorFilter.tint(Color.Green)
                    )
                } else {
                    Image(
                        modifier = Modifier
                            .padding(top = 20.dp, start = 10.dp)
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        painter = painterResource(id = R.drawable.ic_play_icon_splash),
                        contentDescription = "App_Logo",
                        colorFilter = ColorFilter.tint(Color.Green)
                    )
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
            CommonButton(
                modifier = Modifier
                    .alpha(buttonVisibility),
                text = stringResource(R.string.music)
            ) {
                showAd()
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
}
