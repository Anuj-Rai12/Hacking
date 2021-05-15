package com.uptodd.uptoddapp.support.all.support

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.support.Ticket
import com.uptodd.uptoddapp.databinding.SupportTeamFragmentBinding
import com.uptodd.uptoddapp.support.all.AllTicketsFragmentDirections
import com.uptodd.uptoddapp.support.all.AllTicketsViewModel
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import java.util.*

class SupportTeam : Fragment() {

    companion object {
        fun newInstance() = SupportTeam()
    }

    private lateinit var viewModel: AllTicketsViewModel
    private lateinit var uptoddDialogs: UpToddDialogs


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        uptoddDialogs = UpToddDialogs(requireContext())


        val binding: SupportTeamFragmentBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.support_team_fragment,
            container,
            false
        )
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(AllTicketsViewModel::class.java)
        binding.supportTeamBinding = viewModel


        viewModel.isLoading.observe(viewLifecycleOwner, {
            it.let{
                when(it){
                    0 -> {
                        viewModel.sortArray("Support")
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
                }
            }
        })

        binding.generateNewTicket.setOnClickListener {
            findNavController().navigate(AllTicketsFragmentDirections.actionAllTicketsFragmentToCreateTicketFragment("Support"))
        }

        viewModel.tickets.observe(viewLifecycleOwner, {
            addTicketsOnScreen(it, binding.ticketListLayout)
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.i("support", "Support team fragment")
        viewModel.getAllTickets()

    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AllTicketsViewModel::class.java)
    }

    private fun addTicketsOnScreen(ticketList: ArrayList<Ticket>, ticketListLayout: LinearLayout) {
        if(ticketList.isNotEmpty()) {
            ticketListLayout.removeAllViews()
            ticketList.forEachIndexed { index, ticket ->
                if (ticket.type == "Support") {
                    val inflater =
                        requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val ticketView = inflater.inflate(R.layout.ticket_list_item, null)

                    val ticketItem: ConstraintLayout =
                        ticketView.findViewById(R.id.ticket_item_ticket_layout)
                    val ticketItemDetailsLayout: ConstraintLayout =
                        ticketView.findViewById(R.id.ticket_item_details_layout)
                    val ticketItemDetails: TextView =
                        ticketView.findViewById(R.id.ticket_item_details)
                    val ticketItemTitle: TextView = ticketView.findViewById(R.id.ticket_item_title)
                    val ticketItemNumber: TextView =
                        ticketView.findViewById(R.id.ticket_item_number)
                    val ticketItemStatus: TextView =
                        ticketView.findViewById(R.id.ticket_item_status)
                    val ticketItemDate: TextView = ticketView.findViewById(R.id.ticket_item_date)
                    val ticketItemRate: TextView = ticketView.findViewById(R.id.ticket_item_rate)

                    ticketItemTitle.text = ticket.subject
                    ticketItemNumber.text = ticket.ticketNumber
                    ticketItemDate.text = getDateFromTime(ticket.time)
                    when (ticket.status) {
                        1 -> {
                            ticketItemStatus.text = "open"
                            ticketItemStatus.setTextColor(resources.getColor(R.color.ticket_open))
                            ticketItemRate.setTextColor(resources.getColor(R.color.ticket_closed))
                            ticketItemRate.setOnClickListener {}
                        }
                        0 -> {
                            ticketItemStatus.text = "closed"
                            if (ticket.rating == -1) {
                                ticketItemRate.setOnClickListener { getRating(ticket, index) }
                            } else {
                                ticketItemRate.setTextColor(resources.getColor(R.color.ticket_rated))
                                ticketItemRate.setOnClickListener {}
                            }
                        }
                    }

                    ticketItemDetails.setOnClickListener {
                        if (ticketItemDetailsLayout.visibility == View.VISIBLE) {
                            ticketItemDetailsLayout.visibility = View.GONE
                        } else {
                            ticketItemDetailsLayout.visibility = View.VISIBLE
                        }
                    }

                    ticketItem.setOnClickListener {
                        findNavController().navigate(
                            AllTicketsFragmentDirections.actionAllTicketsFragmentToViewTicket(
                                ticket,
                                ticket.type
                            )
                        )
                    }

                    ticketListLayout.addView(ticketView)
                }
            }
        }
    }

    private fun getDateFromTime(time: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        return "" + cal.get(Calendar.DAY_OF_MONTH).toString() + "/" + cal.get(Calendar.MONTH).toString() + "/" + cal.get(
            Calendar.YEAR)

    }

    @SuppressLint("SetTextI18n")
    private fun getRating(ticket: Ticket, index: Int) {
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
        dialog.findViewById<Button>(R.id.rate_dialog_not_now).visibility = View.GONE
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
                submit.setTextColor(resources.getColor(R.color.white))
                submit.setBackgroundResource(R.drawable.round_button_new_ticket)
                submit.isClickable = true
                submit.setOnClickListener {
                    if(rating.value!=0){
                        ticket.rating = rating.value!!
                        viewModel.sendRating(ticket, index, message.text.toString())
                        dialog.dismiss()
                    }
                }
            }
        })
        dialog.show()
    }

}