package com.uptodd.uptoddapp.ui.order.orderdetails

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.order.Order
import com.uptodd.uptoddapp.databinding.FragmentOrderDetailsBinding
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.ToolbarUtils


class OrderDetailsFragment : Fragment() {

    private lateinit var binding:FragmentOrderDetailsBinding

    private var order:Order?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        arguments?.let {
            //param1 = it.getString(ARG_PARAM1)
            order = it.getSerializable("orderObject") as Order?
            Log.i("div", "OrderDetailsFragment L26${order.toString()}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        binding=DataBindingUtil.inflate(layoutInflater,
            R.layout.fragment_order_details,
            container,
            false)

        ToolbarUtils.initNCToolbar(requireActivity(),"Details",binding.toolbar,
            findNavController())
        (activity as AppCompatActivity).supportActionBar!!.title= if(order!=null) getString(R.string.order_no)+" "+order!!.orderNo
        else getString(R.string.order_details)

        setFields()

        return binding.root
    }

    private fun setFields() {
        if(order!=null) {
            binding.textViewOrderNo.text = getString(R.string.order_no)+" "+order!!.orderNo
            if(order!!.deliveryStatus)
                binding.textViewDate.text=getString(R.string.delivery_date)+" "+order!!.deliveryDate
            else
            binding.textViewDate.text=getString(R.string.date)+" "+order!!.deliveryDate
            binding.textViewDetails.text=order!!.details
        }
    }
}