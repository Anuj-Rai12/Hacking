package com.uptodd.uptoddapp.support.create

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.CreateTicketFragmentBinding
import com.uptodd.uptoddapp.utilities.UpToddDialogs


class CreateTicketFragment : Fragment() {

    companion object {
        fun newInstance() = CreateTicketFragment()
    }

    private lateinit var viewModel: CreateTicketViewModel
    private lateinit var uptoddDialogs: UpToddDialogs

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        uptoddDialogs = UpToddDialogs(requireContext())

        val binding: CreateTicketFragmentBinding = DataBindingUtil.inflate(inflater,R.layout.create_ticket_fragment, container, false)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(CreateTicketViewModel::class.java)
        binding.createTicketBinding = viewModel

        val args = CreateTicketFragmentArgs.fromBundle(requireArguments())
        val supportType = args.ticketSupportTeam
        setUpTicketLayout(binding, supportType)


        viewModel.isLoading.observe(viewLifecycleOwner,{
            when(it){
                1 -> {
                    uptoddDialogs.showUploadDialog()
                }
                2 -> {
                    uptoddDialogs.dismissDialog()
                    uptoddDialogs.showDialog(R.drawable.gif_done, "Your ticket has been generated.", "OK", object: UpToddDialogs.UpToddDialogListener{
                        override fun onDialogButtonClicked(dialog: Dialog) {
                            uptoddDialogs.dismissDialog()
                            findNavController().popBackStack(R.id.homePageFragment, false)
                        }
                    })
                }
                -1 ->{
                    uptoddDialogs.dismissDialog()
                    uptoddDialogs.showDialog(R.drawable.network_error, "An error has occurred: ${viewModel.apiError}.", "Close", object: UpToddDialogs.UpToddDialogListener{
                        override fun onDialogButtonClicked(dialog: Dialog) {
                            uptoddDialogs.dismissDialog()
                            findNavController().navigateUp()
                        }
                    })
                }
                -2 ->{
                    uptoddDialogs.dismissDialog()
                    uptoddDialogs.showDialog(R.drawable.network_error, "An unknown error has occurred! Please try again later.", "Close", object: UpToddDialogs.UpToddDialogListener{
                        override fun onDialogButtonClicked(dialog: Dialog) {
                            uptoddDialogs.dismissDialog()
                            findNavController().navigateUp()
                        }
                    })
                }
            }
        })

        return binding.root
    }

    private fun setUpTicketLayout(binding: CreateTicketFragmentBinding, supportType: String) {
        binding.ticketNewSubmit.setOnClickListener {
            viewModel.submitNewTicket(binding.ticketNewTitle.text.toString(), binding.ticketNewMessage.text.toString(),supportType)
        }
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CreateTicketViewModel::class.java)

    }

}