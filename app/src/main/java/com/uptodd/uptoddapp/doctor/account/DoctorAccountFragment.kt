package com.uptodd.uptoddapp.doctor.account

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.transition.MaterialSharedAxis
import com.uptodd.uptoddapp.LoginActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.databinding.DoctorAccountFragmentBinding
import com.uptodd.uptoddapp.doctor.dashboard.DoctorDashboardViewModel
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
class DoctorAccountFragment : Fragment() {

    companion object {
        fun newInstance() = DoctorAccountFragment()
    }

    private lateinit var viewModel: DoctorDashboardViewModel
    private lateinit var binding: DoctorAccountFragmentBinding
    private var editing = false

    private lateinit var uptoddDialogs: UpToddDialogs


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        uptoddDialogs = UpToddDialogs(requireContext())

        binding =
            DataBindingUtil.inflate(inflater, R.layout.doctor_account_fragment, container, false)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(DoctorDashboardViewModel::class.java)
        binding.doctorAccountBinding = viewModel

        setClickListeners()

        viewModel.isLoading.observe(viewLifecycleOwner, {
            it.let {
                when (it) {
                    0 -> {
                        uptoddDialogs.dismissDialog()
                        viewModel.initializeVariables()
                        setHasOptionsMenu(true)
                    }
                    1 -> {
                        uptoddDialogs.showLoadingDialog(findNavController(), false)
                    }
                    -2 -> {
                        uptoddDialogs.dismissDialog()
                        setHasOptionsMenu(false)
                        uptoddDialogs.showDialog(
                            R.drawable.network_error,
                            "An error has occurred: ${viewModel.apiError}",
                            "OK",
                            object : UpToddDialogs.UpToddDialogListener {
                                override fun onDialogButtonClicked(dialog: Dialog) {
                                    uptoddDialogs.dismissDialog()
                                    findNavController().navigateUp()
                                }
                            })
                    }
                    10 -> {
                        uptoddDialogs.dismissDialog()
                        uptoddDialogs.showDialog(
                            R.drawable.gif_done,
                            "Your changes have been submitted.",
                            "Close",
                            object : UpToddDialogs.UpToddDialogListener {
                                override fun onDialogButtonClicked(dialog: Dialog) {
                                    uptoddDialogs.dismissDialog()
                                    stopEditingDetails()
                                    editing = false
                                    viewModel.editMenu.findItem(R.id.doctor_account_edit)
                                        .setIcon(R.drawable.material_edit_white)
                                }
                            })
                    }
                    11 -> {
                        uptoddDialogs.showUploadDialog()
                    }
                    12 -> {
                        stopEditingDetails()
                        editing = false
                        viewModel.editMenu.findItem(R.id.doctor_account_edit)
                            .setIcon(R.drawable.material_edit_white)
                    }
                    else -> {
                        uptoddDialogs.dismissDialog()
                        uptoddDialogs.showDialog(
                            R.drawable.network_error,
                            "An error has occurred: ${viewModel.apiError}",
                            "OK",
                            object : UpToddDialogs.UpToddDialogListener {
                                override fun onDialogButtonClicked(dialog: Dialog) {
                                    uptoddDialogs.dismissDialog()
                                    findNavController().navigateUp()
                                }
                            })
                    }
                }
            }
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).menu.getItem(3).isChecked =
            true
    }

    private fun setClickListeners() {
        binding.doctorAccountLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout?")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    requireContext().getSharedPreferences("LOGIN_INFO", MODE_PRIVATE).edit()
                        .putBoolean("loggedIn", false).apply()

                    AllUtil.unregisterToken()

                    UptoddSharedPreferences.getInstance(requireContext()).clearAllPreferences()

                    CoroutineScope(Dispatchers.IO).launch {
                        UptoddDatabase.getInstance(requireContext()).clearAllTables()
                    }

                    startActivity(Intent(requireContext(), LoginActivity::class.java))

                    requireActivity().finishAffinity()

                }
                .setNegativeButton("No") { di, _ ->
                    di.dismiss()
                }
                .show()
        }

        binding.doctorAccountChangePassword.setOnClickListener {
            findNavController().navigate(
                DoctorAccountFragmentDirections.actionDoctorAccountToChangePasswordFragment(
                    true
                )
            )
        }
    }

    private fun editDetails() {
        binding.doctorAccountEmail.setBackgroundResource(R.drawable.doctor_dashboard_editing)
        binding.doctorAccountEmail.isEnabled = true
        binding.doctorAccountEmail.isFocusable = true
        binding.doctorAccountEmail.isFocusableInTouchMode = true

        binding.doctorAccountPhone.setBackgroundResource(R.drawable.doctor_dashboard_editing)
        binding.doctorAccountPhone.isEnabled = true
        binding.doctorAccountPhone.isFocusable = true
        binding.doctorAccountPhone.isFocusableInTouchMode = true

        binding.doctorAccountWhatsapp.setBackgroundResource(R.drawable.doctor_dashboard_editing)
        binding.doctorAccountWhatsapp.isEnabled = true
        binding.doctorAccountWhatsapp.isFocusable = true
        binding.doctorAccountWhatsapp.isFocusableInTouchMode = true

        binding.doctorAccountEditBankAccountIfsc.setBackgroundResource(R.drawable.doctor_dashboard_editing)
        binding.doctorAccountEditBankAccountIfsc.isEnabled = true
        binding.doctorAccountEditBankAccountIfsc.isFocusable = true
        binding.doctorAccountEditBankAccountIfsc.isFocusableInTouchMode = true

        binding.doctorAccountEditBankAccountName.setBackgroundResource(R.drawable.doctor_dashboard_editing)
        binding.doctorAccountEditBankAccountName.isEnabled = true
        binding.doctorAccountEditBankAccountName.isFocusable = true
        binding.doctorAccountEditBankAccountName.isFocusableInTouchMode = true

        binding.doctorAccountEditBankAccountHolder.setBackgroundResource(R.drawable.doctor_dashboard_editing)
        binding.doctorAccountEditBankAccountHolder.isEnabled = true
        binding.doctorAccountEditBankAccountHolder.isFocusable = true
        binding.doctorAccountEditBankAccountHolder.isFocusableInTouchMode = true

        binding.doctorAccountEditBankAccountNumber.setBackgroundResource(R.drawable.doctor_dashboard_editing)
        binding.doctorAccountEditBankAccountNumber.isEnabled = true
        binding.doctorAccountEditBankAccountNumber.isFocusable = true
        binding.doctorAccountEditBankAccountNumber.isFocusableInTouchMode = true
        viewModel.showBankAccountNumber()

        binding.doctorAccountChangePassword.visibility = View.INVISIBLE
        binding.doctorAccountLogout.visibility = View.INVISIBLE

    }

    private fun stopEditingDetails() {
        binding.doctorAccountEmail.setBackgroundResource(R.drawable.round_edittext)
        binding.doctorAccountEmail.isEnabled = false
        binding.doctorAccountEmail.isFocusable = false
        binding.doctorAccountEmail.isFocusableInTouchMode = false

        binding.doctorAccountPhone.setBackgroundResource(R.drawable.round_edittext)
        binding.doctorAccountPhone.isEnabled = false
        binding.doctorAccountPhone.isFocusable = false
        binding.doctorAccountPhone.isFocusableInTouchMode = false

        binding.doctorAccountWhatsapp.setBackgroundResource(R.drawable.round_edittext)
        binding.doctorAccountWhatsapp.isEnabled = false
        binding.doctorAccountWhatsapp.isFocusable = false
        binding.doctorAccountWhatsapp.isFocusableInTouchMode = false

        binding.doctorAccountEditBankAccountIfsc.setBackgroundResource(R.drawable.round_edittext)
        binding.doctorAccountEditBankAccountIfsc.isEnabled = false
        binding.doctorAccountEditBankAccountIfsc.isFocusable = false
        binding.doctorAccountEditBankAccountIfsc.isFocusableInTouchMode = false

        binding.doctorAccountEditBankAccountName.setBackgroundResource(R.drawable.round_edittext)
        binding.doctorAccountEditBankAccountName.isEnabled = false
        binding.doctorAccountEditBankAccountName.isFocusable = false
        binding.doctorAccountEditBankAccountName.isFocusableInTouchMode = false

        binding.doctorAccountEditBankAccountHolder.setBackgroundResource(R.drawable.round_edittext)
        binding.doctorAccountEditBankAccountHolder.isEnabled = false
        binding.doctorAccountEditBankAccountHolder.isFocusable = false
        binding.doctorAccountEditBankAccountHolder.isFocusableInTouchMode = false

        binding.doctorAccountEditBankAccountNumber.setBackgroundResource(R.drawable.round_edittext)
        binding.doctorAccountEditBankAccountNumber.isEnabled = false
        binding.doctorAccountEditBankAccountNumber.isFocusable = false
        binding.doctorAccountEditBankAccountNumber.isFocusableInTouchMode = false
        viewModel.hideBankAccountNumber()



        binding.doctorAccountChangePassword.visibility = View.VISIBLE
        binding.doctorAccountLogout.visibility = View.VISIBLE
    }

//    private fun getPasswordVerification(item: MenuItem) {
//        val dialog = Dialog(requireContext())
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.setContentView(R.layout.password_dialog)
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.password_dialog_submit.setOnClickListener {
//            if(dialog.password_dialog_password.text.toString() == viewModel.password){
//                dialog.dismiss()
//                editDetails()
//                editing = true
//                item.setIcon(R.drawable.material_save_white)
//            }
//            else{
//                dialog.dismiss()
//                Snackbar.make(requireView(), "Invalid Password", Snackbar.LENGTH_SHORT).show()
//            }
//        }
//        dialog.show()
//    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        viewModel.editMenu = menu
        inflater.inflate(R.menu.doctor_account, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.doctor_account_edit) {
            if (editing) {
                editing = false
                item.setIcon(R.drawable.material_edit_white)
                sendDetails()
            } else {
                editDetails()
                editing = true
                item.setIcon(R.drawable.material_save_white)
//                getPasswordVerification(item)
            }
            return true
        } else
            return super.onOptionsItemSelected(item)
    }

    private fun sendDetails() {
        viewModel.updateAccount()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DoctorDashboardViewModel::class.java)
    }

}