package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentDetailsBinding
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.KidsPeriod
import com.uptodd.uptoddapp.utilities.ScreenDpi
import com.uptodd.uptoddapp.utilities.ToolbarUtils


class DetailsFragment : Fragment() {
    private lateinit var binding: FragmentDetailsBinding

    private var imageUrl: String? = null
    private var title: String? = null
    private var description: String? = null
    private var folderName: String? = null
    private var actionBarName: String? = "Details"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        arguments?.let {
            folderName = it.getString("folder")
            val dpi = ScreenDpi(requireContext()).getScreenDrawableType()
            val period = KidsPeriod(requireActivity()).getPeriod()
            val appendable = "https://www.uptodd.com/images/app/android/details/$folderName/$dpi/"
            if (it.getString("image") == "vaccination")
                imageUrl =
                    "https://www.uptodd.com/images/app/android/details/vaccination/vaccination.webp"
            else if (folderName == "diets")
                imageUrl =
                    "https://www.uptodd.com/images/app/android/details/activities/$period/$dpi/${it.getString(
                        "image"
                    )}.webp"
            else
                imageUrl = appendable + it.getString("image") + ".webp"
            title = it.getString("title")
            description = it.getString("description")
            actionBarName = it.getString("actionBar")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_details, container, false)

        ToolbarUtils.initNCToolbar(requireActivity(),"Details",binding.toolbar,
            findNavController())
        (activity as AppCompatActivity).supportActionBar!!.title = actionBarName

        inflateDetails()

        return binding.root
    }

    private fun inflateDetails() {
        binding.description.text = description
        binding.todoTaskName.text = title

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.default_set_android_detail)
            .into(binding.todoImageView)
    }

//    override fun onResume() {
//        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
//        super.onResume()
//    }
//
//    override fun onPause() {
//        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
//        super.onPause()
//    }
}