package com.uptodd.uptoddapp.support.all.allsessions.bookaslot

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.BookASlotFragmentBinding
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*

class BookASlot : Fragment() {

    companion object {
        fun newInstance() = BookASlot()
    }

    private lateinit var viewModel: BookASlotViewModel
    private lateinit var uptoddDialogs: UpToddDialogs

    var selectedDate = MutableLiveData<LocalDate>()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        uptoddDialogs = UpToddDialogs(requireContext())

        val binding: BookASlotFragmentBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.book_a_slot_fragment,
            container,
            false
        )
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(BookASlotViewModel::class.java)
        binding.bookASlotBinding = viewModel

        uptoddDialogs.dismissDialog()
        binding.bookASlotBookASession.setOnClickListener {
            if(selectedDate.value!=null)
                findNavController().navigate(BookASlotDirections.actionBookASlotToSlotTiming(viewModel.expertName.value!!, viewModel.expertIdValue.value!!, selectedDate.value.toString()))
        }

        viewModel.isLoading.observe(viewLifecycleOwner, {
            it.let{
                when(it){
                    0 -> {
                        uptoddDialogs.dismissDialog()
                        setUpSpinner(binding)
                        viewModel.resetState()
                    }
                    1 -> {
                        uptoddDialogs.showLoadingDialog(findNavController())
                        viewModel.resetState()
                    }
                    2 ->{
                        uptoddDialogs.dismissDialog()
                        initializeCalendarView(binding)
                        viewModel.resetState()
                    }
                    -1 ->{
                        uptoddDialogs.dismissDialog()
                        uptoddDialogs.showDialog(R.drawable.network_error, "An error has occurred: ${viewModel.apiError}", "Close", object: UpToddDialogs.UpToddDialogListener{
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                uptoddDialogs.dismissDialog()
                                findNavController().navigateUp()
                            }
                        })
                        viewModel.resetState()
                    }
                    else ->{}

                }
            }
        })

        selectedDate.observe(viewLifecycleOwner, {
            if(it==null){
//                binding.bookASlotBookASession.setBackgroundResource(R.drawable.round_button_not_yet)
                binding.bookASlotBookASession.alpha = 0.4f
                binding.bookASlotBookASession.isClickable = false
            }
            else{
//                binding.bookASlotBookASession.setBackgroundResource(R.drawable.round_button_book_a_session)
                binding.bookASlotBookASession.alpha = 1f
                binding.bookASlotBookASession.isClickable = true
            }
        })

        return binding.root
    }

    private fun setUpSpinner(binding: BookASlotFragmentBinding) {

        val dropdown: Spinner = binding.bookASlotDoctorName
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            viewModel.mStringArray
        )
        dropdown.adapter = adapter
        dropdown.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.expertName.value = viewModel.expertList[position]
                viewModel.expertIdValue.value = viewModel.expertId[position]
                if(position == 0){
                    viewModel.expertName.value="Any"
                    viewModel.getAllExpertDates()
                }
                else{
                    viewModel.getSpecificExpertAvailability(viewModel.expertId[position])
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { viewModel.expertName.value = "Any" }
        }
    }

    private fun initializeCalendarView(binding: BookASlotFragmentBinding) {
        val calendarView = binding.calendarView
        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(0)
        val lastMonth = currentMonth.plusMonths(1)

        val today = LocalDate.now()

        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: CalendarDay
            val textView = view.findViewById<TextView>(R.id.calendarDayText)

            init {
                textView.setOnClickListener {
                    if(!day.date.isBefore(today) && viewModel.availableDates.contains(day.date)) {
                        if (day.owner == DayOwner.THIS_MONTH) {
                            if (selectedDate.value == day.date) {
                                selectedDate.value = null
                                binding.calendarView.notifyDayChanged(day)
                            } else {
                                val oldDate = selectedDate.value
                                selectedDate.value = day.date
                                binding.calendarView.notifyDateChanged(day.date)
                                if(oldDate!=null){ binding.calendarView.notifyDateChanged(oldDate) }
                            }
                        }
                    }
                }
            }
        }


        calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.textView.text = day.date.dayOfMonth.toString()
                container.day = day
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()
                if (day.owner == DayOwner.THIS_MONTH) {
                    when{
                        day.date==selectedDate.value -> {
                            textView.setTextColor(resources.getColor(R.color.white))
                            textView.setBackgroundResource(R.drawable.date_selected)
                        }
                        day.date.isBefore(today) ->{
                            textView.setTextColor(resources.getColor(R.color.ticket_closed))
                            textView.background = null
                        }
                        day.date==today -> {
                            textView.setTextColor(resources.getColor(R.color.white))
                            textView.setBackgroundResource(R.drawable.date_today)
                        }
                        viewModel.availableDates.contains(day.date) ->{
                            textView.setTextColor(resources.getColor(R.color.ticket_open))
                            textView.background = null
                        }
                        else -> {
                            textView.setTextColor(resources.getColor(R.color.black))
                            textView.background = null
                        }
                    }
                } else {
                    textView.setTextColor(resources.getColor(R.color.ticket_closed))
                    textView.background = null
                }
            }
        }
        binding.legendLayout.children.forEachIndexed { index, view ->
            (view as TextView).apply {
                text = daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                    .toUpperCase(Locale.ENGLISH)
            }
        }

        calendarView.orientation = LinearLayout.HORIZONTAL

        calendarView.monthScrollListener = {
            Log.d("div","BookASlot L226 ${it.month} ${it.year} ${it.yearMonth}")
            binding.bookASlotYear.text = it.yearMonth.year.toString()
            binding.bookASlotMonth.text = monthTitleFormatter.format(it.yearMonth)
        }

        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)


    }

    private fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        var daysOfWeek = DayOfWeek.values()
        // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
        // Only necessary if firstDayOfWeek != DayOfWeek.MONDAY which has ordinal 0.
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }
        return daysOfWeek
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(BookASlotViewModel::class.java)
    }

}