package com.uptodd.uptoddapp.support.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.support.Ticket
import com.uptodd.uptoddapp.databinding.ViewTicketFragmentBinding
import com.uptodd.uptoddapp.support.all.AllTicketsViewModel
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.UpToddDialogs

@SuppressLint("SetTextI18n")
class ViewTicket : Fragment() {

    companion object {
        fun newInstance() = ViewTicket()
    }

    private lateinit var viewModel: ViewTicketViewModel
    private lateinit var uptoddDialogs: UpToddDialogs
    private lateinit var ticket: Ticket
    private lateinit var menu: Menu

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        uptoddDialogs = UpToddDialogs(requireContext())

        val binding: ViewTicketFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.view_ticket_fragment, container, false)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(ViewTicketViewModel::class.java)
        binding.viewTicketBinding = viewModel

        val args = ViewTicketArgs.fromBundle(requireArguments())
        ticket=args.ticket
        Log.d("type",args.supportType);
        binding.ticketViewTicketNumber.text = "Ticket Number: " + ticket.ticketNumber

        val adapter = TicketMessagesAdapter()
            adapter.isExpert=args.supportType!="Support"
        binding.ticketViewMessages.adapter = adapter

        setHasOptionsMenu(true)


        viewModel.getAllMessages(ticket)

        initializeObservers(binding, adapter)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        inflater.inflate(R.menu.close_ticket, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.close_ticket_close) {
            AlertDialog.Builder(requireContext())
                .setTitle("Close ticket")
                .setMessage("Are you sure you want to close this ticket?")
                .setPositiveButton("Yes") { di, _ ->
                    getRating()
                    di.dismiss()
                }
                .setNegativeButton("No"){di,_->
                    di.dismiss()
                }
                .show()
            return true
        }
        else
            return super.onOptionsItemSelected(item)
    }

    private fun initializeObservers(binding: ViewTicketFragmentBinding, adapter: TicketMessagesAdapter) {
        viewModel.isLoading.observe(viewLifecycleOwner, {
            it.let {
                when (it) {
                    0 -> {
                        uptoddDialogs.dismissDialog()
                        initializeBindings(binding)
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
                    20 ->{
                        uptoddDialogs.dismissDialog()
                        uptoddDialogs.showDialog(R.drawable.gif_done, "Your ticket has been closed.", "Close", object: UpToddDialogs.UpToddDialogListener{
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                uptoddDialogs.dismissDialog()
                            }
                        })
                    }
                    21 -> {
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

        viewModel.apiCalled.observe(viewLifecycleOwner, {
            it.let{
                if(it){
                    viewModel.saveData()
                }
            }
        })

        viewModel.messages.observe(viewLifecycleOwner, { arrayList ->
            if (arrayList != null) {
                adapter.submitList(arrayList)
                adapter.notifyDataSetChanged()
                binding.ticketViewMessages.scrollToPosition(adapter.itemCount-1)
            }
        })

        viewModel.reopen.observe(viewLifecycleOwner, {
            it.let {
                if (it) {
                    ticket.status = 1
                    initializeBindings(binding)
                    viewModel.updateReopen()
                }
            }
        })

        viewModel.close.observe(viewLifecycleOwner, {
            it.let {
                if (it) {
                    ticket.status = 0
                    initializeBindings(binding)
                    viewModel.updateClose()
                }
            }
        })

    }

    private fun initializeBindings(binding: ViewTicketFragmentBinding) {
        when(ticket.status){
            1 ->{
                binding.ticketViewNewMessage.visibility=View.VISIBLE
                binding.ticketViewMessageSend.visibility = View.VISIBLE
                binding.ticketViewReopenTicket.visibility=View.GONE
                binding.statusShow.setBackgroundResource(R.drawable.open_status_bg)
                binding.statusShow.text = "Open"
                binding.editBorder.visibility=View.VISIBLE
                menu.findItem(R.id.close_ticket_close).isVisible = true
            }
            0 ->{
                binding.ticketViewNewMessage.visibility=View.GONE
                binding.ticketViewMessageSend.visibility = View.GONE
                binding.ticketViewReopenTicket.visibility=View.VISIBLE
                binding.editBorder.visibility=View.GONE
                binding.statusShow.setBackgroundResource(R.drawable.close_status_bg)
                binding.statusShow.text = "Closed"
                binding.ticketViewReopenTicket.setOnClickListener {
                    viewModel.reopenTicket("Reopen request on: ${AllUtil.getTimeFromMillis(System.currentTimeMillis())}", ticket)
                }
                menu.findItem(R.id.close_ticket_close).isVisible = false
            }
        }

        binding.ticketViewMessageSend.setOnClickListener {
            val message= binding.ticketViewNewMessage.text.toString()
            if(message.trim().isNotEmpty()) {
                viewModel.sendMessage(message, ticket)
                binding.ticketViewNewMessage.text.clear()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ViewTicketViewModel::class.java)
    }

    private fun getRating() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.rating_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rating1: ImageButton = dialog.findViewById(R.id.rating_1)
        val rating2: ImageButton = dialog.findViewById(R.id.rating_2)
        val rating3: ImageButton = dialog.findViewById(R.id.rating_3)
        val rating4: ImageButton = dialog.findViewById(R.id.rating_4)
        val rating5: ImageButton = dialog.findViewById(R.id.rating_5)
        val message: EditText = dialog.findViewById(R.id.rate_dialog_feedback)
        val submit: Button = dialog.findViewById(R.id.rate_dialog_submit)
        val notNow: Button= dialog.findViewById(R.id.rate_dialog_not_now)
        notNow.visibility = View.VISIBLE
        notNow.setOnClickListener {
            dialog.dismiss()
            viewModel.justCloseTicket(ticket.ticketNumber)
        }
        val rating = MutableLiveData<Int>()
        rating.value = 0
        rating1.setOnClickListener {
            rating1.setImageResource(R.drawable.rate_star_full)
            rating2.setImageResource(R.drawable.rate_star_empty)
            rating3.setImageResource(R.drawable.rate_star_empty)
            rating4.setImageResource(R.drawable.rate_star_empty)
            rating5.setImageResource(R.drawable.rate_star_empty)
            rating.value=1
        }
        rating2.setOnClickListener {
            rating1.setImageResource(R.drawable.rate_star_full)
            rating2.setImageResource(R.drawable.rate_star_full)
            rating3.setImageResource(R.drawable.rate_star_empty)
            rating4.setImageResource(R.drawable.rate_star_empty)
            rating5.setImageResource(R.drawable.rate_star_empty)
            rating.value=2
        }
        rating3.setOnClickListener {
            rating1.setImageResource(R.drawable.rate_star_full)
            rating2.setImageResource(R.drawable.rate_star_full)
            rating3.setImageResource(R.drawable.rate_star_full)
            rating4.setImageResource(R.drawable.rate_star_empty)
            rating5.setImageResource(R.drawable.rate_star_empty)
            rating.value=3
        }
        rating4.setOnClickListener {
            rating1.setImageResource(R.drawable.rate_star_full)
            rating2.setImageResource(R.drawable.rate_star_full)
            rating3.setImageResource(R.drawable.rate_star_full)
            rating4.setImageResource(R.drawable.rate_star_full)
            rating5.setImageResource(R.drawable.rate_star_empty)
            rating.value=4
        }
        rating5.setOnClickListener {
            rating1.setImageResource(R.drawable.rate_star_full)
            rating2.setImageResource(R.drawable.rate_star_full)
            rating3.setImageResource(R.drawable.rate_star_full)
            rating4.setImageResource(R.drawable.rate_star_full)
            rating5.setImageResource(R.drawable.rate_star_full)
            rating.value=5
        }
        rating.observe(viewLifecycleOwner, {
            if(it!=0){
                submit.alpha = 1f
                submit.isClickable = true
                submit.setOnClickListener {
                    if(rating.value!=0){
                        ticket.rating = rating.value!!
                        viewModel.closeTicketAndSubmitRating(ticket, message.text.toString())
                        dialog.dismiss()
                    }
                }
            }
            else{
                submit.alpha = 0.4f
                submit.isClickable = false
                submit.setOnClickListener {
                }
            }
        })
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
       var ticketViewModel = ViewModelProvider(this).get(AllTicketsViewModel::class.java)
        ticketViewModel.getAllTickets()
    }

}