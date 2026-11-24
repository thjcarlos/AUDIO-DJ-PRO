package com.audiodjpro

import android.Manifest
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.OpenableColumns
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var playerService: AudioPlaybackService? = null
    private var bound = false
    private val tracks = mutableListOf<Uri>()
    private val names = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var status: TextView
    private lateinit var baseName: TextView
    private lateinit var trackName: TextView
    private lateinit var trackList: ListView

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            playerService = (service as AudioPlaybackService.LocalBinder).service()
            bound = true
            playerService?.onTrackEnded = { runOnUiThread {
                status.text = "AUDIO BASE REPRODUCIÉNDOSE"
                trackName.text = "Audio finalizado · Base activa"
            } }
            status.text = "MOTOR DE AUDIO LISTO"
        }
        override fun onServiceDisconnected(name: ComponentName?) { bound = false; playerService = null }
    }

    private val pickBase = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            baseName.text = displayName(it)
            playerService?.setBase(it)
            status.text = "AUDIO BASE EN BUCLE"
        }
    }
    private val pickTracks = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            if (!tracks.contains(uri)) { tracks.add(uri); names.add(displayName(uri)) }
        }
        adapter.notifyDataSetChanged()
        status.text = "${tracks.size} AUDIOS EN BIBLIOTECA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.statusText)
        baseName = findViewById(R.id.baseName)
        trackName = findViewById(R.id.trackName)
        trackList = findViewById(R.id.trackList)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, names)
        trackList.adapter = adapter
        trackList.setOnItemClickListener { _, _, position, _ -> play(position) }
        findViewById<Button>(R.id.chooseBase).setOnClickListener { pickBase.launch(arrayOf("audio/*")) }
        findViewById<Button>(R.id.addTracks).setOnClickListener { pickTracks.launch(arrayOf("audio/*")) }
        findViewById<Button>(R.id.playTrack).setOnClickListener {
            val position = trackList.checkedItemPosition
            if (position >= 0) play(position) else Toast.makeText(this, "Selecciona una canción de la lista", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.stopTrack).setOnClickListener {
            playerService?.stopTrack()
            status.text = "AUDIO BASE ACTIVO"
        }
        findViewById<SeekBar>(R.id.baseVolume).setOnSeekBarChangeListener(volumeListener { playerService?.setBaseVolume(it) })
        findViewById<SeekBar>(R.id.trackVolume).setOnSeekBarChangeListener(volumeListener { playerService?.setTrackVolume(it) })
        val intent = Intent(this, AudioPlaybackService::class.java)
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 10)
    }

    private fun play(position: Int) {
        if (position !in tracks.indices) return
        trackList.setItemChecked(position, true)
        trackName.text = names[position]
        playerService?.playTrack(tracks[position])
        status.text = "REPRODUCIENDO TRACK"
    }

    private fun displayName(uri: Uri): String {
        var result = "Audio"
        contentResolver.query(uri, null, null, null, null)?.use { c ->
            val index = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (c.moveToFirst() && index >= 0) result = c.getString(index)
        }
        return result
    }

    private fun volumeListener(action: (Float) -> Unit) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { if (fromUser) action(progress / 100f) }
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    override fun onDestroy() {
        if (bound) unbindService(connection)
        super.onDestroy()
    }
}
