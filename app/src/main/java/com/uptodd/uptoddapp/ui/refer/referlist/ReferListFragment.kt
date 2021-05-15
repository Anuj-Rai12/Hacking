package com.uptodd.uptoddapp.ui.refer.referlist

//import com.uptodd.uptoddapp.databinding.ReferralFiltersBinding
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.referrals.ReferredListItemPatient
import com.uptodd.uptoddapp.databinding.FragmentReferListBinding
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import java.util.*
import kotlin.collections.HashMap


class ReferListFragment : Fragment() {

    private var hashMap = HashMap<String, String>()

    private var startDateSelected: Long? = null
    private var endDateSelected: Long? = null

    private lateinit var binding: FragmentReferListBinding
    private lateinit var viewModel: ReferListViewModel

    private lateinit var uptoddDialogs: UpToddDialogs

    var preferences: SharedPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        uptoddDialogs = UpToddDialogs(requireContext())


        binding= DataBindingUtil.inflate(layoutInflater,R.layout.fragment_refer_list,container,false)
        binding.lifecycleOwner=this

        viewModel= ViewModelProvider(this).get(ReferListViewModel::class.java)

        preferences = activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        if(preferences!!.contains("uid"))
            viewModel.uid=preferences!!.getString("uid", "")!!
        if(preferences!!.contains("token"))
            viewModel.token= preferences!!.getString("token","")

        (activity as AppCompatActivity?)?.supportActionBar?.title=getString(R.string.referrals)
        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity?)?.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        setHasOptionsMenu(true)

        loadData()

        viewModel.referredList.observe(viewLifecycleOwner, Observer {
            if(it!=null)
            {
                Log.d("div","ReferListFragment L74 Observer called $it")
                updateList(it)
            }
        })

        initializeObservers()

        setClickListeners()


        return binding.root

    }

    private fun loadData() {
        if(AppNetworkStatus.getInstance(requireContext()).isOnline) {
            viewModel.getFullList()
        }
        else
        {
            Snackbar.make(binding.layout,getString(R.string.no_internet_connection),Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.retry)){
                    loadData()
                }.show()
        }
    }

    private fun initializeObservers() {
        viewModel.referredList.observe(viewLifecycleOwner, {
            if (it != null)
            {
                Log.d("div","ReferListFragment L101 Observer called $it")
                updateList(it)
                if(it.size==0)
                {
                    binding.editTextFilterSearch.visibility=View.INVISIBLE
                    binding.imageButtonSearch.visibility=View.INVISIBLE
                    binding.imageButtonFilter.visibility=View.INVISIBLE
                    binding.imageViewEmpty.visibility=View.VISIBLE
                    binding.textViewEmpty.visibility=View.VISIBLE
                }
                else
                {
                    binding.editTextFilterSearch.visibility=View.VISIBLE
                    binding.imageButtonSearch.visibility=View.VISIBLE
                    binding.imageButtonFilter.visibility=View.VISIBLE
                    binding.imageViewEmpty.visibility=View.INVISIBLE
                    binding.textViewEmpty.visibility=View.INVISIBLE
                }
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, {
            it.let{
                when(it){
                    0 -> {
                        uptoddDialogs.dismissDialog()
                        viewModel.loadFullList()
                    }
                    1 -> {
                        uptoddDialogs.showLoadingDialog(findNavController())
                    }
                    10 ->{
                        uptoddDialogs.dismissDialog()
                        uptoddDialogs.showDialog(R.drawable.gif_done, getString(R.string.thank_you_for_your_feedback), getString(R.string.close)
                            , object: UpToddDialogs.UpToddDialogListener{
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
                        uptoddDialogs.showDialog(R.drawable.network_error, getString(R.string.an_error_has_occurred)+viewModel.apiError, getString(R.string.ok)
                            , object: UpToddDialogs.UpToddDialogListener{
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

    private fun setClickListeners() {
        binding.imageButtonSearch.setOnClickListener {
            applyFiltersAndLoadData()
        }
        binding.imageButtonFilter.setOnClickListener{
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

    private fun applyFiltersAndLoadData() {
        val filterSearch = binding.editTextFilterSearch.text.toString()

        if (filterSearch.isNotEmpty())
            hashMap["search"] = filterSearch

        viewModel.loadData(hashMap)
    }

    //TODO: Maybe find a better way to update list
    private fun updateList(data: ArrayList<ReferredListItemPatient>) {

        binding.tableViewReferrals.removeAllViews()

        data.forEach {

            val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val referListView = inflater.inflate(R.layout.refer_list_item, null)

            val nameTextView: TextView = referListView.findViewById(R.id.refer_list_item_name)
            val emailTextView: TextView = referListView.findViewById(R.id.refer_list_item_email)
            val referralStatusTextView: TextView = referListView.findViewById(R.id.refer_list_item_status)
            val referralDateTextView: TextView = referListView.findViewById(R.id.refer_list_item_date)

            nameTextView.text = it.patientName
            emailTextView.text = it.patientMail
            referralDateTextView.text = viewModel.getDateFromTime(it.referralDateValue)
            referralStatusTextView.text = it.referralStatus

            when(it.referralStatus){
                "Success" -> referralStatusTextView.setBackgroundResource(R.drawable.refer_success)
                "Cancelled" -> referralStatusTextView.setBackgroundResource(R.drawable.refer_cancelled)
                "Pending" -> referralStatusTextView.setBackgroundResource(R.drawable.refer_pending)
            }

            referListView.setOnClickListener {_ ->
                val bundle=Bundle()
                bundle.putString("name",it.patientName)
                bundle.putString("email",it.patientMail)
                bundle.putString("phone",it.patientPhone)
                bundle.putString("referralDate", it.referalDate)
                bundle.putString("registrationDate",it.registrationDate)
                bundle.putString("status",it.referralStatus)
                bundle.putInt("id",it.id)
                view?.findNavController()?.navigate(R.id.action_referListFragment_to_referDetailsFragment,bundle)
            }

            binding.tableViewReferrals.addView(referListView)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}