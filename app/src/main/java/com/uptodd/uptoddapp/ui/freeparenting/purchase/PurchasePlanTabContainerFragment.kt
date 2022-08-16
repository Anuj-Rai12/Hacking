package com.uptodd.uptoddapp.ui.freeparenting.purchase

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.PurchasePlanContentLayoutBinding


class PurchasePlanTabContainerFragment : Fragment(R.layout.purchase_plan_content_layout) {
    private lateinit var binding: PurchasePlanContentLayoutBinding

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = PurchasePlanContentLayoutBinding.bind(view)

    }

}