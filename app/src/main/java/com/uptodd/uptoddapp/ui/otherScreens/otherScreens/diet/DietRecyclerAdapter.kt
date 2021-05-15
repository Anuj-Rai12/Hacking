package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.diet

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.uptodd.uptoddapp.R
import kotlinx.android.synthetic.main.colours_grid_item.view.*

class DietRecyclerAdapter(
    var itemList: List<Diet>,
    private val dietListener: DietListener
) :
    RecyclerView.Adapter<DietRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.colours_grid_item, parent, false)
        return ViewHolder(view, dietListener)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("div", "DietsRecyclerAdapter L33 ${itemList[position].url}")
        val url = itemList[position].url
        Glide.with(holder.itemView.context)
            .load(url)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.default_set_android_thumbnail)
            )
            .into(holder.itemView.confettiImageView)

//        Picasso.get()
//            .load(itemList[position].url)
//            .into(holder.itemView.confettiImageView)

        holder.bind(itemList[position])
    }

    class ViewHolder(itemView: View, toysListener: DietListener) :
        RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                toysListener.onClickToy(bindingAdapterPosition)
            }

        }

        fun bind(diet: Diet) {
            itemView.nameTextView.text = diet.name

        }
    }

    interface DietListener {
        fun onClickToy(position: Int)
    }
}