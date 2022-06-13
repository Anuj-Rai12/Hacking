package com.uptodd.uptoddapp.ui.activitysample

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.uptodd.uptoddapp.databinding.FragmentActivitySampleBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.models.SuggestedVideosModel
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.models.VideosUrlResponse
import com.uptodd.uptoddapp.ui.webinars.podcastwebinar.PodcastWebinarActivity
import com.uptodd.uptoddapp.utilities.*
import com.uptodd.uptoddapp.utils.setUpErrorMessageDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

private const val TAG = "ActivitySampleFragment"

class ActivitySampleFragment : Fragment(), ActivitySampleInterface {


    private lateinit var binding: FragmentActivitySampleBinding

    private val sharedPreferences: SharedPreferences by lazy {
        requireActivity().getSharedPreferences("last_updated", Context.MODE_PRIVATE)
    }

    private val uptoddDatabase: UptoddDatabase by lazy {
        UptoddDatabase.getInstance(requireContext())
    }

    private var activitySampleList = mutableListOf<ActivitySample>()

    private val ioScope = CoroutineScope(Dispatchers.Main)

    private val adapter = ActivitySampleAdapter(this)
    private var videosRespons: VideosUrlResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActivitySampleBinding.inflate(inflater, container, false)
        if (AllUtil.isUserPremium(requireContext())) {
            if (!AllUtil.isSubscriptionOverActive(requireContext())) {
                binding.upgradeButton.visibility = View.GONE
            }
        }
        binding.upgradeButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_activitySampleFragment_to_upgradeFragment)
        }

        if (UptoddSharedPreferences.getInstance(requireContext()).shouldShowSessionTip()) {
            ShowInfoDialog.showInfo(getString(R.string.screen_session), requireFragmentManager())
            UptoddSharedPreferences.getInstance(requireContext()).setShownSessionTip(false)
        }

        fetchTutorials(requireContext())

        binding.collapseToolbar.playTutorialIcon.setOnClickListener {

            fragmentManager?.let { it1 ->
                val intent = Intent(context, PodcastWebinarActivity::class.java)
                intent.putExtra("url", videosRespons?.session)
                intent.putExtra("title", "Sessions")
                intent.putExtra("kit_content", "")
                intent.putExtra("description", "")
                startActivity(intent)
            }

        }

        binding.collapseToolbar.playTutorialIcon.visibility = View.VISIBLE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ToolbarUtils.initToolbar(
            requireActivity(), binding.collapseToolbar,
            findNavController(), getString(R.string.activity_sample), "Curated in UpTodd's Lab",
            R.drawable.session_icon
        )

        val lastFetched = sharedPreferences.getLong("ACTIVITY_SAMPLE", -1)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (lastFetched == -1L || lastFetched < calendar.timeInMillis) {
            binding.activitySampleRefresh.isRefreshing = true
            fetchDataFromApi()
            sharedPreferences.edit().putLong("ACTIVITY_SAMPLE", calendar.timeInMillis).apply()
        } else {
            binding.activitySampleRefresh.isRefreshing = true
            fetchDataFromLocalDb()
        }

        binding.activitySampleRefresh.setOnRefreshListener {
            hideNodata()
            fetchDataFromApi()
        }
    }

    private fun fetchDataFromLocalDb() {
        uptoddDatabase.activitySampleDao.getAll().observe(viewLifecycleOwner, Observer {
            binding.activitySampleRefresh.isRefreshing = false
            if (it.isNullOrEmpty()) {
                showNoData()
                hideRecyclerView()
            } else {
                hideNodata()
                activitySampleList = it.toMutableList()
                setupRecyclerView()
            }
        })
    }

    private fun fetchDataFromApi() {
        val period = getPeriod(requireContext())
        val uid = AllUtil.getUserId()
        val userType = UptoddSharedPreferences.getInstance(requireContext()).getUserType()
        val stage = UptoddSharedPreferences.getInstance(requireContext()).getStage()
        val country = AllUtil.getCountry(requireContext())


        AndroidNetworking.get("https://www.uptodd.com/api/activitysample?userId={userId}&period={period}&userType=$userType&country=$country&motherStage=$stage")
            .addPathParameter("userId", uid.toString())
            .addPathParameter("period", period.toString())
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
                        val data = response.get("data") as JSONArray
                        if (data.length() <= 0) {
                            showNoData()
                            hideRecyclerView()
                        } else {
                            UptoddSharedPreferences.getInstance(requireContext())
                                .saveCountSession(data.length())
                            parseData(response.get("data") as JSONArray)

                            hideNodata()
                        }
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
                    setUpErrorMessageDialog()
                    binding.activitySampleRefresh.isRefreshing = false
                }

            })
    }

    private fun parseData(data: JSONArray) {

        activitySampleList.clear()
        for (i in 0 until data.length()) {
            val obj = data.get(i) as JSONObject

            val sample = ActivitySample(
                id = obj.getInt("id"),
                title = obj.getString("title"),
                video = obj.getString("video")
            )
            activitySampleList.add(sample)
        }

        ioScope.launch {
            uptoddDatabase.activitySampleDao.insertAll(activitySampleList)
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter.list = activitySampleList
        binding.activitySampleRecycler.adapter = adapter
        showRecyclerView()
    }


    private fun hideNodata() {
        binding.noDataContainer.isVisible = false
    }

    private fun showNoData() {
        try {
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
        } catch (e: Exception) {
            activity?.let {
                Toast.makeText(it, "Unknown Error Found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showRecyclerView() {
        binding.activitySampleRecycler.isVisible = true
    }

    private fun hideRecyclerView() {
        binding.activitySampleRecycler.isVisible = false
    }


    override fun onClick(act_sample: ActivitySample) {
        val intent = Intent(context, PodcastWebinarActivity::class.java)
        intent.putExtra("url", act_sample.video)
        intent.putExtra("title", act_sample.title)
        intent.putExtra("videos", SuggestedVideosModel(activitySampleList))
        startActivity(intent)
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
                    setUpErrorMessageDialog()
                }

            })
    }
}
