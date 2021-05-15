package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.editTodoAlarms

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.work.*
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.alarmsAndNotifications.UptoddAlarm
import com.uptodd.uptoddapp.api.getPeriod
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.score.DAILY_TODO
import com.uptodd.uptoddapp.database.score.TYPE_HEADER
import com.uptodd.uptoddapp.database.score.dontsHeader
import com.uptodd.uptoddapp.database.score.dosHeader
import com.uptodd.uptoddapp.database.todo.Todo
import com.uptodd.uptoddapp.database.todo.TodoDatabaseDao
import com.uptodd.uptoddapp.databinding.FragmentEditDailyAlarmsBinding
import com.uptodd.uptoddapp.helperClasses.DateClass
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.workManager.updateApiWorkmanager.UpdateAlarmThroughApiWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


class EditDailyAlarmsFragment : Fragment(), EditAlarmsRecyclerAdapter.TodosInterface {

    private lateinit var binding: FragmentEditDailyAlarmsBinding

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var database: TodoDatabaseDao
    private lateinit var recyclerAdapter: EditAlarmsRecyclerAdapter
    private var period by Delegates.notNull<Int>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        initialiseBinding(inflater, container)
        initialiseDependencies()
        fetchDataFromDatabase()


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }


    private fun initialiseDependencies() {
        period = getPeriod(requireContext())
        database = UptoddDatabase.getInstance(requireContext()).todoDatabaseDao
        recyclerAdapter = EditAlarmsRecyclerAdapter(emptyList(), this)
        binding.recyclerView.adapter = recyclerAdapter
    }

    private fun fetchDataFromDatabase() {
        uiScope.launch {
            val todosList = database.getTodosOfType(DAILY_TODO, period)
            recyclerAdapter.todoList = sortList(todosList)
        }
    }

    private fun refreshList() {
        uiScope.launch {
            val todosList = database.getTodosOfType(DAILY_TODO, period)
            recyclerAdapter.todoList = sortList(todosList)
            recyclerAdapter.notifyDataSetChanged()
        }
    }

    private fun sortList(list: List<Todo>): List<Todo> {

        val finalList = arrayListOf(dosHeader)

        val dos = list.filter {
            it.doType == 1
        }

        finalList.addAll(dos)

        val donts = list.filter {
            it.doType == 0
        }


        if (donts.isEmpty()) return finalList.toList()

        finalList.add(dontsHeader)
        finalList.addAll(donts)
        return finalList.toList()
    }

    private fun initialiseBinding(inflater: LayoutInflater, container: ViewGroup?) {

        binding =
            DataBindingUtil.inflate(inflater,
                R.layout.fragment_edit_daily_alarms,
                container,
                false)
    }

    override fun onClickTodo(position: Int) {
        if (recyclerAdapter.todoList[position].doType == TYPE_HEADER) return  // do not try to oprerate on headers
        val todoId = recyclerAdapter.todoList[position].id
        findNavController().navigate(EditAlarmsViewPagerFragmentDirections.actionEditAlarmsViewPagerFragmentToEditAlarmTimeFragment(
            todoId))
    }

    override fun onAlarmSwitchONTodo(position: Int) {
        if (recyclerAdapter.todoList[position].doType == TYPE_HEADER) return  // do not try to oprerate on headers

        turnOnAlarm(recyclerAdapter.todoList[position])
    }

    private fun turnOnAlarm(todo: Todo) {
        uiScope.launch {
            if (todo.isAlarmSet) return@launch  // cancel operation if alarm is already set
            val alarmTimeInMilli =
                DateClass().convertToTimeInMilliFromTimeString(todo.alarmTimeByUser)
            if (alarmTimeInMilli != null && alarmTimeInMilli > System.currentTimeMillis()) {
                UptoddAlarm.setAlarm(
                    requireContext(),
                    alarmTimeInMilli,
                    todo.id,
                    todo.task
                )

                Toast.makeText(requireContext(), "Alarm turned on", Toast.LENGTH_SHORT).show()
                todo.isAlarmSet = true
                todo.isAlarmNeededByUser = true
                database.update(todo)
                updateAlarmThroughApiUsingWorkManager(todo.id)
            } else {
                Log.d("error", "alarmTime in milli is null for id ${todo.id}")
            }

        }
    }

    override fun onAlarmSwitchOFFTodo(position: Int) {
        if (recyclerAdapter.todoList[position].doType == TYPE_HEADER) return  // do not try to oprerate on headers

        val todo = recyclerAdapter.todoList[position]
        turnOFFAlarm(todo)
    }

    private fun turnOFFAlarm(todo: Todo) {
        uiScope.launch {
            if (!todo.isAlarmSet) return@launch  // cancel operation if alarm is not set

            UptoddAlarm.cancelAlarm(
                requireContext(),
                todo.id
            )  // since alarm request code is same as todoId

            Toast.makeText(requireContext(), "Alarm turned off", Toast.LENGTH_SHORT).show()

            todo.isAlarmNeededByUser = false
            todo.isAlarmSet = false
            database.update(todo)
            updateAlarmThroughApiUsingWorkManager(todo.id)
        }
    }

    private fun updateAlarmThroughApiUsingWorkManager(todoId: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val data = Data.Builder()
        data.putInt("todoToUpdate", todoId)


        val alarmUpdaterWorker =
            OneTimeWorkRequestBuilder<UpdateAlarmThroughApiWorker>()
                .setConstraints(constraints)
                .setInputData(data.build())
                .setInitialDelay(10, TimeUnit.MINUTES)
                .build()

        val workManager = WorkManager.getInstance(requireActivity().application)
        workManager.enqueue(alarmUpdaterWorker)
    }


}