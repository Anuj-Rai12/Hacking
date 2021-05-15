package com.uptodd.uptoddapp.ui.webinars.webinarslist

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.webinars.DualWebinars
import kotlinx.android.synthetic.main.webinar_item_view.view.*


class WebinarsAdapter(
    var allWebinarsList: List<DualWebinars?>,
    private val onCompleteClickListener: OnCompleteClickListener
) :RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType==VIEW_TYPE_ITEM)
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.webinar_item_view, parent, false)
            return ItemViewHolder(view, onCompleteClickListener)
        }
        else
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.progressbar_loading, parent, false)
            return LoadingViewHolder(view, onCompleteClickListener)
        }
    }

    override fun getItemCount(): Int {
        Log.d("div", "WebinarsAdapter L45 " + allWebinarsList.size)
        return allWebinarsList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is ItemViewHolder)
        {
            val card = allWebinarsList[position]
            if (card != null) {
                holder.bind(card)
            }
        }
        else if(holder is LoadingViewHolder)
        {
            showLoadingView(holder, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (allWebinarsList.get(position) == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    class ItemViewHolder(itemView: View, onCompleteClickListener: OnCompleteClickListener) :
        RecyclerView.ViewHolder(itemView) {

        init {
            itemView.imageView1.setOnClickListener {
                onCompleteClickListener.onClickWebinar1(
                    adapterPosition
                )
            }
            itemView.todoImageView.setOnClickListener {
                onCompleteClickListener.onClickWebinar2(
                    adapterPosition
                )
            }
            itemView.textView_title1.setOnClickListener {
                onCompleteClickListener.onClickWebinar1(
                    adapterPosition
                )
            }
            itemView.textView_title2.setOnClickListener{onCompleteClickListener.onClickWebinar2(adapterPosition)}
        }

        fun bind(dualWebinar: DualWebinars) {
            itemView.textView_title1.text = dualWebinar.title1.toString()
            itemView.textView_date1.text = dualWebinar.date1.toString()
            bindImage(itemView.imageView1,dualWebinar.imageURL1)
            Log.d("div","WebinarsAdapter L86 ${dualWebinar.description2.toString()}")
            if(dualWebinar.description2!=null) {
                itemView.textView_title2.text = dualWebinar.title2.toString()
                itemView.textView_date2.text = dualWebinar.date2.toString()
                bindImage(itemView.todoImageView, dualWebinar.imageURL2)
            }
            else
            {
                Log.d("div","WebinarsAdapter L86 View Gone")
                itemView.textView_title2.visibility=View.GONE
                itemView.textView_date2.visibility = View.GONE
                itemView.todoImageView.visibility = View.GONE
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                itemView.imageView1.clipToOutline = true
                itemView.todoImageView.clipToOutline = true
            }

        }
        fun bindImage(imgView: ImageView, imgUrl: String?) {
            imgUrl?.let {
                val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
                Glide.with(imgView.context)
                    .load(imgUri)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.loading_animation)
                            .error(R.drawable.default_set_android_thumbnail)
                    )
                    .into(imgView)
            }
        }
    }

    private class LoadingViewHolder(itemView: View, onCompleteClickListener: OnCompleteClickListener) : RecyclerView.ViewHolder(itemView) {
        var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    }

    private fun showLoadingView(viewHolder: LoadingViewHolder, position: Int) {
        //ProgressBar would be displayed
    }

    interface OnCompleteClickListener {
        fun onClickWebinar1(position: Int)
        fun onClickWebinar2(position: Int)
    }
}

