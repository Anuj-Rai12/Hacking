package com.uptodd.uptoddapp.ui.freeparenting.purchase


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.PurchasePlanContentLayoutBinding
import com.uptodd.uptoddapp.ui.freeparenting.purchase.tabs.PurchasePlanTabsFragment


class PurchasePlanTabContainerFragment : Fragment(R.layout.purchase_plan_content_layout) {
    private lateinit var binding: PurchasePlanContentLayoutBinding
    //private lateinit var viewPagerAdaptor: ViewPagerAdapter

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = PurchasePlanContentLayoutBinding.bind(view)
        val fragmentObj = PurchasePlanTabsFragment()
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.add(binding.viewPagerPaidContent.id, fragmentObj)
        transaction.commit()
    }

}