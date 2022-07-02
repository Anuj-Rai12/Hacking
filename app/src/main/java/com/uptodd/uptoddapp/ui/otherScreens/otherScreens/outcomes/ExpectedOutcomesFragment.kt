package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.outcomes

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.uptodd.uptoddapp.database.expectedoutcome.ExpectedOutcomeDao
import com.uptodd.uptoddapp.databinding.FragmentExpectedOutcomesBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.*
import com.uptodd.uptoddapp.utils.setUpErrorMessageDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class ExpectedOutcomesFragment : Fragment(), ExpectedOutcomesRecyclerAdapter.OutcomeListener {

    private lateinit var binding: FragmentExpectedOutcomesBinding

    private var list = mutableListOf<ExpectedOutcomes>()

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val isLoadingDialogVisible = MutableLiveData<Boolean>()

    private lateinit var preferences: SharedPreferences
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private lateinit var expectedOutcomeDao: ExpectedOutcomeDao

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

        initialiseBindingAndViewModel(inflater, container)

        ToolbarUtils.initToolbar(
            requireActivity(), binding.collapseToolbar,
            findNavController(), getString(R.string.expected_outcomes),
            "Parenting Tools for You",
            R.drawable.toys_icon
        )

        preferences =
            requireActivity().getSharedPreferences("last_updated", Context.MODE_PRIVATE)
        expectedOutcomeDao = UptoddDatabase.getInstance(requireContext()).expectedOutcomeDao


        if (AllUtil.isUserPremium(requireContext())) {
            if (!AllUtil.isSubscriptionOverActive(requireContext())) {
                binding.upgradeButton.visibility = View.GONE
            }
        }
        binding.upgradeButton.setOnClickListener {

            it.findNavController()
                .navigate(R.id.action_expectedOutcomesFragment3_to_upgradeFragment)
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val lastUpdated = preferences.getString("EXP_OUTCOME", "")!!

        val today = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (lastUpdated.isBlank()) {
            fetchOutcomesDataFromApi()
            preferences.edit {
                putString("EXP_OUTCOME", today.timeInMillis.toString())
                apply()
            }
        } else if (lastUpdated.toLong() < today.timeInMillis) {
            fetchOutcomesDataFromApi()
            preferences.edit {
                putString("EXP_OUTCOME", today.timeInMillis.toString())
                apply()
            }
        } else {
            fetchOutcomesDataFromLocalDb()
        }

        binding.expoutRefresh.setOnRefreshListener {
            fetchOutcomesDataFromApi()
        }

    }

    private fun fetchOutcomesDataFromLocalDb() {
        expectedOutcomeDao.getAll().observe(viewLifecycleOwner, Observer { expectedOutcomes ->
            if (expectedOutcomes.isEmpty()) {
                fetchOutcomesDataFromApi()
            } else {
                list = expectedOutcomes.toMutableList()
                setupRecyclerView()
            }
        })
    }

    private fun fetchOutcomesDataFromApi() {
        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
            isLoadingDialogVisible.value = true
            showLoadingDialog()
            val language = ChangeLanguage(requireContext()).getLanguage()
            val userType = UptoddSharedPreferences.getInstance(requireContext()).getUserType()
            val stage = UptoddSharedPreferences.getInstance(requireContext()).getStage()
            val country = AllUtil.getCountry(requireContext())
            uiScope.launch {
                val period = KidsPeriod(requireActivity()).getPeriod()
                AndroidNetworking.get("https://www.uptodd.com/api/expected_outcomes/{period}?lang=$language&userType=$userType&country=$country&motherStage=$stage")
                    .addPathParameter("period", period.toString())
                    .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            if (response != null && response["data"] != "null") {
                                Log.d("putResposnse", response.get("status").toString())
                                val data = response.get("data") as JSONArray
                                Log.d("div", "ExpectedOutcomesFragment L72 $data")
                                if (context != null) {
                                    parseData(data)
                                } else {
                                    activity?.let {
                                        Toast.makeText(
                                            it,
                                            "Oops something Went Wrong",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
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
                            binding.expoutRefresh.isRefreshing = false
                        }


                        override fun onError(anError: ANError?) {
                            binding.expoutRefresh.isRefreshing = false
                            setUpErrorMessageDialog()
                        }
                    })
            }
        } else {
            Snackbar.make(
                binding.root,
                getString(R.string.no_internet_connection),
                Snackbar.LENGTH_LONG
            )
                .setAction(getString(R.string.retry)) {
                    binding.expoutRefresh.isRefreshing = true
                    fetchOutcomesDataFromApi()
                }.show()

            binding.expoutRefresh.isRefreshing = false
        }
    }

    private fun parseData(data: JSONArray) {
        val dpi = ScreenDpi(requireContext()).getScreenDrawableType()
        val appendable =
            "https://www.uptodd.com/images/app/android/thumbnails/expected_outcomes/$dpi/"
        var i = 0
        list.clear()
        while (i < data.length()) {
            val obj = data.get(i) as JSONObject
            list.add(
                ExpectedOutcomes(
                    name = obj.getString("name"),
                    url = appendable + obj.getString("image") + ".webp",
                    image = obj.getString("image"),
                    period = obj.getInt("period"),
                    description = obj.getString("description")
                )
            )
            i++
        }
        if (data.length() == 0) {
            if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
                val title = (requireActivity() as AppCompatActivity).supportActionBar!!.title
                val upToddDialogs = UpToddDialogs(requireContext())
                upToddDialogs.showInfoDialog("$title is not activated/required for you",
                    "Close",
                    object : UpToddDialogs.UpToddDialogListener {
                        override fun onDialogButtonClicked(dialog: Dialog) {
                            dialog.dismiss()
                        }

                        override fun onDialogDismiss() {
                            findNavController().navigateUp()
                            super.onDialogDismiss()
                        }
                    })
            }
        }


        ioScope.launch {
            expectedOutcomeDao.insertAll(list)
        }

        isLoadingDialogVisible.value = false

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = ExpectedOutcomesRecyclerAdapter(list, this)
        binding.recyclerView.adapter = adapter
    }

    private fun initialiseBindingAndViewModel(inflater: LayoutInflater, container: ViewGroup?) {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_expected_outcomes, container, false)
        binding.lifecycleOwner = this
    }

    override fun onClickToy(position: Int) {
        val bundle = Bundle()
        bundle.putString("actionBar", getString(R.string.expected_outcomes))
        bundle.putString("image", list[position].image)
        bundle.putString("title", list[position].name)
        bundle.putString("description", list[position].description)
        bundle.putString("folder", "expected_outcomes")
        findNavController().navigate(
            R.id.action_expectedOutcomesFragment3_to_detailsFragment,
            bundle
        )
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