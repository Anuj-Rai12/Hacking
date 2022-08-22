package com.uptodd.uptoddapp.ui.freeparenting.purchase


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.PurchasePlanContentLayoutBinding
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.datamodel.upgrade.UpgradeResponse
import com.uptodd.uptoddapp.ui.freeparenting.purchase.tabs.PurchasePlanTabsFragment
import com.uptodd.uptoddapp.ui.freeparenting.purchase.viewmodel.PurchaseViewModel
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import com.uptodd.uptoddapp.utils.ApiResponseWrapper
import com.uptodd.uptoddapp.utils.dialog.showDialogBox
import com.uptodd.uptoddapp.utils.setLogCat


class PurchasePlanTabContainerFragment : Fragment(R.layout.purchase_plan_content_layout) {
    private lateinit var binding: PurchasePlanContentLayoutBinding
    private val viewModel: PurchaseViewModel by viewModels()
    private val dialogs by lazy {
        UpToddDialogs(requireContext())
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = PurchasePlanContentLayoutBinding.bind(view)
        val fragmentObj = PurchasePlanTabsFragment()
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.add(binding.viewPagerPaidContent.id, fragmentObj)
        transaction.commit()
        viewModel.event.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { err ->
                showErrorDialogBox(err)
            }
        }
        binding.buyBtn.setOnClickListener {
            viewModel.doCourseUpgrade(
                LoginSingletonResponse.getInstance().getLoginResponse()?.data?.id?.toLong()
                    ?: LoginSingletonResponse.getInstance().getUserId()!!
            )
        }
        getEndRollResponse()
    }


    private fun getEndRollResponse() {
        viewModel.upgradeCourseResponse.observe(viewLifecycleOwner) { res ->
            res?.let {
                when (it) {
                    is ApiResponseWrapper.Error -> {
                        dialogs.dismissDialog()
                        if (it.data == null) {
                            it.exception?.localizedMessage?.let { err ->
                                showErrorDialogBox(err)
                            }
                        } else {
                            showErrorDialogBox("${it.data}")
                        }
                    }
                    is ApiResponseWrapper.Loading -> {
                        dialogs.showOnlyLoadingDialog()
                    }
                    is ApiResponseWrapper.Success -> {
                        dialogs.dismissDialog()
                        val data = it.data as UpgradeResponse?
                        data?.let { _ ->
                            activity?.showDialogBox(
                                "Success",
                                "Thank to Enrolling into the course",
                                icon = R.drawable.payment_success
                            ) {}
                        } ?: showErrorDialogBox("Failed to show Response")
                    }
                }
            }
        }
    }

    private fun showErrorDialogBox(msg: String) {
        activity?.showDialogBox(
            title = "Failed",
            desc = msg,
            icon = android.R.drawable.stat_notify_error
        ) {
            setLogCat("showErrorDialogBox", "nothing")
        }
    }

}