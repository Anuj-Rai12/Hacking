package com.uptodd.uptoddapp.ui.order.orderlist

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.order.Order
import com.uptodd.uptoddapp.databinding.DialogExtendSubscriptionBinding
import com.uptodd.uptoddapp.databinding.FragmentOrderListBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import kotlinx.android.synthetic.main.order_item_view.view.*
import java.text.SimpleDateFormat
import java.util.*


class OrderListFragment : Fragment() {

    private lateinit var binding: FragmentOrderListBinding
    private lateinit var viewModel: OrderViewModel

    lateinit var preferences: SharedPreferences
    var row=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_order_list, container, false)
        binding.lifecycleOwner = this

        Log.d("div", "OrderListFragment L44")
        viewModel = ViewModelProvider(this).get(OrderViewModel::class.java)

        preferences =
            requireActivity().getSharedPreferences("LOGIN_INFO", AppCompatActivity.MODE_PRIVATE)
        if (preferences.contains("uid"))
            viewModel.userId = preferences.getString("uid", "").toString()
        if (preferences.contains("token"))
            viewModel.token = preferences.getString("token", "")

        Log.d("div", "OrderListFragment L51 ${viewModel.userId} ${viewModel.token}")

        (requireActivity() as AppCompatActivity?)?.supportActionBar?.title =
            getString(R.string.order_history)
        (requireActivity() as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        Log.d("div", "OrderListFragment L53")
        loadData()
        Log.d("div", "OrderListFragment L55")

        initObservers()


        binding.buttonUpgrade.setOnClickListener { onClickExtendSubscription() }
        binding.buttonExtendSubscription.setOnClickListener { onClickExtendSubscription() }
        val supportActionBar = (requireActivity() as AppCompatActivity).supportActionBar!!
        val country=if(UptoddSharedPreferences.getInstance(requireContext()).getPhone()?.startsWith("+91")!!)
            "india"
        else
            "row"
        if(country=="row") {
            row=true
            supportActionBar.title = "Expert Prescription"
        }
        displayOrders(createOrderList())


        return binding.root
    }

    private fun initObservers() {
        //Observing for data
        viewModel.allOrderList.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                Log.d(
                    "div",
                    "OrderListFragment L52 Observercalled ${viewModel.allOrderList.value!!}"
                )
                if (viewModel.allOrderList.value!!.isEmpty()) {
                   // binding.imageViewEmpty.visibility = View.VISIBLE
                    //binding.textViewEmpty.visibility = View.VISIBLE
                    //binding.buttonExtendSubscription.visibility = View.INVISIBLE
                } else {
                    binding.imageViewEmpty.visibility = View.INVISIBLE
                    binding.textViewEmpty.visibility = View.INVISIBLE
                    binding.buttonExtendSubscription.visibility = View.VISIBLE
                }
                displayOrders(viewModel.allOrderList.value!!)
            }
        })

        //Observing for extendSubscription request
        viewModel.isLoadingDialogVisible.observe(viewLifecycleOwner, Observer {
            Log.d("div", "OrderListFragment L153 ${viewModel.isLoadingDialogVisible.value}")
            if (!it) {
                if (viewModel.isExtendSubscriptionRequestMade) {
                    UpToddDialogs(requireContext()).showDialog(R.drawable.gif_done,
                        getString(R.string.the_request_for_extending_subscription_has_been_registered_we_ll_reach_out_out_to_you_soon),
                        getString(R.string.close),
                        object : UpToddDialogs.UpToddDialogListener {
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                dialog.dismiss()
                            }
                        })
                }
            }
        })
    }

    private fun loadData() {
        Log.d("div", "OrderListFragment L69")
        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
            showLoadingDialog()
            viewModel.getOrdersFromDatabase()
        } else {
            Log.d("div", "OrderListFragment L76")
            Snackbar.make(
                binding.layout,
                getString(R.string.no_internet_connection),
                Snackbar.LENGTH_LONG
            )
                .setAction(getString(R.string.retry)) {
                    loadData()
                }.show()
            //Toast.makeText(activity,"No internet connection",Toast.LENGTH_LONG).show()
        }
    }

    private fun createOrderList():List<Order>
    {

        var orderList= arrayListOf<Order>()
        for (i in 0..2)
        {
            var order=Order(i.toLong(),
                "${System.currentTimeMillis()}",
                (Math.random()%2).toLong()+1,
                "Demo $i",
                (Math.random()%2).toLong()+1,true,
                SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time),"","")
            orderList.add(order)
        }
        return orderList.toList()
    }

    private fun displayOrders(orderList: List<Order>?) {
        if (orderList != null) {
            for (order in orderList) {
                var childView: View
                val inflater =
                    activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                childView = inflater.inflate(R.layout.order_item_view, null)

                if(row) {
                    childView.textView2.text = " Prescription No: "
                }
                childView.textView_deliveryStatus.text="Date: "
                childView.textView_orderNo.text = order.orderNo
                childView.textView_monthNo.text ="Month: " + order.monthNo
                if(row)
                {
                    childView.textView_productNameAndQty.text = "Prescription Name: "+order.productname + " | " + getString(R.string.qty) + order.quantity
                }
                //val date=decodeDate(order.deliveryDate)
                childView.textView_date.text = "( " + order.deliveryDate + " )"
                if (order.details == "null" || order.details == null) {
                    childView.button_viewDetails.visibility = View.GONE
                    childView.button_download.visibility = View.GONE
                } else {
                    childView.button_viewDetails.setOnClickListener { onClickViewDetails(order) }
                    childView.button_download.setOnClickListener { onClickViewDetails(order) }
                }
                //childView.button_viewDetails.setOnClickListener{onClickViewDetails("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",order.orderNo)}
                //childView.button_download.setOnClickListener { onClickViewDetails("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",order.orderNo)}
                binding.linearLayout.addView(childView)
            }
        }

    }

    private fun onClickViewDetails(order: Order) {
        val bundle = Bundle()
        bundle.putSerializable("orderObject", order)
        //bundle.putString("url",detailsUrl)
        //bundle.putString("pdfName","UpToddOrder $orderNo.pdf")
        view?.findNavController()
            ?.navigate(R.id.action_orderListFragment_to_orderDetailsFragment2, bundle)
    }

    private fun decodeDate(deliveryDate: String?): String {
        if (deliveryDate!!.length < 8)
            return ""
        var date: String =
            "${deliveryDate[0]}${deliveryDate[1]}, ${deliveryDate[4]}${deliveryDate[5]}${deliveryDate[6]}${deliveryDate[7]}"
        val month = mapOf(
            "01" to "Jan",
            "02" to "Feb",
            "03" to "Mar",
            "04" to "Apr",
            "05" to "May",
            "06" to "June",
            "07" to "July",
            "08" to "Aug",
            "09" to "Sep",
            "10" to "Oct",
            "11" to "Nov",
            "12" to "Dec"
        )
        date = "(${month["${deliveryDate[2]}${deliveryDate[3]}"]} $date)"
        return date
    }

    private fun onClickExtendSubscription() {
        val dialogBinding = DataBindingUtil.inflate<DialogExtendSubscriptionBinding>(
            layoutInflater, R.layout.dialog_extend_subscription, null, false
        )
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogBinding.buttonYes.setOnClickListener {
            if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
                viewModel.isLoadingDialogVisible.value = true
                viewModel.isExtendSubscriptionRequestMade = false
                showLoadingDialog()
                viewModel.requestExtendSubscription()

                viewModel.isLoadingDialogVisible.observe(viewLifecycleOwner, Observer {
                    Log.d("div", "OrderListFragment L153 ${viewModel.isLoadingDialogVisible.value}")
                    if (!it) {
                        if (viewModel.isExtendSubscriptionRequestMade) {
                            dialog.dismiss()
                        }
                    }
                })

            } else {
                //showInternetNotConnectedDialog()
                Snackbar.make(
                    binding.layout,
                    getString(R.string.no_internet_connection),
                    Snackbar.LENGTH_LONG
                ).show()
                dialog.dismiss()
            }
        }
        dialogBinding.buttonNo.setOnClickListener { dialog.dismiss() }
        //dialog.setCancelable(false)
        dialog.show()

    }

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }*/

    private fun showInternetNotConnectedDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_loading,
            getString(R.string.no_internet_connection),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }
            })
    }

    private fun showLoadingDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_loading,
            getString(R.string.loading_please_wait),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }
            })
        viewModel.isLoadingDialogVisible.observe(viewLifecycleOwner, Observer {
            if (!it) {
                upToddDialogs.dismissDialog()
            }
        })
        val handler = Handler()
        handler.postDelayed({
            upToddDialogs.dismissDialog()
        }, R.string.loadingDuarationInMillis.toLong())

    }

}