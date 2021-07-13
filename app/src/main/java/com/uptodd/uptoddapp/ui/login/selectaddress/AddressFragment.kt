package com.uptodd.uptoddapp.ui.login.selectaddress

import android.accounts.Account
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.logindetails.UserInfo
import com.uptodd.uptoddapp.databinding.FragmentAddressBinding
import com.uptodd.uptoddapp.databinding.FragmentStageBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.login.stage.StageViewModel
import com.uptodd.uptoddapp.ui.todoScreens.TodosListActivity
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.UpToddDialogs

class AddressFragment : Fragment()
{
    lateinit var binding: FragmentAddressBinding
    lateinit var viewModel: AddressViewModel

    var address: String = " "

    var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_address, container, false)
        binding.lifecycleOwner = this

        viewModel = ViewModelProvider(this).get(AddressViewModel::class.java)

        preferences = activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        editor = preferences!!.edit()

        viewModel.uid = preferences!!.getString("uid", "")

        binding.next.setOnClickListener {


            if (AppNetworkStatus.getInstance(requireContext()).isOnline) {


                if(!binding.address.text.isNullOrBlank() && !binding.state.text.isNullOrBlank() && !binding.city.text.isNullOrBlank() && !binding.pincode.text.isNullOrBlank())
                {

                    address="${binding.address.text.toString()}  ${binding.state.text.toString()} ${binding.city.text.toString()} ${binding.pincode.text.toString()}"
                    editor?.putString(UserInfo::address.name,address)?.apply()
                    showLoadingDialog()
                    viewModel.insertAddressDetails(address)
                }
                else{
                    Snackbar.make(
                        binding.root,
                        "Please Enter All details",
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                //viewModel.getLoginDetails()
            } else {
                //showInternetNotConnectedDialog()
                Snackbar.make(
                    binding.root,
                    getString(R.string.no_internet_connection),
                    Snackbar.LENGTH_LONG
                ).show()
            }


        }


        return binding.root
    }

    private fun showLoadingDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(
            R.drawable.gif_upload,
            getString(R.string.loading_please_wait),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }
            })
        viewModel.isLoadingDialogVisible.observe(viewLifecycleOwner, Observer {
            if (!it) {
                upToddDialogs.dismissDialog()
                if (viewModel.isDataLoadedToDatabase) {
                        //view?.findNavController()?.navigate(R.id.action_stageFragment_to_homeFragment)
                        startActivity(Intent(activity, TodosListActivity::class.java))
                        activity?.finish()

                } else {
                    Toast.makeText(
                        activity,
                        getString(R.string.failed_to_load_to_database),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
        val handler = Handler()
        handler.postDelayed({
            upToddDialogs.dismissDialog()
        }, R.string.loadingDuarationInMillis.toLong())

    }

    }