package com.uptodd.uptoddapp.ui.freeparenting.content.tabs

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FreeDemoVideoModuleFragmentsBinding
import com.uptodd.uptoddapp.datamodel.videocontent.ModuleList
import com.uptodd.uptoddapp.datamodel.videocontent.VideoContent
import com.uptodd.uptoddapp.ui.freeparenting.content.DemoVideoContentFragment
import com.uptodd.uptoddapp.ui.freeparenting.content.adaptor.ModuleAdaptor
import com.uptodd.uptoddapp.ui.freeparenting.content.repo.VideoContentRepository
import com.uptodd.uptoddapp.ui.freeparenting.content.viewmodel.VideoContentViewModel
import com.uptodd.uptoddapp.utils.*
import com.uptodd.uptoddapp.utils.dialog.showDialogBox

class FreeDemoVideoModuleFragments :
    Fragment(R.layout.free_demo_video_module_fragments) {
    private lateinit var binding: FreeDemoVideoModuleFragmentsBinding
    private lateinit var adaptorListItem: ModuleAdaptor

    private val viewModel: VideoContentViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FreeDemoVideoModuleFragmentsBinding.bind(view)
        setUpAdaptor()
        viewModel.event.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {res->
                showErrorDialogBox(res)
            }
        }

        getVideContentResponse()
    }

    private fun setUpAdaptor() {
        binding.recycleView.apply {
            setHasFixedSize(true)
            adaptorListItem = ModuleAdaptor {
                setToParentFragment(it)
            }
            adapter = adaptorListItem
            adaptorListItem.submitList(VideoContent.getVideoContent())
        }
    }

    private fun setToParentFragment(moduleList: ModuleList) {
        val handler = Handler(Looper.getMainLooper())
        val parent = parentFragment
        handler.post {
            parent?.let { fragment ->
                (fragment as DemoVideoContentFragment).setNewVideo(moduleList)
            } ?: activity?.toastMsg(VideoContentRepository.error.second)
        }
    }


    private fun getVideContentResponse() {
        viewModel.videoContentResponse.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResponseWrapper.Error -> {
                    hideRecycleView("")
                    if (it.data == null) {
                        it.exception?.localizedMessage?.let { res ->
                            showErrorDialogBox(res)
                        }
                    } else {
                        showErrorDialogBox("${it.data}")
                    }
                }
                is ApiResponseWrapper.Loading -> {
                    hideRecycleView("${it.data}")
                }
                is ApiResponseWrapper.Success -> {
                    showRecycleView()
                    activity?.toastMsg("${it.data}")

                }
            }
        }
    }

    private fun hideRecycleView(txt: String) {
        binding.recycleView.hide()
        binding.loadingTxt.show()
        binding.loadingTxt.text = txt
    }


    private fun showRecycleView() {
        binding.recycleView.show()
        binding.loadingTxt.hide()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getVideoContent()
    }

    private fun showErrorDialogBox(msg: String) {
        activity?.showDialogBox(
            title = "Failed",
            desc = msg,
            icon = android.R.drawable.stat_notify_error
        ) {
            setLogCat("showErrorDialogBox", "nothing")
        }
    }

    companion object {
        enum class VideoContentTabsEnm {
            VIDEO,
            MUSIC
        }
    }
}