package com.example.arabic_unity.utils

import android.content.Context
import android.net.Uri
import android.view.SurfaceView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView

object ExoPlayerViewManager  {
    private var videoUri: Uri? = null
    private var player: ExoPlayer? = null
    private var currentVolume = 0f

    fun prepareExoPlayer(context: Context?, exoPlayerView: StyledPlayerView?) {
        if (context == null || exoPlayerView == null) {
            return
        }
        if (player == null) {
            player = ExoPlayer.Builder(context)
                .setSeekForwardIncrementMs(10000L)
                .setSeekBackIncrementMs(10000L)
                .build().also {
                    val mediaItem = MediaItem.fromUri(videoUri!!)
                    it.setMediaItem(mediaItem)
                    it.playWhenReady = true
                    it.prepare()
                    it.addListener(object : Player.Listener {
                        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                            if (playbackState == ExoPlayer.STATE_READY) {
                               exoPlayerView.showController()
                            }
                            else if (playbackState == ExoPlayer.STATE_BUFFERING) {
                                exoPlayerView.hideController()
                                //performEndExoPlayer()
                            }
                        }
                    })
                }
        }
        player!!.clearVideoSurface()
        player!!.setVideoSurfaceView(exoPlayerView.videoSurfaceView as SurfaceView?)
        player!!.seekTo(player!!.currentPosition + 1)
        exoPlayerView.player = player
    }

    fun setVolume(): Boolean{
        return if(player!!.volume == 0f){
            player?.volume = currentVolume
            false
        }else {
            currentVolume = player!!.volume
            player?.volume = 0f
            true
        }
    }

    fun releaseVideoPlayer() {
        if (player != null) {
            player!!.release()
        }
        player = null
    }

    fun goToBackground() {
        if (player != null) {
            player!!.pause()
        }
    }

    fun setVideoUrl(videoUrl: String): ExoPlayerViewManager {
        videoUri = Uri.parse(videoUrl)
        return this
    }
}
