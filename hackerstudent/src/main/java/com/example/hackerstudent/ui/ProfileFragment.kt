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
    private var updateUserInfoDialog: UpdateUserInfoDialog? = null

    @Inject
    lateinit var networkUtils: NetworkUtils

    @Inject
    lateinit var customProgress: CustomProgress

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity?)!!.hide()
        showBottomNavBar()
        activity?.changeStatusBarColor()
        binding = ProfileFramgnetBinding.bind(view)
        setUpRecycleView()
        if (networkUtils.isConnected()) {
            getData()
        } else {
            noInternetConnection()
            activity?.msg(GetConstStringObj.NO_INTERNET, GetConstStringObj.RETRY, {
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
                    noInternetConnection()
                    hideLoading()
                    dir(msg = "${it.exception?.localizedMessage}")
                }
                is MySealed.Loading -> {
                    deviceNetworkConnected()
                    showLoading(it.data as String)
                }

                is MySealed.Success -> {
                    hideLoading()
                    deviceNetworkConnected()
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
                        profileData.add(ProfileDataClass.Title("Other Features"))
                        profileData.add(ProfileDataClass.OptionFooter("Rate Us"))
                        profileData.add(ProfileDataClass.OptionFooter("Share"))
                        profileData.add(ProfileDataClass.OptionFooter("About Me"))
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
                if (it == GetConstStringObj.change_profile_name || it == GetConstStringObj.change_profile_password || it == GetConstStringObj.change_email_address)
                    openDialog(it)
            }
            adapter = allProfileAdaptor
        }
    }

    private fun openDialog(info: String) {
        updateUserInfoDialog = UpdateUserInfoDialog(info, { CurrEmail, CurrentPass, NewEmail ->
            Log.i(TAG, "openDialog: $CurrEmail and $CurrentPass,and $NewEmail")
            updateEmail(CurrEmail, CurrentPass, NewEmail)
            updateUserInfoDialog?.dismiss()
        }, { email, currentPassword, NewPassword ->
            Log.i(TAG, "openDialog: $email , $currentPassword and $NewPassword")
            updateRestPassword(email, currentPassword, NewPassword)
            updateUserInfoDialog?.dismiss()
        }, { firstName, lastName ->
            Log.i(TAG, "openDialog: $firstName and lastName $lastName")
            updateUserName(firstName, lastName)
            updateUserInfoDialog?.dismiss()
        })
        updateUserInfoDialog?.show(childFragmentManager, "Update Dialog")
    }

    private fun updateEmail(currEmail: String, currentPass: String, newEmail: String) {
        authViewModel.updateEmail(currEmail, currentPass, newEmail).observe(viewLifecycleOwner) {
            updateRepo(it)
            if (it is MySealed.Success) {
                authViewModel.updatePassword(password = newEmail, TAG = "EMAIL_ADDRESS")
            }
        }
    }

    private fun updateRestPassword(email: String, currentPassword: String, newPassword: String) {
        authViewModel.updatePassword(email, currentPassword, newPassword)
            .observe(viewLifecycleOwner) {
                updateRepo(it)
                if (it is MySealed.Success) {
                    authViewModel.updatePassword(password = newPassword, TAG = "PASSWORD")
                }
            }
    }

    private fun updateUserName(firstName: String, lastName: String) {
        authViewModel.updateUserName(firstName, lastName).observe(viewLifecycleOwner) {
            updateRepo(it)
        }
    }

    private fun updateRepo(it: MySealed<out String>?) {
        when (it) {
            is MySealed.Error -> {
                hideLoading()
                dir(msg = "${it.exception?.localizedMessage}")
            }
            is MySealed.Loading -> showLoading("${it.data}")
            is MySealed.Success -> {
                hideLoading()
                dir(title = "Success", msg = "${it.data}")
            }
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
    private fun deviceNetworkConnected() {
        binding.profileLayout.show()
        binding.noInternetProfile.hide()
    }

    private fun noInternetConnection() {
        binding.noInternetProfile.show()
        binding.noInternetProfile.setAnimation(R.raw.no_connection)
        binding.profileLayout.hide()
    }

    override fun onPause() {
        super.onPause()
        hideLoading()
        updateUserInfoDialog?.dismiss()
    }
}