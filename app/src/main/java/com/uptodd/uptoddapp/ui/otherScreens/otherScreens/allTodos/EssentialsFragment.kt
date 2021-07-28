package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.allTodos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.api.getPeriod
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.score.ESSENTIALS_TODO
import com.uptodd.uptoddapp.database.todo.TodoDatabaseDao
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.ScreenDpi
import kotlinx.android.synthetic.main.fragment_essentials2.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class EssentialsFragment : Fragment(), AllTodosRecyclerAdapter.AllTodosListener {

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var database: TodoDatabaseDao
    private val arrayList = ArrayList<AllTodos>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        database = UptoddDatabase.getInstance(requireContext()).todoDatabaseDao

        val period = getPeriod(requireContext())
        val dpi = ScreenDpi(requireContext()).getScreenDrawableType()
        val appendable =
            "https://www.uptodd.com/images/app/android/details/activities/$period/$dpi/"

        uiScope.launch {
            val list = database.getTodosOfType(ESSENTIALS_TODO, period)
            for (todo in list) {
                val imageUrl = todo.imageUrl
                arrayList.add(AllTodos(todo.id, todo.task, "$appendable$imageUrl.webp"))
            }

            recyclerView.adapter = AllTodosRecyclerAdapter(arrayList, this@EssentialsFragment)

        }

        return inflater.inflate(R.layout.fragment_essentials2, container, false)
    }


    override fun onClickAllTodos(position: Int) {
        findNavController().navigate(AllTodosViewPagerFragmentDirections.actionAllTodosViewPagerFragmentToTodoDetailsFragment(
            arrayList[position].id))
    }


}