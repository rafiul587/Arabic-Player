package com.example.arabic_unity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.arabic_unity.databinding.ActivityMainBinding
import com.example.arabic_unity.ui.component.CommonButton
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.android.material.composethemeadapter.MdcTheme
import java.io.*
import java.lang.reflect.Field


class MainActivity : ComponentActivity() {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }
    val fileIds = listOf(R.raw.toyor, R.raw.untitled)
    val fileNames = listOf("toyor.3gp", "untitled.mp4")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            }

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE


            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
            }
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }


        binding.root.setContent {
            MdcTheme() {
                Surface() {
                    MainView(
                        { getThumbnail(it) },
                        fileIds,
                        fileNames,
                        { navigateToPage3(it) }) { name, position ->
                        startDownload(name, position)
                    }
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

    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }

    private fun startDownload(fileName: Int, position: Int) {
        val directoryTest = File(
            getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), ""
        )
        /* val value = TypedValue()
         resources.getValue(fileName, value, true)
         val s = value.string.toString().split("/").toTypedArray()*/

        directoryTest.mkdirs()
        val `in` = resources.openRawResource(fileName)
        val out = FileOutputStream(directoryTest.path.toString() + "/${fileNames[position]}")
        val buff = ByteArray(1024)
        var read = 0

        try {
            while (`in`.read(buff).also { read = it } > 0) {
                out.write(buff, 0, read)
            }
        } finally {
            `in`.close()
            out.close()
        }
        Toast.makeText(this, "File Downloaded. Check your Download folder.", Toast.LENGTH_SHORT)
            .show()
        /* val directoryTest = File(
             getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "raw2sd"
         )
         directoryTest.mkdirs()
         try {
             val sound = FileOutputStream(directoryTest.path.toString() + "/untitled.mp4")
             val `is` = resources.openRawResource(R.raw.untitled)
             val a = `is`.available()
             val buf = ByteArray(a)
             `is`.read(buf, 0, a)
             sound.write(buf)
             sound.flush()
             sound.close()
             Log.d("TAG", "startDownload: ${directoryTest.path.toString()}")
         } catch (e: FileNotFoundException) {
             Log.d("TAG", "startDownload: ${e.printStackTrace()}")
             return
         } catch (e: Exception) {
             Log.d("TAG", "startDownload: ${e.printStackTrace()}")
             return
         }*/
    }


    private fun listRaw(list: SnapshotStateList<String>) {
        val fields: Array<Field> = R.raw::class.java.fields
        for (count in fields.indices) {
            val filename = fields[count].name
            list.add(filename)
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

    private fun navigateToPage3(fileName: String) {
        startActivity(Intent(this@MainActivity, VideoPlayerActivity::class.java).apply {
            val rawId = resources.getIdentifier(fileName, "raw", packageName)
            putExtra("URI", "android.resource://$packageName/$rawId")
            putExtra("FileName", fileName)
        })

    }
}

@Composable
fun MainView(
    getDrawable: (Int) -> Drawable,
    ids: List<Int>,
    names: List<String>,
    navigateToPage3: (String) -> Unit,
    startDownLoad: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var selected by remember { mutableStateOf(-1) }
        ids.forEachIndexed { index, name ->
            val s = names[index].split(".")
            val fileName = s[0]
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
                            text = fileName,
                            color = Color(0xFF1DB854),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Unknown",
                            color = Color(0xFF1DB854),
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))

                    Image(
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .clickable {
                                startDownLoad(name, index)
                            }
                            .padding(10.dp),
                        painter = painterResource(id = R.drawable.ic_baseline_download_24),
                        contentDescription = "Download"
                    )

                }
            }
            if (index != ids.lastIndex) {
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        CommonButton(text = "Play") {
            val s = names[selected].split(".")
            val fileName = s[0]
            if (selected >= 0) navigateToPage3(fileName)
        }
    }

}