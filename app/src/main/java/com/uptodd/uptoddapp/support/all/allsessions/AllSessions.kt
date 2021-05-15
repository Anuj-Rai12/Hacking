package com.uptodd.uptoddapp.support.all.allsessions


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.support.Sessions
import com.uptodd.uptoddapp.databinding.AllSessionsFragmentBinding
import com.uptodd.uptoddapp.support.all.AllTicketsFragmentDirections
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import java.util.*

class AllSessions : Fragment() {

    companion object {
        fun newInstance() = AllSessions()
    }

    private lateinit var viewModel: AllSessionsViewModel
    private lateinit var uptoddDialogs: UpToddDialogs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ChangeLanguage(requireContext()).setLanguage()

        uptoddDialogs = UpToddDialogs(requireContext())

        val binding: AllSessionsFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.all_sessions_fragment, container, false)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(AllSessionsViewModel::class.java)
        binding.allSessionsBinding = viewModel

        viewModel.getAllSessions()

        binding.allSessionsBookASession.setOnClickListener {
            findNavController().navigate(AllTicketsFragmentDirections.actionAllTicketsFragmentToBookASlot())
        }

        viewModel.sessions.observe(viewLifecycleOwner, {
            addSessionsOnScreen(it, binding.allSessionsListLayout)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, {
            it.let{
                when(it){
                    0 -> {
                        uptoddDialogs.dismissDialog()
                    }
                    1 -> {
                        uptoddDialogs.showLoadingDialog(findNavController())
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

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.i("support", "All sessions fragment")
        viewModel.getAllSessions()
    }



    @SuppressLint("SetTextI18n")
    private fun addSessionsOnScreen(sessions: ArrayList<Sessions>, sessionListLayout: LinearLayout) {
        if(sessions.isNotEmpty()) {
            sessionListLayout.removeAllViews()
            sessions.forEachIndexed { index, session ->
                if (session.sessionTopic != null) {
                    val inflater =
                        requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val sessionView = inflater.inflate(R.layout.session_list_item, null)

                    val sessionItem: ConstraintLayout =
                        sessionView.findViewById(R.id.session_item_session_layout)
                    val sessionItemDetailsLayout: ConstraintLayout =
                        sessionView.findViewById(R.id.session_item_details_layout)
                    val sessionItemDetails: TextView =
                        sessionView.findViewById(R.id.session_item_details)
                    val sessionItemTitle: TextView =
                        sessionView.findViewById(R.id.session_item_title)
                    val sessionItemNumber: TextView =
                        sessionView.findViewById(R.id.session_item_number)
                    val sessionItemSessionBy: TextView =
                        sessionView.findViewById(R.id.session_item_session_by)
                    val sessionItemDate: TextView = sessionView.findViewById(R.id.session_item_date)
                    val sessionItemRate: TextView = sessionView.findViewById(R.id.session_item_rate)
                    val sessionItemStatus: TextView =
                        sessionView.findViewById(R.id.session_item_status)


                    sessionItemTitle.text = session.sessionTopic
                    sessionItemNumber.text = session.id.toString()
                    sessionItemDate.text = getDateFromTime(session.sessionBookingDateValue)
                    sessionItemSessionBy.text = session.expertName

                    sessionItemDetails.setOnClickListener {
                        if (sessionItemDetailsLayout.visibility == View.VISIBLE) {
                            sessionItemDetailsLayout.visibility = View.GONE
                        } else {
                            sessionItemDetailsLayout.visibility = View.VISIBLE
                        }
                    }
                    when (session.sessionStatus) {
                        1 -> {
                            sessionItemStatus.text = "Booked"
                            sessionItemRate.setTextColor(resources.getColor(R.color.ticket_closed))
                            sessionItemRate.setOnClickListener {}
                        }
                        0 -> {
                            sessionItemRate.setTextColor(resources.getColor(R.color.ticket_open))
                            sessionItemStatus.text = "Completed"
                            if (session.sessionRating == -1) {
                                sessionItemRate.setOnClickListener { getRating(session, index) }
                            } else {
                                sessionItemRate.setTextColor(resources.getColor(R.color.ticket_rated))
                                sessionItemRate.setOnClickListener {}
                            }
                        }
                    }
//                sessionItemRate.setOnClickListener {
//                    getRating(session, index)
//                }

                    sessionListLayout.addView(sessionView)
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
    private fun getRating(session: Sessions, index: Int) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.rating_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val title: TextView = dialog.findViewById(R.id.rate_dialog_title)
        title.text = "How would you like to rate this session?"
        val rating1: ImageButton = dialog.findViewById(R.id.rating_1)
        val rating2: ImageButton = dialog.findViewById(R.id.rating_2)
        val rating3: ImageButton = dialog.findViewById(R.id.rating_3)
        val rating4: ImageButton = dialog.findViewById(R.id.rating_4)
        val rating5: ImageButton = dialog.findViewById(R.id.rating_5)
        val message: EditText = dialog.findViewById(R.id.rate_dialog_feedback)
        val submit: Button = dialog.findViewById(R.id.rate_dialog_submit)
        dialog.findViewById<Button>(R.id.rate_dialog_not_now).visibility = View.GONE
        submit.gravity = Gravity.CENTER
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
                submit.isFocusable = true
                submit.isFocusableInTouchMode = true
            }
        })

        submit.setOnClickListener {
            if(rating.value!=0){
                session.sessionRating = rating.value!!
                viewModel.rateSession(session, message.text.toString(), index)
                dialog.dismiss()
                Toast.makeText(requireContext(), "Thank you for your rating!", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AllSessionsViewModel::class.java)
    }

}