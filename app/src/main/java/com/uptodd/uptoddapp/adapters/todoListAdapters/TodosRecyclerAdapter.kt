package com.uptodd.uptoddapp.adapters.todoListAdapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.score.TYPE_HEADER
import com.uptodd.uptoddapp.database.score.WEEKLY_TODO
import com.uptodd.uptoddapp.database.todo.Todo
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.KidsPeriod
import com.uptodd.uptoddapp.utilities.ScreenDpi
import kotlinx.android.synthetic.main.daily_todo_recycler_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class TodosRecyclerAdapter(
    var todoList: List<Todo>,
    private val todosInterface: TodosInterface,
) :
    RecyclerView.Adapter<TodosRecyclerAdapter.ViewHolder>() {

    val TYPE_DOS_HEADER = 33
    val TYPE_DONT_HEADER = 44
    val TYPE_TODO_ITEM = 22

    var selectedItemList = ArrayList<Todo>()
    var multipleSelectionFlag: Boolean = false

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    fun selectAllItems() {
        uiScope.launch {
            selectedItemList.clear()
            selectedItemList.addAll(todoList.filter {
                it.doType != TYPE_HEADER
            })
            notifyDataSetChanged()
        }
    }

    fun clearAllSelected() {
        selectedItemList.clear()
        notifyDataSetChanged()
    }

    fun notifySelectedItemRemovedAt(position: Int) {
        uiScope.launch {
            selectedItemList.remove(todoList[position])
        }

    }

    fun notifyItemReselectedAt(position: Int) {
        uiScope.launch {
            selectedItemList.add(todoList[position])
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (viewType == TYPE_DOS_HEADER) {
            val view = inflater.inflate(R.layout.header_dos, parent, false)
            return ViewHolder(view, todosInterface)
        }
        if (viewType == TYPE_DONT_HEADER) {
            val view = inflater.inflate(R.layout.header_donts, parent, false)
            return ViewHolder(view, todosInterface)
        } else {
            val view = inflater.inflate(R.layout.daily_todo_recycler_item, parent, false)
            return ViewHolder(view, todosInterface)
        }
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (todoList[position].dosHeader == true) return TYPE_DOS_HEADER
        else if (todoList[position].dontsHeader == true) return TYPE_DONT_HEADER
        else return TYPE_TODO_ITEM
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todo = todoList[position]
        if (todo.doType != TYPE_HEADER) {
            when(holder.bindingAdapterPosition % 5) {
                1->   holder.itemView.todoRecyclerItem.setBackgroundColor(Color.parseColor("#F0FCF9"))
                1->   holder.itemView.todoRecyclerItem.setBackgroundColor(Color.parseColor("#F8F3E9"))
                2->  holder.itemView.todoRecyclerItem.setBackgroundColor(Color.parseColor("#A6D2FA"))
                3-> holder.itemView.todoRecyclerItem.setBackgroundColor(Color.parseColor("#F1F0FC"))
                4-> holder.itemView.todoRecyclerItem.setBackgroundColor(Color.parseColor("#E7FEFF"))
            }

            if (multipleSelectionFlag) {
                holder.itemView.completeStatus.visibility = View.VISIBLE
                holder.itemView.completeStatus.isChecked = selectedItemList.contains(todo)
            } else {
                holder.itemView.completeStatus.visibility = View.GONE
            }

            if (todo.doType == 0) {  // dont's will not have alarm switch
                holder.itemView.alarmSwitch.visibility = View.INVISIBLE
            }

            holder.bind(todo, todosInterface)

        }

    }

    class ViewHolder(itemView: View, todosInterface: TodosInterface) :
        RecyclerView.ViewHolder(itemView) {


        init {
            itemView.setOnClickListener {
                todosInterface.onClickTodo(bindingAdapterPosition)
            }

            itemView.setOnLongClickListener {
                todosInterface.onLongClickTodo(bindingAdapterPosition)
                return@setOnLongClickListener true
            }

//            itemView.completeStatus.setOnCheckedChangeListener { buttonView, isChecked ->
//                todosInterface.onClickMultipleSelectionItem(bindingAdapterPosition, isChecked)
//            }


        }

        fun bind(todo: Todo, todosInterface: TodosInterface) {

          //  Glide.with(itemView).load(todo.imageUrl).into(itemView.thumbnail_todo)
            val period = KidsPeriod(itemView.context).getPeriod()
            val dpi = ScreenDpi(itemView.context).getScreenDrawableType()
            val appendable =
                "https://uptodd.com/images/app/android/details/activities/$period/$dpi/"

            Glide.with(itemView)
                .load("$appendable${todo.imageUrl}.webp")
                .into(itemView.thumbnail_todo)
                .onLoadStarted(
                    ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.loading_animation
                    )
                )
            Log.e("thumbanail",todo.imageUrl)
            itemView.nameTextView.text = todo.task
            itemView.timeTextView.text = todo.alarmTimeByUser.substringBeforeLast(':')
            itemView.alarmSwitch.isChecked = todo.isAlarmNeededByUser

            itemView.alarmSwitch.visibility =
                if (todo.alarmTimeByUser != "00:00:00") View.VISIBLE else View.INVISIBLE
            itemView.timeTextView.visibility =
                if (todo.alarmTimeByUser != "00:00:00") View.VISIBLE else View.INVISIBLE


            if (todo.type == WEEKLY_TODO && todo.alarmTimeByUser != "00:00:00") {
                val today = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                }

                val weekDay = today.get(Calendar.DAY_OF_WEEK)

                when (weekDay) {
                    1 -> {
                        itemView.alarmSwitch.isChecked = todo.weeklySunday
                    }
                    2 -> {
                        itemView.alarmSwitch.isChecked = todo.weeklyMonday
                    }
                    3 -> {
                        itemView.alarmSwitch.isChecked = todo.weeklyTuesday
                    }
                    4 -> {
                        itemView.alarmSwitch.isChecked = todo.weeklyWednesday
                    }
                    5 -> {
                        itemView.alarmSwitch.isChecked = todo.weeklyThursday
                    }
                    6 -> {
                        itemView.alarmSwitch.isChecked = todo.weeklyFriday
                    }
                    7 -> {
                        itemView.alarmSwitch.isChecked = todo.weeklySaturday
                    }
                }

            }

            // setting click listeners after bind : (otherwise onCreate se hi problem shuru!)
            itemView.alarmSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) { // if switch is turned ON
                    todosInterface.onAlarmSwitchONTodo(bindingAdapterPosition)
                } else { // if switch is turned OFF
                    todosInterface.onAlarmSwitchOFFTodo(bindingAdapterPosition)
                }
            }

            itemView.completeStatus.setOnCheckedChangeListener { buttonView, isChecked ->
                todosInterface.onClickMultipleSelectionItem(bindingAdapterPosition, isChecked)
            }

        }
    }

    interface TodosInterface {
        fun onClickTodo(position: Int)
        fun onLongClickTodo(position: Int)
        fun onAlarmSwitchONTodo(position: Int)
        fun onAlarmSwitchOFFTodo(position: Int)
        fun onClickMultipleSelectionItem(position: Int, isChecked: Boolean)
    }

}