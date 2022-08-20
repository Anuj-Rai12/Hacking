package com.uptodd.uptoddapp.ui.freeparenting.login.updatepass

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputLayout
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.LoginParentingFragmentBinding
import com.uptodd.uptoddapp.datamodel.changepass.ChangePasswordRequest
import com.uptodd.uptoddapp.ui.freeparenting.login.viewmodel.LoginViewModel
import com.uptodd.uptoddapp.utils.*
import com.uptodd.uptoddapp.utils.dialog.showDialogBox

class UpdateUserPasswordFragment : Fragment(R.layout.login_parenting_fragment) {
    private lateinit var binding: LoginParentingFragmentBinding
    private var flagForBackPressed = false

    private val args: UpdateUserPasswordFragmentArgs by navArgs()
    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LoginParentingFragmentBinding.bind(view)

        viewModel.event.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { err ->
                showErrorDialog(err)
            }
        }

        onBackPress()
        getChangePassResponse()
        binding.backIconImage.setOnClickListener {
            goBack()
        }
        binding.goToDemoDashBoard.setOnClickListener {
            val pass = binding.userPassEd.text.toString()
            if (checkUserInput(pass)) {
                binding.root.showSnackbar("Please Enter the Credentials")
                return@setOnClickListener
            }
            viewModel.changePassResponse(ChangePasswordRequest(args.response.data.email, pass))
        }

    }


    private fun getChangePassResponse() {
        viewModel.changePass.observe(viewLifecycleOwner) { res ->
            res?.let {
                when (it) {
                    is ApiResponseWrapper.Error -> {
                        showBtn()
                        if (it.data == null) {
                            it.exception?.localizedMessage?.let { err ->
                                showErrorDialog(err)
                            }
                        } else {
                            showErrorDialog("${it.data}")
                        }
                    }
                    is ApiResponseWrapper.Loading -> {
                        hideBtn()
                    }
                    is ApiResponseWrapper.Success -> {
                        showBtn()
                        binding.root.showSnackbar("Account Recovered successfully")
                        val handle = Handler(Looper.getMainLooper())
                        handle.post {
                            goBack()
                        }
                    }
                }
            }
        }
    }


    private fun onBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (flagForBackPressed) {
                goBack()
            }
        }.handleOnBackPressed()
    }

    private fun showErrorDialog(msg: String) {
        activity?.showDialogBox("Failed", msg, icon = android.R.drawable.stat_notify_error) {}
    }

    private fun hideBtn() {
        binding.goToDemoDashBoard.invisible()
        binding.pbBtn.isVisible = true
    }

    private fun showBtn() {
        binding.goToDemoDashBoard.show()
        binding.pbBtn.isVisible = false
    }

    override fun onPause() {
        super.onPause()
        viewModel.changePassToNull()
    }


    private fun goBack() {
        val action =
            UpdateUserPasswordFragmentDirections.actionGlobalParentingLoginFragment()
        findNavController().navigate(action)
    }


    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        flagForBackPressed = true
        binding.pageTitle.text =
            "Almost there ${args.response.data.name.split("\\s".toRegex())[0]},"
        binding.pageDesc.text =
            "you just need to create new Password ${getEmojiByUnicode(0X1F4AA)} strong password"
        binding.userPassTxt.text = "New Password"
        binding.goToDemoDashBoard.text = "Update password"
        binding.forgetPass.hide()
        binding.emailIdEd.setText(args.response.data.email)
        binding.emailIdEdLayout.endIconMode = TextInputLayout.END_ICON_NONE
        binding.emailIdEd.isEnabled = false
        binding.backIconImage.setImageResource(R.drawable.ic_cancel)
    }
}