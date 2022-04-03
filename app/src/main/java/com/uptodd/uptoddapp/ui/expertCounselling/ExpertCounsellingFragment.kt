package com.uptodd.uptoddapp.ui.expertCounselling
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
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.api.getPeriod
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.activitysample.ActivitySample
import com.uptodd.uptoddapp.database.expertCounselling.ExpertCounselling
import com.uptodd.uptoddapp.databinding.FragmentExpertCounsellingBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.monthlyDevelopment.HomeExpertCounsellingDirections
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.models.VideosUrlResponse
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

class ExpertCounsellingFragment : Fragment(), ExpertCounsellingInterface {


    private lateinit var binding: FragmentExpertCounsellingBinding
    private var videosRespons: VideosUrlResponse?=null

    private val sharedPreferences: SharedPreferences by lazy {
        requireActivity().getSharedPreferences("last_updated", Context.MODE_PRIVATE)
    }

    private val uptoddDatabase: UptoddDatabase by lazy {
        UptoddDatabase.getInstance(requireContext())
    }

    private var expertCounsellingList = mutableListOf<ExpertCounselling>()

    private val ioScope = CoroutineScope(Dispatchers.Main)

    private val adapter = ExpertCounsellingAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExpertCounsellingBinding.inflate(inflater, container, false)
        if(AllUtil.isUserPremium(requireContext()))
        {
            if(!AllUtil.isSubscriptionOverActive(requireContext()))
            {
                binding.upgradeButton.visibility= View.GONE
            }
        }
        binding.upgradeButton.setOnClickListener {


            it.findNavController().navigate(R.id.action_activitySampleFragment_to_upgradeFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        fetchTutorials(requireContext())



        val lastFetched = sharedPreferences.getLong("EXPERT_COUNSELLING", -1)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (lastFetched == -1L || lastFetched < calendar.timeInMillis) {
            binding.expConeRefresh.isRefreshing = true
            fetchDataFromApi()
            sharedPreferences.edit().putLong("EXPERT_COUNSELLING", calendar.timeInMillis).apply()
        } else {
            binding.expConeRefresh.isRefreshing = true
            fetchDataFromApi()
        }

        binding.expConeRefresh.setOnRefreshListener {
            fetchDataFromApi()
        }
    }

    private fun fetchDataFromLocalDb() {
        uptoddDatabase.expertCounsellingDao.getAll().observe(viewLifecycleOwner, Observer {
            binding.expConeRefresh.isRefreshing = false
            if (it.isNullOrEmpty()) {
                fetchDataFromApi()
                hideRecyclerView()
            } else {
                hideNodata()
               expertCounsellingList = it.toMutableList()
                setupRecyclerView()
            }
        })
    }

    private fun fetchDataFromApi() {
        val uid = AllUtil.getUserId()
        AndroidNetworking.get("https://www.uptodd.com/api/appusers/previousSessionDetails/$uid")
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
                        val count=(response.get("data") as JSONObject).get("leftSessions") as Int
                        binding.sessionCount.text = "$count sessions left"
                        binding.sessionCount.visibility=View.VISIBLE

                        val data = (response.get("data") as JSONObject).get("previousSessions") as JSONArray
                        if (data.length() <= 0) {
                            showNoData()
                            hideRecyclerView()
                        } else {
                            parseData((response.get("data") as JSONObject).get("previousSessions").toString())
                            hideNodata()
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

    private fun parseData(jsonString: String) {

       expertCounsellingList.clear()
        val expList=AllUtil.getExpertCounselling(jsonString)
        expertCounsellingList.addAll(expList)

        ioScope.launch {
            uptoddDatabase.expertCounsellingDao.clear()
            uptoddDatabase.expertCounsellingDao.insertAll(expertCounsellingList)
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter.list = expertCounsellingList
        binding.expConRecycler.adapter = adapter
        showRecyclerView()
    }


    private fun hideNodata() {
        binding.noDataContainer.isVisible = false
    }

    private fun showNoData() {
        binding.noDataContainer.isVisible = true
    }

    private fun showRecyclerView() {
        binding.expConRecycler.isVisible = true
    }

    private fun hideRecyclerView() {
        binding.expConRecycler.isVisible = false
    }

    override fun onResume() {
        super.onResume()
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