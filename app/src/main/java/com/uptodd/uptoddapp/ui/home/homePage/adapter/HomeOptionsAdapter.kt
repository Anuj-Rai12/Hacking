package com.uptodd.uptoddapp.ui.home.homePage.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.HomeOptionsRecyclerviewItemBinding
import com.uptodd.uptoddapp.ui.home.homePage.adapter.models.OptionsItem
import com.uptodd.uptoddapp.ui.home.homePage.adapter.viewholders.HomeOptionsViewHolder

class HomeOptionsAdapter(
    var context: Context,
    var type: Int,
    var listener: HomeOptionsClickListener
) : RecyclerView.Adapter<HomeOptionsViewHolder>() {

    val optionsList = getOptionsList(type)

    companion object {
        const val PERSONALIZED = 0
        const val PREMIUM = 1
        const val PARENT = 2
        const val FreeDemoContent = 3

        fun calculateColumns(context: Context, columnWidthDp: Float = 140F): Int {
            val metrics = context.resources.displayMetrics
            val screenWidth = metrics.widthPixels / metrics.density

            return (screenWidth / columnWidthDp + 0.5).toInt()
        }

        fun getOptionsList(type: Int): ArrayList<OptionsItem> {
            return when (type) {
                PERSONALIZED -> OptionsItem.getPersonalizedList()
                PREMIUM -> OptionsItem.getPremiumList()
                //FreeDemoContent -> OptionsItem.getFreeDemoSection()
                else -> OptionsItem.getParentToolList()
            }
        }

    }

    fun addKitTutorial() {
        if (optionsList.size == 5) {
            optionsList.add(
                OptionsItem(
                    R.id.action_homePageFragment_to_kitTutorialFragment,
                    R.drawable.ic_kit_tutorial, "Kit tutorial"
                )
            )
            notifyDataSetChanged()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeOptionsViewHolder {

        return HomeOptionsViewHolder(
            HomeOptionsRecyclerviewItemBinding.inflate(LayoutInflater.from(context))
        )
    }

    override fun onBindViewHolder(holder: HomeOptionsViewHolder, position: Int) {
        holder.bind(optionsList[position])

        holder.itemView.setOnClickListener {
            listener.onClickedItem(optionsList[position].navId)
        }
    }

    override fun getItemCount(): Int {
        return optionsList.size
    }


    interface HomeOptionsClickListener {

        fun onClickedItem(navId: Int)
    }

}