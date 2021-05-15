package com.uptodd.uptoddapp.media.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.uptodd.uptoddapp.utilities.UpToddMediaPlayer

class MediaStopReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent!=null) {
            if(intent.extras!=null) {
                val upToddMediaPlayer = UpToddMediaPlayer()
                if(UpToddMediaPlayer.songIndex!=-1){
                    upToddMediaPlayer.playNext()
                    val broadcastIntent = Intent(context, BackgroundPlayer::class.java)
                    broadcastIntent.putExtra("toRun", true)
                    broadcastIntent.putExtra("musicType", intent.getStringExtra("musicType"))
                    context!!.sendBroadcast(broadcastIntent)
                }
            }
        }
    }
}