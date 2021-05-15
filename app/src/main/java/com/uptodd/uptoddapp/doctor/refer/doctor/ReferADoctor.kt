package com.uptodd.uptoddapp.doctor.refer.doctor

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ReferADoctorFragmentBinding
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import pl.droidsonroids.gif.GifImageView

class ReferADoctor : Fragment() {

    companion object {
        fun newInstance() = ReferADoctor()
    }

    private lateinit var uptoddDialogs: UpToddDialogs

    private lateinit var viewModel: ReferADoctorViewModel


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

        Log.i("debugger", "Created view")

        val binding: ReferADoctorFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.refer_a_doctor_fragment, container, false)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(ReferADoctorViewModel::class.java)
        binding.referDoctorBinding = viewModel

        setClickListeners(binding)

        initializeObservers()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val supportActionBar = (requireActivity() as AppCompatActivity).supportActionBar!!
        supportActionBar.setHomeButtonEnabled(true)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
    }

    private fun setClickListeners(binding: ReferADoctorFragmentBinding) {

        binding.referDoctorSeePreviousReferrals.setOnClickListener {
            findNavController().navigate(ReferADoctorDirections.actionReferADoctorToReferredListDoctor())
        }

    }

    private fun initializeObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner, {
            when (it) {
                1 -> {
                    uptoddDialogs.showUploadDialog()
                }
                0 -> {
                    uptoddDialogs.dismissDialog()
                    uptoddDialogs.showDialog(
                        R.drawable.gif_done,
                        "Thank you for your referral.",
                        "OK",
                        object : UpToddDialogs.UpToddDialogListener {
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                uptoddDialogs.dismissDialog()
                                findNavController().navigateUp()
                            }
                        })
                }
                -1 -> {
                    uptoddDialogs.dismissDialog()
                    uptoddDialogs.showDialog(
                        R.drawable.network_error,
                        "An error has occurred: ${viewModel.apiError}.",
                        "Close",
                        object : UpToddDialogs.UpToddDialogListener {
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                uptoddDialogs.dismissDialog()
                                findNavController().navigateUp()
                            }
                        })
                }
                -2 -> {
                    uptoddDialogs.dismissDialog()
                    uptoddDialogs.showDialog(
                        R.drawable.network_error,
                        "Error: ${viewModel.variableError}",
                        "Retry",
                        object : UpToddDialogs.UpToddDialogListener {
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                uptoddDialogs.dismissDialog()
                                viewModel.resetIsLoading()
                            }

                            override fun onDialogReady(
                                dialog: Dialog,
                                dialogText: TextView,
                                dialogButton: Button,
                                dialogGIF: GifImageView
                            ) {
                                dialog.setCancelable(true)
                            }
                        })
                }
                else -> {

                }
            }
        })

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ReferADoctorViewModel::class.java)
    }

}