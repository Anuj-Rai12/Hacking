package com.uptodd.uptoddapp.ui.home.homePage

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentAppreciationBinding
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.TodosViewModel
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.masterFragment.DAILY_TODOS_TAB_POSITION
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.masterFragment.ESSENTIALS_TODOS_TAB_POSITION
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.masterFragment.MONTHLY_TODOS_TAB_POSITION
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.masterFragment.WEEKLY_TODOS_TAB_POSITION
import com.uptodd.uptoddapp.utilities.ChangeLanguage


class AppreciationFragment : Fragment() {

    private val viewModel: TodosViewModel by activityViewModels()
    private lateinit var binding: FragmentAppreciationBinding

    private lateinit var mPlayer: MediaPlayer
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        initialiseBindingAndViewModel(inflater, container)

        mPlayer = MediaPlayer.create(context, R.raw.clap)
        mPlayer.start()

        viewModel.tabPosition.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.completedTextView.text =
                    when (it) {
                        DAILY_TODOS_TAB_POSITION -> "Daily Tasks Completed"
                        WEEKLY_TODOS_TAB_POSITION -> "Weekly Tasks Completed"
                        MONTHLY_TODOS_TAB_POSITION -> "Monthly Tasks Completed"
                        ESSENTIALS_TODOS_TAB_POSITION -> "Essential Tasks Completed"
                        else -> "Care Tasks Completed"
                    }
            }
        })

        binding.btnShare.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("previous", "Share")
            view?.findNavController()?.navigate(R.id.action_appreciationFragment_to_captureImageFragment,bundle)
        }

        return binding.root
    }


    override fun onPause() {
        super.onPause()
        mPlayer.stop()
    }

    private fun initialiseBindingAndViewModel(inflater: LayoutInflater, container: ViewGroup?) {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_appreciation, container, false)

        binding.appreciationViewModel = viewModel
        binding.lifecycleOwner = this

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val enterFromBelowAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.enter_from_below)

        val appearAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.appear_animation)

        binding.completedTextView.startAnimation(enterFromBelowAnimation)
        binding.superParentTextView.startAnimation(appearAnimation)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        Glide.with(this)
            .load(R.drawable.confettigif)
            .into(binding.confettiImageView)
    }


}