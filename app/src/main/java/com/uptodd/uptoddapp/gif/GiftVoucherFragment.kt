package com.uptodd.uptoddapp.gif

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.utilities.ChangeLanguage

class GiftVoucherFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gift_voucher, container, false)
    }
}