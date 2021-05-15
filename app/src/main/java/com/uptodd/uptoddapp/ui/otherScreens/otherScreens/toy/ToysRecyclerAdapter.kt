package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.toy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uptodd.uptoddapp.R
import kotlinx.android.synthetic.main.colours_grid_item.view.*


class ToysRecyclerAdapter(
    var itemList: List<Toy>,
    private val toysListener: ToysListener
) :
    RecyclerView.Adapter<ToysRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.colours_grid_item, parent, false)
        return ViewHolder(view, toysListener)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    class ViewHolder(itemView: View, toysListener: ToysListener) :
        RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                toysListener.onClickToy(it, bindingAdapterPosition)
            }

        }

        fun bind(toy: Toy) {

            itemView.nameTextView.text = toy.name

            Glide.with(itemView.context)
                .load(toy.url)
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.default_set_android_thumbnail)
                .into(itemView.confettiImageView)
        }
    }

    interface ToysListener {
        fun onClickToy(view: View, position: Int)
    }

}