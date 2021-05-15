package com.uptodd.uptoddapp.ui.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import kotlinx.android.synthetic.main.fragment_gift_voucher_gif.*


class GiftVoucherGif : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gift_voucher_gif, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(this)
            .load(R.drawable.confettigif)
            .into(confettiImageView)

        Glide.with(this)
            .load(R.drawable.gif_gift)
            .into(giftImageView)
    }


}