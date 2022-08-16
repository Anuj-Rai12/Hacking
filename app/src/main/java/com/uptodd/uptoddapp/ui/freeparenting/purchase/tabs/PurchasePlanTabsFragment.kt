package com.uptodd.uptoddapp.ui.freeparenting.purchase.tabs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.PurchasePlanTabsFragmentsBinding

class PurchasePlanTabsFragment :
    Fragment(R.layout.purchase_plan_tabs_fragments) {
    private lateinit var binding: PurchasePlanTabsFragmentsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = PurchasePlanTabsFragmentsBinding.bind(view)



    }
}