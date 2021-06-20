package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.yoga

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
import com.uptodd.uptoddapp.database.yoga.YogaDao
import com.uptodd.uptoddapp.databinding.FragmentYogaBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.otherScreens.otherScreens.yoga.allYogas.Yoga
import com.uptodd.uptoddapp.utilities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


class YogaFragment : Fragment(), YogaRecyclerAdapter.YogasListener {

    private lateinit var binding: FragmentYogaBinding

    private var list = mutableListOf<Yoga>()

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var yogaName = ""

    private val isLoadingDialogVisible = MutableLiveData<Boolean>()

    private lateinit var yogaDao: YogaDao
    private lateinit var preferences: SharedPreferences
    private val ioScope = CoroutineScope(Dispatchers.IO)

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

        if(AllUtil.isUserPremium(requireContext()))
        {
            if(AllUtil.isSubscriptionOverActive(requireContext()))
            {
                binding.upgradeButton.visibility= View.GONE
            }
        }
        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.yoga)

        val args = YogaFragmentArgs.fromBundle(requireArguments())
        yogaName = args.yogaName

        yogaDao = UptoddDatabase.getInstance(requireContext()).yogaDao

        fetchYogaDataFromLocalDb()

        return binding.root
    }

    private fun fetchYogaDataFromLocalDb() {
        yogaDao.getAll().observe(viewLifecycleOwner, Observer { yogas ->
            if (yogas.isEmpty()) {
                fetchYogaDataFromApi()
            } else {
                var step = 0
                for (i in yogas.indices) {
                    if (yogas[i].name == yogaName) {
                        step++
                        list.add(
                            Yoga(
                                id = yogas[i].id,
                                name = yogas[i].name,
                                url = yogas[i].url,
                                image = yogas[i].image,
                                steps = yogas[i].steps,
                                description = yogas[i].description
                            )
                        )
                    }
                }
                setupRecyclerView()
            }
        })
    }

    private fun fetchYogaDataFromApi() {
        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
            isLoadingDialogVisible.value = true
            showLoadingDialog()
            val language = ChangeLanguage(requireContext()).getLanguage()
            val userType= UptoddSharedPreferences.getInstance(requireContext()).getUserType()
            uiScope.launch {
                AndroidNetworking.get("https://uptodd.com/api/yoga?lang=$language&userType=$userType")
                    .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            if (response != null && response["data"] != "null") {
                                Log.d("putResposnse", response.get("status").toString())
                                val data = response.get("data") as JSONArray
                                Log.d("div", "YogaFragment L72 $data")
                                parseData(data)
                            } else
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
                        }

                        override fun onError(anError: ANError?) {

                        }
                    })
            }
        } else {
            Snackbar.make(
                binding.layout,
                getString(R.string.no_internet_connection),
                Snackbar.LENGTH_LONG
            )
                .setAction(getString(R.string.retry)) {
                    fetchYogaDataFromApi()
                }.show()
        }
    }

    private fun parseData(data: JSONArray) {
        val dpi = ScreenDpi(requireContext()).getScreenDrawableType()
        val appendable = "https://uptodd.com/images/app/android/details/yogas/$dpi/"
        var i = 0
        var step = 0
        list.clear()
        while (i < data.length()) {
            val obj = data.get(i) as JSONObject
            if (obj.getString("name") == yogaName) {
                step++
                list.add(
                    Yoga(
                        id = obj.getInt("id"),
                        name = getString(R.string.step) + step,
                        url = appendable + obj.getString("image") + ".webp",
                        image = obj.getString("image"),
                        steps = obj.getString("steps"),
                        description = obj.getString("description")
                    )
                )
                Log.d("div", "YogaFragment ${list[list.size - 1].url}")
            }

            i++
        }

        isLoadingDialogVisible.value = false

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        list.sortBy { it.steps }
        val adapter = YogaRecyclerAdapter(list, this)
        binding.recyclerView.adapter = adapter
    }

    private fun initialiseBindingAndViewModel(inflater: LayoutInflater, container: ViewGroup?) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_yoga, container, false)
        binding.lifecycleOwner = this
    }

    override fun onClickYoga(position: Int) {
        val bundle = Bundle()
        bundle.putString("actionBar", getString(R.string.yoga))
        bundle.putString("image", list[position].image)
        bundle.putString("title", list[position].name)
        bundle.putString("description", list[position].description)
        bundle.putString("folder", "yoga")
        findNavController().navigate(R.id.action_yogaFragment_to_detailsFragment, bundle)
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