package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.vaccination

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uptodd.uptoddapp.R
import kotlinx.android.synthetic.main.colours_grid_item.view.*

class VaccinationRecyclerAdapter(
    var itemList: List<Vaccination>,
    private val vaccinationListener: VaccinationListener
) :
    RecyclerView.Adapter<VaccinationRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.colours_grid_item, parent, false)
        return ViewHolder(view, vaccinationListener)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(itemList[position])
    }

    class ViewHolder(itemView: View, toysListener: VaccinationListener) :
        RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                toysListener.onClickToy(bindingAdapterPosition)
            }

        }

        fun bind(vaccination: Vaccination) {
            itemView.nameTextView.text = vaccination.name
            Glide.with(itemView.context)
                .load(vaccination.url)
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_broken_image)
                .into(itemView.confettiImageView)

        }
    }

    interface VaccinationListener {
        fun onClickToy(position: Int)
    }
}