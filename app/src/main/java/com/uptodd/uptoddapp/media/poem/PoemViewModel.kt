package com.uptodd.uptoddapp.media.poem

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.media.music.MusicFiles
import com.uptodd.uptoddapp.database.media.music.MusicFilesDatabaseDao
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.UpToddMediaPlayer
import kotlinx.coroutines.launch
import org.json.JSONObject

class PoemViewModel(val database: MusicFilesDatabaseDao, application: Application) : AndroidViewModel(
    application)  {

    private var dpi = ""

    private lateinit var downloadedPoems: List<MusicFiles>

    private var _isLoading: MutableLiveData<Int> = MutableLiveData()
    val isLoading: LiveData<Int>
        get() = _isLoading

    private var _isMediaReady: MutableLiveData<Boolean> = MutableLiveData()
    val isMediaReady: LiveData<Boolean>
        get() = _isMediaReady

    var apiError = ""

    private var _poems = MutableLiveData<ArrayList<MusicFiles>>()
    val poems: LiveData<ArrayList<MusicFiles>>
        get() = _poems

    private var _title = MutableLiveData<String>()
    val title: LiveData<String>
        get() = _title

    private var _image = MutableLiveData<String>()
    val image: LiveData<String>
        get() = _image

    private var _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean>
        get() = _isPlaying

    private var currentPlaying = 0

    private var mediaPlayer: UpToddMediaPlayer = UpToddMediaPlayer.upToddMediaPlayer

    init {

        initializePoems()

        _poems.value = ArrayList()

        _isLoading.value = 1

        _isMediaReady.value = true

        _isPlaying.value = UpToddMediaPlayer.isPlaying

    }

    private fun initializePoems() {
        viewModelScope.launch {
            downloadedPoems = database.getAllDownloadedPoem()
        }
    }

    fun initializeOffline(){
        if (_isPlaying.value!!) {
            _title.value = UpToddMediaPlayer.songPlaying.name
            currentPlaying = UpToddMediaPlayer.songPlaying.id
            _image.value = AllUtil.getPoemImage(UpToddMediaPlayer.songPlaying, dpi)
        } else {
            _title.value = ""
            _image.value = ""
        }

        viewModelScope.launch {
            downloadedPoems = database.getAllDownloadedPoem()
            downloadedPoems.forEach{
                Log.i("DP", "${it.id} -> ${it.name}")
            }
            _poems.value = ArrayList(downloadedPoems)
            _poems.value?.forEach{
                Log.i("PV", "${it.id} -> ${it.name}")
            }
            _isLoading.value = 0
        }

        mediaPlayer.setMediaPlayerListener(object : UpToddMediaPlayer.MediaPlayerListener {
            override fun onComplete() {
                _isPlaying.value = UpToddMediaPlayer.isPlaying
            }

            override fun onReady() {
                mediaPlayer.playPause()
                _isMediaReady.value = true
            }

            override fun onReset(song: MusicFiles) {
                _isPlaying.value = UpToddMediaPlayer.isPlaying
                _image.value = AllUtil.getPoemImage(song, dpi)
                _title.value = song.name
                currentPlaying = song.id
            }

            override fun onStartPlaying() {
                _isPlaying.value = UpToddMediaPlayer.isPlaying
            }

            override fun onPause() {
                _isPlaying.value = UpToddMediaPlayer.isPlaying
            }
        })
    }

    fun initializeAll() {
        if (_isPlaying.value!!) {
            _title.value = UpToddMediaPlayer.songPlaying.name
            currentPlaying = UpToddMediaPlayer.songPlaying.id
            _image.value = AllUtil.getPoemImage(UpToddMediaPlayer.songPlaying, dpi)
        } else {
            _title.value = ""
            _image.value = ""
        }

        getAllPoems()

        mediaPlayer.setMediaPlayerListener(object : UpToddMediaPlayer.MediaPlayerListener {
            override fun onComplete() {
                _isPlaying.value = UpToddMediaPlayer.isPlaying
            }

            override fun onReady() {
                mediaPlayer.playPause()
                _isMediaReady.value = true
            }

            override fun onReset(song: MusicFiles) {
                _isPlaying.value = UpToddMediaPlayer.isPlaying
                _image.value = AllUtil.getPoemImage(song, dpi)
                _title.value = song.name
                currentPlaying = song.id
            }

            override fun onStartPlaying() {
                _isPlaying.value = UpToddMediaPlayer.isPlaying
            }

            override fun onPause() {
                _isPlaying.value = UpToddMediaPlayer.isPlaying
            }
        })
    }

    private fun getAllPoems() {
        AndroidNetworking.get("https://uptodd.com/api/poems")
            .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    if (response.getString("status") == "Success") {
                        viewModelScope.launch {
                            val  poems = AllUtil.getAllMusic(response.get("data").toString())
                            poems.forEach {
                                if(getIsPoemDownloaded(it))
                                    it.filePath = database.getFilePath(it.id)
                                else
                                    it.filePath = "NA"
                            }
                            _poems.value = poems
                            _isLoading.value = 0
                        }

                    } else {
                        apiError = response.getString("message")
                        _isLoading.value = -1
                    }
                }

                override fun onError(error: ANError) {
                    apiError = error.message.toString()
                    _isLoading.value = -1
                    Log.i("error", error.errorBody)
                }
            })
    }

    fun playPauseMusic() {
        if (UpToddMediaPlayer.songIndex != -1) {
            mediaPlayer.playPause()
        }
    }

    fun nextSong() {
        mediaPlayer.playNext()
    }

    fun prevSong(){
        mediaPlayer.playPrevious()
    }

    fun playFile(poem: MusicFiles) {
        _title.value = poem.name
        _image.value = AllUtil.getPoemImage(poem, dpi)
        _isMediaReady.value = false
        mediaPlayer.setSource(poem)
        mediaPlayer.setPlaylist(_poems.value!!, _poems.value!!.indexOf(poem))
    }

    fun setDpi(dpi: String) {
        this.dpi = dpi
    }

    fun getDpi(): String {
        return dpi
    }

    fun getIsPoemDownloaded(poem: MusicFiles): Boolean {
        downloadedPoems.forEach {
            if (it.id == poem.id)
                return@getIsPoemDownloaded true
        }
        return false
    }

}