package com.example.arabic_unity

import android.content.Intent
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.arabic_unity.ui.component.CommonButton
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme() {
                Surface {
                    SplashScreen() {
                        navigateToMain()
                    }
                }
            }
        }

        /*setContentView(R.layout.activity_splash)
            UnityAds.initialize(this.baseContext, resources.getString(R.string.game_id), testMode)
            object : Thread() {
                override fun run() {
                    try {
                        sleep(4000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    } finally {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    }
                }
            }.start()*/
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    public override fun onPause() {
        super.onPause()
        finish()
    }
}

@Composable
fun SplashScreen(navigateToMain: () -> Unit) {
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
                ), repeatMode = RepeatMode.Reverse, iterations = 3
            )
        )
        delay(500)
        buttonVisibility = 1f
        //navigateToMain()
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
            Image(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .aspectRatio(1f)
                    .scale(scale.value),
                contentScale = ContentScale.FillBounds,
                painter = painterResource(id = R.drawable.splash_icon),
                contentDescription = "Icon"
            )
            Spacer(modifier = Modifier.height(80.dp))
            CommonButton(
                modifier = Modifier.alpha(buttonVisibility),
                text = "Music"
            ) {
                navigateToMain()
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    SplashScreen {

    }

}