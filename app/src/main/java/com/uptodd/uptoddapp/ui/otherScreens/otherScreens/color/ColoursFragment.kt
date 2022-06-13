package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.color

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
import androidx.recyclerview.widget.GridLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.colour.ColourDao
import com.uptodd.uptoddapp.databinding.FragmentColorsBinding
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


class ColoursFragment : Fragment(), ColoursRecyclerAdapter.ColoursListener {

    private lateinit var binding: FragmentColorsBinding

    private var list = mutableListOf<Colour>()

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val isLoadingDialogVisible = MutableLiveData<Boolean>()

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private lateinit var preferences: SharedPreferences

    private lateinit var colourDao: ColourDao

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

        binding?.toolbar?.let { ToolbarUtils.initNCToolbar(requireActivity(),"Colours", it,
            findNavController()) }

        preferences = requireActivity().getSharedPreferences("last_updated", Context.MODE_PRIVATE)
        colourDao = UptoddDatabase.getInstance(requireContext()).colourDao

        if(AllUtil.isUserPremium(requireContext()))
        {
            if(!AllUtil.isSubscriptionOverActive(requireContext()))
            {
                binding.materialButton.visibility= View.GONE
            }
        }
        binding.materialButton.setOnClickListener {

            it.findNavController().navigate(R.id.action_coloursFragment_to_upgradeFragment)
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val today = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val lastUpdated = preferences.getString("COLOR", "")!!

        if (lastUpdated.isBlank()) {
            binding.colorProgress.visibility = View.VISIBLE
            fetchColorsDataFromApi()
            preferences.edit {
                putString("COLOR", today.timeInMillis.toString())
                apply()
            }
        } else if (lastUpdated.toLong() < today.timeInMillis) {
            binding.colorProgress.visibility = View.VISIBLE
            fetchColorsDataFromApi()
            preferences.edit {
                putString("COLOR", today.timeInMillis.toString())
                apply()
            }
        } else {
            binding.colorProgress.visibility = View.VISIBLE
            fetchColorsDataFromLocalDb()
        }

        binding.colorRefresh.setOnRefreshListener {
            fetchColorsDataFromApi()
        }

    }

    private fun fetchColorsDataFromLocalDb() {
        colourDao.getAll().observe(viewLifecycleOwner, Observer { colours ->
            if (colours.isEmpty()) {
                fetchColorsDataFromApi()
            } else {
                list = colours.toMutableList()
                binding.colorProgress.visibility = View.GONE
                setUpRecyclerView()
            }
        })
    }

    private fun setUpRecyclerView() {

        val layoutManager = GridLayoutManager(requireContext(), 2)
        layoutManager.requestSimpleAnimationsInNextLayout()
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        val adapter = ColoursRecyclerAdapter(list, this)
        binding.recyclerView.adapter = adapter

    }

    private fun fetchColorsDataFromApi() {
        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
            val language = ChangeLanguage(requireContext()).getLanguage()
            isLoadingDialogVisible.value = true
            showLoadingDialog()
            val userType= UptoddSharedPreferences.getInstance(requireContext()).getUserType()
            val stage=UptoddSharedPreferences.getInstance(requireContext()).getStage()
            val country=AllUtil.getCountry(requireContext())
            uiScope.launch {
                AndroidNetworking.get("https://www.uptodd.com/api/colors?lang=$language&userType=$userType&country=$country&motherStage=$stage")
                    .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            if (response != null && response["data"]!="null") {
                                Log.d("putResposnse", response.get("status").toString())
                                val data = response.get("data") as JSONArray
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
                            binding.colorRefresh.isRefreshing = false
                            binding.colorProgress.visibility = View.GONE
                        }

                        override fun onError(anError: ANError?) {
                            binding.colorRefresh.isRefreshing = false
                            binding.colorProgress.visibility = View.GONE
                            setUpErrorMessageDialog()
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
                    binding.colorRefresh.isRefreshing = true
                    fetchColorsDataFromApi()
                }.show()
            binding.colorRefresh.isRefreshing = false
            binding.colorProgress.visibility = View.GONE
        }
    }

    private fun parseData(data: JSONArray) {
        val dpi = ScreenDpi(requireContext()).getScreenDrawableType()
        val appendable = "https://www.uptodd.com/images/app/android/thumbnails/colors/$dpi/"
        var i = 0
        list.clear()
        while (i < data.length()) {
            val obj = data.get(i) as JSONObject
            list.add(
                Colour(
                    name = obj.getString("name"),
                    url = appendable + obj.getString("image") + ".webp",
                    image = obj.getString("image"),
                    description = obj.getString("description")
                )
            )
            i++
        }


        if(data.length()==0)
        {
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
            colourDao.insertAll(list)
        }

        setUpRecyclerView()
        isLoadingDialogVisible.value = false

    }

    private fun initialiseBindingAndViewModel(inflater: LayoutInflater, container: ViewGroup?) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_colors, container, false)
        binding.lifecycleOwner = this

    }

    override fun onClickToy(position: Int) {
        val bundle = Bundle()
        bundle.putString("actionBar", getString(R.string.colours))
        bundle.putString("image", list[position].image)
        bundle.putString("title", list[position].name)
        bundle.putString("description", list[position].description)
        bundle.putString("folder", "colors")
        findNavController().navigate(R.id.action_coloursFragment_to_detailsFragment, bundle)
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