package com.example.arabic_unity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
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
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.example.arabic_unity.databinding.ActivityVideoPlayerBinding
import com.example.arabic_unity.ui.Constants
import com.example.arabic_unity.ui.component.CommonButton
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.composethemeadapter.MdcTheme
import com.google.android.material.snackbar.Snackbar


class VideoPlayerActivity : AppCompatActivity(), MaxAdViewAdListener {
    private lateinit var videoUrl: String
    private val binding by lazy {
        ActivityVideoPlayerBinding.inflate(layoutInflater)
    }
    private var fullscreen = false
    private lateinit var fullscreenButton: ImageView
    lateinit var snackBar: Snackbar
    private lateinit var interstitialAd: MaxInterstitialAd
    private var adView: MaxAdView? = null
    private val isLyricShowing = mutableStateOf(false)
    private var uri: String = ""
    private var fileName = ""
    private var artistName = ""

    @OptIn(ExperimentalUnitApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        getAllIntentData()
        setupPlayerView(binding.videoView, videoUrl)

        binding.bottomNavigatinView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.back -> {
                    onBackPressed()
                }
                R.id.mute -> {
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
                    val dataToShare =
                        "Install this app from here https://play.google.com/store/apps/details?id=${packageName} "
                    shareData.putExtra(Intent.EXTRA_SUBJECT, "Install this app")
                    shareData.putExtra(Intent.EXTRA_TEXT, dataToShare)
                    startActivity(Intent.createChooser(shareData, "Share Via"))
                }
            }
            true
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
                    MainPlayerView(
                        isLyricShowing,
                        fileName = fileName,
                        showAd = { showAd() }) {
                        readFileText(it)
                    }
                }
            }
        }
    }

    private fun getAllIntentData() {
        intent.getStringExtra(Constants.INT_3)?.let {
            createInterstitialAd(it)
        }
        intent.getStringExtra(Constants.BAN_2)?.let {
            createBannerAd(it)
        }
        snackBar = Snackbar.make(
            this.findViewById(android.R.id.content),
            "Ad is coming..",
            Snackbar.LENGTH_INDEFINITE
        )
        uri = intent.getStringExtra("URI").toString()
        fileName = intent.getStringExtra("FileName").toString()
        artistName = intent.getStringExtra("ArtistName").toString()

        binding.fileName.text = fileName
        binding.artistName.text = artistName
        videoUrl = uri
    }

    private fun createBannerAd(adUnitId: String) {
        adView = MaxAdView(adUnitId, this)
        adView?.setListener(this)
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val heightPx = resources.getDimensionPixelSize(R.dimen.banner_height)
        adView?.layoutParams = FrameLayout.LayoutParams(width, heightPx)
        adView?.setBackgroundColor(android.graphics.Color.WHITE)
        binding.bannerAdLayout.addView(adView)
        adView?.loadAd()
    }


    private fun readFileText(fileName: String): String {
        return assets.open("$fileName.txt").bufferedReader().use { it.readText() }
    }

    private fun createInterstitialAd(adUnitId: String) {
        interstitialAd = MaxInterstitialAd(adUnitId, this)
        interstitialAd.setListener(object : MaxAdListener {
            override fun onAdLoaded(maxAd: MaxAd) {}
            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                if (snackBar.isShown) {
                    snackBar.setText("Ad Loading Failed")
                    snackBar.dismiss()
                }
                isLyricShowing.value = true
            }
            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                if (snackBar.isShown) {
                    snackBar.setText("Ad Loading Failed")
                    snackBar.dismiss()
                }
                isLyricShowing.value = true
            }
            override fun onAdDisplayed(maxAd: MaxAd) {
                if (snackBar.isShown) snackBar.dismiss()
            }
            override fun onAdClicked(maxAd: MaxAd) {}
            override fun onAdHidden(maxAd: MaxAd) {
                isLyricShowing.value = true
                interstitialAd.loadAd()
            }
        })
        interstitialAd.loadAd()
    }

    private fun showAd() {
        snackBar.setText("Ad is coming..")
        snackBar.show()
        if (interstitialAd.isReady) {
            interstitialAd.showAd()
        } else {
            snackBar.dismiss()
            isLyricShowing.value = true
        }
    }

    override fun onAdLoaded(maxAd: MaxAd) {
        binding.bannerAdLayout.visibility = View.VISIBLE
    }

    override fun onAdDisplayed(ad: MaxAd?) {}
    override fun onAdHidden(ad: MaxAd?) {}
    override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {}

    override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {}

    override fun onAdExpanded(ad: MaxAd?) {}
    override fun onAdCollapsed(ad: MaxAd?) {}
    override fun onAdClicked(maxAd: MaxAd) {}

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
    }

    private fun goToFullScreenView(isButtonClick: Boolean) {
        fullscreenButton.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_baseline_clear_24
            )
        )
        val windowInsetsController = ViewCompat.getWindowInsetsController(binding.videoView)
        windowInsetsController?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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
        params.height = (200 * applicationContext.resources.displayMetrics.density).toInt()
        binding.videoView.layoutParams = params
        binding.bottomNavigatinView.visibility = View.VISIBLE
        fullscreen = false
    }
}

@OptIn(ExperimentalUnitApi::class)
@Composable
fun MainPlayerView(
    isLyricShowing: MutableState<Boolean>,
    fileName: String,
    showAd: () -> Unit,
    readFileText: (String) -> String
) {
    val scrollState = rememberScrollState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CommonButton(text = if (isLyricShowing.value) "Hide Lyric" else "See Lyric") {
            if (!isLyricShowing.value) {
                showAd()
            } else {
                isLyricShowing.value = false
            }
        }
        if (isLyricShowing.value) {
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
                        text = readFileText(fileName),
                        lineHeight = TextUnit(2f, TextUnitType.Em)
                    )
                }
            }
        }
    }
}
