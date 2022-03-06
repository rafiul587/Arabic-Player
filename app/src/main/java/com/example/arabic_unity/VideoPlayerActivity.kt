package com.example.arabic_unity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
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
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.unity3d.ads.UnityAds
import com.unity3d.services.banners.IUnityBannerListener
import java.util.*


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
            MaterialTheme() {
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
        /*listRaw()
        binding.bnt.setOnClickListener {
            //showInterstitialAd()
            startActivity(Intent(this@page3, page4::class.java))
        }
        binding.rateapp.setOnClickListener {
            val appName = packageName
            try {
                startActivity(
                    Intent(
                        "android.intent.action.VIEW",
                        Uri.parse("market://details?id=$appName")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        "android.intent.action.VIEW",
                        Uri.parse("http://play.google.com/store/apps/details?id=$appName")
                    )
                )
            }
        }
        binding.moreapp.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    "android.intent.action.VIEW",
                    Uri.parse(getString(R.string.play_more_apps))
                )
            )
        })
        iUnityBannerListener = object : IUnityBannerListener {
            override fun onUnityBannerLoaded(s: String, view: View) {
                (findViewById<View>(R.id.bannerAdLayout) as ViewGroup).removeView(view)
                (findViewById<View>(R.id.bannerAdLayout) as ViewGroup).addView(view)
            }

            override fun onUnityBannerUnloaded(s: String) {}
            override fun onUnityBannerShow(s: String) {}
            override fun onUnityBannerClick(s: String) {}
            override fun onUnityBannerHide(s: String) {}
            override fun onUnityBannerError(s: String) {}
        }
        UnityBanners.setBannerListener(iUnityBannerListener)*/
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .setSeekForwardIncrementMs(10000L)
            .setSeekBackIncrementMs(10000L)
            .build()
            .also { exoPlayer ->
                binding.videoView.player = exoPlayer
                val mediaItem = MediaItem.fromUri(
                    Uri.parse(
                        "android.resource://" +
                                packageName + "/" + R.raw.toyor
                    )
                )

                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        if (playbackState == ExoPlayer.STATE_READY) {
                        }
                        if (playbackState == ExoPlayer.STATE_ENDED) {
                            //performEndExoPlayer()
                        }
                    }
                })
            }
    }

    private fun stringForTime(timeMs: Long): String? {
        val mFormatBuilder = StringBuilder()
        val mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
        val totalSeconds = timeMs / 1000
        //  videoDurationInSeconds = totalSeconds % 60;
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        mFormatBuilder.setLength(0)
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
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

    private fun showInterstitialAd() {
        if (UnityAds.isInitialized()) {
            UnityAds.show(this@VideoPlayerActivity, resources.getString(R.string.interstitial))
        }
    }
}