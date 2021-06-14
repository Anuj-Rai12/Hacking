package com.uptodd.uptoddapp.ui.upgrade

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.facebook.all.All
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.UpgradeFragmentBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import java.text.SimpleDateFormat

class UpgradeFragment :Fragment(),UpgradeAdapterInterface
{
    var binding:UpgradeFragmentBinding?=null
    var viewModel:UpgradeViewModel?=null
    var adapter:UpgradeAdapter?=null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding= UpgradeFragmentBinding.inflate(inflater,container,false)
        viewModel=ViewModelProvider(this)[UpgradeViewModel::class.java]
        adapter= UpgradeAdapter(this)

        if(!AllUtil.isUserPremium(requireContext()))
        {
            val end=SimpleDateFormat("yyyy-MM-dd").parse("2021-6-4")

            if(AllUtil.isSubscriptionOver(end))
            {
                binding?.upgradeTitle?.text=getString(R.string.subscriptionEnded)
            }
        }

            binding?.upGradeRecyclerView?.adapter=adapter
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        var stage= UptoddSharedPreferences.getInstance(requireContext()).getStage()
        if(stage=="pre birth"|| stage=="prenatal")
        {
            binding?.imageView6?.setImageResource(R.drawable.pre_birth)
        }
        else
        {
            binding?.imageView6?.setImageResource(R.drawable.post_birth)
        }

        val dialogs=UpToddDialogs(requireContext())
        dialogs.showLoadingDialog(view?.findNavController()!!,true)
        viewModel?.checkIsPaymentDone(requireContext())

        viewModel?.isPaymentDone?.observe(viewLifecycleOwner, Observer {

            if(it)
            {
                dialogs.dismissDialog()
                if(!UpgradeViewModel.paymentDone) {
                    view?.findNavController()?.navigate(R.id.action_upgradeFragment_to_paymentSuccessFragment)
                }
                else
                {
                    view?.findNavController().navigateUp()
                    UpgradeViewModel.paymentDone=false
                }
            }
            else
            {
                viewModel?.getUpgradeList(requireContext())
                initObserver()
            }


        })
        viewModel?.isLoading?.observe(viewLifecycleOwner, Observer {
            if(!it)
            {
                dialogs.dismissDialog()
            }

        })
    }

    private fun initObserver()
    {


        viewModel?.upList?.observe(viewLifecycleOwner, Observer {
            adapter?.updateList(it)
        })


    }

    override fun onResume() {
        if(UpgradeViewModel.paymentStatus=="")
        {
            if(!AllUtil.isUserPremium(requireContext()))
            {
                val end=SimpleDateFormat("yyyy-MM-dd").parse(UptoddSharedPreferences.getInstance(requireContext()).getSubEnd())

                if(AllUtil.isSubscriptionOver(end))
                {
                    binding?.upgradeTitle?.text=getString(R.string.subscriptionEnded)
                }
            }

        }
        else
            binding?.upgradeTitle?.text=UpgradeViewModel.paymentStatus

            if(UpgradeViewModel.paymentStatus==PaymentActivity.PAYMENT_FAILED) {
                binding?.upgradeTitle?.setTextColor(Color.RED)
                binding?.upgradeTagline?.text =
                    "In a case of any query drop an email with payment details on support@uptodd.com"
            }
        super.onResume()
    }

    override fun onClickPoem(item: UpgradeItem) {

        viewModel?.notifySalesTeam(item,requireContext())
        val intent=Intent(requireActivity(),PaymentActivity::class.java)
        intent.putExtra(PaymentActivity.AMOUNT_KEY,item.amountToBePaid.toDouble())
        intent.putExtra(PaymentActivity.PAYMENT_TYPE,item.country)
        intent.putExtra(PaymentActivity.PAYMENT_PRODUCT_MONTH,item.productMonth)
        startActivity(intent)
    }


}