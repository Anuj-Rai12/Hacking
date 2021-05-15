package com.uptodd.uptoddapp.ui.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.HappyBirthdayBabyBinding
import com.uptodd.uptoddapp.utilities.ChangeLanguage

class HappyBirthdayFragment : Fragment() {
    private lateinit var binding: HappyBirthdayBabyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        return inflater.inflate(R.layout.happy_birthday_baby, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val enterFromBelowAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.enter_from_below)

        val appearAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.appear_animation)

        binding.completedTextView.startAnimation(enterFromBelowAnimation)
        binding.superParentTextView.startAnimation(appearAnimation)


        Glide.with(this)
            .load(R.drawable.confettigif)
            .into(binding.confettiImageView)


        Glide.with(this)
            .load(R.drawable.gifhappybday)
            .into(binding.imageView4)
    }


}
