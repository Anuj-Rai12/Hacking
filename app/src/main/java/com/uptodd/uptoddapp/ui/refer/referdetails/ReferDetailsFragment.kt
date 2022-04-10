package com.uptodd.uptoddapp.ui.refer.referdetails

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.referrals.ReferredListItemPatient
import com.uptodd.uptoddapp.databinding.FragmentReferDetailsBinding
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.ToolbarUtils
import com.uptodd.uptoddapp.utilities.UpToddDialogs

class ReferDetailsFragment : Fragment() {

    private lateinit var binding:FragmentReferDetailsBinding
    private lateinit var viewModel: ReferDetailsViewModel

    private var editing = false

    private var referUser=ReferredListItemPatient()

    private lateinit var uptoddDialogs: UpToddDialogs
    private lateinit var editMenu: Menu

    var preferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            referUser.patientName=it.getString("name")
            referUser.patientMail=it.getString("email")
            referUser.patientPhone=it.getString("phone")
            referUser.referalDate=it.getString("referralDate")
            referUser.registrationDate=it.getString("registrationDate")
            referUser.referralStatus=it.getString("status")
            referUser.id=it.getInt("id")
            Log.d("div","ReferDetailsFragment L50 ${referUser.patientName} ${referUser.patientMail} ${referUser.patientPhone} ${referUser.referalDate} ${referUser.registrationDate} ${referUser.referralStatus} ${referUser.id}")
//            referUser.isPaid=it.getBoolean("isPaid")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        uptoddDialogs = UpToddDialogs(requireContext())

        binding=DataBindingUtil.inflate(layoutInflater,R.layout.fragment_refer_details,container,false)
        binding.lifecycleOwner=this

        viewModel=ViewModelProvider(this).get(ReferDetailsViewModel::class.java)

        preferences = activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        viewModel.uid=referUser.id.toString()
        if(preferences!!.contains("token"))
            viewModel.token= preferences!!.getString("token","")

        (activity as AppCompatActivity?)?.supportActionBar?.title=getString(R.string.referral_details)
        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity?)?.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        setHasOptionsMenu(true)

        if(referUser.referralStatus == "Pending") {
            setHasOptionsMenu(true)
        }

        setFields()

        binding.toolbar?.let {
            ToolbarUtils.initNCToolbar(requireActivity(),"Details", it,
                findNavController())
        }

        binding.referralDetailsViewModel=viewModel

        viewModel.isLoading.observe(viewLifecycleOwner, {
            when(it){
                1 ->{
                    showUploadingDialog()
                }
                0 -> {
                    uptoddDialogs.showDialog(R.drawable.gif_done, getString(R.string.new_details_have_been_submitted_successfully), getString(R.string.ok), object: UpToddDialogs.UpToddDialogListener{
                        override fun onDialogButtonClicked(dialog: Dialog) {
                            uptoddDialogs.dismissDialog()
                            stopEditingDetails()
                            editing = false
                            editMenu.findItem(R.id.item_edit).setIcon(R.drawable.ic_baseline_edit_24)
                        }
                    })
                }
            }
        })

        binding.referralDetailsSubmit.setOnClickListener {onClickSubmit() }

        return binding.root
    }

    private fun setFields() {
        Log.d("div","ReferDetailsFragment L114 $referUser")
        viewModel.referralName=referUser.patientName
        viewModel.referralEmail.value=referUser.patientMail
        viewModel.referralPhone.value=referUser.patientPhone
        viewModel.referralDate=referUser.referalDate.substring(8,10)+"/"+referUser.referalDate.substring(5,7)+"/"+referUser.referalDate.substring(0,4)
        if(referUser.referalDate==null || referUser.registrationDate=="null")
            viewModel.referralRegistrationDate=referUser.registrationDate
        viewModel.referralStatus=referUser.referralStatus
    }

    private fun onClickSubmit() {
        viewModel.referralPhone.value=binding.referralDetailsPhone.text.toString()
        viewModel.referralEmail.value=binding.referralDetailsEmail.text.toString()
        //if(referUser.referralStatus == "Success") { }
        if(AppNetworkStatus.getInstance(requireContext()).isOnline) {
            viewModel.submit()
        }
        else
        {
            Snackbar.make(binding.layout, getString(R.string.no_internet_connection),Snackbar.LENGTH_LONG)
                .setAction( getString(R.string.retry)){
                    onClickSubmit()
                }.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun stopEditingDetails(){
        binding.referralDetailsEmail.isEnabled = false
        binding.referralDetailsEmail.isFocusable = false
        binding.referralDetailsEmail.isFocusableInTouchMode = false

        binding.referralDetailsPhone.isEnabled = false
        binding.referralDetailsPhone.isFocusable = false
        binding.referralDetailsPhone.isFocusableInTouchMode = false

        binding.referralDetailsSubmit.visibility=View.INVISIBLE

        binding.referralDetailsLayout1Field2.setBackgroundResource(R.drawable.round_edittext)
        binding.referralDetailsLayout2Field2.setBackgroundResource(R.drawable.round_edittext)
        binding.referralDetailsLayout3Field2.setBackgroundResource(R.drawable.round_edittext)

    }

    private fun editDetails(){
        binding.referralDetailsEmail.isEnabled = true
        binding.referralDetailsEmail.isFocusable = true
        binding.referralDetailsEmail.isFocusableInTouchMode = true

        binding.referralDetailsPhone.isEnabled = true
        binding.referralDetailsPhone.isFocusable = true
        binding.referralDetailsPhone.isFocusableInTouchMode = true

        binding.referralDetailsSubmit.visibility=View.VISIBLE

        binding.referralDetailsLayout1Field2.setBackgroundResource(0)
        binding.referralDetailsLayout2Field2.setBackgroundResource(0)
        binding.referralDetailsLayout3Field2.setBackgroundResource(0)

    }

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.doctor_account_edit) {
            if (editing) {
                editing = false
                item.setIcon(R.drawable.material_edit_white)
                sendDetails()
                stopEditingDetails()
            } else {
                editing = true
                item.setIcon(R.drawable.material_save_white)
                editDetails()
            }
            return true
        }
        else
            return super.onOptionsItemSelected(item)
    }*/

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        editMenu = menu
        inflater.inflate(R.menu.menu_user_account, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.item_edit) {
            if (editing) {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.cancel_editing))
                    .setMessage(getString(R.string.are_you_sure_you_want_to_discard_the_changes))
                    .setPositiveButton("Yes") { _, _ ->
                        editing = false
                        item.setIcon(R.drawable.material_edit_white)
                        viewModel.setUp(referUser)
                        stopEditingDetails()
                    }
                    .setNegativeButton(getString(R.string.no)){ df, _->
                        df.dismiss()
                    }
                    .show()
            } else {
                editing = true
                item.setIcon(R.drawable.material_cancel_white)
                editDetails()
            }
            return true
        }
        else if(item.itemId==R.id.home)
        {
            activity?.onBackPressed()
            return true
        }
        else
            return super.onOptionsItemSelected(item)
    }

    private fun showUploadingDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_upload,
            getString(R.string.loading_please_wait),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                    //findNavController().navigateUp()
                }
            })
        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            if (it==0) {
                upToddDialogs.dismissDialog()
            }
        })
        val handler= Handler()
        handler.postDelayed({
            upToddDialogs.dismissDialog()
        }, R.string.loadingDuarationInMillis.toLong())

    }
}