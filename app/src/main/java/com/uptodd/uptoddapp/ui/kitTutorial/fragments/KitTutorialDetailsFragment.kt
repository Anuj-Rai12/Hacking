package com.uptodd.uptoddapp.ui.kitTutorial.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.activitysample.ActivitySample
import com.uptodd.uptoddapp.databinding.FragmentActivitySampleBinding
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.models.SuggestedVideosModel
import com.uptodd.uptoddapp.ui.tutorials.TutorialAdapter
import com.uptodd.uptoddapp.ui.tutorials.TutorialInterface
import com.uptodd.uptoddapp.ui.webinars.podcastwebinar.PodcastWebinarActivity
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.ToolbarUtils
import com.uptodd.uptoddapp.utilities.UpToddDialogs

private const val TAG = "ActivitySampleFragment"

class KitTutorialDetailsFragment : Fragment(), TutorialInterface {


    private lateinit var binding: FragmentActivitySampleBinding


    private var activitySampleList = mutableListOf<ActivitySample>()


    private val adapter = TutorialAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActivitySampleBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.upgradeButton.visibility=View.GONE
        ToolbarUtils.initToolbar(requireActivity(),binding.collapseToolbar,findNavController(),"Kit Tutorial"
            ,"Curated in UpTodd's Lab"
            ,R.drawable.kit_tutorial_icon)
        binding.collapseToolbar.appBarLayout.setExpanded(false)

        arguments.let {
            val args=KitTutorialDetailsFragmentArgs.fromBundle(requireArguments())
            args.videoList.let {
                args.videoList?.tutorials?.let { it1 -> parseData(it1) }
            }
        }
    }




    private fun parseData(videoList:ArrayList<ActivitySample>) {

        activitySampleList.clear()
        videoList.forEach {
            activitySample ->
            run {
                activitySampleList.add(activitySample)
            }
        }

        if(activitySampleList.isEmpty()){
            showNoData()
            return
        } else
        setupRecyclerView()
        hideNodata()
    }

    private fun setupRecyclerView() {
        adapter.list = activitySampleList
        binding.activitySampleRecycler.adapter = adapter
        showRecyclerView()
    }


    private fun hideNodata() {
        binding.noDataContainer.isVisible = false
    }

    private fun showNoData() {
        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
                val title = (requireActivity() as AppCompatActivity).supportActionBar?.title

                val upToddDialogs = UpToddDialogs(requireContext())
                upToddDialogs.showInfoDialog("$title is not activated/required for you",
                    "Close",
                    object : UpToddDialogs.UpToddDialogListener {
                        override fun onDialogButtonClicked(dialog: Dialog) {
                            dialog.dismiss()

                        }

                        override fun onDialogDismiss() {
                            findNavController().navigateUp()
                        }
                    })

        }
        binding.noDataContainer.isVisible = true
    }

    private fun showRecyclerView() {
        binding.activitySampleRecycler.isVisible = true
    }

    private fun hideRecyclerView() {
        binding.activitySampleRecycler.isVisible = false
    }



    override fun onClick(act_sample: ActivitySample) {
        val intent = Intent(context, PodcastWebinarActivity::class.java)
        intent.putExtra("url", act_sample.video)
        intent.putExtra("title", act_sample.title)
        intent.putExtra("videos",SuggestedVideosModel(activitySampleList))
        startActivity(intent)
    }

}