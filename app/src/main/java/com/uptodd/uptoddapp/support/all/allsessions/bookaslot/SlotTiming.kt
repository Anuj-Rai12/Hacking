package com.uptodd.uptoddapp.support.all.allsessions.bookaslot

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.SlotTimingFragmentBinding
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.UpToddDialogs

class SlotTiming : Fragment() {

    private var slotTime = ""
    private lateinit var uptoddDialogs: UpToddDialogs


    companion object {
        fun newInstance() = SlotTiming()
    }

    private lateinit var viewModel: SlotTimingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ChangeLanguage(requireContext()).setLanguage()

        uptoddDialogs = UpToddDialogs(requireContext())

        val binding: SlotTimingFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.slot_timing_fragment, container, false)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(SlotTimingViewModel::class.java)
        binding.slotTimingBinding = viewModel

        val args = SlotTimingArgs.fromBundle(requireArguments())
        binding.slotTimingDate.text = args.slotDate
        binding.slotTimingExpertName.text = args.expertName

        initializeSpinner(binding)

        viewModel.isLoading.observe(viewLifecycleOwner, {
            it.let{
                when(it){
                    0 -> {
                        uptoddDialogs.dismissDialog()
                        uptoddDialogs.showDialog(R.drawable.gif_done, "Your slot has been booked!", "OK", object: UpToddDialogs.UpToddDialogListener{
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                uptoddDialogs.dismissDialog()
                                findNavController().popBackStack(R.id.homePageFragment, false)
                            }
                        })
                        viewModel.resetState()
                    }
                    1 -> {
                        uptoddDialogs.showUploadDialog()
                        viewModel.resetState()
                    }
                    -1 ->{
                        uptoddDialogs.dismissDialog()
                        uptoddDialogs.showDialog(R.drawable.network_error, "An error has occurred: ${viewModel.apiError}", "Close", object: UpToddDialogs.UpToddDialogListener{
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                uptoddDialogs.dismissDialog()
                            }
                        })
                        viewModel.resetState()
                    }
                    else ->{}

                }
            }
        })

        binding.slotTimeSubmit.setOnClickListener {
            if(binding.slotTimeTopic.text.toString().isNotEmpty())
                viewModel.bookASlot(args.slotDate, args.expertId, args.expertName, slotTime, binding.slotTimeTopic.text.toString())
            else{
                uptoddDialogs.showDialog(R.drawable.network_error, "Topic must not be empty!", "Retry", object: UpToddDialogs.UpToddDialogListener{
                    override fun onDialogButtonClicked(dialog: Dialog) {
                        uptoddDialogs.dismissDialog()
                    }
                })
            }
        }
        return binding.root
    }

    private fun initializeSpinner(binding: SlotTimingFragmentBinding) {
        val dropdown: Spinner = binding.slotTimingTime
        val spinnerItems: Array<String> = arrayOf("Any", "Morning", "Afternoon", "Evening")
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            spinnerItems
        )
        dropdown.adapter = adapter
        dropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                slotTime = when (position) {
                    1 -> "Morning"
                    2 -> "Afternoon"
                    3 -> "Evening"
                    else -> "Anytime"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                slotTime = "Anytime"
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SlotTimingViewModel::class.java)
    }

}