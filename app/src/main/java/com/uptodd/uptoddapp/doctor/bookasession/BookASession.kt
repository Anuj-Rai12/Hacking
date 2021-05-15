package com.uptodd.uptoddapp.doctor.bookasession

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.BookASessionFragmentBinding
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import pl.droidsonroids.gif.GifImageView


class BookASession : Fragment() {

    companion object {
        fun newInstance() = BookASession()
    }

    private lateinit var uptoddDialogs: UpToddDialogs


    private lateinit var viewModel: BookASessionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        uptoddDialogs = UpToddDialogs(requireContext())
//        showLoadingDialog()

        val binding: BookASessionFragmentBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.book_a_session_fragment,
            container,
            false
        )
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(BookASessionViewModel::class.java)
        binding.bookASessionBinding = viewModel

        viewModel.sharedPreferences = requireContext().getSharedPreferences("REFERRAL", Context.MODE_PRIVATE)

        initializeSpinner(binding)

        viewModel.isLoading.observe(viewLifecycleOwner,{
            when(it){
                1 -> {
                    Log.i("error", "l59")
                    uptoddDialogs.showUploadDialog()
                }
                0 -> {
                    Log.i("error", "l63")
                    uptoddDialogs.dismissDialog()
                    uptoddDialogs.showDialog(R.drawable.gif_done, "Thank you for your referral.", "OK", object: UpToddDialogs.UpToddDialogListener{
                        override fun onDialogButtonClicked(dialog: Dialog) {
                            uptoddDialogs.dismissDialog()
                            findNavController().navigateUp()
                        }
                    })
                }
                -1 ->{
                    Log.i("error", "l73")
                    uptoddDialogs.dismissDialog()
                    uptoddDialogs.showDialog(R.drawable.network_error, "An error has occurred: ${viewModel.apiError}.", "Close", object: UpToddDialogs.UpToddDialogListener{
                        override fun onDialogButtonClicked(dialog: Dialog) {
                            uptoddDialogs.dismissDialog()
                            findNavController().navigateUp()
                        }
                    })
                }
                -2 ->{
                    Log.i("error", "l83")
                    uptoddDialogs.dismissDialog()
                    uptoddDialogs.showDialog(R.drawable.network_error, "Error: ${viewModel.variableError}", "Retry", object: UpToddDialogs.UpToddDialogListener{
                        override fun onDialogButtonClicked(dialog: Dialog) {
                            uptoddDialogs.dismissDialog()
                            viewModel.resetLoading()
                        }
                        override fun onDialogReady(dialog: Dialog, dialogText: TextView, dialogButton: Button, dialogGIF: GifImageView) {
                            dialog.setCancelable(true)
                        }
                    })
                }
                else -> {

                }
            }
        })

        return binding.root
    }

    private fun initializeSpinner(binding: BookASessionFragmentBinding) {
        val babyAgeSpinnerItems: Array<String> = arrayOf(
            "",
            "Pre Birth", "0 months",
            "1 months", "2 months",
            "3 months", "4 months",
            "5 months", "6 months",
            "7 months", "8 months",
            "9 months", "10 months",
            "11 months", "12 months",
            "13 months", "14 months",
            "15 months", "16 months",
            "17 months", "18 months",
            "19 months", "20 months",
            "21 months", "22 months",
            "23 months", "24 months",
            "25 months", "26 months",
            "27 months", "28 months",
            "29 months", "30 months",
            "31 months", "32 months",
            "33 months", "34 months",
            "35 months", "36 months"
            )
        val babyAgeSpinnerAdapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            babyAgeSpinnerItems
        )
        binding.bookASessionBabyAge.adapter = babyAgeSpinnerAdapter
        binding.bookASessionBabyAge.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.babyAge.value = when (position) {
                    1 -> "Pre Birth"
                    2 -> "0 months"
                    3 -> "1 months"
                    4 -> "2 months"
                    5 -> "3 months"
                    6 -> "4 months"
                    7 -> "5 months"
                    8 -> "6 months"
                    9 -> "7 months"
                    10 -> "8 months"
                    11 -> "9 months"
                    12 -> "10 months"
                    13 -> "11 months"
                    14 -> "12 months"
                    15 -> "13 months"
                    16 -> "14 months"
                    17 -> "15 months"
                    18 -> "16 months"
                    19 -> "17 months"
                    20 -> "18 months"
                    21 -> "19 months"
                    22 -> "20 months"
                    23 -> "21 months"
                    24 -> "22 months"
                    25 -> "23 months"
                    26 -> "24 months"
                    27 -> "25 months"
                    28 -> "26 months"
                    29 -> "27 months"
                    30 -> "28 months"
                    31 -> "29 months"
                    32 -> "30 months"
                    33 -> "31 months"
                    34 -> "32 months"
                    35 -> "33 months"
                    36 -> "34 months"
                    37 -> "35 months"
                    38 -> "36 months"
                    else -> ""
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.babyAge.value = ""
            }
        }


//        val babyGenderSpinnerItems: Array<String> = arrayOf("", "Boy", "Girl")
//        val babyGenderSpinnerAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
//            requireContext(),
//            android.R.layout.simple_spinner_dropdown_item,
//            babyGenderSpinnerItems
//        )
//        binding.bookASessionBabyGender.adapter = babyGenderSpinnerAdapter
//        binding.bookASessionBabyGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                viewModel.babyGender.value = when (position) {
//                    1 -> "M"
//                    2 -> "F"
//                    else -> ""
//                }
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//                viewModel.babyGender.value = ""
//            }
//        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(BookASessionViewModel::class.java)
    }

}