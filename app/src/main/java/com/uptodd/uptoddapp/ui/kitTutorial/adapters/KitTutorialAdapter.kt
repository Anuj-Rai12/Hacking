package com.uptodd.uptoddapp.ui.kitTutorial.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.makeramen.roundedimageview.RoundedImageView
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.activitysample.ActivitySample
import com.uptodd.uptoddapp.database.kitTutorial.KitTutorial

class KitTutorialAdapter(val clickListener: KitTutorialInterface) :
    RecyclerView.Adapter<KitTutorialAdapter.KitViewHolder>() {

    var list = listOf<KitTutorial>()
    var colors= arrayOf("#e9f6fe","#fffce1","#fef4eb")

    @SuppressLint("NotifyDataSetChanged")
    fun refreshList(list: ArrayList<KitTutorial>){
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):KitViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.kit_tutorial_recyclerview_item, parent, false)
        return KitViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: KitViewHolder, position: Int) {
        holder.bind(list[position])
        if(position==0 || position==1)
        holder.cardView.setCardBackgroundColor(Color.parseColor(colors[0]))
        else if(position==2 || position ==3)
            holder.cardView.setCardBackgroundColor(Color.parseColor(colors[1]))
        else
            holder.cardView.setCardBackgroundColor(Color.parseColor(colors[2]))
    }

    inner class KitViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val imageThumbnail: RoundedImageView =
            view.findViewById(R.id.iv_icon)
        private val categoryTitle: TextView = view.findViewById<TextView>(R.id.tv_title)
        val cardView :CardView = view.findViewById(R.id.rootCardView)

        fun bind(kitTutorial: KitTutorial) {

            val imageUrl = "https://www.uptodd.com/images/app/android/thumbnails/kit_tutorials/" +
                    "${kitTutorial.image}.webp"
            Glide.with(view.context)
                .load(Uri.parse(imageUrl))
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.default_set_android_thumbnail)
                .into(imageThumbnail)



           categoryTitle.text = kitTutorial.category

            view.setOnClickListener {
                clickListener.onClick(kitTutorial)
            }
        }

    }
}

interface KitTutorialInterface {
    fun onClick(kitTutorial: KitTutorial)
}
