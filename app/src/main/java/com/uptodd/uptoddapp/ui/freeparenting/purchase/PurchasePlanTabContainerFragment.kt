package com.uptodd.uptoddapp.ui.freeparenting.purchase

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.PurchasePlanContentLayoutBinding
import com.uptodd.uptoddapp.ui.freeparenting.purchase.tabs.PurchasePlanTabsFragment
import com.uptodd.uptoddapp.ui.freeparenting.purchase.viewpager.ViewPagerAdapter


class PurchasePlanTabContainerFragment : Fragment(R.layout.purchase_plan_content_layout) {
    private lateinit var binding: PurchasePlanContentLayoutBinding
    private lateinit var viewPagerAdaptor: ViewPagerAdapter

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = PurchasePlanContentLayoutBinding.bind(view)
        setAdaptor()
        viewPagerAdaptor.setFragment(PurchasePlanTabsFragment())
    }

    private fun setAdaptor() {
        viewPagerAdaptor = ViewPagerAdapter(parentFragmentManager, lifecycle)
        binding.viewPagerPaidContent.adapter = viewPagerAdaptor
    }
}