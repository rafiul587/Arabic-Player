package com.example.arabic_unity

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.ui.StyledPlayerView


// Fullscreen related code taken from Android Studio blueprint
class FullScreenVideoActivity : AppCompatActivity() {
    private val mHideHandler: Handler = Handler()
    private var mContentView: View? = null
    private val mHidePart2Runnable = Runnable { // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of
        // API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(mContentView!!) ?: return@Runnable
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
    private val mHideRunnable = Runnable { hide() }
    private var mVideoUri: String? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_video)
        mContentView = findViewById(R.id.enclosing_layout)
        val playerView = findViewById<StyledPlayerView>(R.id.videoView)
/*        mVideoUri = intent.getStringExtra(ExoPlayerViewManager.EXTRA_VIDEO_URI)
        mVideoUri?.let {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            ExoPlayerViewManager.getInstance(it)
                .prepareExoPlayer(this, playerView)
        }*/

        // Set the fullscreen button to "close fullscreen" icon
        val fullscreenIcon: ImageView = playerView.findViewById(R.id.exo_fullscreen_button)
        fullscreenIcon.setImageResource(R.drawable.ic_baseline_clear_24)
        fullscreenIcon.setOnClickListener { finish() }
    }

/*    public override fun onResume() {
        super.onResume()
        mVideoUri?.let { ExoPlayerViewManager.getInstance(it).goToForeground() }
    }

    public override fun onPause() {
        super.onPause()
        mVideoUri?.let { ExoPlayerViewManager.getInstance(it).goToBackground() }
    }*/

    public override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide()
    }

    private fun hide() {
        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private fun delayedHide() {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, 100)
    }

    companion object {
        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}