package com.example.hackingwork.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.example.hackingwork.MainActivity
import com.example.hackingwork.R
import com.example.hackingwork.TAG
import com.example.hackingwork.databinding.StorageScreenFragmentBinding
import com.example.hackingwork.recycle.storagerecyle.StorageRecycle
import com.example.hackingwork.utils.*
import com.example.hackingwork.viewmodels.AdminViewModel
import com.example.hackingwork.work.UploadFileWorkManger
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class StorageScreenFragment : Fragment(R.layout.storage_screen_fragment) {
    private val adminViewModel: AdminViewModel by activityViewModels()
    private lateinit var binding: StorageScreenFragmentBinding
    private val getUri = registerForActivityResult(GetUriFile()) {
        it.uri?.let { uri ->
            Log.i(TAG, "MY MIME ->: ${getMimeType(uri)}")
            when {
                getMimeType(uri)?.contains("video/") == true -> makeData(uri)
                getMimeType(uri) == "application/pdf" -> setAssignment(uri)
                else -> dir(message = "This File is Neither Pdf Nor Video File")
            }
        }
    }

    private val getVideoPreview = registerForActivityResult(GetUriFile()) {
        it.uri?.let { uri ->
            adminViewModel.videoPreview = uri.toString()
            adminViewModel.videoPreview?.let {
                dir(title = "Preview Video", message = "Preview ViewVideo Is:\n$uri")
            }
        }
    }

    private fun setAssignment(uri: Uri) {
        val videoTitle = binding.uploaderVideoName.text.toString()
        Assignment(
            title = binding.uploaderAssignment.text.toString(),
            uri = uri.toString()
        ).also { assignment ->
            Log.i(TAG, "setAssignment: Assignment-> $assignment")
            if (checkFieldValue(videoTitle)) {
                Snackbar.make(requireView(), "Enter the Assignment Title", Snackbar.LENGTH_SHORT)
                    .show()
                return
            }
            val videoInstance = adminViewModel.videoMap.value?.get(videoTitle)
            Log.i(TAG, "setAssignment My Video Assignment -> : $videoInstance")
            videoInstance?.let { video ->
                adminViewModel.updateDataWithAssignment(assignment, video)
            }
        }
    }

    private val getImage = registerForActivityResult(GetUriFile()) {
        it.uri?.let { uri ->
            if (it.requestCode) {
                getGalImage(uri)
            }
        }
    }


    @Inject
    lateinit var customProgress: CustomProgress

    @Inject
    lateinit var storageRecycle: StorageRecycle
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = StorageScreenFragmentBinding.bind(view)
        MainActivity.getCourseContent?.let {
            if (adminViewModel.getCourseContent.value==null) {
                dir(title = "File UploadStatus", message = getMsg(it))
                adminViewModel.getCourseContent.value=it
                Log.i(TAG, "onViewCreated: ${adminViewModel.getCourseContent.value}")
            }
        }
        adminViewModel.thumbnailNail?.let {
            binding.fileImage.setImageURI(it.toUri())
        }
        setUpRecycleView()
        setUpData()
        binding.openFileExplore.setOnClickListener {
            val module = binding.ModuleName.text.toString()
            val videoTile = binding.uploaderVideoName.text.toString()
            if (getCheckout(module, videoTile))
                return@setOnClickListener
            getUri.launch(InputData(intent = getIntent("video/*")))
        }
        binding.openAssignmentExplore.setOnClickListener {
            val assignmentTitle = binding.uploaderAssignment.text.toString()
            if (checkFieldValue(assignmentTitle)) {
                Snackbar.make(requireView(), "Enter the Assignment Name", Snackbar.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            getUri.launch(InputData(intent = getIntent("*/*")))
        }
        binding.UploadVideoFile.setOnClickListener {
            Log.i(TAG, "onViewCreated: Working Correct")
            val module = binding.ModuleName.text.toString()
            if (checkFieldValue(module)) {
                Snackbar.make(requireView(), "Enter the Module Name", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            adminViewModel.setAllDataModelMap(module)
        }
        binding.UploadVideoFile.setOnLongClickListener {
            Log.i(TAG, "onViewCreated: Hello ji Long Press")
            adminViewModel.moduleMap?.let { moduleContent ->
                GetCourseContent(
                    thumbnail = adminViewModel.thumbnailNail,
                    previewvideo = adminViewModel.videoPreview,
                    module = moduleContent
                ).also {
                    uploadFile(it)
                }
            }
            return@setOnLongClickListener true
        }
        binding.ThumbNailExplore.setOnClickListener {
            getImage.launch(InputData(intent = getIntent("image/*")))
        }
        setHasOptionsMenu(true)
    }

    private fun uploadFile(courseContent: GetCourseContent) {
        val str = Helper.serializeToJson(courseContent)
        str?.let { course ->
            showLoading()
            val data =
                Data.Builder().putString(GetConstStringObj.EMAIL_VERIFICATION_LINK, course).build()
            val constant =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val workerInstance = WorkManager.getInstance(requireActivity())
            val uploadMyFile =
                OneTimeWorkRequestBuilder<UploadFileWorkManger>().setConstraints(constant)
                    .setInputData(data).build()
            workerInstance.enqueue(uploadMyFile)
            workerInstance.getWorkInfoByIdLiveData(uploadMyFile.id)
                .observe(viewLifecycleOwner) { work ->
                    Log.i(TAG, "uploadFile: ${work.state}")
                    if (work.state.toString() == "RUNNING")
                        showLoading()
                    if (work.state.toString() == "SUCCEEDED" || work.state.toString() == "FAILED")
                        hideLoading()
                    work.outputData.getString(GetConstStringObj.EMAIL_VERIFICATION_LINK)?.let {
                        val courseInstance = Helper.deserializeFromJson(it)
                        courseInstance?.let { get ->
                            dir(title = "File UploadStatus", message = getMsg(get))
                            adminViewModel.getCourseContent.value = get
                            Log.i(TAG, "uploadFile: ${adminViewModel.getCourseContent.value}")
                        }
                    }
                }
        }
    }

    private fun getMsg(get: GetCourseContent): String {
        var str = str(get.thumbnail, "Thumbnail")
        str += str(get.previewvideo, "PreviewVideo")
        get.module?.values?.forEach {
            str + "-----------${it.module}-------------\n\n"
            it.video?.values?.forEach { video ->
                str += "${video.title}\n\n${str(video.uri, "Video Uri")}"
                video.assignment?.let { assignment ->
                    str += "${assignment.title}\n\n${str(assignment.uri, "Assignment Uri")}"
                }
            }
        }
        return str
    }

    private fun str(previewVideo: String?, str: String) =
        if (previewVideo == null) "$str -> Unknown" else "$str -> ${previewVideo}\n\n"

    private fun getGalImage(it: Uri) {
        adminViewModel.thumbnailNail = it.toString()
        binding.fileImage.setImageURI(it)
    }

    private fun getMimeType(uri: Uri) = getMimeType(uri, requireContext())

    @SuppressLint("NotifyDataSetChanged")
    private fun setUpData() {
        Log.i(TAG, "setUpData: Hello From SetUpData")
        adminViewModel.videoMap.observe(viewLifecycleOwner) {
            Log.i(TAG, "setUpData: $it")
            storageRecycle.notifyDataSetChanged()
            val list = mutableListOf<Video>()
            it.filterValues { video ->
                list.add(video)
                return@filterValues true
            }
            Log.i(TAG, "setUpData: Value of Video List -> $list")
            storageRecycle.submitList(list)
        }
    }

    private fun getIntent(string: String): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = string
        return intent
    }

    private fun getCheckout(module: String, videoTile: String): Boolean {
        return if (checkFieldValue(module) || checkFieldValue(videoTile)) {
            Snackbar.make(requireView(), getString(R.string.wrong_detail), Snackbar.LENGTH_SHORT)
                .show()
            true
        } else
            false
    }

    private fun setUpRecycleView() {
        binding.apply {
            videoFileRecycleView.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = storageRecycle
            }
            ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ) = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val getSelectedItem =
                        storageRecycle.currentList[viewHolder.absoluteAdapterPosition]
                    getSelectedItem?.let {
                        adminViewModel.delete(it)
                    }
                }
            }).attachToRecyclerView(videoFileRecycleView)
        }
        lifecycleScope.launch {
            adminViewModel.taskEvent.collect { data ->
                when (data) {
                    is MySealedChannel.DeleteAndChannel<*> -> {
                        Snackbar.make(requireView(), "File Deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                adminViewModel.getVideo(data.userdata as Video)
                            }.show()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.viedo_preview_menu, menu)
        val videoPreview = menu.findItem(R.id.VideoPreview)
        videoPreview.setOnMenuItemClickListener {
            getVideoPreview.launch(InputData(intent = getIntent("video/*")))
            return@setOnMenuItemClickListener true
        }
    }

    private fun makeData(uri: Uri) {
        val durationTime = getDuration(uri)
        Video(
            title = binding.uploaderVideoName.text.toString(),
            uri = uri.toString(),
            duration = durationTime,
            assignment = null
        ).also {
            Log.i(TAG, "makeData: Video -> $it")
            adminViewModel.getVideo(it)
        }
    }

    private fun getDuration(uri: Uri): String? {
        val mp: MediaPlayer? = MediaPlayer.create(activity, uri)
        val duration = mp?.duration
        mp?.release()
        Log.i(TAG, "getDuration: Duration => $duration")
        duration?.let {
            return String.format(
                "%d min : %d sec",
                TimeUnit.MILLISECONDS.toMinutes(it.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(it.toLong()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(it.toLong()))
            )
        }
        return null
    }

    private fun showLoading(string: String = "File is Uploading...") =
        customProgress.showLoading(requireActivity(), string)

    private fun hideLoading() = customProgress.hideLoading()
    override fun onPause() {
        super.onPause()
        hideLoading()
    }

    private fun dir(title: String = "Error", message: String = "") {
        val action = StorageScreenFragmentDirections.actionGlobalPasswordDialog2(message, title)
        findNavController().navigate(action)
    }

}