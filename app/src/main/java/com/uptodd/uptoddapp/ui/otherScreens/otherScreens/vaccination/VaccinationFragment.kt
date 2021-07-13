package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.vaccination

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
import com.uptodd.uptoddapp.database.vaccination.VaccinationDao
import com.uptodd.uptoddapp.databinding.FragmentVaccinationBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class VaccinationFragment : Fragment(), VaccinationRecyclerAdapter.VaccinationListener {

    private lateinit var binding: FragmentVaccinationBinding

    private var list = mutableListOf<Vaccination>()

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val isLoadingDialogVisible = MutableLiveData<Boolean>()

    private lateinit var preferences: SharedPreferences
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private lateinit var vaccinationDao: VaccinationDao

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
        vaccinationDao = UptoddDatabase.getInstance(requireContext()).vaccinationDao

        if(AllUtil.isUserPremium(requireContext()))
        {
            if(!AllUtil.isSubscriptionOverActive(requireContext()))
            {
                binding.upgradebutton.visibility= View.GONE
            }
        }
        binding.upgradebutton.setOnClickListener {

            it.findNavController().navigate(R.id.action_vaccinationFragment_to_upgradeFragment)
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        val lastUpdated = preferences.getString("VACCINATION", "")!!

        val today = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (lastUpdated.isBlank()) {
            fetchVaccinationDataFromApi()
            preferences.edit {
                putString("VACCINATION", today.timeInMillis.toString())
                apply()
            }
        } else if (lastUpdated.toLong() < today.timeInMillis) {
            fetchVaccinationDataFromApi()
            preferences.edit {
                putString("VACCINATION", today.timeInMillis.toString())
                apply()
            }
        } else {
            fetchVaccinationDataFromLocalDb()
        }

        binding.vaccinationRefresh.setOnRefreshListener {
            fetchVaccinationDataFromApi()
        }
    }

    private fun fetchVaccinationDataFromLocalDb() {
        vaccinationDao.getAll().observe(viewLifecycleOwner, Observer { vaccinations ->
            if (vaccinations.isEmpty()) {
                fetchVaccinationDataFromApi()
            } else {
                list = vaccinations.toMutableList()
                setupRecyclerView()
            }
        })
    }

    private fun fetchVaccinationDataFromApi() {
        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
            isLoadingDialogVisible.value = true
            showLoadingDialog()
            val language = ChangeLanguage(requireContext()).getLanguage()
            val userType= UptoddSharedPreferences.getInstance(requireContext()).getUserType()
            uiScope.launch {
                AndroidNetworking.get("https://uptodd.com/api/vaccination?lang=$language&userType=$userType")
                    .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            if (response != null && response["data"]!="null") {
                                Log.d("putResposnse", response.get("status").toString())
                                val data = response.get("data") as JSONArray
                                Log.d("div", "VaccinationFragment L72 $data")
                                parseData(data)
                            }
                            else
                            {
                                if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
                                    if (!AllUtil.isUserPremium(requireContext())) {
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
                                }
                            }
                            binding.vaccinationRefresh.isRefreshing = false
                        }

                        override fun onError(anError: ANError?) {
                            binding.vaccinationRefresh.isRefreshing = false
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
                    binding.vaccinationRefresh.isRefreshing = true
                    fetchVaccinationDataFromApi()
                }.show()
            binding.vaccinationRefresh.isRefreshing = false
        }
    }

    private fun parseData(data: JSONArray) {
        val dpi = ScreenDpi(requireContext()).getScreenDrawableType()
        val appendable =
            "https://uptodd.com/images/app/android/details/vaccination/vaccination.webp"
        var i = 0
        list.clear()
        while (i < data.length()) {
            val obj = data.get(i) as JSONObject
            Log.d("div", "VaccinationFragment L82 $obj")
            list.add(
                Vaccination(
                    name = obj.getString("age"),
                    url = appendable,
                    image = obj.getString("image"),
                    description = obj.getString("name"),
                    age = obj.getString("age")
                )
            )
            i++
        }

        ioScope.launch {
            vaccinationDao.insertAll(list)
        }

        isLoadingDialogVisible.value = false

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = VaccinationRecyclerAdapter(list, this)
        binding.recyclerView.adapter = adapter
    }

    private fun initialiseBindingAndViewModel(inflater: LayoutInflater, container: ViewGroup?) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_vaccination, container, false)
        binding.lifecycleOwner = this
    }

    override fun onClickToy(position: Int) {
        val bundle = Bundle()
        bundle.putString("actionBar", getString(R.string.vaccination))
        bundle.putString("image", "vaccination")
        bundle.putString("title", list[position].name)
        bundle.putString("description", list[position].description)
        bundle.putString("folder", "vaccination")
        findNavController().navigate(R.id.action_vaccinationFragment_to_detailsFragment, bundle)
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