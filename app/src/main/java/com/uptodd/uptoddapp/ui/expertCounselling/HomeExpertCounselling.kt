package com.uptodd.uptoddapp.ui.expertCounselling

import com.uptodd.uptoddapp.support.all.AllTicketsViewModel
import com.uptodd.uptoddapp.databinding.FragmentHomeExpertCounsellingBinding
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.transition.MaterialSharedAxis
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.expertCounselling.ExpertCounselling
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.models.VideosUrlResponse
import com.uptodd.uptoddapp.ui.webinars.podcastwebinar.PodcastWebinarActivity
import com.uptodd.uptoddapp.utilities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat

private const val TAG = "Expert Counselling"
class HomeExpertCounselling : Fragment(), ExpertCounsellingInterface {

    companion object {
        fun newInstance() = HomeExpertCounselling()
    }

    private var videosRespons: VideosUrlResponse?=null

    private lateinit var viewModel: AllTicketsViewModel
    private lateinit var binding: FragmentHomeExpertCounsellingBinding
    private lateinit var uptoddDialogs: UpToddDialogs

    private val sharedPreferences: SharedPreferences by lazy {
        requireActivity().getSharedPreferences("last_updated", Context.MODE_PRIVATE)
    }

    private val uptoddDatabase: UptoddDatabase by lazy {
        UptoddDatabase.getInstance(requireContext())
    }

    private var expertCounsellingList = mutableListOf<ExpertCounselling>()

    private val ioScope = CoroutineScope(Dispatchers.Main)

    private val adapter = ExpertCounsellingAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        uptoddDialogs = UpToddDialogs(requireContext())

        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_home_expert_counselling, container, false)
        binding?.lifecycleOwner = this

        ToolbarUtils.initToolbar(
            requireActivity(), binding?.collapseToolbar!!,
            findNavController(),getString(R.string.expert_counselling),"Happy Parenting Journey",
            R.drawable.counselling_icon
        )

        viewModel = ViewModelProvider(this).get(AllTicketsViewModel::class.java)
        binding?.allTicketsBinding = viewModel

        fetchTutorials(requireContext())
        binding?.collapseToolbar?.playTutorialIcon?.setOnClickListener {

            fragmentManager?.let { it1 ->
                val intent = Intent(context, PodcastWebinarActivity::class.java)
                intent.putExtra("url", videosRespons?.counselling)
                intent.putExtra("title", "Couselling Support")
                intent.putExtra("kit_content","")
                intent.putExtra("description","")
                startActivity(intent)
            }
        }

        binding?.collapseToolbar?.playTutorialIcon?.visibility=View.VISIBLE


        val end=SimpleDateFormat("yyyy-MM-dd").parse(UptoddSharedPreferences.getInstance(requireContext()).getAppExpiryDate())
        if(!AllUtil.isUserPremium(requireContext()))
        {
            val upToddDialogs = UpToddDialogs(requireContext())
            upToddDialogs.showInfoDialog("24*7 Support is only for Premium Subscribers","Close",
                object :UpToddDialogs.UpToddDialogListener
                {
                    override fun onDialogButtonClicked(dialog: Dialog) {
                        dialog.dismiss()
                    }

                    override fun onDialogDismiss() {
                        view?.findNavController()?.navigateUp()
                    }
                }
            )
        }
        else if(AllUtil.isSubscriptionOver(end))
        {
            val upToddDialogs = UpToddDialogs(requireContext())
            upToddDialogs.showInfoDialog("24*7 Support is only for Premium Subscribers","Close",
                object :UpToddDialogs.UpToddDialogListener
                {
                    override fun onDialogButtonClicked(dialog: Dialog) {
                        dialog.dismiss()
                    }

                    override fun onDialogDismiss() {
                        view?.findNavController()?.navigateUp()
                    }

                }
            )
        }
        else{
            fetchDataFromApi()
        }

        return binding.root
    }

    private fun fetchDataFromApi() {
        val uid = AllUtil.getUserId()
        AndroidNetworking.get("https://www.uptodd.com/api/appusers/allSessionDetails/$uid")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    if (response == null) return

                    Log.i(TAG, "${response.get("data")}")

                    /* the get method doesn't support returning
                       nullable types so in order to handle nullable
                       objects try block is used to detect nullable
                       object.
                    */

                    try {
                        val obj = response.get("data") as JSONObject
                        val data = obj.get("allSessions") as JSONArray
                        if (data.length() <= 0) {
                            showNoData()
                            hideRecyclerView()
                        } else {
                            parseData(AllUtil.getExpertCounselling(obj.get("allSessions").toString()))
                            hideNodata()


                        }
                        binding.tncText.setOnClickListener {
                            TermsAndConditions.show((response.get("data") as JSONObject).getString("tnc")
                                ,parentFragmentManager)
                        }
                        if (obj.getInt("sessionBookingAllowed") == 1) {

                            if(obj.getString("bookingLink").toString().isEmpty() ||
                                obj.getString("bookingLink")=="null") {
                                Log.d(TAG,"empty")
                                fetchBookingLink(obj.getInt("sessionBookingId"))
                            }
                            else {
                                binding.bookingButton.visibility=View.VISIBLE
                                val link=obj.getString("bookingLink")
                                binding.bookingButton.setOnClickListener {
                                    if(!TextUtils.isEmpty(link) && link.startsWith("http")) {
                                        var intent = Intent(
                                            Intent.ACTION_VIEW, Uri.parse(
                                                link
                                            )
                                        )
                                        startActivity(intent)
                                    }
                                }
                            }

                        }
                        else {
                            binding.bookingButton.visibility=View.GONE
                        }
                    } catch (e: Exception) {
                        Log.i(TAG, "${e.message}")
                        showNoData()
                        hideRecyclerView()
                        return
                    } finally {
                        binding.expConeRefresh.isRefreshing = false
                    }
                }


                override fun onError(anError: ANError?) {
                    Log.e(TAG, "${anError?.message}")
                    binding.expConeRefresh.isRefreshing = false
                }
            })
    }

    private fun parseData(data: ArrayList<ExpertCounselling>) {

        expertCounsellingList.clear()
        expertCounsellingList.addAll(data)
        setupRecyclerView()

        ioScope.launch {
            uptoddDatabase.expertCounsellingDao.clear()
            uptoddDatabase.expertCounsellingDao.insertAll(expertCounsellingList)
        }
    }

    private fun setupRecyclerView() {
        adapter.list = expertCounsellingList
        binding.expConRecycler.adapter = adapter
        showRecyclerView()
    }

    fun fetchBookingLink(sessionId:Int) {
        val uid = AllUtil.getUserId()
        AndroidNetworking.get("https://www.uptodd.com/api/appusers/getBookingLink/$uid?sessionBookingId=$sessionId")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    if (response == null) return

                    Log.i(TAG, "${response.get("data")}")

                    /* the get method doesn't support returning
                       nullable types so in order to handle nullable
                       objects try block is used to detect nullable
                       object.
                    */

                    try {
                        binding.bookingButton.visibility=View.VISIBLE
                        val data = (response.get("data") as String)
                        binding.bookingButton.setOnClickListener {
                            if(!TextUtils.isEmpty(data) && data.startsWith("http")) {
                                var intent = Intent(Intent.ACTION_VIEW, Uri.parse(data))
                                startActivity(intent)
                            }
                            else
                                binding.bookingButton.visibility=View.GONE
                        }


                    } catch (e: Exception) {
                        Log.i(TAG, e.message.toString())
                    } finally {
                        binding.expConeRefresh.isRefreshing = false
                    }
                }


                override fun onError(anError: ANError?) {
                    Log.e(TAG, "${anError?.message}")
                    binding.expConeRefresh.isRefreshing = false
                }

            })
    }


    private fun hideNodata() {
        binding.noDataContainer.visibility = View.GONE
    }

    private fun showNoData() {
        binding.noDataContainer.visibility = View.VISIBLE
    }

    private fun showRecyclerView() {
        binding.expConRecycler.visibility = View.VISIBLE
    }

    private fun hideRecyclerView() {
        binding.expConRecycler.visibility = View.GONE
    }


    override fun onClick(exp_con: ExpertCounselling) {
        findNavController().navigate(
            HomeExpertCounsellingDirections.
            actionHomeExpertCounsellingFragmentToExpertSuggestionsFragment(exp_con))
    }

    fun fetchTutorials(context: Context) {
        AndroidNetworking.get("https://uptodd.com/api/featureTutorials?userId=${AllUtil.getUserId()}")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    val data = response?.get("data") as JSONObject
                    videosRespons = AllUtil.getVideosUrlResponse(data.toString())
                }

                override fun onError(anError: ANError?) {

                }

            })
    }
}