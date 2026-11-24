package com.audiodjpro

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class AudioPlaybackService : Service() {
    inner class LocalBinder : Binder() { fun service() = this@AudioPlaybackService }
    private val binder = LocalBinder()
    lateinit var basePlayer: ExoPlayer
        private set
    lateinit var trackPlayer: ExoPlayer
        private set
    var onTrackEnded: (() -> Unit)? = null

    override fun onCreate() {
        super.onCreate()
        basePlayer = ExoPlayer.Builder(this).build()
        trackPlayer = ExoPlayer.Builder(this).build()
        trackPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    basePlayer.play()
                    onTrackEnded?.invoke()
                    updateNotification("Audio base activo")
                }
            }
        })
        createChannel()
        startForeground(41, notification("Listo · selecciona el audio base"))
    }

    fun setBase(uri: android.net.Uri) {
        basePlayer.setMediaItem(MediaItem.fromUri(uri))
        basePlayer.repeatMode = Player.REPEAT_MODE_ONE
        basePlayer.prepare()
        basePlayer.volume = 0.75f
        trackPlayer.pause()
        basePlayer.play()
        startForeground(41, notification("Audio base en reproducción"))
    }

    fun playTrack(uri: android.net.Uri) {
        basePlayer.pause()
        trackPlayer.stop()
        trackPlayer.setMediaItem(MediaItem.fromUri(uri))
        trackPlayer.prepare()
        trackPlayer.play()
        startForeground(41, notification("Reproduciendo canción"))
    }

    fun stopTrack() {
        trackPlayer.stop()
        if (basePlayer.mediaItemCount > 0) basePlayer.play()
        updateNotification("Audio base activo")
    }

    fun setBaseVolume(value: Float) { basePlayer.volume = value }
    fun setTrackVolume(value: Float) { trackPlayer.volume = value }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel("audio_dj", "AUDIO DJ PRO", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
    private fun notification(message: String) = NotificationCompat.Builder(this, "audio_dj")
        .setSmallIcon(android.R.drawable.ic_media_play).setContentTitle("AUDIO DJ PRO")
        .setContentText(message).setOngoing(true)
        .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)).build()
    private fun updateNotification(message: String) {
        getSystemService(NotificationManager::class.java).notify(41, notification(message))
    }
    override fun onBind(intent: Intent?): IBinder = binder
    override fun onDestroy() {
        basePlayer.release()
        trackPlayer.release()
        super.onDestroy()
    }
}
