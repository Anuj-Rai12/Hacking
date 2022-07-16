package com.uptodd.uptoddapp.ui.freeparenting.dashboard

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FreeDemoDashboardScreenFramgentBinding
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.ui.home.homePage.adapter.HomeOptionsAdapter
import com.uptodd.uptoddapp.utils.toastMsg

class FreeDemoBashBoardFragment : Fragment(R.layout.free_demo_dashboard_screen_framgent),
    HomeOptionsAdapter.HomeOptionsClickListener {

    private lateinit var binding: FreeDemoDashboardScreenFramgentBinding
    private var freeDemoContentAdaptor: HomeOptionsAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FreeDemoDashboardScreenFramgentBinding.bind(view)

        val ins=LoginSingletonResponse.getInstance()
        activity?.toastMsg("${ins.getLoginRequest()}")
        activity?.toastMsg("${ins.getLoginResponse()}")

        setUpContentRecycleView()

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
        }catch (e:Exception){
            activity?.toastMsg("$navId")
        }
    }



}