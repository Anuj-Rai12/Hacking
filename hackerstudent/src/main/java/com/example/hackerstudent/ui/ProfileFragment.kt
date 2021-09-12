package com.example.hackerstudent.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hackerstudent.R
import com.example.hackerstudent.TAG
import com.example.hackerstudent.databinding.ProfileFramgnetBinding
import com.example.hackerstudent.recycle.profile.AllProfileAdaptor
import com.example.hackerstudent.utils.*
import com.example.hackerstudent.viewmodels.PrimaryViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.profile_framgnet) {
    private lateinit var binding: ProfileFramgnetBinding
    private val authViewModel: PrimaryViewModel by viewModels()
    private lateinit var allProfileAdaptor: AllProfileAdaptor
    private val profileData: MutableList<ProfileDataClass> = mutableListOf()

    @Inject
    lateinit var networkUtils: NetworkUtils

    @Inject
    lateinit var customProgress: CustomProgress

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity?)!!.hide()
        binding = ProfileFramgnetBinding.bind(view)
        setUpRecycleView()
        if (networkUtils.isConnected()) {
            getData()
        } else {
            activity?.msg("Device is Offline", "RETRY", {
                if (networkUtils.isConnected()) {
                    Log.i(TAG, "onViewCreated From Retry section : ${networkUtils.isConnected()}")
                    getData()
                }
            })
        }
        binding.logOutBtn.setOnClickListener {
            dir(23, msg = "")
        }
    }

    private fun getData() {
        authViewModel.userInfo.observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    dir(msg = "${it.exception?.localizedMessage}")
                }
                is MySealed.Loading -> showLoading(it.data as String)

                is MySealed.Success -> {
                    hideLoading()
                    val data = it.data as CreateUserAccount?
                    data?.let { acc ->
                        profileData.add(
                            ProfileDataClass.ImageHeader(
                                email = acc.email!!,
                                firstname = acc.firstname!!,
                                lastname = acc.lastname!!
                            )
                        )
                        profileData.add(ProfileDataClass.Title("Information"))
                        profileData.add(ProfileDataClass.OptionFooter(acc.firstname))
                        profileData.add(ProfileDataClass.OptionFooter(acc.lastname))
                        profileData.add(ProfileDataClass.OptionFooter(acc.phone!!))
                        profileData.add(ProfileDataClass.OptionFooter(acc.email))
                        profileData.add(ProfileDataClass.Title("Account Service"))
                        profileData.add(ProfileDataClass.OptionFooter(GetConstStringObj.change_profile_name))
                        profileData.add(ProfileDataClass.OptionFooter(GetConstStringObj.change_email_address))
                        profileData.add(ProfileDataClass.OptionFooter(GetConstStringObj.change_profile_password))
                        allProfileAdaptor.submitList(profileData)
                    }
                }
            }
        }
    }

    private fun setUpRecycleView() {
        binding.profileLayout.apply {
            setHasFixedSize(true)
            allProfileAdaptor = AllProfileAdaptor {
                context.msg("$it Clicked")
            }
            adapter = allProfileAdaptor
        }
    }

    private fun dir(choose: Int = 0, title: String = "Error", msg: String) {
        val action = when (choose) {
            0 -> ProfileFragmentDirections.actionGlobalPasswordDialog2(title, msg)
            else -> ProfileFragmentDirections.actionGlobalPasswordDialog2()
        }
        findNavController().navigate(action)
    }

    private fun showLoading(string: String) = customProgress.showLoading(requireActivity(), string)
    private fun hideLoading() = customProgress.hideLoading()
    override fun onPause() {
        super.onPause()
        hideLoading()
    }
}