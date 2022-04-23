package com.uptodd.uptoddapp.ui.recipe

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
import com.uptodd.uptoddapp.BR
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.api.getPeriod
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.activitysample.ActivitySample
import com.uptodd.uptoddapp.database.recipe.Recipe
import com.uptodd.uptoddapp.database.recipe.RecipeAdapter
import com.uptodd.uptoddapp.database.recipe.RecipeClickListener
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.models.SuggestedVideosModel
import com.uptodd.uptoddapp.ui.tutorials.TutorialAdapter
import com.uptodd.uptoddapp.ui.tutorials.TutorialInterface
import com.uptodd.uptoddapp.ui.webinars.fullwebinar.FullWebinarActivity
import com.uptodd.uptoddapp.databinding.FragmentActivitySampleBinding
import com.uptodd.uptoddapp.ui.webinars.podcastwebinar.PodcastWebinarActivity
import com.uptodd.uptoddapp.utilities.*
import kotlinx.android.synthetic.main.fragment_diet.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

private const val TAG = "ActivitySampleFragment"

class RecipeFragment : Fragment(), RecipeClickListener {


    private lateinit var binding: FragmentActivitySampleBinding


    private var activitySampleList = mutableListOf<Recipe>()


    private val adapter = RecipeAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActivitySampleBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ToolbarUtils.initToolbar(
            requireActivity(), binding.collapseToolbar,
            findNavController(),"Recipe","Happy Parenting Journey",
            R.drawable.recepie_icon_inner
        )
        binding.activitySampleRefresh.isRefreshing = true
        binding.upgradeButton.visibility=View.GONE
        fetchDataFromApi()

        binding.activitySampleRefresh.setOnRefreshListener {
            hideNodata()
            fetchDataFromApi()
        }
    }



    private fun fetchDataFromApi() {
        val uid = AllUtil.getUserId()

        AndroidNetworking.get("https://uptodd.com/api/recipeTutorialsV2")
            .addQueryParameter("userId", uid.toString())
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    if (response == null) return

                    val data = response.get("data") as JSONArray
                    Log.e("data",data.toString())
                    Log.i(TAG, "${response.get("data")}")

                    /* the get method doesn't support returning
                       nullable types so in order to handle nullable
                       objects try block is used to detect nullable
                       object.
                    */

                    try {
                        if (data.length() <= 0) {
                            showNoData()
                            hideRecyclerView()
                        } else {

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
                    binding.activitySampleRefresh.isRefreshing = false
                }

            })
    }

    private fun parseData(data: JSONArray) {
        activitySampleList.clear()
        for (i in 0 until data.length()) {
            val obj = data.get(i) as JSONObject

            val sample = Recipe(
                id = obj.getInt("id"),
                title = obj.getString("name"),
                description = obj.getString("description"),
                video = obj.getString("video"),
            )
            activitySampleList.add(sample)
        }
        Log.e("data", activitySampleList.toString())
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter.list = activitySampleList
        binding.activitySampleRecycler.adapter = adapter
        binding.activitySampleRecycler.visibility = View.VISIBLE
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
        binding.activitySampleRecycler.isVisible = true
    }

    private fun hideRecyclerView() {
        binding.activitySampleRecycler.isVisible = false
    }



    override fun onClick(recipe: Recipe) {
        val intent = Intent(context, PodcastWebinarActivity::class.java)
        intent.putExtra("url", recipe.video)
        intent.putExtra("title", recipe.title)
        intent.putExtra("description", recipe.description)
        intent.putExtra("videos",SuggestedVideosModel(mutableListOf()))
        startActivity(intent)
    }

}