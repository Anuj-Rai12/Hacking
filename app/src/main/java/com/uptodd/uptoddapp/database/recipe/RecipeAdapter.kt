package com.uptodd.uptoddapp.database.recipe

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ItemActPodcastBinding
import com.uptodd.uptoddapp.utils.getAdaptorViewHolderBg

class RecipeAdapter(val clickListener: RecipeClickListener) :
    RecyclerView.Adapter<RecipeAdapter.ActivityViewHolder>() {
    var list = listOf<Recipe>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: ItemActPodcastBinding =
            DataBindingUtil.inflate(inflater, R.layout.item_act_podcast, parent, false)
        return ActivityViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(list[position],position)
    }

    inner class ActivityViewHolder(val view: ItemActPodcastBinding) :
        RecyclerView.ViewHolder(view.root) {

        fun bind(recipe: Recipe,position: Int) {
            view.rootLayoutItemActPodcast.setBackgroundResource(getAdaptorViewHolderBg[position % getAdaptorViewHolderBg.size])
            val imageUrl = "https://img.youtube.com/vi/${recipe.video}/mqdefault.jpg"
            Glide.with(view.root.context)
                .load(Uri.parse(imageUrl))
                .transform(CenterCrop(), RoundedCorners(20))
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.default_set_android_thumbnail)
                .into(view.itemActPodcastVideoThumbnail)

            view.itemActPodcastVideoTitle.text = recipe.title

            view.root.setOnClickListener {
                clickListener.onClick(recipe)
            }
        }

    }
}

interface RecipeClickListener {
    fun onClick(recipe: Recipe)
}