package com.uptodd.uptoddapp.ui.freeparenting.content.tabs

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FreeDemoVideoModuleFragmentsBinding
import com.uptodd.uptoddapp.datamodel.videocontent.ModuleList
import com.uptodd.uptoddapp.datamodel.videocontent.VideoContent
import com.uptodd.uptoddapp.ui.freeparenting.content.DemoVideoContentFragment
import com.uptodd.uptoddapp.ui.freeparenting.content.adaptor.ModuleAdaptor
import com.uptodd.uptoddapp.utils.hide
import com.uptodd.uptoddapp.utils.show
import com.uptodd.uptoddapp.utils.toastMsg

class FreeDemoVideoModuleFragments(val type: String, val description: String? = null) :
    Fragment(R.layout.free_demo_video_module_fragments) {
    private lateinit var binding: FreeDemoVideoModuleFragmentsBinding
    private lateinit var adaptorListItem: ModuleAdaptor
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FreeDemoVideoModuleFragmentsBinding.bind(view)
        binding.titleTxt.movementMethod = ScrollingMovementMethod()
        binding.titleTxt.text = description ?: sampleText
        setUpAdaptor()

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
            } ?: activity?.toastMsg("Testing file")
        }
    }

    override fun onResume() {
        super.onResume()
        when (VideoContentTabsEnm.valueOf(type)) {
            VideoContentTabsEnm.MODULE -> {
                binding.titleTxt.hide()
                binding.recycleView.show()
            }
            VideoContentTabsEnm.INFO -> {
                binding.recycleView.hide()
                binding.titleTxt.show()
            }
        }
    }


    companion object {
        enum class VideoContentTabsEnm {
            MODULE,
            INFO
        }
    }

    private val sampleText: String
        get() = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi sed ligula in lorem mollis egestas ac sit amet sapien. Aenean felis magna, eleifend ut dolor nec, aliquam rhoncus turpis. In at mattis leo. Integer mi urna, venenatis ut sagittis sit amet, facilisis id dui. Ut tempus tincidunt nisl ac pharetra. Vestibulum rutrum tempor magna, sed lobortis sem hendrerit sed. Praesent aliquam, lectus vel pellentesque auctor, magna sapien tempor magna, eu aliquam purus arcu at sem. Fusce tincidunt, odio vitae rhoncus placerat, turpis nulla blandit mi, ac semper neque urna vitae justo. Nulla porta tortor sed neque faucibus congue. Vivamus vitae metus a sapien luctus iaculis. Sed ultricies interdum nisi, at ultrices ipsum luctus id. Curabitur ipsum nulla, semper sit amet nulla at, iaculis posuere ex. Nunc eu finibus ex. Aenean sit amet dui augue.\n" +
                "\n" +
                "Phasellus vehicula elit sit amet maximus maximus. Donec sed lacinia enim. Mauris sit amet pretium lorem. Morbi fringilla lorem non turpis imperdiet elementum. Duis quis lacinia arcu. Pellentesque ornare est at turpis tempor luctus efficitur nec arcu. Quisque felis eros, ullamcorper quis lacinia eget, tristique id odio.\n" +
                "\n" +
                "Sed scelerisque dapibus eros, non volutpat elit tincidunt et. Nulla mattis condimentum massa id auctor. Phasellus lobortis justo vitae nibh blandit consequat. Fusce neque erat, vehicula ac auctor eu, cursus nec dolor. Mauris in nulla neque. Aliquam varius semper est at posuere. Etiam rutrum facilisis augue, a mattis sapien rutrum sed. Proin et turpis euismod, semper diam efficitur, ultricies quam. Cras rutrum neque id ipsum egestas, blandit tempor massa vehicula. Proin dictum tellus vitae tellus egestas dictum. Maecenas placerat scelerisque velit varius fermentum.\n" +
                "\n" +
                "Nunc nec mauris id nisi interdum ultricies. Ut vel condimentum risus. Vivamus ligula tortor, fringilla at lacinia eget, vestibulum nec neque. Nullam vehicula porttitor elit, eu vulputate est vulputate eget. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Nunc in pulvinar ex, ut maximus metus. Fusce eu leo ligula. Vivamus elementum fringilla mattis. Curabitur tincidunt nisl diam, quis iaculis sapien sodales vitae. Maecenas faucibus sem non ipsum blandit, quis ornare odio ullamcorper.\n" +
                "\n" +
                "Nunc et iaculis risus. Praesent id dignissim metus. Nulla nibh dui, hendrerit a mi sed, pulvinar viverra velit. Aliquam et vulputate tortor. Phasellus ac facilisis purus, ac convallis metus. Nulla non sapien ac justo euismod aliquam id quis odio. Aliquam sed justo nisi. Etiam non lacus finibus, congue justo a, hendrerit lorem. Nulla non est metus. Donec vestibulum lectus sit amet nisl auctor, id blandit metus vestibulum."

}