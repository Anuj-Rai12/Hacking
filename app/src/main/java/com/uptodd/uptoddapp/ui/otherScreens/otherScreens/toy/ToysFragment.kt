package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.toy

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.toys.ToysDao
import com.uptodd.uptoddapp.databinding.FragmentToysBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.*
import com.uptodd.uptoddapp.utils.setUpErrorMessageDialog
import com.uptodd.uptoddapp.utils.toastMsg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class ToysFragment : Fragment(), ToysRecyclerAdapter.ToysListener {

    private lateinit var binding: FragmentToysBinding
    private var list = mutableListOf<Toy>()

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val isLoadingDialogVisible = MutableLiveData<Boolean>()

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private lateinit var toysDatabase: ToysDao

    private lateinit var preferences: SharedPreferences

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
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        preferences = requireActivity().getSharedPreferences("last_updated", Context.MODE_PRIVATE)
        toysDatabase = UptoddDatabase.getInstance(requireContext()).toysDatabaseDao

        initialiseBindingAndViewModel(inflater, container)

        ToolbarUtils.initToolbar(
            requireActivity(), binding.collapseToolbar,
            findNavController(), getString(R.string.toys), "Parenting Tools for You",
            R.drawable.milestone_icon
        )




        if (AllUtil.isUserPremium(requireContext())) {
            if (!AllUtil.isSubscriptionOverActive(requireContext())) {
                binding.upgradeButton.visibility = View.GONE
            }
        }
        binding.upgradeButton.setOnClickListener {

            it.findNavController().navigate(R.id.action_toysFragment_to_upgradeFragment)
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val lastUpdated = preferences.getString("TOYS", "")!!

        val today = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (lastUpdated.isBlank()) {
            fetchToyDataFromApi()
            preferences.edit {
                putString("TOYS", today.timeInMillis.toString())
            }
        } else if (lastUpdated.toLong() < today.timeInMillis) {
            fetchToyDataFromApi()
            preferences.edit {
                putString("TOYS", today.timeInMillis.toString())
            }
        } else {
            fetchToyDataFromLocalDb()
        }


        binding.toysRefresh.setOnRefreshListener {
            fetchToyDataFromApi()
        }
    }

    private fun fetchToyDataFromLocalDb() {
        toysDatabase.getAll().observe(viewLifecycleOwner, Observer { toys ->
            if (toys.isEmpty()) {
                fetchToyDataFromApi()
            } else {
                list = toys.toMutableList()
                setupRecyclerView()
            }
        })
    }

    private fun setupRecyclerView() {
        val adapter = ToysRecyclerAdapter(list, this)
        binding.recyclerView.adapter = adapter
    }

    private fun fetchToyDataFromApi() {
        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
            isLoadingDialogVisible.value = true
            showLoadingDialog()
            val language = ChangeLanguage(requireContext()).getLanguage()
            val userType = UptoddSharedPreferences.getInstance(requireContext()).getUserType()
            val stage = UptoddSharedPreferences.getInstance(requireContext()).getStage()
            val country = AllUtil.getCountry(requireContext())
            val userId = AllUtil.getUserId()
            uiScope.launch {
                AndroidNetworking.get("https://www.uptodd.com/api/toys/{age}?lang=$language&userType=$userType&country=$country&motherStage=$stage&userId=$userId")
                    .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                    .addPathParameter(
                        "age",
                        if (stage == "prenatal") (-1).toString() else KidsPeriod(requireActivity()).getKidsAge()
                            .toString()
                    )
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            if (response != null && response["data"] != "null") {
                                Log.d("putResposnse", response.get("status").toString())
                                val data = response.get("data") as JSONArray
                                Log.d("div", "ToysFragment L72 $data")
                                parseData(data)
                            } else {
                                if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
                                    if (!AllUtil.isUserPremium(requireContext())) {
                                        val title =
                                            (requireActivity() as AppCompatActivity).supportActionBar?.title

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
                                }
                            }

                            binding.toysRefresh.isRefreshing = false
                        }

                        override fun onError(anError: ANError?) {
                            setUpErrorMessageDialog()
                            binding.toysRefresh.isRefreshing = false
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
                    binding.toysRefresh.isRefreshing = true
                    fetchToyDataFromApi()
                }.show()
            binding.toysRefresh.isRefreshing = false
        }
    }


    private fun parseData(data: JSONArray) {
        if (context == null) {
            activity?.toastMsg("Oops SomeThing Went Wrong")
            return
        }
        val dpi = ScreenDpi(requireContext()).getScreenDrawableType()
        val appendable = "https://www.uptodd.com/images/app/android/thumbnails/toys/$dpi/"
        var i = 0
        list.clear()

        if (data.length() == 0) {
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
        } else {
            while (i < data.length()) {
                val fetchedTodoData = data.get(i) as JSONObject
                list.add(
                    Toy(
                        name = fetchedTodoData.getString("name"),
                        url = appendable + fetchedTodoData.getString("image") + ".webp",
                        image = fetchedTodoData.getString("image"),
                        description = fetchedTodoData.getString("description"),
                        minAge = fetchedTodoData.getInt("min_age"),
                        maxAge = fetchedTodoData.getInt("max_age")
                    )
                )
                i++
            }
        }


        ioScope.launch {
            toysDatabase.insertAll(list)
            isLoadingDialogVisible.postValue(false)
        }
        setupRecyclerView()
    }

    override fun onClickToy(view: View, position: Int) {

//        val bundle = Bundle()
//        bundle.putString("actionBar", getString(R.string.toys))
//        bundle.putString("image", list[position].image)
//        bundle.putString("title", list[position].name)
//        bundle.putString("description", list[position].description)
//        bundle.putString("folder", "toys")


        val direction = ToysFragmentDirections.actionToysFragmentToDetailsFragment(
            getString(R.string.toys),
            list[position].image,
            list[position].name,
            list[position].description,
            "toys"
        )

        findNavController().navigate(direction)

    }


    private fun initialiseBindingAndViewModel(inflater: LayoutInflater, container: ViewGroup?) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_toys, container, false)
    }

    private fun showLoadingDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_loading,
            getString(R.string.loading_please_wait),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    if (isAdded) {
                        dialog.dismiss()
                        findNavController().navigateUp()
                    }
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


