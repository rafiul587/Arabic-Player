package com.example.arabic_unity

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arabic_unity.databinding.ActivityMainBinding
import com.example.arabic_unity.ui.component.CommonButton
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.unity3d.ads.UnityAds
import java.lang.reflect.Field


class MainActivity : ComponentActivity() {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.root.setContent {
            MaterialTheme() {
                Surface() {
                    val list = remember {
                        mutableStateListOf<String>()
                    }
                    LaunchedEffect(Unit) {
                        listRaw(list)
                    }
                    MainView({ getThumbnail(it) }, list) { navigateToPage3(it) }
                }
            }
        }
        /*binding.checkBox3 .setOnCheckedChangeListener { _, _ ->
            //showInterstitialAd()
        }
        binding.btn1.setOnClickListener {
            //showInterstitialAd()
            startActivity(Intent(this@MainActivity, page3::class.java))
        }*/
    }

    private fun listRaw(list: SnapshotStateList<String>) {
        val fields: Array<Field> = R.raw::class.java.fields
        for (count in fields.indices) {
            // Use that if you just need the file name
            val filename = fields[count].name
            Log.i("filename", filename)
            val rawId = resources.getIdentifier(filename, "raw", packageName)
            val value = TypedValue()
            resources.getValue(rawId, value, true)
            val s = value.string.toString().split("/").toTypedArray()
            list.add(s[s.size - 1])
        }
    }

    private fun getThumbnail(fileName: String): Drawable {

        val rawId = resources.getIdentifier(fileName.split(".")[0], "raw", packageName)
        Log.d("TAG", "getThumbnail: $fileName")
        val videoURI: Uri = Uri.parse(
            "android.resource://$packageName/$rawId"
        )
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(this@MainActivity, videoURI)
        Log.d("TAG", "getThumbnail: $retriever.")
        val bitmap = retriever
            .getFrameAtTime(5000000, MediaMetadataRetriever.OPTION_PREVIOUS_SYNC)
        return BitmapDrawable(resources, bitmap)
    }

    private fun navigateToPage3(fileName: String) {
        startActivity(Intent(this@MainActivity, VideoPlayerActivity::class.java).apply {
            val rawId = resources.getIdentifier(fileName, "raw", packageName)
            putExtra("URI", "android.resource://$packageName/$rawId")
            putExtra("FileName", fileName)
        })

    }

    override fun onResume() {
        super.onResume()
        /*val iUnityBannerListener: IUnityBannerListener = object : IUnityBannerListener {
            override fun onUnityBannerLoaded(s: String, view: View) {
                (findViewById<View>(R.id.bannerAdLayout) as ViewGroup).removeView(view)
                (findViewById<View>(R.id.bannerAdLayout) as ViewGroup).addView(view)
            }

            override fun onUnityBannerUnloaded(s: String) {}
            override fun onUnityBannerShow(s: String) {}
            override fun onUnityBannerClick(s: String) {}
            override fun onUnityBannerHide(s: String) {}
            override fun onUnityBannerError(s: String) {}
        }*/
/*        UnityBanners.setBannerListener(iUnityBannerListener)
        if (UnityAds.isInitialized()) {
            UnityAds.load(resources.getString(R.string.interstitial))
            UnityBanners.loadBanner(this@MainActivity, resources.getString(R.string.banner))
        }*/
    }

    private fun showInterstitialAd() {
        if (UnityAds.isReady(resources.getString(R.string.interstitial))) {
            UnityAds.show(this@MainActivity, resources.getString(R.string.interstitial))
        }
    }
}

@Composable
fun MainView(
    getDrawable: (String) -> Drawable,
    list: List<String>,
    navigateToPage3: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var selected by remember { mutableStateOf(-1) }
        list.forEachIndexed { index, name ->
            Card(
                Modifier
                    .padding(
                        horizontal = 20.dp
                    )
                    .clickable {
                        selected = index
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
                            onClick = { selected = index })
                    }
                    Spacer(Modifier.width(15.dp))
                    Image(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        contentScale = ContentScale.FillBounds,
                        painter = rememberDrawablePainter(getDrawable(name)),
                        contentDescription = "Thumbnail"
                    )
                    Spacer(Modifier.width(15.dp))
                    Column(
                        modifier = Modifier
                            .wrapContentHeight()
                    ) {
                        Text(
                            text = name,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Unknown",
                            color = Color.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            if (index != list.lastIndex) {
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        CommonButton(text = "Play") {
            if (selected >= 0) navigateToPage3(list[selected].split(".")[0])
        }
    }

}