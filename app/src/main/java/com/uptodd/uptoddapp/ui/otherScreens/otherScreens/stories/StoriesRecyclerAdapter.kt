package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.stories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uptodd.uptoddapp.R
import kotlinx.android.synthetic.main.colours_grid_item.view.*

class StoriesRecyclerAdapter(
    var itemList: List<Story>,
    private val storiesListener: StoriesListener
) :
    RecyclerView.Adapter<StoriesRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.colours_grid_item, parent, false)
        return ViewHolder(view, storiesListener)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    class ViewHolder(itemView: View, storiesListener: StoriesListener) :
        RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                storiesListener.onClickStory(bindingAdapterPosition)
            }

        }

        fun bind(story: Story) {
            itemView.nameTextView.text = story.name
            Glide.with(itemView.context)
                .load(story.url)
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.default_set_android_thumbnail)
                .into(itemView.confettiImageView)

        }
    }

    interface StoriesListener {
        fun onClickStory(position: Int)
    }

}