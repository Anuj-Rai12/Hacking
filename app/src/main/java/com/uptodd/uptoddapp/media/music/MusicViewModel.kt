package com.uptodd.uptoddapp.media.music

import android.app.Application
import android.content.Context
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
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.UpToddMediaPlayer
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.collections.set

class MusicViewModel(val database: MusicFilesDatabaseDao, application: Application) :
    AndroidViewModel(
        application
    ) {

    private lateinit var dpi: String

    private lateinit var downloadedMusic: List<MusicFiles>

    private var _isLoading: MutableLiveData<Int> = MutableLiveData()
    val isLoading: LiveData<Int>
        get() = _isLoading

    private var _isMediaReady: MutableLiveData<Boolean> = MutableLiveData()
    val isMediaReady: LiveData<Boolean>
        get() = _isMediaReady

    private var _presetTimer = MutableLiveData<Int>()
    val presetTimer: LiveData<Int>
        get() = _presetTimer

    var apiError = ""
    var variableError = ""

    var musicFiles = HashMap<String, ArrayList<MusicFiles>>()

    //Create Media Player variables
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

    var notActive=true

    //Initialize the required variables and set media-player-listener
    init {

        initializeMusic()

        _isLoading.value = 1

        _isMediaReady.value = true

        _presetTimer.value = -1

        _isPlaying.value = UpToddMediaPlayer.isPlaying

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
                _image.value = AllUtil.getMusicImage(song, dpi)
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


    fun recheckState()
    {
        if(UpToddMediaPlayer.isPlaying && UpToddMediaPlayer.songPlaying!=null)
        {
            if(UpToddMediaPlayer.type=="music" )
            {
                Log.d("type","music")
                _image.value = AllUtil.getMusicImage(UpToddMediaPlayer.songPlaying, dpi)
            }
            else
            {
                Log.d("type","poem")
                _image.value = AllUtil.getPoemImage(UpToddMediaPlayer.songPlaying, dpi)

            }
        }

    }

    fun initializeAll(context: Context) {
        if (_isPlaying.value!!) {
            _title.value = UpToddMediaPlayer.songPlaying.name
            currentPlaying = UpToddMediaPlayer.songPlaying.id
            if(UpToddMediaPlayer.isPlaying && UpToddMediaPlayer.songPlaying!=null)
            {
                if(UpToddMediaPlayer.type=="music" )
                {
                    Log.d("type","music")
                    _image.value = AllUtil.getMusicImage(UpToddMediaPlayer.songPlaying, dpi)
                }
                else
                {
                    Log.d("type","poem")
                    _image.value = AllUtil.getPoemImage(UpToddMediaPlayer.songPlaying, dpi)

                }
            }
        } else {
            _title.value = "Dummy text will always be dummy"
            _image.value =
                "https://www.pngfind.com/pngs/m/427-4277341_add-play-button-to-image-online-overlay-play.png"
        }
        getAllMusicCategories(context)

    }



    fun initializeOffline() {
        if (_isPlaying.value!!) {
            _title.value = UpToddMediaPlayer.songPlaying.name
            currentPlaying = UpToddMediaPlayer.songPlaying.id



        } else {
            _title.value = ""
            _image.value = ""
        }

        musicFiles= HashMap()

        viewModelScope.launch {
            downloadedMusic = database.getAllDownloadedMusic()
            downloadedMusic.forEach {
                if (musicFiles.containsKey(it.image)) {

                    if(!musicFiles[it.image]!!.contains(it))
                        musicFiles[it.image]!!.add(it)
                } else {
                    musicFiles[it.image!!] = ArrayList()
                    musicFiles[it.image!!]?.add(it)
                }
            }
            _isLoading.value = 0
        }



        mediaPlayer.setMediaPlayerListener(object : UpToddMediaPlayer.MediaPlayerListener {
            override fun onComplete() {
                if(UpToddMediaPlayer.type=="music" )
                    _image.value = AllUtil.getMusicImage(UpToddMediaPlayer.songPlaying, dpi)
                else
                    _image.value = AllUtil.getPoemImage(UpToddMediaPlayer.songPlaying, dpi)
                _isPlaying.value = UpToddMediaPlayer.isPlaying
            }

            override fun onReady() {
                mediaPlayer.playPause()
                _isMediaReady.value = true
            }

            override fun onReset(song: MusicFiles) {
                _isPlaying.value = UpToddMediaPlayer.isPlaying
                _image.value = AllUtil.getMusicImage(song, dpi)
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


    fun resetPresetTimer() {
        _presetTimer.value = -1
    }

    private fun getAllMusicCategories(context: Context) {
        _isLoading.value=1
        musicFiles = HashMap()
        val language = AllUtil.getLanguage()
        val userType=UptoddSharedPreferences.getInstance(context).getUserType()
        val stage=UptoddSharedPreferences.getInstance(context).getStage()
        val country=AllUtil.getCountry(context)

        AndroidNetworking.get("https://www.uptodd.com/api/musics?lang=$language&userType=$userType&country=$country&motherStage=$stage")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    if (response.getString("status") == "Success") {

                        if (response.get("data").toString() != "null") {
                            viewModelScope.launch {

                                try {
                                    val apiFiles = AllUtil.getAllMusic(response.get("data").toString())

                                    musicFiles=HashMap()
                                    apiFiles.forEach {
                                        if (musicFiles.containsKey(it.image)) {
                                            Log.i("musicc", it.name!!)
                                            if(!musicFiles[it.image]?.contains(it)!!)
                                                musicFiles[it.image]?.add(it)
                                        } else {
                                            Log.i("musicnc", it.name!!)
                                            musicFiles[it.image!!] = ArrayList()
                                            musicFiles[it.image!!]?.add(it)
                                        }
                                        if (getIsMusicDownloaded(it))
                                            it.filePath = database.getFilePath(it.id)
                                        else
                                            it.filePath = "NA"
                                    }
                                    notActive=apiFiles.isEmpty()
                                    _isLoading.value = 0
                                }
                                catch (e:Exception)
                                {
                                    notActive=true
                                    _isLoading.value = 1
                                }
                            }
                        }
                        else {
                            notActive=true
                            _isLoading.value = 1
                        }

                    }
                    else {
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
//        if (UpToddMediaPlayer.songIndex != -1) {
////            if (UpToddMediaPlayer.isPlaying)
//            mediaPlayer.playPause()
//        }
        mediaPlayer.playPause()
    }

    fun nextSong() {
        mediaPlayer.playNext()
    }

    fun prevSong() {
        mediaPlayer.playPrevious()
    }

    //Set source
    fun playFile(music: MusicFiles, musicCategory: String) {
        _title.value = music.name
        _image.value = AllUtil.getMusicImage(music, dpi)
        mediaPlayer.setSource(music)
        mediaPlayer.setPlaylist(
            musicFiles[musicCategory]!!,
            musicFiles[musicCategory]!!.indexOf(music)
        )
        UpToddMediaPlayer.type="music"
        _isMediaReady.value = false
        if (music.playtimeInMinutes != 0) {
            _presetTimer.value = music.playtimeInMinutes
        }
    }

    fun setDpi(dpi: String) {
        this.dpi = dpi
    }

    fun getDpi(): String {
        return dpi
    }
//
//    fun downloadOrDeleteMusic(music: MusicFiles, uptoddDownloadManager: UpToddDownloadManager, destinationDir: File) {
//        if(getIsMusicDownloaded(music)){
//            val file = File(music.filePath)
//            file.delete()
//            viewModelScope.launch {
//                database.delete(music)
//                initializeMusic()
//            }
//        }
//        else{
//            val file = File(destinationDir.path, "${music.file}.aac")
//            if(file.exists())
//                file.delete()
//            if(!destinationDir.exists())
//                destinationDir.mkdirs()
//            if(!destinationDir.canWrite())
//                destinationDir.setWritable(true)
//
//            val destinationUri = Uri.fromFile(file)
//
//            uptoddDownloadManager.setDestinationUri(destinationUri)
//            uptoddDownloadManager.setUrl("https://uptodd.com/files/music/${music.image!!.trim()}/${music.file!!.trim()}.aac")
//            uptoddDownloadManager.setListener(object : UpToddDownloadManager.DownloadListener {
//                override fun onProgress(progress: Float) {
//                    if(progress<1F){
//                        Log.i("progress", (progress*100).toString())
//                    }
//                }
//
//                override fun onSuccess(path: String) {
//                    updatePath(music, path)
//                    Log.i("download", "Completed")
//                }
//
//                override fun onFailed(throwable: Throwable) {
//
//                }
//            })
//            uptoddDownloadManager.download()
//        }
//    }

    private fun initializeMusic() {
        viewModelScope.launch {
            downloadedMusic = database.getAllDownloadedMusic()
        }
    }

//    private fun updatePath(music: MusicFiles, path: String) {
//        viewModelScope.launch {
//            music.filePath = path
//            music.language = "NA"
//            database.insert(music)
//            initializeMusic()
//        }
//    }

    fun getIsMusicDownloaded(music: MusicFiles): Boolean {
        downloadedMusic.forEach {
            if (it.id == music.id)
                return@getIsMusicDownloaded true
        }
        return false
    }

    fun doneLoading() {
        _isLoading.value = 110
    }

    fun showLoading() {
        _isLoading.value = 1
    }

}