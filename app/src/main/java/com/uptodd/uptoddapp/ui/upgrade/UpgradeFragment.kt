package com.uptodd.uptoddapp.ui.upgrade

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.facebook.all.All
import com.razorpay.Checkout
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.logindetails.UserInfo
import com.uptodd.uptoddapp.databinding.UpgradeFragmentBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.todoScreens.TodosListActivity
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import org.json.JSONObject
import java.text.SimpleDateFormat

class UpgradeFragment :Fragment(),UpgradeAdapterInterface, TodosListActivity.RazorPayListener {
    var binding:UpgradeFragmentBinding?=null
    var viewModel:UpgradeViewModel?=null
    var adapter:UpgradeAdapter?=null
    private val checkout by lazy {
        Checkout()
    }
    private var productMonth:Int=0

    val dialogs by lazy {
        UpToddDialogs(requireContext())
    }

    companion object
    {
        var over=false
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding= UpgradeFragmentBinding.inflate(inflater,container,false)
        viewModel=ViewModelProvider(this)[UpgradeViewModel::class.java]
        adapter= UpgradeAdapter()

        if(!AllUtil.isUserPremium(requireContext()))
        {
            val end=SimpleDateFormat("yyyy-MM-dd").parse(UptoddSharedPreferences.getInstance(requireContext()).getSubEnd())


            if(AllUtil.isSubscriptionOver(end))
            {
                over=true
                val act=activity as AppCompatActivity
                act.supportActionBar?.hide()
                binding?.upgradeTitle?.text=getString(R.string.subscriptionEnded)
            }
        }

            binding?.upGradeRecyclerView?.adapter=adapter
        binding?.upGradeRecyclerView?.addItemDecoration(DividerItemDecoration(requireContext(),DividerItemDecoration.VERTICAL))
        return binding?.root
    }

    override fun onAttach(context: Context) {

        var ac=activity
        if(ac is TodosListActivity)
        {
            ac.rpListener=this
        }

        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        var stage= UptoddSharedPreferences.getInstance(requireContext()).getStage()
        if(stage=="pre birth"|| stage=="prenatal")
        {
            binding?.imageView6?.setImageResource(R.drawable.prenatal_up)
        }
        else
        {
            binding?.imageView6?.setImageResource(R.drawable.postnatal_up)
        }

        viewModel?.isPaymentDone?.observe(viewLifecycleOwner, Observer {

            if(it)
            {
                dialogs.dismissDialog()
                try {
                    if(!UpgradeViewModel.paymentDone) {
                        view?.findNavController()?.navigate(R.id.action_upgradeFragment_to_paymentSuccessFragment)
                    }
                    else
                    {
                        view?.findNavController().navigateUp()
                        UpgradeViewModel.paymentDone=false
                    }
                }
                catch (e:Exception)
                {
                    findNavController()?.navigate(R.id.action_upgradeFragment2_to_paymentSuccessFragment2)
                }
            }
            else
            {
                if(viewModel?.error!="")
                {
                    val upToddDialogs = UpToddDialogs(requireContext())
                    upToddDialogs.showInfoDialog("Server is busy right now,Please try again later","Close",
                        object :UpToddDialogs.UpToddDialogListener {
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                dialog.dismiss()
                            }

                            override fun onDialogDismiss() {
                                view?.findNavController().navigateUp()
                            }
                        }
                    )
                }
                else
                    adapter?.clickListener=this
            }


        })
        initObserver()
        viewModel?.isLoading?.observe(viewLifecycleOwner, Observer {
            if(!it)
            {
                dialogs.dismissDialog()
            }
            else
            {
                dialogs.showOnlyLoadingDialog()
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
        if(!AllUtil.isUserPremium(requireContext()))
        {
            val end=SimpleDateFormat("yyyy-MM-dd").parse(UptoddSharedPreferences.getInstance(requireContext()).getSubEnd())

            if(AllUtil.isSubscriptionOver(end))
            {
                binding?.upgradeTitle?.text=getString(R.string.subscriptionEnded)
                binding?.logout?.visibility=View.VISIBLE
                binding?.logout?.setOnClickListener {
                    AllUtil.logout(requireContext(),requireActivity())
                }
            }
        }


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
                adapter?.status=true
                adapter?.notifyDataSetChanged()
                binding?.upgradeTagline?.text =
                    "In a case of any query drop an email with payment details on support@uptodd.com"
            }

        dialogs?.showOnlyLoadingDialog()
        viewModel?.getUpgradeList(requireContext())
        viewModel?.checkIsPaymentDone(requireContext())
           /*
           else if(!UpgradeViewModel.paymentDone && UpgradeViewModel.paymentStatus==PaymentActivity.PAYMENT_SUCCESS)
            {
                try {
                    dialogs.dismissDialog()
                    findNavController()?.navigate(R.id.action_upgradeFragment_to_paymentSuccessFragment)
                }
                catch (e:Exception)
                {

                }
            }

            */
        super.onResume()
    }

    override fun onDestroy() {
        UpgradeViewModel.paymentDone=false
        UpgradeViewModel.paymentStatus=getString(R.string.up_program_title)
        dialogs?.dismissDialog()
        super.onDestroy()
    }



    override fun onClickOffer(item: UpgradeItem) {


        viewModel?.notifySalesTeam(item,requireContext())

        if(!AllUtil.isRow(requireContext()))
        {
            startRazorPayPayment(item.productMonth,item.amountToBePaid.toDouble(),requireContext())
        }
        else
        {
            val intent=Intent(requireActivity(),PaymentActivity::class.java)
            intent.putExtra(PaymentActivity.AMOUNT_KEY,item.amountToBePaid.toDouble())
            intent.putExtra(PaymentActivity.PAYMENT_TYPE,item.country)
            intent.putExtra(PaymentActivity.PAYMENT_PRODUCT_MONTH,item.productMonth)
            startActivity(intent)

        }

    }

    override fun onPaymentSuccess(id: String?) {
        UpgradeViewModel.paymentStatus = PaymentActivity.PAYMENT_SUCCESS
        id?.let { viewModel?.savePaymentDetails(requireContext(), it,productMonth ) }
        id?.let { Log.d("succces paymentID:id", it) }
    }

    override fun onPaymentFailure(d: Int, error: String?) {
        UpgradeViewModel.paymentStatus = PaymentActivity.PAYMENT_FAILED

    }


    private fun startRazorPayPayment(productMonth:Int,amounts: Double,context: Context) {

        this.productMonth=productMonth
        var paisa =
            amounts * 100 //Generate your razorpay key from Settings-> API Keys-> copy Key Id
        checkout.setKeyID(PaymentActivity.razorpayKey)

        val pref = UptoddSharedPreferences.getInstance(context)

        try {
            val options = JSONObject()
            options.put("name", pref.getName())
            options.put("description", "charge for $productMonth month plan")
            options.put("currency", "INR")
            options.put("amount", paisa)

            val preFill = JSONObject()
            pref.getEmail().let {   preFill.put("email",it)}
            pref.getPhone().let {  preFill.put("contact",it) }
            options.put("prefill", preFill)
            checkout.open(requireActivity(), options)

        } catch (e: Exception) {
            Toast.makeText(context, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

    }



}