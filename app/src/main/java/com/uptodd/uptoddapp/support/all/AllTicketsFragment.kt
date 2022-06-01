package com.uptodd.uptoddapp.support.all

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.transition.MaterialSharedAxis
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.support.Ticket
import com.uptodd.uptoddapp.databinding.AllTicketsFragmentBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.models.VideosUrlResponse
import com.uptodd.uptoddapp.ui.webinars.podcastwebinar.PodcastWebinarActivity
import com.uptodd.uptoddapp.utilities.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AllTicketsFragment : Fragment() {

    companion object {
        fun newInstance() = AllTicketsFragment()
    }

    private var videosRespons: VideosUrlResponse? = null

    private lateinit var viewModel: AllTicketsViewModel
    private lateinit var binding: AllTicketsFragmentBinding
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
        ChangeLanguage(requireContext()).setLanguage()

        uptoddDialogs = UpToddDialogs(requireContext())

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.all_tickets_fragment,
            container,
            false
        )
        binding?.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(AllTicketsViewModel::class.java)
        binding?.allTicketsBinding = viewModel

        ToolbarUtils.initToolbar(
            requireActivity(), binding.collapseToolbar!!,
            findNavController(), getString(R.string.support), "Parenting Tools for You",
            R.drawable.support_icon
        )

        fetchTutorials(requireContext())

        binding?.collapseToolbar?.playTutorialIcon?.setOnClickListener {

            fragmentManager?.let { it1 ->
                val intent = Intent(context, PodcastWebinarActivity::class.java)
                intent.putExtra("url", videosRespons?.support)
                intent.putExtra("title", "Support")
                intent.putExtra("kit_content", "")
                intent.putExtra("description", "")
                startActivity(intent)
            }


        }

        binding?.collapseToolbar?.playTutorialIcon?.visibility = View.VISIBLE

        val end = SimpleDateFormat("yyyy-MM-dd").parse(
            UptoddSharedPreferences.getInstance(requireContext()).getAppExpiryDate()
        )
        if (!AllUtil.isUserPremium(requireContext())) {
            val upToddDialogs = UpToddDialogs(requireContext())
            upToddDialogs.showInfoDialog("24*7 Support is only for Premium Subscribers", "Close",
                object : UpToddDialogs.UpToddDialogListener {
                    override fun onDialogButtonClicked(dialog: Dialog) {
                        dialog.dismiss()
                    }

                    override fun onDialogDismiss() {
                        view?.findNavController()?.navigateUp()
                    }

                }
            )
        } else if (AllUtil.isSubscriptionOver(end)) {
            val upToddDialogs = UpToddDialogs(requireContext())
            upToddDialogs.showInfoDialog("24*7 Support is only for Premium Subscribers", "Close",
                object : UpToddDialogs.UpToddDialogListener {
                    override fun onDialogButtonClicked(dialog: Dialog) {
                        dialog.dismiss()
                    }

                    override fun onDialogDismiss() {
                        view?.findNavController()?.navigateUp()
                    }

                }
            )
        } else {
            viewModel.init()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            it.let {
                when (it) {
                    0 -> {
                        uptoddDialogs.dismissDialog()
                    }
                    1 -> {
                        if (AllUtil.isUserPremium(requireContext()))
                            uptoddDialogs.showLoadingDialog(findNavController())
                        else {

                        }

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
        }


        if (UptoddSharedPreferences.getInstance(requireContext()).shouldShowSupportTip()) {
            ShowInfoDialog.showInfo(getString(R.string.screen_support), requireFragmentManager())
            UptoddSharedPreferences.getInstance(requireContext()).setShownSupportTip(false)
        }

        binding.generateSupportTicket.setOnClickListener {
            findNavController().navigate(
                AllTicketsFragmentDirections.actionAllTicketsFragmentToCreateTicketFragment(
                    "Support"
                )
            )
        }

        binding.generateExpertTicket.setOnClickListener {
            findNavController().navigate(
                AllTicketsFragmentDirections.actionAllTicketsFragmentToCreateTicketFragment(
                    "Expert Suggestion"
                )
            )
        }

        viewModel.allTickets.observe(viewLifecycleOwner, {
            addTicketsOnScreen(it.asReversed(), binding.ticketListLayout)
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.i("support", "All tickets fragment")
        viewModel.getAllTickets()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AllTicketsViewModel::class.java)
    }

    fun fetchTutorials(context: Context) {
        AndroidNetworking.get("https://uptodd.com/api/featureTutorials?userId=${AllUtil.getUserId()}")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    val data = response?.get("data") as JSONObject
                    videosRespons = AllUtil.getVideosUrlResponse(data.toString())
                }

                override fun onError(anError: ANError?) {

                }

            })
    }

    private fun addTicketsOnScreen(ticketList: MutableList<Ticket>, ticketListLayout: LinearLayout) {
        if (ticketList.isNotEmpty()) {
            ticketListLayout.removeAllViews()
            ticketList.forEachIndexed { _, ticket ->
                val inflater =
                    requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val ticketView = inflater.inflate(R.layout.ticket_list_item, null)

                val ticketItem: ConstraintLayout =
                    ticketView.findViewById(R.id.ticket_item_ticket_layout)

                val ticketItemDetails: TextView = ticketView.findViewById(R.id.ticket_item_details)
                val statusButton = ticketView.findViewById<AppCompatButton>(R.id.status_show)
                val ticketItemTitle: TextView = ticketView.findViewById(R.id.ticket_item_title)
                val ticketType: TextView = ticketView.findViewById(R.id.type_ticket)

                ticketItemTitle.text = ticket.subject
                when (ticket.status) {
                    1 -> {
                        statusButton.text = "Open"
                        statusButton.setBackgroundResource(R.drawable.open_status_bg)
                    }
                    0 -> {
                        statusButton.text = "Closed"
                        statusButton.setBackgroundResource(R.drawable.close_status_bg)
                    }
                }

                ticketItemDetails.text= getDateFromTime(ticket.time)

                if(ticket.type.equals("Expert Suggestion")){
                    ticketType.text = "Expert"
                }else{
                    ticketType.text = "Support"
                }
                //ticketType.setBackgroundResource(R.drawable.ticket_type_bg)

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

    private fun getDateFromTime(time: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        return "" + cal.get(Calendar.DAY_OF_MONTH).toString() + "/" + cal.get(Calendar.MONTH).toString() + "/" + cal.get(
            Calendar.YEAR)
    }

}