package com.uptodd.uptoddapp.media.memorybooster

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
import com.uptodd.uptoddapp.api.getMonth
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.media.memorybooster.MemoryFilesDao
import com.uptodd.uptoddapp.database.media.music.MusicFiles
import com.uptodd.uptoddapp.database.media.music.MusicFilesDatabaseDao
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.UpToddMediaPlayer
import kotlinx.coroutines.launch
import org.json.JSONObject
class MemoryBoosterViewModel(val database: MusicFilesDatabaseDao, var applicationContext: Application) :
    AndroidViewModel(
        applicationContext
    ) {

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

    private var _description = MutableLiveData<String>()
    val description: LiveData<String>
        get() = _description

    var _currentFile = MutableLiveData<MusicFiles>()
    val currentFile: LiveData<MusicFiles>
        get() = _currentFile

    private var _image = MutableLiveData<String>()
    val image: LiveData<String>
        get() = _image

    private var _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean>
        get() = _isPlaying


    private var _isDownloaded = MutableLiveData<Boolean>()
    val isDownloaded:LiveData<Boolean>
        get() = _isDownloaded

    private var  memoryDatabase: MemoryFilesDao?=null

    var notActivate=false

    private var currentPlaying = 0

    private var mediaPlayer: UpToddMediaPlayer = UpToddMediaPlayer()

    init {

        initializePoems()

        _poems.value = ArrayList()

        _isLoading.value = 1

        _isMediaReady.value = true

        _isPlaying.value = UpToddMediaPlayer.isPlaying

    }

    private fun initializePoems() {
        memoryDatabase= UptoddDatabase.getInstance(applicationContext.applicationContext).memoryBoosterDao
        viewModelScope.launch {
            downloadedPoems = database.getAllSpeedBoosterFiles()
        }
    }

    fun initializeOffline() {
        if (_isPlaying.value!!) {
            _title.value = UpToddMediaPlayer.songPlaying.name
            currentPlaying = UpToddMediaPlayer.songPlaying.id
            _image.value = AllUtil.getPoemImage(UpToddMediaPlayer.songPlaying, dpi)
        } else {
            _title.value = ""
            _image.value = ""
        }

        viewModelScope.launch {
            downloadedPoems = database.getAllSpeedBoosterFiles()
            downloadedPoems.forEach {
                Log.i("DP", "${it.id} -> ${it.name}")
            }
            _poems.value = ArrayList(downloadedPoems)
            _poems.value?.forEach {
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
                _description.value=song.description
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

    fun initializeAll(context: Context) {
        if (_isPlaying.value!!) {
            _title.value = UpToddMediaPlayer.songPlaying.name
            currentPlaying = UpToddMediaPlayer.songPlaying.id
            _image.value = AllUtil.getPoemImage(UpToddMediaPlayer.songPlaying, dpi)
        } else {
            _title.value = ""
            _image.value = ""
        }

        getAllPoems(context)

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

    private fun getAllPoems(context: Context) {
        val uid = AllUtil.getUserId()
        val stage=UptoddSharedPreferences.getInstance(context).getStage()
        val prenatal =if(stage=="pre birth" || stage=="prenatal")  0 else 1
        Log.d("prenatal",UptoddSharedPreferences.getInstance(context).getStage()!!)
        val lang = AllUtil.getLanguage()
        val userType=UptoddSharedPreferences.getInstance(context).getUserType()
        val country=AllUtil.getCountry(context)
        AndroidNetworking.get("https://www.uptodd.com/api/memorybooster?userId={userId}&prenatal={prenatal}&lang={lang}&userType=$userType&country=$country&motherStage=$stage")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .addPathParameter("userId",uid.toString())
            .addPathParameter("prenatal",prenatal.toString())
            .addPathParameter("lang",lang)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    if (response.getString("status") == "Success") {
                        if(response.get("data").toString()!="null") {
                            viewModelScope.launch {
                                val poems = AllUtil.getAllMusic(response.get("data").toString())


                                var checkSize=0

                                poems.forEachIndexed { index, musicFiles ->
                                    if(getIsPoemDownloaded(poems[index])){
                                        checkSize++;
                                    }
                                }

                                if(poems.size>0)
                                    _isDownloaded.postValue(checkSize>10)

                                poems.forEachIndexed { index, musicFiles ->
                                    if(!getIsPoemDownloaded(poems[index])){
                                        if(_isDownloaded.value==null||_isDownloaded.value!!) {
                                            _isDownloaded.postValue(false)
                                        }

                                    }
                                }


                                UptoddSharedPreferences.getInstance(context)
                                    .saveCountMemoryBooster(poems.size)
                                poems.forEach {
                                    getIsPoemDownloaded(it)
                                }

                                notActivate=poems.isEmpty()
                                _poems.value = poems

                                _isLoading.value = 0
                            }
                        }
                        else
                        {
                            notActivate=true
                            _poems.value = ArrayList()
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

    fun prevSong() {
        mediaPlayer.playPrevious()
    }

    fun playFile(poem: MusicFiles) {
        _currentFile.value=poem
        _title.value = poem.name
        _description.value=poem.description
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

    suspend fun getIsPoemDownloaded(poem: MusicFiles): Boolean {
        memoryDatabase?.getAllFiles()?.forEach {
            if (it.id == poem.id)
            {
                poem.filePath=it.filePath
                return@getIsPoemDownloaded true
            }
        }
        poem.filePath="NA"
        return false
    }

}