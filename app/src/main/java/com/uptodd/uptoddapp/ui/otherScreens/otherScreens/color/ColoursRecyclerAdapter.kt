package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.color

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uptodd.uptoddapp.R
import kotlinx.android.synthetic.main.colours_grid_item.view.*

class ColoursRecyclerAdapter(
    var itemList: List<Colour>,
    private val coloursListener: ColoursListener
) :
    RecyclerView.Adapter<ColoursRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.colours_grid_item, parent, false)
        return ViewHolder(view, coloursListener)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(itemList[position])
    }

    class ViewHolder(itemView: View, toysListener: ColoursListener) :
        RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                toysListener.onClickToy(bindingAdapterPosition)
            }

        }

        fun bind(colour: Colour) {
            itemView.nameTextView.text = colour.name
            Glide.with(itemView.context)
                .load(colour.url)
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.default_set_android_thumbnail)
                .into(itemView.confettiImageView)
        }
    }

    interface ColoursListener {
        fun onClickToy(position: Int)
    }

}