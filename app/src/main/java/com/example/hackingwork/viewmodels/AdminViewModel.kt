package com.example.hackingwork.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackingwork.TAG
import com.example.hackingwork.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor() : ViewModel() {
    var videoPreview: String? = null
    var moduleMap: MutableMap<String, Module>? = null
    var thumbnailNail: String? = null
    var getCourseContent = MutableStateFlow<GetCourseContent?>(null)
    private val _taskEvent = Channel<MySealedChannel>()
    val taskEvent = _taskEvent.receiveAsFlow()
    private var _videoMap = MutableLiveData<MutableMap<String, Video>>()
    val videoMap: MutableLiveData<MutableMap<String, Video>>
        get() = _videoMap

    fun getVideo(video: Video) {
        val map = mutableMapOf<String, Video>()
        map[video.title!!] = video
        _videoMap.value?.let { map.putAll(it) }
        _videoMap.value = map
        Log.i(TAG, "getVideo: ${_videoMap.value}")
    }

    fun delete(video: Video) {
        viewModelScope.launch {
            val map = mutableMapOf<String, Video>()
            _videoMap.value?.let { map.putAll(it) }
            map.remove(video.title)
            _videoMap.value = map
            _taskEvent.send(MySealedChannel.DeleteAndChannel(video))
        }
    }

    fun updateDataWithAssignment(assignment: Assignment, video: Video) {
        val map = mutableMapOf<String, Video>()
        _videoMap.value?.let { value -> map.putAll(value) }
        Video(
            title = video.title,
            uri = video.uri,
            duration = video.duration,
            assignment = assignment
        ).also {
            map[video.title!!] = it
        }
        _videoMap.value = map
        Log.i(TAG, "updateDataWithAssignment: ${_videoMap.value}")
    }

    fun setAllDataModelMap(title: String) {
        if (!_videoMap.value.isNullOrEmpty()) {
            val map = mutableMapOf<String, Module>()
            moduleMap?.let { maps -> map.putAll(maps) }
            map[title] = Module(title, _videoMap.value)
            moduleMap = map
            Log.i(TAG, "My ViewModel From Upload Btn: $moduleMap")
        }
    }
}