package com.uptodd.uptoddapp.ui.kitTutorial.fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.api.getPeriod
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.activitysample.ActivitySample
import com.uptodd.uptoddapp.database.kitTutorial.KitTutorial
import com.uptodd.uptoddapp.databinding.FragmentActivitySampleBinding
import com.uptodd.uptoddapp.databinding.FragmentKitTutorialBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.kitTutorial.adapters.KitTutorialAdapter
import com.uptodd.uptoddapp.ui.kitTutorial.adapters.KitTutorialInterface
import com.uptodd.uptoddapp.ui.webinars.fullwebinar.FullWebinarActivity
import com.uptodd.uptoddapp.ui.webinars.podcastwebinar.PodcastWebinarActivity
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.ShowInfoDialog
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

private const val TAG = "ActivitySampleFragment"

class KitTutorialFragment : Fragment(), KitTutorialInterface {


    private lateinit var binding: FragmentKitTutorialBinding


    private var kitTutorialList = mutableListOf<KitTutorial>()


    private val adapter = KitTutorialAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentKitTutorialBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

            binding.activitySampleRefresh.isRefreshing = true
        binding.upgradeButton.visibility=View.GONE
        binding.kitTutorialRecyclerView.layoutManager=GridLayoutManager(requireContext(),2)
          fetchDataFromApi()

        binding.activitySampleRefresh.setOnRefreshListener {
            hideNodata()
            fetchDataFromApi()
        }
    }



    private fun fetchDataFromApi() {
        val uid = AllUtil.getUserId()

        AndroidNetworking.get("https://www.uptodd.com/api/kitTutorials")
            .addQueryParameter("userId", uid.toString())
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
                        parseData(response.get("data").toString())
                    } catch (e: Exception) {
                        Log.i(TAG, "${e.message}")
                        showNoData()
                        hideRecyclerView()
                        return
                    } finally {
                        binding.activitySampleRefresh.isRefreshing = false
                    }
                }


                override fun onError(anError: ANError?) {
                    Log.e(TAG, "${anError?.message}")
                    binding.activitySampleRefresh.isRefreshing = false
                }

            })
    }

    private fun parseData(data: String) {

        kitTutorialList.clear()

        kitTutorialList.addAll(AllUtil.getAllKitTutorials(data))
       if(kitTutorialList.isEmpty()){
           showNoData()
           return
       } else
           setupRecyclerView()

        hideNodata()
    }

    private fun setupRecyclerView() {
        adapter.list = kitTutorialList
        binding.kitTutorialRecyclerView.adapter = adapter
        showRecyclerView()
    }


    private fun hideNodata() {
        binding.noDataContainer.isVisible = false
    }

    private fun showNoData() {
        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
                val title = (requireActivity() as AppCompatActivity).supportActionBar?.title

                val upToddDialogs = UpToddDialogs(requireContext())
                upToddDialogs.showInfoDialog("$title is not activated/required for you",
                    "Close",
                    object : UpToddDialogs.UpToddDialogListener {
                        override fun onDialogButtonClicked(dialog: Dialog) {
                            dialog.dismiss()

                        }

                        override fun onDialogDismiss() {
                            findNavController().navigateUp()
                        }
                    })

        }
        binding.noDataContainer.isVisible = true
    }

    private fun showRecyclerView() {
        binding.kitTutorialRecyclerView.isVisible = true
    }

    private fun hideRecyclerView() {
        binding.kitTutorialRecyclerView.isVisible = false
    }


    override fun onClick(kitTutorial: KitTutorial) {
        findNavController().navigate(KitTutorialFragmentDirections.
        actionKitTutorialFragmentToKitTutorialDetailsFragment(kitTutorial))
    }

}