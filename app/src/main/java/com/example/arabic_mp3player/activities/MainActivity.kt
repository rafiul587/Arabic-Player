package com.example.arabic_mp3player.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.example.arabic_mp3player.R
import com.example.arabic_mp3player.component.CommonButton
import com.example.arabic_mp3player.databinding.ActivityMainBinding
import com.example.arabic_mp3player.utils.Constants
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.android.material.composethemeadapter.MdcTheme
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.math.pow


class MainActivity : ComponentActivity(), MaxAdViewAdListener {
    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val fileIds = listOf(R.raw.aadat)
    private val fileNames = listOf("aadat.mp3")
    private lateinit var artistNames: List<String>
    private var int3 = ""
    private var ban2 = ""
    val shouldDownload = mutableStateOf(false)
    lateinit var snackBar: Snackbar
    private var interstitialAd: MaxInterstitialAd? = null
    private var adView: MaxAdView? = null
    private var retryAttempt = 0
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        getAllIntentData()
        snackBar = Snackbar.make(
            this.findViewById(android.R.id.content),
            getString(R.string.ad_coming_msg),
            Snackbar.LENGTH_INDEFINITE
        )
        artistNames = resources.getStringArray(R.array.artist_name).toList()
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                shouldDownload.value = true
            }
        }

        binding.composeView.setContent {
            MdcTheme {
                Surface(Modifier.fillMaxSize()) {
                    MainView(
                        ids = fileIds,
                        names = fileNames,
                        shouldDownload = shouldDownload,
                        artistNames = artistNames,
                        showAd = { showAd() },
                        navigateToPage3 = { navigateToPage3(it) },
                        getThumbnail = { getThumbnail(it) },
                    ) { id, position ->
                        startDownload(id, position)
                    }
                }
            }
        }
    }

    private fun getAllIntentData() {
        intent.getStringExtra(Constants.INT_2)?.let {
            createInterstitialAd(it)
        }
        intent.getStringExtra(Constants.BAN_1)?.let {
            createBannerAd(it)
        }
        intent.getStringExtra(Constants.INT_3)?.let {
            int3 = it
        }
        intent.getStringExtra(Constants.BAN_2)?.let {
            ban2 = it
        }
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

    //Banner ad listener
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

    private fun createInterstitialAd(adUnitId: String) {
        interstitialAd = MaxInterstitialAd(adUnitId, this)
        interstitialAd?.setListener(object : MaxAdListener {
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
                shouldDownload.value = true
            }

            override fun onAdDisplayed(maxAd: MaxAd) {
                if (snackBar.isShown) snackBar.dismiss()
            }

            override fun onAdClicked(maxAd: MaxAd) {
            }

            override fun onAdHidden(maxAd: MaxAd) {
                interstitialAd?.loadAd()
                shouldDownload.value = true
            }
        })
        interstitialAd?.loadAd()
    }

    private fun showAd() {
        snackBar.setText(getString(R.string.ad_coming_msg))
        snackBar.show()
        if (interstitialAd?.isReady == true) {
            interstitialAd?.showAd()
        } else {
            snackBar.dismiss()
            shouldDownload.value = true
        }
    }

    private fun startDownload(fileName: Int, position: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {

                    val directoryTest = File(
                        getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        getString(R.string.folder_name)
                    )
                    if (!directoryTest.exists()) {
                        directoryTest.mkdirs()
                    }
                    try {
                        val `in` = resources.openRawResource(fileName)
                        val file = File(directoryTest.path.toString() + "/${fileNames[position]}")
                        if (file.exists()) {
                            file.delete()
                        }
                        val out = FileOutputStream(file)
                        val buff = ByteArray(1024)
                        var read: Int
                        while (`in`.read(buff).also { read = it } > 0) {
                            out.write(buff, 0, read)
                        }
                        `in`.close()
                        out.close()
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    Toast.makeText(
                        this,
                        getString(R.string.file_download_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                }
                else -> {
                    requestPermissionLauncher.launch(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
            }
        }
    }

    private fun getThumbnail(fileName: Int): Drawable? {
        val videoURI: Uri = Uri.parse(
            "android.resource://$packageName/$fileName"
        )
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(this@MainActivity, videoURI)

        val data = retriever.embeddedPicture
        retriever.release()
        return if (data != null) {
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            BitmapDrawable(resources, bitmap)
        } else {
            AppCompatResources.getDrawable(this, R.drawable.ic_default_mp3_icon)
        }

    }

    private fun navigateToPage3(position: Int) {
        startActivity(Intent(this@MainActivity, VideoPlayerActivity::class.java).apply {
            putExtra(Constants.URI, "android.resource://$packageName/${fileIds[position]}")
            putExtra(Constants.FILE_NAME, fileNames[position].split(".")[0])
            putExtra(Constants.ARTIST_NAME, artistNames[position])
            putExtra(Constants.INT_3, int3)
            putExtra(Constants.BAN_2, ban2)
        })
    }
}

@Composable
fun MainView(
    ids: List<Int>,
    names: List<String>,
    shouldDownload: MutableState<Boolean>,
    artistNames: List<String>,
    showAd: () -> Unit,
    navigateToPage3: (Int) -> Unit,
    getThumbnail: (Int) -> Drawable?,
    startDownLoad: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var selected by remember { mutableStateOf(-1) }
        var position = 0
        Text(
            text = stringResource(R.string.playlist_title),
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1DB854)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.playlist_description),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(30.dp))
        ids.forEachIndexed { index, id ->
            MainCard(
                index = index,
                selected = selected,
                onSelected = { selected = it },
                name = names[index],
                artistName = artistNames[index],
                id = id,
                onPositionChange = { position = it },
                getDrawable = getThumbnail,
                showAd,
            )
            if (index != ids.lastIndex) {
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        CommonButton(text = stringResource(id = R.string.play)) {
            if (selected >= 0) navigateToPage3(selected)
        }
        LaunchedEffect(shouldDownload.value) {
            if (shouldDownload.value) {
                startDownLoad(ids[position], position)
                shouldDownload.value = false
            }
        }
    }
}

@Composable
fun MainCard(
    index: Int,
    selected: Int,
    onSelected: (Int) -> Unit,
    name: String,
    artistName: String,
    id: Int,
    onPositionChange: (Int) -> Unit,
    getDrawable: (Int) -> Drawable?,
    showAd: () -> Unit
) {
    val fileName = name.split(".")[0]
    Card(
        Modifier
            .clickable {
                onSelected(index)
            },
        backgroundColor = Color(0xFFFFFFFF)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            verticalAlignment = CenterVertically
        ) {
            if (index == selected) {
                RadioButton(
                    selected = true,
                    onClick = { })
            }
            Spacer(Modifier.width(15.dp))
            getDrawable(id)?.let {
                Image(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    contentScale = ContentScale.FillBounds,
                    painter = rememberDrawablePainter(it),
                    contentDescription = "Thumbnail"
                )
            }
            Spacer(Modifier.width(15.dp))
            Column(
                modifier = Modifier
                    .wrapContentHeight()
            ) {
                Text(
                    text = fileName,
                    color = Color(0xFF1DB854),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = artistName,
                    color = Color(0xFF1DB854),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            Image(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .clickable {
                        onPositionChange(index)
                        showAd()
                    }
                    .padding(10.dp),
                painter = painterResource(id = R.drawable.ic_baseline_download_24),
                contentDescription = "Download"
            )
        }
    }
}
