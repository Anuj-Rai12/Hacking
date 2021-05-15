package com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens

import android.graphics.Canvas
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.adapters.todoListAdapters.TodosRecyclerAdapter
import com.uptodd.uptoddapp.database.score.TYPE_HEADER
import com.uptodd.uptoddapp.databinding.FragmentDailyTodosBinding
import com.uptodd.uptoddapp.helperClasses.DateClass
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.masterFragment.TodosViewPagerFragmentDirections
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlin.properties.Delegates


class DailyTodosFragment : Fragment(), TodosRecyclerAdapter.TodosInterface {

    private lateinit var binding: FragmentDailyTodosBinding
    private val viewModel: TodosViewModel by activityViewModels()
    private lateinit var todosRecyclerAdapter: TodosRecyclerAdapter

    private var multipleItemSelectionFlag: Boolean = false
    private var numberOfHeaders by Delegates.notNull<Int>() // taking a safe default value // because lateinit not allowed on Int


    override fun onResume() {
        super.onResume()
        viewModel.loadDailyTodoScore()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        initialiseBindingAndViewModel(inflater, container)
        initialiseRecyclerView()

//        val freshness = validateFreshness()
//        if (!freshness) {
//            hideAllUIElements()
//            return binding.noInternetTextView
//        }



        viewModel.dailyPendingTodosList.observe(viewLifecycleOwner, {
            it?.let {
                if (it.size == 1) {
                    changeToAllCompleteLayout()
                }
            }
        })

        viewModel.alarmSetToast.observe(viewLifecycleOwner, {
            it?.let {
                if (it) {
                    Toast.makeText(
                        this.requireContext(),
                        "Alarm turned ON",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.doneShowingAlarmSetNotification()

                }
            }
        })

        viewModel.alarmCancelledToast.observe(viewLifecycleOwner, {
            it?.let {
                if (it) {
                    Toast.makeText(
                        this.requireContext(),
                        "Alarm cancelled",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.doneShowingAlarmCancelledNotification()

                }
            }
        })

        return binding.root
    }

//    private fun validateFreshness(): Boolean {
//        val lastTodoFetchedOn =
//            UptoddSharedPreferences.getInstance(requireContext()).getLastDailyTodoFetchedDate()
//        if (lastTodoFetchedOn != null) {
//            return DateClass().isTodoFresh(lastTodoFetchedOn)
//        } else return false  // return false incase user opens the app for the very first time( last to-do fetched date would be nul in that case)
//
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dismissMultipleSelection()

        binding.btnMarkAllAsDone.setOnClickListener {
            launchMultipleItemSelection()
        }

        binding.btnDone.setOnClickListener {
            val totalTodos = todosRecyclerAdapter.todoList.size
            val totalTodosSelected = todosRecyclerAdapter.selectedItemList.size


            viewModel.markAllDailyAsComplete(
                todosRecyclerAdapter.selectedItemList,
                requireContext(),
            )

            viewModel.multipleDailySelectionTaskCompleteFlag.observe(viewLifecycleOwner, {
                if (it == true) {
                    todosRecyclerAdapter.clearAllSelected()
                    viewModel.multipleDailySelectionTaskComplete()
                    viewModel.multipleDailySelectionTaskCompleteFlag.removeObservers(
                        viewLifecycleOwner
                    )
                }
            })

            dismissMultipleSelection()

            viewModel.navigateToAppreciationScreenFlag.observe(viewLifecycleOwner, {
                if (it) {
                    gotoAppreciationFragment()
                    viewModel.navigateToAppreciationScreenFlag.removeObservers(viewLifecycleOwner)
                }

            })


        }

        binding.btnCancel.setOnClickListener {
            dismissMultipleSelection()
            todosRecyclerAdapter.clearAllSelected()
        }
    }


    private fun initialiseBindingAndViewModel(inflater: LayoutInflater, container: ViewGroup?) {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_daily_todos, container, false)

        binding.todosViewModel = viewModel
        binding.lifecycleOwner = this

    }

    private fun initialiseRecyclerView() {

        todosRecyclerAdapter = TodosRecyclerAdapter(emptyList(), this)
        binding.recyclerView.adapter = todosRecyclerAdapter

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)  // for swipe behaviour
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        viewModel.dailyPendingTodosList.observe(viewLifecycleOwner, { todosList ->
            todosList.let {

                if (todosRecyclerAdapter.todoList != it) {  // this line is vvv imp ---> observer calls this func even if no changes are there in the list
                    todosRecyclerAdapter.todoList = it
                    todosRecyclerAdapter.notifyDataSetChanged()

                    numberOfHeaders = todosList.filter {
                        it.doType == TYPE_HEADER
                    }.size
                }
            }
        })
    }

    private val itemTouchHelperCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {


                fun markThisItemComplete() {

                    val todosLeft = todosRecyclerAdapter.todoList.size
                    val position = viewHolder.bindingAdapterPosition
                    val todoCompleted = todosRecyclerAdapter.todoList[position]

                    if (todoCompleted.doType == TYPE_HEADER) return

                    // recyclerAdapter.notifyItemRemoved(position)  --> Not needed because recyclerAdapter is observed by live data
                    viewModel.markAsComplete(todoCompleted, requireContext())

                    val mPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.ting)
                    mPlayer.start()

                    if (todosLeft == numberOfHeaders + 1) {   // to account for header to-do which cannot be swiped
                        changeToAllCompleteLayout()
                        gotoAppreciationFragment()
                    }
                }

                if (!multipleItemSelectionFlag) {
                    markThisItemComplete()

                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean,
            ) {

                if (todosRecyclerAdapter.todoList[viewHolder.absoluteAdapterPosition].doType == TYPE_HEADER)
                    return    // do not swipe viewholders containing the header!

                val swipeDecorator = RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )

                if (viewHolder.bindingAdapterPosition % 2 == 0) {
                    swipeDecorator.addBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.onSwipeGreen
                        )
                    )
                } else {
                    swipeDecorator.addBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.onSwipeBlue
                        )
                    )
                }
                val typeface = Typeface.create("Crimson Text", Typeface.BOLD)

                swipeDecorator
                    .addSwipeRightLabel("Completed")
                    .setSwipeRightLabelTypeface(typeface)
                    .create()
                    .decorate()



                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

        }

    override fun onClickTodo(position: Int) {
        if (todosRecyclerAdapter.todoList[position].doType == TYPE_HEADER) return  // do not try to oprerate on headers
        val todoId = todosRecyclerAdapter.todoList[position].id
        findNavController().navigate(
            TodosViewPagerFragmentDirections.actionTodosViewPagerFragmentToTodoDetailsFragment(
                todoId
            )
        )
        // make any changes here ;)
    }

    override fun onLongClickTodo(position: Int) {
        if (todosRecyclerAdapter.todoList[position].doType == TYPE_HEADER) return  // do not try to oprerate on headers
    }

    override fun onAlarmSwitchONTodo(position: Int) {
        if (todosRecyclerAdapter.todoList[position].doType == TYPE_HEADER) return  // do not try to oprerate on headers

        viewModel.turnOnAlarm(todosRecyclerAdapter.todoList[position], this.requireContext())
    }

    override fun onAlarmSwitchOFFTodo(position: Int) {
        if (todosRecyclerAdapter.todoList[position].doType == TYPE_HEADER) return  // do not try to oprerate on headers

        val todo = todosRecyclerAdapter.todoList[position]
        viewModel.turnOFFAlarm(todo, this.requireContext())
    }

    override fun onClickMultipleSelectionItem(position: Int, isChecked: Boolean) {
        if (todosRecyclerAdapter.todoList[position].doType == TYPE_HEADER) return  // do not try to oprerate on headers

        if (isChecked) {
            todosRecyclerAdapter.notifyItemReselectedAt(position)
        } else {
            todosRecyclerAdapter.notifySelectedItemRemovedAt(position)
        }
    }

    private fun launchMultipleItemSelection() {

        todosRecyclerAdapter.selectAllItems()
        binding.apply {
            btnCancel.visibility = View.VISIBLE
            btnDone.visibility = View.VISIBLE

            btnMarkAllAsDone.visibility = View.INVISIBLE
        }
        multipleItemSelectionFlag = true
        todosRecyclerAdapter.multipleSelectionFlag = true
    }

    private fun dismissMultipleSelection() {

        binding.apply {
            btnCancel.visibility = View.INVISIBLE
            btnDone.visibility = View.INVISIBLE


            btnMarkAllAsDone.visibility = View.VISIBLE
        }
        multipleItemSelectionFlag = false
        todosRecyclerAdapter.multipleSelectionFlag = false


    }

    private fun changeToAllCompleteLayout() {
        binding.apply {

            binding.btnMarkAllAsDone.visibility = View.INVISIBLE
            recyclerView.visibility = View.INVISIBLE
            superManImageView.visibility = View.VISIBLE
            youAreASuperParentTextView.visibility = View.VISIBLE
            youAreASuperParentTextView.visibility = View.VISIBLE
            allActivitiesCompletedTextView.visibility = View.VISIBLE
            confettiImageView.visibility = View.VISIBLE
            Glide.with(requireActivity())
                .load(R.drawable.superparentgif)
                .into(binding.confettiImageView)

        }
    }

    private fun gotoAppreciationFragment() {
        findNavController().navigate(R.id.appreciationFragment)
    }

    private fun hideAllUIElements() {
        binding.apply {
            noInternetTextView.visibility = View.VISIBLE

            recyclerView.visibility = View.INVISIBLE
            btnMarkAllAsDone.visibility = View.INVISIBLE
            superManImageView.visibility = View.INVISIBLE
            youAreASuperParentTextView.visibility = View.INVISIBLE
            youAreASuperParentTextView.visibility = View.INVISIBLE
            allActivitiesCompletedTextView.visibility = View.INVISIBLE
            confettiImageView.visibility = View.INVISIBLE
        }
    }


}