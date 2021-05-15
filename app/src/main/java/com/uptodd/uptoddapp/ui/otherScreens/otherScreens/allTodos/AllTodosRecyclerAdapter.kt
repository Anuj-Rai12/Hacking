package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.allTodos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.uptodd.uptoddapp.R
import kotlinx.android.synthetic.main.colours_grid_item.view.*

class AllTodosRecyclerAdapter(
    var itemList: ArrayList<AllTodos>,
    private val allTodosListener: AllTodosListener,
) :
    RecyclerView.Adapter<AllTodosRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.colours_grid_item, parent, false)
        return ViewHolder(view, allTodosListener)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(itemList[position].imageUrl)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.default_set_android_thumbnail)
            )
            .into(holder.itemView.confettiImageView)

        holder.bind(itemList[position])
    }

    class ViewHolder(itemView: View, allTodosListener: AllTodosListener) :
        RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                allTodosListener.onClickAllTodos(bindingAdapterPosition)
            }

        }

        fun bind(allTodos: AllTodos) {
            itemView.nameTextView.text = allTodos.name
            //  itemView.starsTextView.text = toy.stars
        }
    }

    interface AllTodosListener {
        fun onClickAllTodos(position: Int)
    }

}