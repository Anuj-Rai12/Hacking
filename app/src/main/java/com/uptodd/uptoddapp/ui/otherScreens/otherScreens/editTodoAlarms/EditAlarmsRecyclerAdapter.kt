package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.editTodoAlarms

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.score.TYPE_HEADER
import com.uptodd.uptoddapp.database.todo.Todo
import kotlinx.android.synthetic.main.daily_todo_recycler_item.view.*

class EditAlarmsRecyclerAdapter(
    var todoList: List<Todo>,
    private val todosInterface: TodosInterface,
) :
    RecyclerView.Adapter<EditAlarmsRecyclerAdapter.ViewHolder>() {

    val TYPE_DOS_HEADER = 33
    val TYPE_DONT_HEADER = 44
    val TYPE_TODO_ITEM = 22

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
        return if (todoList[position].dosHeader) TYPE_DOS_HEADER
        else if (todoList[position].dontsHeader) TYPE_DONT_HEADER
        else TYPE_TODO_ITEM
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todo = todoList[position]
        if (todo.doType != TYPE_HEADER) {
            if (holder.bindingAdapterPosition % 2 != 0) {
                holder.itemView.todoRecyclerItem.setBackgroundResource(R.drawable.todo_bg_blue)
            } else holder.itemView.todoRecyclerItem.setBackgroundResource(R.drawable.todo_bg_green)


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


        }

        fun bind(todo: Todo, todosInterface: TodosInterface) {


            itemView.nameTextView.text = todo.task
            itemView.timeTextView.text = todo.alarmTimeByUser.substringBeforeLast(':')
            itemView.alarmSwitch.isChecked = todo.isAlarmNeededByUser
            itemView.alarmSwitch.visibility =
                if (todo.alarmTimeByUser != "00:00:00") View.VISIBLE else View.INVISIBLE
            itemView.timeTextView.visibility =
                if (todo.alarmTimeByUser != "00:00:00") View.VISIBLE else View.INVISIBLE
            itemView.alarmClockIcon.visibility =
                if (todo.alarmTimeByUser == "00:00:00") View.VISIBLE else View.INVISIBLE

            // setting click listeners after bind: (otherwise onCreate se hi problem shuru!)
            itemView.alarmSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) { // if switch is turned ON
                    todosInterface.onAlarmSwitchONTodo(bindingAdapterPosition)
                } else { // if switch is turned OFF
                    todosInterface.onAlarmSwitchOFFTodo(bindingAdapterPosition)
                }
            }

        }
    }

    interface TodosInterface {
        fun onClickTodo(position: Int)
        fun onAlarmSwitchONTodo(position: Int)
        fun onAlarmSwitchOFFTodo(position: Int)
    }

}