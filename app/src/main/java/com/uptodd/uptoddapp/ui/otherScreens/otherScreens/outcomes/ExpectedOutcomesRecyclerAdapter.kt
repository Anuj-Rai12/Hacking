package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.outcomes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uptodd.uptoddapp.R
import kotlinx.android.synthetic.main.colours_grid_item.view.*

class ExpectedOutcomesRecyclerAdapter(
    var itemList: List<ExpectedOutcomes>,
    private val outcomeListener: OutcomeListener
) :
    RecyclerView.Adapter<ExpectedOutcomesRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.colours_grid_item, parent, false)
        return ViewHolder(view, outcomeListener)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    class ViewHolder(itemView: View, toysListener: OutcomeListener) :
        RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                toysListener.onClickToy(bindingAdapterPosition)
            }

        }

        fun bind(outcome: ExpectedOutcomes) {
            itemView.nameTextView.text = outcome.name
            Glide.with(itemView.context)
                .load(outcome.url)
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.default_set_android_thumbnail)
                .into(itemView.confettiImageView)
        }
    }

    interface OutcomeListener {
        fun onClickToy(position: Int)
    }
}