package com.example.arabic_unity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
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
import com.example.arabic_unity.databinding.ActivityMainBinding
import com.example.arabic_unity.ui.Constants
import com.example.arabic_unity.ui.component.CommonButton
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.android.material.composethemeadapter.MdcTheme
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream


class MainActivity : ComponentActivity(), MaxAdViewAdListener {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val fileIds = listOf(R.raw.toyor, R.raw.untitled)
    private val fileNames = listOf("toyor.3gp", "untitled.mp4")
    lateinit var artistNames: List<String>
    var int3 = ""
    var ban2 = ""
    val shouldDownload = mutableStateOf(false)
    lateinit var snackBar: Snackbar
    private lateinit var interstitialAd: MaxInterstitialAd
    private var adView: MaxAdView? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        getAllIntentData();
        artistNames = resources.getStringArray(R.array.artist_name).toList()
        requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) { shouldDownload.value = true }
            }

        binding.composeView.setContent {
            MdcTheme() {
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
        intent.getStringExtra(Constants.INT_2)?.let{
            createInterstitialAd(it)
        }
        intent.getStringExtra(Constants.BAN_1)?.let{
            createBannerAd(it)
        }
        intent.getStringExtra(Constants.INT_3)?.let{
            int3 = it
        }
        intent.getStringExtra(Constants.BAN_2)?.let{
            ban2 = it
        }
        snackBar = Snackbar.make(
            this.findViewById(android.R.id.content),
            "Ad is coming..",
            Snackbar.LENGTH_INDEFINITE
        )
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
        interstitialAd.setListener(object : MaxAdListener {
            override fun onAdLoaded(maxAd: MaxAd) {
            }
            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                if (snackBar.isShown) {
                    snackBar.setText("Ad Loading Failed")
                    snackBar.dismiss()
                }
                shouldDownload.value = true
            }
            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                if (snackBar.isShown) {
                    snackBar.setText("Ad Loading Failed")
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
                Toast.makeText(this@MainActivity, "onAdHidden", Toast.LENGTH_SHORT).show()
                interstitialAd.loadAd()
                shouldDownload.value = true
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
                        "Arabic_Unity"
                    )

                    directoryTest.mkdirs()
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
                        "File Downloaded. Check your Download folder.",
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

    private fun getThumbnail(fileName: Int): Drawable {
        val videoURI: Uri = Uri.parse(
            "android.resource://$packageName/$fileName"
        )
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(this@MainActivity, videoURI)
        val bitmap = retriever
            .getFrameAtTime(5000000, MediaMetadataRetriever.OPTION_PREVIOUS_SYNC)
        return BitmapDrawable(resources, bitmap)
    }

    private fun navigateToPage3(position: Int) {
        startActivity(Intent(this@MainActivity, VideoPlayerActivity::class.java).apply {
            putExtra("URI", "android.resource://$packageName/${fileIds[position]}")
            putExtra("FileName", fileNames[position].split(".")[0])
            putExtra("ArtistName", artistNames[position])
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
    getThumbnail: (Int) -> Drawable,
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
                names = names,
                artistNames = artistNames,
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
        CommonButton(text = "Play") {
            if(selected>=0) navigateToPage3(selected)
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
    names: List<String>,
    artistNames: List<String>,
    id: Int,
    onPositionChange: (Int) -> Unit,
    getDrawable: (Int) -> Drawable,
    showAd: () -> Unit
) {
    val fileName = names[index].split(".")[0]
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
            Image(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(2.dp)),
                contentScale = ContentScale.FillBounds,
                painter = rememberDrawablePainter(getDrawable(id)),
                contentDescription = "Thumbnail"
            )
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
                    text = artistNames[index],
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
