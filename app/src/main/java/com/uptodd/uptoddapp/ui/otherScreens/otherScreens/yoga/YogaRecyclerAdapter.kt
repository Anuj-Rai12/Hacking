package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.yoga

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.ui.otherScreens.otherScreens.yoga.allYogas.Yoga
import kotlinx.android.synthetic.main.yoga_recycler_item.view.*

class YogaRecyclerAdapter(
    var itemList: List<Yoga>,
    private val yogaListener: YogasListener
) :
    RecyclerView.Adapter<YogaRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.yoga_recycler_item, parent, false)
        return ViewHolder(view, yogaListener)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(itemList[position])
    }

    class ViewHolder(itemView: View, yogasListener: YogasListener) :
        RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                yogasListener.onClickYoga(bindingAdapterPosition)
            }

        }

        fun bind(yoga: Yoga) {
            Log.d("div", "YogaRecyclerAdapter L55 $yoga ${yoga.description.trim()}")
            itemView.todoTaskName.text = yoga.steps
            itemView.description.text = yoga.description.trim()
            Glide.with(itemView.context)
                .load(yoga.url)
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.default_set_android_thumbnail)
                .into(itemView.todoImageView)
        }
    }

    interface YogasListener {
        fun onClickYoga(position: Int)
    }

}