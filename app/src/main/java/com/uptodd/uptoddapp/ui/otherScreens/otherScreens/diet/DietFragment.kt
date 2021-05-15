package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.diet

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.diet.DietDao
import com.uptodd.uptoddapp.databinding.FragmentDietBinding
import com.uptodd.uptoddapp.utilities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class DietFragment : Fragment(), DietRecyclerAdapter.DietListener {

    private lateinit var binding: FragmentDietBinding

    private var list = mutableListOf<Diet>()

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val isLoadingDialogVisible = MutableLiveData<Boolean>()

    private lateinit var preferences: SharedPreferences
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private lateinit var dietDao: DietDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fadeThrough = MaterialFadeThrough().apply {
            duration = 1000
        }

        enterTransition = fadeThrough
        reenterTransition = fadeThrough
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        initialiseBindingAndViewModel(inflater, container)

        preferences = requireActivity().getSharedPreferences("last_updated", Context.MODE_PRIVATE)
        dietDao = UptoddDatabase.getInstance(requireContext()).dietDao


        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val lastUpdated = preferences.getString("DIET", "")!!
        val today = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (lastUpdated.isBlank()) {
            fetchDietDataFromApi()
            preferences.edit {
                putString("DIET", today.timeInMillis.toString())
            }
        } else if (lastUpdated.toLong() < today.timeInMillis) {
            fetchDietDataFromApi()
            preferences.edit {
                putString("DIET", today.timeInMillis.toString())
            }
        } else {
            fetchDietDataFromLocalDb()
        }

        binding.dietRefresh.setOnRefreshListener {
            fetchDietDataFromApi()
        }
    }

    private fun fetchDietDataFromLocalDb() {
        dietDao.getAll().observe(viewLifecycleOwner, Observer { diets ->
            if (diets.isEmpty()) {
                fetchDietDataFromApi()
            } else {
                list = diets.toMutableList()
                setupRecyclerView()
            }
        })
    }

    private fun fetchDietDataFromApi() {
        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
            isLoadingDialogVisible.value = true
            showLoadingDialog()
            val language = ChangeLanguage(requireContext()).getLanguage()
            uiScope.launch {
                AndroidNetworking.get("https://uptodd.com/api/diets/{period}?lang=$language")
                    .addPathParameter(
                        "period",
                        KidsPeriod(requireActivity()).getPeriod().toString()
                    )
                    .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            if (response != null) {
                                Log.d("putResposnse", response.get("status").toString())
                                val data = response.get("data") as JSONArray
                                Log.d("div", "DietFragment L72 $data")
                                parseData(data)
                            }
                            binding.dietRefresh.isRefreshing = false
                        }

                        override fun onError(anError: ANError?) {
                            binding.dietRefresh.isRefreshing = false
                        }
                    })
            }
        } else {
            Snackbar.make(
                requireView(),
                getString(R.string.no_internet_connection),
                Snackbar.LENGTH_LONG
            )
                .setAction(getString(R.string.retry)) {
                    binding.dietRefresh.isRefreshing = true
                    fetchDietDataFromApi()
                }.show()
            binding.dietRefresh.isRefreshing = false
        }

    }

    private fun parseData(data: JSONArray) {
        val dpi = ScreenDpi(requireContext()).getScreenDrawableType()
        val period = KidsPeriod(requireActivity()).getPeriod()
        val appendable = "https://uptodd.com/images/app/android/thumbnails/activities/$period/$dpi/"
        Log.d("div", "DietFragment L100 $data")
        var i = 0
        list.clear()
        while (i < data.length()) {
            val obj = data.get(i) as JSONObject
            Log.d("div", "DietFragment L82 $obj")
            list.add(
                Diet(
                    name = obj.getString("name"),
                    url = appendable + obj.getString("image") + ".webp",
                    image = obj.getString("image"),
                    description = obj.getString("description"),
                    period = obj.getInt("period"),
                    type = obj.getString("type")
                )
            )
            Log.d("div", "DietsFragment L117 ${list[i].url}")
            i++
        }

        ioScope.launch {
            dietDao.insertAll(list)
        }

        isLoadingDialogVisible.value = false

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = DietRecyclerAdapter(list, this)
        binding.recyclerView.adapter = adapter
    }

    private fun initialiseBindingAndViewModel(inflater: LayoutInflater, container: ViewGroup?) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_diet, container, false)
        binding.lifecycleOwner = this
    }

    override fun onClickToy(position: Int) {
        val bundle = Bundle()
        bundle.putString("actionBar", getString(R.string.diet))
        bundle.putString("image", list[position].image)
        bundle.putString("title", list[position].name)
        bundle.putString("description", list[position].description)
        bundle.putString("folder", "diets")
        findNavController().navigate(R.id.action_dietFragment_to_detailsFragment, bundle)
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
        isLoadingDialogVisible.observe(viewLifecycleOwner, Observer {
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