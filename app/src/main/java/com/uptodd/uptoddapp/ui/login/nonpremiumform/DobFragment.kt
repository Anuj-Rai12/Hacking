package com.uptodd.uptoddapp.ui.login.nonpremiumform

import android.app.DatePickerDialog
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.logindetails.UserInfo
import com.uptodd.uptoddapp.databinding.FragmentNonpremiumDobBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.todoScreens.TodosListActivity
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import java.text.SimpleDateFormat
import java.util.*


class DobFragment : Fragment() {

    lateinit var binding: FragmentNonpremiumDobBinding
    var viewModel:BirthViewModel?=null
    var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    var dob=""
    var dobObj:Date?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentNonpremiumDobBinding.inflate(inflater,container,false)
        viewModel= ViewModelProvider(this)[BirthViewModel::class.java]
        preferences = activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        editor = preferences!!.edit()

        viewModel?.uid = preferences!!.getString("uid", "")

        binding.editTextDob.setOnClickListener {
            val date= Date(System.currentTimeMillis())
            val datePickerDialog = DatePickerDialog(requireContext(),
                DatePickerDialog.OnDateSetListener { _, year, month,
                                                     date ->
                    dob="$year-${month+1}-$date"
                    dobObj=SimpleDateFormat("yyyy-mm-dd").parse(dob)
                    dob=SimpleDateFormat("yyyy-mm-dd").format(dobObj)
                binding.editTextDob.text = "$year-${month+1}-$date" },2000+date.year,date.month,date.date)
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }


        binding.buttonNext.setOnClickListener {
            if(binding.editTextDob.text.toString().compareTo("Select Date of Birth",true)!=0) {


                editor?.putString("kidsDob",dob)?.apply()
                if(AllUtil.isUserPremium(requireContext()))
                {

                    editor?.putString(UserInfo::kidsDob.name,dob)?.apply()


                    showLoadingDialog()
                    viewModel?.insertDobDetails(binding.editTextDob.text.toString())
                    Log.d("dob",dob)
                }
                else
                {
                    BirthViewModel.npAcc.kidsDob=dob

                    view?.findNavController()?.navigate(R.id.action_dobFragment_to_toysFragment2)
                    viewModel?.putDob(dob)
                }
            }
        }
        return binding.root
    }
    private fun showLoadingDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_upload,
            getString(R.string.loading_please_wait),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }
            })
        viewModel?.isLoadingDialogVisible?.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (!it) {
                upToddDialogs.dismissDialog()
                if (viewModel?.isDataLoadedToDatabase!!) {

                    val address= preferences!!.getString(UserInfo::address.name,null)

                    val country=if(UptoddSharedPreferences.getInstance(requireContext()).getPhone()?.startsWith("+91")!!)
                        "india"
                    else
                        "row"
                    if((address=="null" || address==null)&& country=="india")
                    {
                        findNavController().navigate(R.id.action_dobFragment_to_addressFragment)
                    }
                    else
                    { //view?.findNavController()?.navigate(R.id.action_stageFragment_to_homeFragment)
                        startActivity(Intent(activity, TodosListActivity::class.java))
                        activity?.finish()
                    }
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