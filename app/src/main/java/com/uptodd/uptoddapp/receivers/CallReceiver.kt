package com.uptodd.uptoddapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.uptodd.uptoddapp.media.player.BackgroundPlayer
import com.uptodd.uptoddapp.utilities.UpToddMediaPlayer


class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            if(UpToddMediaPlayer.isPlaying)
            {
                UpToddMediaPlayer.upToddMediaPlayer.playPause()
                val intent = Intent(context, BackgroundPlayer::class.java)
                intent.putExtra("toRun", false)
                intent.putExtra("musicType", "music")
                context?.sendBroadcast(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}