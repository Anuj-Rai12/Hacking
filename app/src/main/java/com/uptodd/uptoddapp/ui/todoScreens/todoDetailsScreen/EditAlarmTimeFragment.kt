package com.uptodd.uptoddapp.ui.todoScreens.todoDetailsScreen

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.score.DAILY_TODO
import com.uptodd.uptoddapp.database.score.ESSENTIALS_TODO
import com.uptodd.uptoddapp.database.score.MONTHLY_TODO
import com.uptodd.uptoddapp.database.score.WEEKLY_TODO
import com.uptodd.uptoddapp.databinding.FragmentEditAlarmTimeBinding
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import kotlinx.android.synthetic.main.day_picker_layout.*
import kotlinx.android.synthetic.main.day_picker_layout.view.*
import java.util.*
import kotlin.collections.ArrayList

class EditAlarmTimeFragment : Fragment() {

    private lateinit var viewModel: EditAlarmViewModel
    private lateinit var binding: FragmentEditAlarmTimeBinding
    private var todoId: Int? = null

    private var alarmTime = ""
    private var alarmTimeInMilli: Long = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        initialiseBindingAndViewModel(inflater, container)
        disableAlarmDays()


        viewModel.todo.observe(viewLifecycleOwner, Observer { todo ->
            when (todo.type) {
                DAILY_TODO -> setupForDaily()
                WEEKLY_TODO -> setupForWeekly()
                MONTHLY_TODO -> setupForMontly()
                ESSENTIALS_TODO -> setupForEssential()
            }
            val timeString = todo.alarmTimeByUser.substringBeforeLast(':')
            viewModel.timeString.value = timeString
            binding.alarmTimeTextView.text = timeString
        })


        binding.btnSave.setOnClickListener {
            getDaysSelected()
            viewModel.saveAlarm()
            showLoadingDialog()
        }

        return binding.root
    }

    private fun setupForDaily() {
        enableAlarmTime()
        disableAlarmDays()
    }

    private fun setupForWeekly() {
        enableAlarmTime()
        enableAlarmDays()
        viewModel.todo.value?.let { todo ->
            binding.dayPicker.tM.isChecked = todo.weeklyMonday
            binding.dayPicker.tTu.isChecked = todo.weeklyTuesday
            binding.dayPicker.tW.isChecked = todo.weeklyWednesday
            binding.dayPicker.tTh.isChecked = todo.weeklyThursday
            binding.dayPicker.tF.isChecked = todo.weeklyFriday
            binding.dayPicker.tSa.isChecked = todo.weeklySaturday
            binding.dayPicker.tSu.isChecked = todo.weeklySunday
        }
    }

    private fun setupForMontly() {
        enableAlarmTime()
        disableAlarmDays()
        binding.monthlyAlarmDescription.visibility = View.VISIBLE

    }

    private fun setupForEssential() {
        enableAlarmTime()
        disableAlarmDays()
        binding.essentialsAlarmDescription.visibility = View.VISIBLE
    }

    private fun enableAlarmTime() {
        binding.alarmTimeTextView.apply {
            isEnabled = true
            setOnClickListener {
                getCalendar()
            }
        }

    }

    private fun disableAlarmDays() {
        binding.dayPicker.visibility = View.GONE
    }

    private fun disableAlarmTime() {
        binding.alarmTimeTextView.isEnabled = false
    }

    private fun enableAlarmDays() {

        binding.dayPicker.visibility = View.VISIBLE
//        binding.selectDaysTextView.isVisible = true
//        binding.selectDaysTextView.setOnClickListener {
//            openDaySelectionBottomSheet()
//        }
    }

    private fun initialiseBindingAndViewModel(inflater: LayoutInflater, container: ViewGroup?) {

        val application = requireNotNull(this.activity).application
        val arguments = TodoDetailsFragmentArgs.fromBundle(requireArguments())
        todoId = arguments.todoId
        val viewModelFactory =
            TodoDetailsViewModelFactory(application, arguments.todoId)

        viewModel = ViewModelProvider(
            this, viewModelFactory
        ).get(EditAlarmViewModel::class.java)

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_edit_alarm_time, container, false)
        binding.lifecycleOwner = this
        binding.editAlarmViewModel = viewModel
    }

    private fun getCalendar() {
        val cal: Calendar = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            alarmTime = formatAlarmTime(hour, minute)
            alarmTimeInMilli = cal.timeInMillis
            // set the time
            viewModel.setTimeSelected(alarmTimeInMilli, alarmTime)
            return@OnTimeSetListener
        }

        TimePickerDialog(
            this.requireContext(),
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()

    }

    private fun formatAlarmTime(hour: Int, minute: Int): String {
        return if (minute < 10 && hour > 10) "$hour:0$minute:00"
        else if (hour < 10 && minute > 10) "0$hour:$minute:00"
        else if (hour < 10 && minute < 10) "0$hour:0$minute:00"
        else "$hour:$minute:00"
    }

//    private fun openDaySelectionBottomSheet() {
//        todoId?.let {
//            val editDaysBottomSheet = EditDaysBottomSheet(this, it)
//            editDaysBottomSheet.show(childFragmentManager, null)
//        }
//    }


    private fun showLoadingDialog() {
        val upToddDialogs = UpToddDialogs(requireActivity())
        upToddDialogs.showDialog(R.drawable.gif_done,
            "Alarm updated successfully",
            "Back",
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                }
            })

        val handler = Handler()
        handler.postDelayed({
            upToddDialogs.dismissDialog()
        }, R.string.loadingDuarationInMillis.toLong())

    }

    private fun getDaysSelected() {
        val daysArrayList: ArrayList<Int> = ArrayList()
        var string = ""

        if (tM.isChecked) {
            daysArrayList.add(1)
            string += "M"
        }
        if (tTu.isChecked) {
            daysArrayList.add(2)
            string += ",T"
        }
        if (tW.isChecked) {
            daysArrayList.add(3)
            string += ",W"
        }
        if (tTh.isChecked) {
            daysArrayList.add(4)
            string += ",Th"
        }
        if (tF.isChecked) {
            daysArrayList.add(5)
            string += ",F"
        }
        if (tSa.isChecked) {
            daysArrayList.add(6)
            string += ",Sa"
        }
        if (tSu.isChecked) {
            daysArrayList.add(7)
            string += ",Su"
        }

        viewModel.setDaysSelected(daysArrayList, string)
    }

}