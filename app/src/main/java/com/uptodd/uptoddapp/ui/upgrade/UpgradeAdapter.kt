package com.uptodd.uptoddapp.ui.upgrade

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.KeyItemLayoutBinding
import com.uptodd.uptoddapp.databinding.PoemListItemBinding
import com.uptodd.uptoddapp.databinding.UpgradeOfferListItemBinding

class UpgradeAdapter(val clickListener: UpgradeAdapterInterface) : RecyclerView.Adapter<UpgradeAdapter.UpgradeViewHolder>(){

    var list:ArrayList<UpgradeItem>?= ArrayList()
    var status=false

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
            if(status)
            {
                binding.upKeyFeature.visibility=View.GONE
            }

            var keyFeature=""
            item.keyFeatures?.forEach { fea ->
                keyFeature += "• $fea\n"
            }
            val currency=
                if (item.country=="india")"INR"
                else
                    "USD"

            if(item.discount==0)
            {
                binding.upSpecial.visibility= View.GONE
                binding.upOriginal.visibility=View.GONE
            }
            else
            {
                binding.upOriginal.text="Original Price-$currency ${item.original}"

            }

            if(item.productMonth==6)
            {
                binding.upSpecial.setTextColor(binding.root.context.resources.getColor(R.color.buttonBlue))
                binding.upUpgrade.setBackgroundColor(binding.root.context.resources.getColor(R.color.solidUpColor))
                binding.upUpgrade.background=ColorDrawable(binding.root.context.resources.getColor(R.color.solidUpColor))
            }
            if(item.discount>0)
            {

                    binding.upUpgrade.text="UpGrade Now @  $currency ${item.amountToBePaid}"
                if (item.country == "india")
                    binding.upEmi.text="Interest FREE EMI start @ $currency ${item.emiAmount}"
                else
                    binding.upEmi.text=""
            }
            else {
                if (item.country == "india")
                    binding.upEmi.text="Interest FREE EMI start @ $currency ${item.emiAmount}"
                else
                    binding.upEmi.text=""

                    binding.upUpgrade.text = "UpGrade Now @ $currency ${item.amountToBePaid}"
            }
            val keyAdapter= item.keyFeatures?.let { KeyAdapter(it) }
            binding.upKeyFeature.adapter=keyAdapter

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

    inner class KeyAdapter(var keyList:ArrayList<String>):
        RecyclerView.Adapter<KeyAdapter.KeyViewHolder>()
    {





        inner class KeyViewHolder(private val binding: KeyItemLayoutBinding):RecyclerView.ViewHolder(binding.root)
        {
            fun bind(key:String)
            {
                binding.keyText.text=key
            }

        }

        override fun getItemCount(): Int {
            return keyList.size
        }

        override fun onBindViewHolder(holder: KeyViewHolder, position: Int) {
           holder.bind(keyList[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeyViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = KeyItemLayoutBinding.inflate(inflater, parent, false)
            return KeyViewHolder(binding)
        }

    }
}

interface UpgradeAdapterInterface {
    fun onClickPoem(item:UpgradeItem)
}