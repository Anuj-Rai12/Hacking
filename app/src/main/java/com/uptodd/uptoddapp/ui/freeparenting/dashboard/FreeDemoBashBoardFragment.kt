package com.uptodd.uptoddapp.ui.freeparenting.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FreeDemoDashboardScreenFramgentBinding
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.ui.freeparenting.login.viewmodel.LoginViewModel
import com.uptodd.uptoddapp.ui.home.homePage.adapter.HomeOptionsAdapter
import com.uptodd.uptoddapp.utils.ApiResponseWrapper
import com.uptodd.uptoddapp.utils.dialog.showDialogBox
import com.uptodd.uptoddapp.utils.setLogCat
import com.uptodd.uptoddapp.utils.showSnackbar
import com.uptodd.uptoddapp.utils.toastMsg

class FreeDemoBashBoardFragment : Fragment(R.layout.free_demo_dashboard_screen_framgent),
    HomeOptionsAdapter.HomeOptionsClickListener {

    private lateinit var binding: FreeDemoDashboardScreenFramgentBinding
    private var freeDemoContentAdaptor: HomeOptionsAdapter? = null

    private val viewModel: LoginViewModel by viewModels()

    private val loginSingletonResponse by lazy {
        LoginSingletonResponse.getInstance()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FreeDemoDashboardScreenFramgentBinding.bind(view)


        viewModel.event.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { res ->
                view.showSnackbar(msg = res, color = Color.RED)
                showErrorDialogBox(res)
            }
        }
        setLogCat("Response",loginSingletonResponse.getLoginRequest().toString())
        viewModel.fetchResponse(loginSingletonResponse.getLoginRequest()!!)

        getLoginResponse()

        setUpContentRecycleView()

    }

    private fun getLoginResponse() {
        viewModel.loginResponse.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResponseWrapper.Error -> {
                    hideLoading()
                    if (it.data == null) {
                        it.exception?.localizedMessage?.let { err ->
                            setLogCat("Error_Data", err)
                            //activity?.toastMsg("Error $err")
                            showErrorDialogBox(err)
                        }
                    } else {
                        setLogCat("Error_Data", "${it.data}")
                        //activity?.toastMsg(" Error Data ${it.data}")
                        showErrorDialogBox("${it.data}")
                    }
                }
                is ApiResponseWrapper.Loading -> {
                    showLoading()
                }
                is ApiResponseWrapper.Success -> {
                    hideLoading()
                    setLogCat("Response", loginSingletonResponse.getLoginResponse().toString())
                    showInformation()
                }
            }
        }
    }

    private fun showInformation() {
        binding.nameTextView.text = loginSingletonResponse.getLoginResponse()?.data?.name
        binding.profilePhone.text = loginSingletonResponse.getLoginResponse()?.data?.phone
    }

    private fun showLoading() {
        "Loading...".also { binding.babyAgeView.text = it }
    }

    private fun hideLoading() {
        "More".also { binding.babyAgeView.text = it }
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

    private fun setUpContentRecycleView() {
        binding.freeDemoContent.apply {
            freeDemoContentAdaptor = HomeOptionsAdapter(
                context,
                HomeOptionsAdapter.FreeDemoContent,
                this@FreeDemoBashBoardFragment
            )
            adapter = freeDemoContentAdaptor
        }
    }

    override fun onClickedItem(navId: Int) {
        try {
            findNavController().navigate(navId)
        } catch (e: Exception) {
            activity?.toastMsg("$navId")
        }
    }


}