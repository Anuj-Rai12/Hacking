package com.uptodd.uptoddapp.ui.todoScreens.ranking

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.UptoddViewModelFactory
import com.uptodd.uptoddapp.database.score.Score
import com.uptodd.uptoddapp.databinding.FragmentRankingBinding
import com.uptodd.uptoddapp.utilities.ChangeLanguage

class RankingFragment : Fragment() {

    private lateinit var binding: FragmentRankingBinding
    private lateinit var viewModel: RankingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()
        // Inflate the layout for this fragment

        initialiseBindingAndViewModel(inflater, container)
        //   initialiseObservers()

        binding.btnShare.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("previous", "Share")
            view?.findNavController()
                ?.navigate(R.id.to_capture_image_fragment, bundle)
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //init animation variables
        val enterAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.enter_animation)
        val enterFromBelowAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.enter_from_below)
        val appearAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.appear_animation)

        //apply anim to views
        binding.apply {
            dailyScoreView.startAnimation(enterAnimation)
            weeklyScoreView.startAnimation(enterAnimation)
            monthlyScoreView.startAnimation(enterAnimation)
            essentialsScoreView.startAnimation(enterAnimation)
            rankView.startAnimation(enterFromBelowAnimation)

            rankingTextView.startAnimation(appearAnimation)
            dailyTextView.startAnimation(appearAnimation)
            weeklyTextView.startAnimation(appearAnimation)
            monthlyTextView.startAnimation(appearAnimation)
            essentialsTextView.startAnimation(appearAnimation)

            btnShare.startAnimation(enterAnimation)
        }
    }

    fun initialiseBindingAndViewModel(inflater: LayoutInflater, container: ViewGroup?) {

        val application = requireActivity().application
        val viewModelFactory = UptoddViewModelFactory.getInstance(application)

        viewModel = ViewModelProvider(
            this, viewModelFactory
        ).get(RankingViewModel::class.java)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ranking, container, false)
        binding.rankingViewModel = viewModel
        binding.lifecycleOwner = this

    }

    private fun formatScore(score: Score): SpannableString {
        val spannable =
            SpannableString(score.completedTodos.toString() + "/" + score.totalTodos.toString())
        spannable.setSpan(
            RelativeSizeSpan(1.2f),
            0, score.completedTodos.toString().length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.darkBlue)),
            0, score.completedTodos.toString().length + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannable
    }

    private fun initialiseObservers() {
//        viewModel.dailyScore.observe(viewLifecycleOwner, {
//            it?.let {
//                binding.dailyScoreView.text = formatScore(it)
//            }
//
//        })
//
//        viewModel.weeklyScore.observe(viewLifecycleOwner, {
//            it?.let {
//                binding.weeklyScoreView.text = formatScore(it)
//            }
//
//        })
//
//        viewModel.monthlyScore.observe(viewLifecycleOwner, {
//            it?.let {
//                binding.monthlyScoreView.text = formatScore(it)
//            }
//
//        })
//
//        viewModel.essentialsScore.observe(viewLifecycleOwner, {
//            it?.let {
//                binding.essentialsScoreView.text = formatScore(it)
//            }
//        })
    }
}