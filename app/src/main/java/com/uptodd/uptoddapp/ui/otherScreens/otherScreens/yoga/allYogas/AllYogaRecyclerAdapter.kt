package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.yoga.allYogas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.uptodd.uptoddapp.R
import kotlinx.android.synthetic.main.colours_grid_item.view.*

class AllYogaRecyclerAdapter(
    var itemList: List<Yoga>,
    private val yogaListener: YogasListener
) :
    RecyclerView.Adapter<AllYogaRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.colours_grid_item, parent, false)
        return ViewHolder(view, yogaListener)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(itemList[position].url)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.default_set_android_thumbnail)
            )
            .into(holder.itemView.confettiImageView)

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
            itemView.nameTextView.text = yoga.name

        }
    }

    interface YogasListener {
        fun onClickYoga(position: Int)
    }

}