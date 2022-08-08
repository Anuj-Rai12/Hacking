package com.uptodd.uptoddapp.ui.details

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.media.music.MusicFiles
import com.uptodd.uptoddapp.databinding.DetailsFragmentBinding
import com.uptodd.uptoddapp.utilities.ScreenDpi
import com.uptodd.uptoddapp.utilities.ToolbarUtils

class Details : Fragment() {

    companion object {
        fun newInstance() = Details()
    }

    private lateinit var binding: DetailsFragmentBinding
    private lateinit var viewModel: DetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.details_fragment, container, false)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(DetailsViewModel::class.java)
        binding.musicDetailsBinding = viewModel


        ToolbarUtils.initNCToolbar(
            requireActivity(), "Details", binding.toolbar,
            findNavController()
        )

        //Get music id from safeargs
        val args = DetailsArgs.fromBundle(requireArguments())
        if (args.type == "Music") {
            setUpMusicDetails(args.file)
        }
        if (args.type == "Poem") {
            setUpPoemDetails(args.file, binding)
        }

        binding.detailsTitle.text = args.file.name
        binding.detailsDesc.text = args.file.description

        return binding.root
    }

    private fun setUpPoemDetails(poems: MusicFiles, binding: DetailsFragmentBinding) {
        Glide.with(binding.detailsImage.context)
            .load(
                "https://www.uptodd.com/images/app/android/details/poems/${
                    ScreenDpi(
                        requireContext()
                    ).getScreenDrawableType()
                }/${poems.image}.webp"
            )
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.default_set_android_detail)
            .into(binding.detailsImage)
        /*Picasso.get()
            .load("https://www.uptodd.com/images/app/android/details/poems/${ScreenDpi(requireContext()).getScreenDrawableType()}/${poems.image}.webp")
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.default_set_android_detail)
            .into(binding.detailsImage)*/

        binding.detailsDesc.gravity = Gravity.CENTER_HORIZONTAL

    }

    private fun setUpMusicDetails(music: MusicFiles) {
        Glide.with(binding.detailsImage.context)
            .load(
                "https://www.uptodd.com/images/app/android/details/musics/${
                    ScreenDpi(
                        requireContext()
                    ).getScreenDrawableType()
                }/${music.image}.webp"
            )
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.default_set_android_detail)
            .into(binding.detailsImage)
        /*Picasso.get()
            .load(
                "https://www.uptodd.com/images/app/android/details/musics/${
                    ScreenDpi(
                        requireContext()
                    ).getScreenDrawableType()
                }/${music.image}.webp"
            )
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.default_set_android_detail)
            .into(binding.detailsImage)*/

        Log.i(
            "imageLink",
            "https://www.uptodd.com/images/app/android/details/musics/${ScreenDpi(requireContext()).getScreenDrawableType()}/${music.image}.webp"
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DetailsViewModel::class.java)
    }

}