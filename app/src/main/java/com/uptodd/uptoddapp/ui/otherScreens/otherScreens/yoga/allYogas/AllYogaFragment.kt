package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.yoga.allYogas

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
import com.uptodd.uptoddapp.database.yoga.YogaDao
import com.uptodd.uptoddapp.databinding.FragmentAllYogaBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class AllYogaFragment : Fragment(), AllYogaRecyclerAdapter.YogasListener {

    private lateinit var binding: FragmentAllYogaBinding

    private var list = mutableListOf<Yoga>()

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private lateinit var preferences: SharedPreferences
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private lateinit var yogaDao: YogaDao

    private val isLoadingDialogVisible = MutableLiveData<Boolean>()

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
            if(!AllUtil.isSubscriptionOverActive(requireContext()))
            {
                binding.upgradeButton.visibility= View.GONE
            }
        }
        binding.upgradeButton.setOnClickListener {

            it.findNavController().navigate(R.id.action_allYogaFragment_to_upgradeFragment)
        }

        ToolbarUtils.initNCToolbar(requireActivity(),"Yoga",binding.toolbar,
            findNavController())
        preferences = requireActivity().getSharedPreferences("last_updated", Context.MODE_PRIVATE)
        yogaDao = UptoddDatabase.getInstance(requireContext()).yogaDao


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

        val lastUpdated = preferences.getString("YOGA", "")!!

        if (lastUpdated.isBlank()) {
            fetchYogaDataFromApi()
            preferences.edit {
                putString("YOGA", today.timeInMillis.toString())
                apply()
            }
        } else if (lastUpdated.toLong() < today.timeInMillis) {
            fetchYogaDataFromApi()
            preferences.edit {
                putString("YOGA", today.timeInMillis.toString())
                apply()
            }
        } else {
            fetchYogaDataFromLocalDb()
        }

        binding.yogaRefresh.setOnRefreshListener {
            fetchYogaDataFromApi()
        }
    }

    private fun fetchYogaDataFromLocalDb() {
        yogaDao.getAll().observe(viewLifecycleOwner, Observer { yogas ->
            if (yogas.isEmpty()) {
                fetchYogaDataFromApi()
            } else {
                list.clear()
                val hashMap = HashMap<String, Boolean>()
                for (yoga in yogas) {
                    if (!hashMap.containsKey(yoga.name)) {
                        list.add(yoga)
                        Log.i("YOGA", "Added ${yoga.id}")
                    }
                    hashMap[yoga.name] = true
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
                AndroidNetworking.get("https://www.uptodd.com/api/yoga?lang=$language&userType=$userType")
                    .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            if (response != null) {
                                Log.d("putResposnse", response.get("status").toString())
                                val data = response.get("data") as JSONArray
                                parseData(data)
                            }
                            binding.yogaRefresh.isRefreshing = false
                        }

                        override fun onError(anError: ANError?) {
                            binding.yogaRefresh.isRefreshing = false
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
                    binding.yogaRefresh.isRefreshing = true
                    fetchYogaDataFromApi()
                }.show()
            binding.yogaRefresh.isRefreshing = false
        }
    }

    private fun parseData(data: JSONArray) {
        val dpi = ScreenDpi(requireContext()).getScreenDrawableType()
        val appendable = "https://www.uptodd.com/images/app/android/thumbnails/yogas/$dpi/"

        val yogaList = mutableListOf<Yoga>()

        for (i in 0 until data.length()) {
            val obj = data.get(i) as JSONObject
            val yoga = Yoga(
                id = obj.getInt("id"),
                name = obj.getString("name"),
                url = appendable + obj.getString("image") + ".webp",
                image = obj.getString("image"),
                steps = obj.getString("steps"),
                description = obj.getString("description")
            )

            yogaList.add(yoga)
        }

        list.clear()
        Log.i("YOGA", "${list.size}")
        val hashMap = HashMap<String, Boolean>()
        for (yoga in yogaList) {
            if (!hashMap.containsKey(yoga.name)) {
                list.add(yoga)
                Log.i("YOGA", "Added ${yoga.id}")
            }
            hashMap[yoga.name] = true
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


//        var i = 0
//        val hashMap = HashMap<String, Boolean>()
//        list.clear()
//        while (i < data.length()) {
//            val obj = data.get(i) as JSONObject
//            if (!hashMap.containsKey(obj.getString("name"))) {
//                list.add(
//                    Yoga(
//                        id = obj.getInt("id"),
//                        name = obj.getString("name"),
//                        url = appendable + obj.getString("image") + ".webp",
//                        image = obj.getString("image"),
//                        steps = obj.getString("steps"),
//                        description = obj.getString("description")
//                    )
//                )
//                hashMap[obj.getString("name")] = true
//                //Log.d("div","AllYogaFragment ${list[i].url}")
//            }
//            i++
//        }

        ioScope.launch {
            yogaDao.insertAll(yogaList)
        }

        isLoadingDialogVisible.value = false

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = AllYogaRecyclerAdapter(list, this)
        binding.recyclerView.adapter = adapter
    }

    private fun initialiseBindingAndViewModel(inflater: LayoutInflater, container: ViewGroup?) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_all_yoga, container, false)
        binding.lifecycleOwner = this
    }

    override fun onClickYoga(position: Int) {
        findNavController().navigate(
            AllYogaFragmentDirections.actionAllYogaFragmentToYogaFragment(
                list[position].name
            )
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