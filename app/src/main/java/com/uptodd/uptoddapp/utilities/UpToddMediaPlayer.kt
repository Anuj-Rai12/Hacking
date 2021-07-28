package com.uptodd.uptoddapp.utilities

import android.media.MediaPlayer
import android.util.Log
import com.uptodd.uptoddapp.database.media.music.MusicFiles

class UpToddMediaPlayer {
    private var mediaPlayerListener: MediaPlayerListener? = null

    private var thread = Thread {}


    companion object {
        val mediaPlayer: MediaPlayer = MediaPlayer()
        val upToddMediaPlayer=UpToddMediaPlayer()
        var songPlaying: MusicFiles = MusicFiles()
        var songIndex: Int = -1
        var isPlaying = false
        var timer: Long? = null
        var isMemoryBooster:Boolean?=false
        var type=""
        private var mediaPlaylist: ArrayList<MusicFiles> = ArrayList()
    }



    fun setSource(song: MusicFiles) {

        isMemoryBooster = song.prenatal!=-1

        if (isPlaying) {
            mediaPlayer.stop()
            isPlaying = false
        }
        Log.d("url source",generateUrl(song))
        thread = Thread {
            try {
                mediaPlayer.reset()
                if (song.filePath == "NA")
                {
                    mediaPlayer.setDataSource(generateUrl(song))
                    Log.d("downloaded",false.toString())
                }
                else
                {
                    Log.d("downloaded",true.toString())
                    mediaPlayer.setDataSource(song.filePath)
                }

                mediaPlayer.prepare()
                mediaPlayer.setOnPreparedListener {
                    mediaPlayerListener!!.onReady()
                }

            } catch (e: Exception) {

                Log.e("Error", e.message!!)
                isPlaying=false
                mediaPlayerListener?.onError()
            }
        }
        thread.start()
        songPlaying = song
        mediaPlayerListener?.onReset(song)
    }

    private fun generateUrl(song: MusicFiles): String {
        if (song.prenatal!=-1)
            return  "https://www.uptodd.com/files/memory_booster/${song.file!!.trim()}.aac"
        else if (song.language == null || song.language=="null")
            return "https://www.uptodd.com/files/music/${song.image!!.trim()}/${song.file!!.trim()}.aac"
        else
            return "https://www.uptodd.com/files/poem/${song.name!!.trim()}.aac"
    }

    fun setPlaylist(playlist: ArrayList<MusicFiles>, indexOf: Int) {
        mediaPlaylist = playlist
        songIndex = indexOf
    }

    fun stop() {
        mediaPlayer.stop()
        isPlaying = false
        mediaPlayerListener?.onPause()
    }

    //fun to play or pause music
    fun playPause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            isPlaying = false
            mediaPlayerListener?.onPause()
        } else {
            if (!thread.isAlive || isMemoryBooster!!) {
                mediaPlayer.start()
                mediaPlayer.setOnCompletionListener {
                    if (songIndex < mediaPlaylist.size - 1) {
                        setSource(mediaPlaylist[songIndex + 1])
                        songIndex += 1
                    } else {
                        if (timer == null) {
                            isPlaying = false
                            UptoddNotificationUtilities.dismiss(notificationId = 150)
                            mediaPlayerListener?.onComplete()
                        } else {
                            playNext()
                        }
                    }
                }
                isPlaying = true
                mediaPlayerListener?.onStartPlaying()
            }
            else
            {
                Log.d("false alive","true")
            }
        }
    }

    fun setMediaPlayerListener(mediaPlayerListener: MediaPlayerListener): UpToddMediaPlayer {
        this.mediaPlayerListener = mediaPlayerListener
        return this
    }

    fun playNext() {
        if (mediaPlaylist.isEmpty()) return

        if (songIndex < mediaPlaylist.size - 1) {
            setSource(mediaPlaylist[songIndex + 1])
            songIndex += 1
        } else {
            setSource(mediaPlaylist[0])
            songIndex = 0
        }
    }

    fun playPrevious() {
        if (songIndex > 0) {
            setSource(mediaPlaylist[songIndex - 1])
            songIndex -= 1
        } else {
            setSource(songPlaying)
        }
    }

    interface MediaPlayerListener {
        fun onComplete()

        fun onReady()

        fun onReset(song: MusicFiles)

        fun onError(){

        }

        fun onStartPlaying()

        fun onPause()
    }
}