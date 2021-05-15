package com.uptodd.uptoddapp.doctor.refer.referrals.patients

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.referrals.ReferredListItemPatient
import com.uptodd.uptoddapp.databinding.ReferredListPatientsFragmentBinding
import com.uptodd.uptoddapp.doctor.refer.referrals.doctor.ReferredDoctorsListArgs
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import java.util.*
import kotlin.collections.HashMap


class ReferredPatientsList : Fragment() {

    private var hashMap = HashMap<String, String>()

    private var startDateSelected: Long? = null
    private var endDateSelected: Long? = null

    private lateinit var uptoddDialogs: UpToddDialogs


    companion object {
        fun newInstance() = ReferredPatientsList()
    }

    private lateinit var viewModel: ReferredPatientsListViewModel
    private lateinit var binding: ReferredListPatientsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        uptoddDialogs = UpToddDialogs(requireContext())

        binding= DataBindingUtil.inflate(
            inflater,
            R.layout.referred_list_patients_fragment,
            container,
            false
        )
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(ReferredPatientsListViewModel::class.java)
        binding.referredPatientsListBinding = viewModel

        initializeObservers(binding)

        setClickListeners()

        return binding.root
    }

    private fun setClickListeners() {
        binding.referredListSearch.setOnClickListener {
            applyFiltersAndLoadData()
        }
        binding.referredListFilter.setOnClickListener{
            showFilterDialog()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun showFilterDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.referral_filters)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val statusSuccessful = dialog.findViewById<CheckBox>(R.id.referral_filters_status_successful)
        val statusPending = dialog.findViewById<CheckBox>(R.id.referral_filters_status_pending)
        val statusFailed = dialog.findViewById<CheckBox>(R.id.referral_filters_status_failed)
        val startDateText = dialog.findViewById<TextView>(R.id.referral_filters_date_start)
        val endDateText = dialog.findViewById<TextView>(R.id.referral_filters_date_end)
        val clearDate = dialog.findViewById<TextView>(R.id.referral_filters_clear_date)


        val now = Calendar.getInstance()
        val selected = Calendar.getInstance()

        val startDatePickerListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            selected.set(year , month, dayOfMonth,0 ,0)
            startDateSelected = selected.timeInMillis
            startDateText.text = "$dayOfMonth/${month+1}/$year"
        }

        val endDatePickerListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            selected.set(year , month, dayOfMonth,23 ,59)
            endDateSelected = selected.timeInMillis
            endDateText.text = "$dayOfMonth/${month+1}/$year"
        }

        if(startDateSelected!=null){
            selected.timeInMillis = startDateSelected!!
            startDateText.text = "${selected.get(Calendar.DAY_OF_MONTH)}/${selected.get(Calendar.MONTH)+1}/${selected.get(Calendar.YEAR)}"
        }
        if(endDateSelected!=null){
            selected.timeInMillis = endDateSelected!!
            endDateText.text = "${selected.get(Calendar.DAY_OF_MONTH)}/${selected.get(Calendar.MONTH)+1}/${selected.get(Calendar.YEAR)}"
        }

        startDateText.setOnClickListener {
            val mDate = DatePickerDialog(requireContext(), startDatePickerListener,now.get(Calendar.YEAR),  now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
            mDate.datePicker.maxDate = System.currentTimeMillis()
            mDate.show()
        }

        endDateText.setOnClickListener {
            if(startDateSelected!=null){
                val mDate = DatePickerDialog(requireContext(), endDatePickerListener, now.get(Calendar.YEAR),  now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
                mDate.datePicker.maxDate = System.currentTimeMillis()
                mDate.show()
            }
            else{
                val mDate = DatePickerDialog(requireContext(), endDatePickerListener, now.get(Calendar.YEAR),  now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
                mDate.datePicker.maxDate = System.currentTimeMillis()
                mDate.show()
            }
        }

        clearDate.setOnClickListener {
            startDateText.text = ""
            endDateText.text = ""
            startDateSelected = null
            endDateSelected = null
        }

        if(hashMap.containsKey("successful"))
            statusSuccessful.isChecked = true
        if(hashMap.containsKey("pending"))
            statusPending.isChecked = true
        if(hashMap.containsKey("failed"))
            statusFailed.isChecked = true

        dialog.findViewById<Button>(R.id.referral_filters_date_done).setOnClickListener {
            if(statusSuccessful.isChecked)
                hashMap["successful"] = "true"
            else
                hashMap.remove("successful")

            if(statusPending.isChecked)
                hashMap["pending"] = "true"
            else
                hashMap.remove("pending")

            if(statusFailed.isChecked)
                hashMap["failed"] = "true"
            else
                hashMap.remove("failed")

            if(startDateSelected!=null)
                hashMap["start_date"] = startDateSelected.toString()
            else
                hashMap.remove("start_date")

            if(endDateSelected!=null)
                hashMap["end_date"] = endDateSelected.toString()
            else
                hashMap.remove("end_date")

            applyFiltersAndLoadData()
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun initializeObservers(binding: ReferredListPatientsFragmentBinding) {
        viewModel.referredListPatient.observe(viewLifecycleOwner, {
            if (it != null)
                updateList(it, binding)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, {
            it.let{
                when(it){
                    0 -> {
                        uptoddDialogs.dismissDialog()
                        viewModel.loadFullList()
                        val args = ReferredDoctorsListArgs.fromBundle(requireArguments())
                        if(args.filtersStatus!=null){
                            if(args.filtersStatus == "Success") {
                                hashMap["successful"] = "true"
                                viewModel.loadData(hashMap)
                            }
                        }
                    }
                    1 -> {
                        uptoddDialogs.showLoadingDialog(findNavController())
                    }
                    10 ->{
                        uptoddDialogs.dismissDialog()
                        uptoddDialogs.showDialog(R.drawable.gif_done, "Thank you for your feedback.", "Close", object: UpToddDialogs.UpToddDialogListener{
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                uptoddDialogs.dismissDialog()
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
    }

    //Loads data based on filters
    private fun applyFiltersAndLoadData() {
        val filterSearch = binding.filterSearch.text.toString()
        if (filterSearch.isNotEmpty())
            hashMap["search"] = filterSearch

        viewModel.loadData(hashMap)
    }

    private fun updateList(data: ArrayList<ReferredListItemPatient>, binding: ReferredListPatientsFragmentBinding) {

        if(data.isNotEmpty()) {

            binding.tableViewReferrals.removeAllViews()

            data.forEach {

                val inflater =
                    requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val referListView = inflater.inflate(R.layout.refer_list_item, null)

                val nameTextView: TextView = referListView.findViewById(R.id.refer_list_item_name)
                val emailTextView: TextView = referListView.findViewById(R.id.refer_list_item_email)
                val referralStatusTextView: TextView =
                    referListView.findViewById(R.id.refer_list_item_status)
                val referralDateTextView: TextView =
                    referListView.findViewById(R.id.refer_list_item_date)

                nameTextView.text = it.patientName
                emailTextView.text = it.patientMail
                referralDateTextView.text = viewModel.getDateFromTime(it.referralDateValue)
                referralStatusTextView.text = it.referralStatus

                when (it.referralStatus) {
                    "Success" -> referralStatusTextView.setBackgroundResource(R.drawable.refer_success)
                    "Cancelled" -> referralStatusTextView.setBackgroundResource(R.drawable.refer_cancelled)
                    "Pending" -> referralStatusTextView.setBackgroundResource(R.drawable.refer_pending)
                }

                referListView.setOnClickListener { _ ->
                    findNavController().navigate(
                        ReferredPatientsListDirections.actionReferredListToReferralDetails(
                            it.id,
                            false,
                            it.referralStatus
                        )
                    )
                }

                binding.tableViewReferrals.addView(referListView)
            }
            if (data.size == 0) {
                val placeholderText = TextView(requireContext())
                placeholderText.gravity = Gravity.CENTER
                placeholderText.textSize = 18f
                placeholderText.text = "No data found!!"
                binding.tableViewReferrals.addView(placeholderText)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).menu.getItem(1).isChecked = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ReferredPatientsListViewModel::class.java)
    }

}