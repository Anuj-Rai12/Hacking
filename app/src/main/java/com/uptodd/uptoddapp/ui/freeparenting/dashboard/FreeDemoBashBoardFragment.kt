package com.uptodd.uptoddapp.ui.freeparenting.dashboard

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FreeDemoDashboardScreenFramgentBinding

class FreeDemoBashBoardFragment : Fragment(R.layout.free_demo_dashboard_screen_framgent) {

    private lateinit var binding: FreeDemoDashboardScreenFramgentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FreeDemoDashboardScreenFramgentBinding.bind(view)

    }


}