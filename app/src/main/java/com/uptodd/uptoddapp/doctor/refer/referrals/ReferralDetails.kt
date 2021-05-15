package com.uptodd.uptoddapp.doctor.refer.referrals

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ReferralDetailsFragmentBinding
import com.uptodd.uptoddapp.utilities.UpToddDialogs

class ReferralDetails : Fragment() {

    companion object {
        fun newInstance() = ReferralDetails()
    }

    private lateinit var binding: ReferralDetailsFragmentBinding
    private lateinit var viewModel: ReferralDetailsViewModel
    private var referredPersonId = 0
    private var isDoctor = false
    private var referralStatus = ""
    private var editing = false
    private lateinit var editMenu: Menu

    private lateinit var uptoddDialogs: UpToddDialogs

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        uptoddDialogs = UpToddDialogs(requireContext())

        binding= DataBindingUtil.inflate(inflater, R.layout.referral_details_fragment, container, false)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(ReferralDetailsViewModel::class.java)
        binding.referralDetailsBinding = viewModel

        //Get arguments from safeargs and set edit text fields
        val args = ReferralDetailsArgs.fromBundle(requireArguments())
        referredPersonId = args.id
        isDoctor = args.doctor
        referralStatus = args.referralStatus

        viewModel.getReferralDetails(args.doctor, args.id)

        viewModel.isLoading.observe(viewLifecycleOwner, {
            it.let{
                when(it){
                    0 -> {
                        uptoddDialogs.dismissDialog()
                        viewModel.setUp(args.doctor)
                    }
                    1 -> {
                        uptoddDialogs.showLoadingDialog(findNavController())
                    }
                    10 ->{
                        uptoddDialogs.dismissDialog()
                        uptoddDialogs.showDialog(R.drawable.gif_done, "Your changes have been submitted.", "Close", object: UpToddDialogs.UpToddDialogListener{
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                uptoddDialogs.dismissDialog()
                                stopEditingDetails()
                                editing = false
                                editMenu.findItem(R.id.doctor_account_edit).setIcon(R.drawable.material_edit_white)
                            }
                        })
                    }
                    11 -> {
                        uptoddDialogs.showUploadDialog()
                    }
                    else -> {
                        uptoddDialogs.dismissDialog()
                        uptoddDialogs.showDialog(R.drawable.network_error, "An error has occurred: ${viewModel.apiError}", "OK", object: UpToddDialogs.UpToddDialogListener{
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                uptoddDialogs.dismissDialog()
                                findNavController().navigateUp()
                            }
                        })
                    }
                }
            }
        })

        if(referralStatus == "Pending") {
            setHasOptionsMenu(true)
            binding.referralDetailsNote.setTextColor(resources.getColor(R.color.details_note))
        }

        setClickListeners(binding)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        editMenu = menu
        inflater.inflate(R.menu.doctor_account, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setClickListeners(
        binding: ReferralDetailsFragmentBinding,
    ) {
        if(referralStatus == "Pending") {
            binding.referralDetailsNote.setOnClickListener {
                findNavController().navigate(ReferralDetailsDirections.actionReferralDetailsToNoteFragment( viewModel.referralId.value!! ,viewModel.referralName.value!!, isDoctor))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun stopEditingDetails(){
        binding.referralDetailsEmail.setBackgroundResource(R.drawable.round_edittext)
        binding.referralDetailsEmail.isEnabled = false
        binding.referralDetailsEmail.isFocusable = false
        binding.referralDetailsEmail.isFocusableInTouchMode = false

        binding.referralDetailsPhone.setBackgroundResource(R.drawable.round_edittext)
        binding.referralDetailsPhone.isEnabled = false
        binding.referralDetailsPhone.isFocusable = false
        binding.referralDetailsPhone.isFocusableInTouchMode = false

        binding.referralDetailsCity.setBackgroundResource(R.drawable.round_edittext)
        binding.referralDetailsCity.isEnabled = false
        binding.referralDetailsCity.isFocusable = false
        binding.referralDetailsCity.isFocusableInTouchMode = false

        binding.referralDetailsBabyName.setBackgroundResource(R.drawable.round_edittext)
        binding.referralDetailsBabyName.isEnabled = false
        binding.referralDetailsBabyName.isFocusable = false
        binding.referralDetailsBabyName.isFocusableInTouchMode = false

        binding.referralDetailsBabyDob.setBackgroundResource(R.drawable.round_edittext)
        binding.referralDetailsBabyDob.isEnabled = false
        binding.referralDetailsBabyDob.isFocusable = false
        binding.referralDetailsBabyDob.isFocusableInTouchMode = false

        binding.referralDetailsSubmit.visibility = View.INVISIBLE
    }

    private fun editDetails(){
        binding.referralDetailsEmail.setBackgroundResource(R.drawable.doctor_dashboard_editing)
        binding.referralDetailsEmail.isEnabled = true
        binding.referralDetailsEmail.isFocusable = true
        binding.referralDetailsEmail.isFocusableInTouchMode = true

        binding.referralDetailsPhone.setBackgroundResource(R.drawable.doctor_dashboard_editing)
        binding.referralDetailsPhone.isEnabled = true
        binding.referralDetailsPhone.isFocusable = true
        binding.referralDetailsPhone.isFocusableInTouchMode = true

        binding.referralDetailsCity.setBackgroundResource(R.drawable.doctor_dashboard_editing)
        binding.referralDetailsCity.isEnabled = true
        binding.referralDetailsCity.isFocusable = true
        binding.referralDetailsCity.isFocusableInTouchMode = true

        binding.referralDetailsBabyName.setBackgroundResource(R.drawable.doctor_dashboard_editing)
        binding.referralDetailsBabyName.isEnabled = true
        binding.referralDetailsBabyName.isFocusable = true
        binding.referralDetailsBabyName.isFocusableInTouchMode = true

        binding.referralDetailsBabyDob.setBackgroundResource(R.drawable.doctor_dashboard_editing)
        binding.referralDetailsBabyDob.isEnabled = true
        binding.referralDetailsBabyDob.isFocusable = true
        binding.referralDetailsBabyDob.isFocusableInTouchMode = true

        binding.referralDetailsSubmit.visibility = View.VISIBLE
        binding.referralDetailsSubmit.setOnClickListener {
            sendDetails()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.doctor_account_edit) {
            if (editing) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Cancel Editing?")
                    .setMessage("Are you sure you want to discard the changes?")
                    .setPositiveButton("Yes") { _, _ ->
                        editing = false
                        item.setIcon(R.drawable.material_edit_white)
                        viewModel.setUp(isDoctor)
                        stopEditingDetails()
                    }
                    .setNegativeButton("No"){df,_->
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
        else
            return super.onOptionsItemSelected(item)
    }

    private fun sendDetails() {
        if(isDoctor)
            viewModel.submitNewDoctorDetails()
        else
            viewModel.submitNewPatientDetails()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ReferralDetailsViewModel::class.java)

    }

}