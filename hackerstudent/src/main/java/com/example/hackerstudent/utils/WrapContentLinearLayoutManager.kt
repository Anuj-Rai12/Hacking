package com.example.hackerstudent.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.IndexOutOfBoundsException

@SuppressLint("WrongConstant")
class WrapContentLinearLayoutManager(context: FragmentActivity, type: Int=VERTICAL) :
    LinearLayoutManager(context, type, false) {
    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        }catch (e:IndexOutOfBoundsException){
            Log.i("ANUJ", "onLayoutChildren: ${e.localizedMessage}")
        }
    }
}
