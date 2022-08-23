package com.uptodd.uptoddapp.ui.freeparenting.feedback

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.uptodd.uptoddapp.FreeParentingDemoActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FreeDemoFeedBackFramgentBinding
import com.uptodd.uptoddapp.datamodel.feedback.FeedBackRequest
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.ui.freeparenting.feedback.viewmodel.FeedBackViewModel
import com.uptodd.uptoddapp.utils.*
import com.uptodd.uptoddapp.utils.dialog.showDialogBox


class FreeDemoFreeBackFragments : Fragment(R.layout.free_demo_feed_back_framgent) {

    private lateinit var binding: FreeDemoFeedBackFramgentBinding
    private val viewModel: FeedBackViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FreeDemoFeedBackFramgentBinding.bind(view)

        viewModel.event.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { err ->
                showErrorDialogBox(err)
            }
        }

        binding.submitFreeParenting.setOnClickListener {
            val feedBack = binding.textMessage.text.toString()
            if (checkUserInput(feedBack)) {
                binding.root.showSnackBarMsg(
                    "Please Add Your response!!",
                    anchor = (activity as FreeParentingDemoActivity?)?.getBottomNav()!!
                )
                return@setOnClickListener
            }
            viewModel.sendResponse(
                FeedBackRequest(
                    feedBack,
                    LoginSingletonResponse.getInstance().getUserId()?.toInt() ?: 0
                )
            )
        }
        responseForFeedBack()
    }

    private fun responseForFeedBack() {
        viewModel.sendFeedBackResponse.observe(viewLifecycleOwner) { res ->
            res?.let {
                when (it) {
                    is ApiResponseWrapper.Error -> {
                        binding.submitFreeParenting.show()
                        binding.pbBtn.isVisible = false
                        if (it.data == null) {
                            it.exception?.localizedMessage?.let { err ->
                                showErrorDialogBox(err)
                            }
                        } else {
                            showErrorDialogBox("${it.data}")
                        }
                    }
                    is ApiResponseWrapper.Loading -> {
                        binding.submitFreeParenting.invisible()
                        binding.pbBtn.isVisible = true
                    }
                    is ApiResponseWrapper.Success -> {
                        binding.submitFreeParenting.show()
                        binding.pbBtn.isVisible = false
                        activity?.showDialogBox(
                            "Submitted",
                            "${it.data}",
                            icon = R.drawable.payment_success
                        ) {}
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        (activity as FreeParentingDemoActivity?)?.showBottomNavBar()
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