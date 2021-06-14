package com.uptodd.uptoddapp.ui.upgrade

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.databinding.PoemListItemBinding
import com.uptodd.uptoddapp.databinding.UpgradeOfferListItemBinding

class UpgradeAdapter(val clickListener: UpgradeAdapterInterface) : RecyclerView.Adapter<UpgradeAdapter.UpgradeViewHolder>(){

    var list:ArrayList<UpgradeItem>?= ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpgradeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = UpgradeOfferListItemBinding.inflate(inflater, parent, false)
        return UpgradeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UpgradeViewHolder, position: Int) {
        list?.get(position)?.let { holder.bind(it) }
    }

    inner class UpgradeViewHolder(private val binding: UpgradeOfferListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UpgradeItem) {

            binding.upTitle.text=item.programName

            var keyFeature=""
            item.keyFeatures?.forEach { fea ->
                keyFeature += "• $fea\n"
            }

            if(item.discount==0)
            {
                binding.upSpecial.visibility= View.GONE
                binding.upOriginal.visibility=View.GONE
            }
            else
            {
                binding.upOriginal.text="Original Price-INR ${item.original}"
            }
            if(item.discount>0)
            {
                if(item.country=="india")
                binding.upUpgrade.text="UpGrade Now@INR ${item.amountToBePaid}"
                else
                    binding.upUpgrade.text="UpGrade Now@USD ${item.amountToBePaid}"
            }
            else
            {
                binding.upUpgrade.text="UpGrade Now@INR ${item.original}"
            }
            binding.upKeyFeature.text=keyFeature
            binding.upEmi.text="Interest FREE EMI starts@INR 4${item.emiAmount}"

            binding.upUpgrade.setOnClickListener {
                clickListener.onClickPoem(item)
            }
        }

    }

    override fun getItemCount(): Int {
        return list?.size!!
    }

    fun updateList(upList:ArrayList<UpgradeItem>)
    {
        list=upList
        notifyDataSetChanged()
    }
}
interface UpgradeAdapterInterface {
    fun onClickPoem(item:UpgradeItem)
}