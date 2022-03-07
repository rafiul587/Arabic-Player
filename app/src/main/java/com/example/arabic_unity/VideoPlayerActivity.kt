package com.example.arabic_unity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.arabic_unity.databinding.ActivityVideoPlayerBinding
import com.example.arabic_unity.ui.component.CommonButton
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.composethemeadapter.MdcTheme
import com.unity3d.services.banners.IUnityBannerListener


class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var videoUrl: String
    private val binding by lazy {
        ActivityVideoPlayerBinding.inflate(layoutInflater)
    }
    private lateinit var player: ExoPlayer
    private var fullscreen = false
    private lateinit var fullscreenButton: ImageView
    private var iUnityBannerListener: IUnityBannerListener? = null

    @OptIn(ExperimentalUnitApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val uri = intent.getStringExtra("URI")
        val fileName = intent.getStringExtra("FileName")
        videoUrl = uri!!
        setupPlayerView(binding.videoView, videoUrl)
        binding.bottomNavigatinView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.back -> {
                    onBackPressed()
                }
                R.id.mute -> {
                    it.isCheckable = false
                    val isMute = ExoPlayerViewManager.setVolume()
                    if (isMute) {
                        it.setIcon(R.drawable.ic_baseline_mute_24)
                    } else {
                        it.setIcon(R.drawable.ic_baseline_volume_up_24)
                    }
                }
                R.id.share -> {
                    val shareData = Intent(Intent.ACTION_SEND)
                    shareData.type = "text/plain"
                    val dataToShare = "Install this app from here https://play.google.com/store/apps/details?id=${packageName} "
                    shareData.putExtra(Intent.EXTRA_SUBJECT, "Install this app")
                    shareData.putExtra(Intent.EXTRA_TEXT, dataToShare)
                    startActivity(Intent.createChooser(shareData, "Share Via"))
                }
            }
            true
        }
        fun readFileText(fileName: String): String {
            return assets.open("$fileName.txt").bufferedReader().use { it.readText() }
        }
        val iconColorStates = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ), intArrayOf(
                android.graphics.Color.parseColor("#ffffff"),
                android.graphics.Color.parseColor("#ffffff")
            )
        )

        binding.bottomNavigatinView.itemIconTintList = iconColorStates
        binding.bottomNavigatinView.itemTextColor = iconColorStates
        binding.composeView.setContent {
            MdcTheme() {
                Surface() {
                    val scrollState = rememberScrollState()
                    var isLyricShowing by remember {
                        mutableStateOf(false)
                    }
                    Column(
                        horizontalAlignment = CenterHorizontally
                    ) {
                        CommonButton(text = if (isLyricShowing) "Hide Lyric" else "See Lyric") {
                            isLyricShowing = !isLyricShowing
                        }
                        if (isLyricShowing) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp)
                                    .weight(1f, false)
                                    .border(5.dp, color = Color.Green, RoundedCornerShape(5.dp))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .verticalScroll(scrollState)
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = readFileText(fileName!!),
                                        lineHeight = TextUnit(2f, TextUnitType.Em)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        ExoPlayerViewManager.goToBackground()
    }

    override fun onDestroy() {
        super.onDestroy()
        ExoPlayerViewManager.releaseVideoPlayer()
    }

    private fun initializeFullScreen() {
        fullscreenButton = binding.videoView.findViewById(R.id.exo_fullscreen_button)
        fullscreenButton.setOnClickListener {
            if (fullscreen) {
                goToNormalView(true)
            } else {
                goToFullScreenView(true)
            }
        }
    }


    private fun setupPlayerView(videoView: StyledPlayerView, videoUrl: String) {
        ExoPlayerViewManager
            .setVideoUrl(videoUrl)
            .prepareExoPlayer(this, videoView)
        initializeFullScreen()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            goToFullScreenView(false)
        } else {
            goToNormalView(false)
        }
    }

    override fun onBackPressed() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            goToNormalView(true)
        } else {
            super.onBackPressed()
        }
        //showInterstitialAd()
    }

    private fun goToFullScreenView(isButtonClick: Boolean) {
        fullscreenButton.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_baseline_clear_24
            )
        )
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(binding.videoView)
        // Configure the behavior of the hidden system bars
        windowInsetsController?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and the navigation bar
        windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        if (isButtonClick) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val params = binding.videoView.layoutParams as ConstraintLayout.LayoutParams
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        binding.videoView.layoutParams = params
        binding.bottomNavigatinView.visibility = View.GONE
        fullscreen = true
    }

    private fun goToNormalView(isButtonClick: Boolean) {
        fullscreenButton.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_baseline_fullscreen_24
            )
        )
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView)
        // Configure the behavior of the hidden system bars
        windowInsetsController?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and the navigation bar
        windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
        if (supportActionBar != null) {
            supportActionBar!!.show()
        }
        if (isButtonClick) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val params = binding.videoView.layoutParams as ConstraintLayout.LayoutParams
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = (300 * applicationContext.resources.displayMetrics.density).toInt()
        binding.videoView.layoutParams = params
        binding.bottomNavigatinView.visibility = View.VISIBLE
        fullscreen = false
    }
}