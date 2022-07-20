package com.uptodd.uptoddapp.media.memorybooster


import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.UptoddViewModelFactory
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.media.music.MusicFiles
import com.uptodd.uptoddapp.databinding.MemoryBoosterFragmentBinding
import com.uptodd.uptoddapp.databinding.PoemFragmentBinding
import com.uptodd.uptoddapp.media.player.MediaStopReceiver
import com.uptodd.uptoddapp.media.poem.PoemFragmentDirections
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.models.SuggestedVideosModel
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.models.VideosUrlResponse
import com.uptodd.uptoddapp.ui.webinars.podcastwebinar.PodcastWebinarActivity
import com.uptodd.uptoddapp.utilities.*
import com.uptodd.uptoddapp.utilities.downloadmanager.UpToddDownloadManager
import com.uptodd.uptoddapp.workManager.updateApiWorkmanager.CheckMemoryBoosterWorkManager
import org.json.JSONObject
import java.util.*


class MemoryBoosterFragment : Fragment(), SpeedBoosterAdpaterInterface {

    private lateinit var binding: MemoryBoosterFragmentBinding
    private lateinit var downloadManager: UpToddDownloadManager
    private lateinit var uptoddDialogs: UpToddDialogs
    private lateinit var viewModel: MemoryBoosterViewModel
    private lateinit var preferences: SharedPreferences
    private var videosRespons: VideosUrlResponse? = null

    private val adapter = SpeedBoosterAdapter(this)
    var count = 0

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
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.memory_booster_fragment,
            container,
            false

        )


        ToolbarUtils.initToolbar(
            requireActivity(), binding.collapseToolbar,
            findNavController(), getString(R.string.memory_booster), "Curated in UpTodd's Lab",
            R.drawable.memory_booster_icon
        )
        if (AllUtil.isUserPremium(requireContext())) {
            if (!AllUtil.isSubscriptionOverActive(requireContext())) {
                binding.upgradeButton.visibility = View.GONE
            }
        }
        binding.upgradeButton.setOnClickListener {

            it.findNavController().navigate(R.id.action_speedBoosterFragment_to_upgradeFragment)
        }
        preferences = requireActivity().getSharedPreferences("SPEED_BOOSTER", Context.MODE_PRIVATE)
        requestWorkManager()
        uptoddDialogs = UpToddDialogs(requireContext())

        downloadManager = UpToddDownloadManager(requireContext())

        binding.lifecycleOwner = this

        // setup recyclerView

        binding.poemListRecyclerView.adapter = adapter

        val dataSource = UptoddDatabase.getInstance(requireContext())
        val viewModelFactory =
            UptoddViewModelFactory.getInstance(requireActivity().application)

        viewModel =
            ViewModelProvider(this, viewModelFactory).get(MemoryBoosterViewModel::class.java)
        binding.speedBoosterViewModel = viewModel

        viewModel.setDpi(ScreenDpi(requireContext()).getScreenDrawableType())

        val lastUpdated = preferences.getString("last_updated", "")!!

        val today = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (lastUpdated.isBlank()) {
            updatePoems(today)
        } else if (lastUpdated.toLong() < today.timeInMillis) {
            updatePoems(today)
        } else {
            updatePoems(today)
        }


//        if (AllUtil.isNetworkAvailable(requireContext()))
//            viewModel.initializeAll()
//        else
//            viewModel.initializeOffline()


        setTimer(binding)

        viewModel.poems.observe(viewLifecycleOwner, Observer { poems ->
            Log.i("update", "$poems")
//            redrawList(poems, binding)
            if (poems.isEmpty() && viewModel.notActivate) {
                if (!AllUtil.isUserPremium(requireContext())) {
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
                binding.poemListGridView.isVisible = true
            } else {
                binding.poemListGridView.isVisible = false
                adapter.submitList(poems)
            }

        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            when (it) {
                1 -> {
                    uptoddDialogs.showLoadingDialog(findNavController())
                }
                0 -> {
                    initializeObservers(binding)
                    if (AppNetworkStatus.getInstance(requireContext()).isOnline && viewModel.notActivate) {
                        val title =
                            (requireActivity() as AppCompatActivity).supportActionBar!!.title
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
                    uptoddDialogs.dismissDialog()
                }
                -1 -> {
                    uptoddDialogs.dismissDialog()
                    uptoddDialogs.showDialog(R.drawable.network_error,
                        getString(R.string.an_error_has_occurred) + { viewModel.apiError },
                        getString(R.string.close),
                        object : UpToddDialogs.UpToddDialogListener {
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                val handler = Handler(Looper.getMainLooper())
                                var isShowDialogButton = false
                                handler.post {
                                    if (!isShowDialogButton){
                                        isShowDialogButton=true
                                        uptoddDialogs.dismissDialog()
                                        findNavController().navigateUp()
                                    }
                                }
                            }
                        })
                }
                else -> {

                }
            }
        })

        binding.poemRefresh.setOnRefreshListener {
            updatePoems(today)
        }
        Handler().postDelayed({
            ShowInfoDialog.showHint(
                requireActivity(),
                binding.collapseToolbar.tvLayout, "Memory Booster",
                getString(R.string.screen_booster),
                id*2
            )
        }, 1000)
        if (UptoddSharedPreferences.getInstance(requireContext()).shouldShowBoosterTip()) {
            UptoddSharedPreferences.getInstance(requireContext()).setShownBoosterTip(false)
        }

        fetchTutorials(requireContext())

        binding.collapseToolbar.playTutorialIcon.setOnClickListener {

            fragmentManager?.let { it1 ->
                val intent = Intent(context, PodcastWebinarActivity::class.java)
                intent.putExtra("url", videosRespons?.memoryBooster)
                intent.putExtra("title", "Memory Booster")
                intent.putExtra("kit_content", "")
                intent.putExtra("description", "")
                startActivity(intent)
            }


        }

        binding.collapseToolbar.playTutorialIcon.visibility = View.VISIBLE

        return binding.root
    }

    private fun updatePoems(today: Calendar) {
        if (AllUtil.isNetworkAvailable(requireContext())) {
            viewModel.initializeAll(requireContext())
            preferences.edit {
                putString("last_updated", today.timeInMillis.toString())
                apply()
            }
        }
        binding.poemRefresh.isRefreshing = false
    }

    private fun initializeObservers(binding: MemoryBoosterFragmentBinding) {


        viewModel.isMediaReady.observe(viewLifecycleOwner, Observer {
            it.let {
                if (it) {
                    binding.musicPlay.visibility = View.VISIBLE
                    binding.musicLoading.visibility = View.INVISIBLE
                    binding.musicLoading.setImageResource(R.drawable.media_loading)
                } else {
                    binding.musicPlay.visibility = View.INVISIBLE
                    binding.musicLoading.visibility = View.VISIBLE
                }
            }
        })


        viewModel.image.observe(viewLifecycleOwner, Observer {
            if (it != "")
                Picasso.get()
                    .load(viewModel.image.value)
                    .resize(
                        Conversion.convertDpToPixel(64F, requireContext()),
                        Conversion.convertDpToPixel(64F, requireContext())
                    )
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.app_icon)
                    .into(binding.musicIcon)
        })

        viewModel.title.observe(viewLifecycleOwner, Observer {
            if (it != "")
                binding.musicTitle.text = viewModel.title.value
        })
    }


    private fun redrawList(
        list: ArrayList<MusicFiles>,
        binding: PoemFragmentBinding
    ) {
        if (list.isNotEmpty()) {
            uptoddDialogs.showLoadingDialog(findNavController(), false)
            count = 0
            val poemList = binding.poemListGridView
            poemList.removeAllViews()
            var row = getNewRow()
            list.forEach { poem ->
                val inflater =
                    requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val v = inflater.inflate(R.layout.poem_list_item, null)
                val poemTitle: TextView = v.findViewById(R.id.poem_item_title)
                val poemImage: RoundedImageView = v.findViewById(R.id.poem_item_image)

                poemTitle.text = poem.name
                poemImage.setPadding(10, 5, 10, 5)
                poemImage.scaleType = ImageView.ScaleType.FIT_XY
//            if(AllUtil.isNetworkAvailable(requireContext()))
                Picasso.get()
                    .load(AllUtil.getPoemImage(poem, viewModel.getDpi()))
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_image)
                    .into(poemImage)

                Log.i("got", "${poem.name}")

                poemImage.setOnClickListener {
                    viewModel.playFile(poem)
                    binding.musicTitle.text = poem.name
                    binding.musicPlayerLayout.visibility = View.VISIBLE
                    //if time is already set and the user changes music, cancel the timer
                    if (UpToddMediaPlayer.timer != null)
                        binding.musicTimer.performClick()
                }
                poemImage.setOnLongClickListener {

                    val fonts: Array<String> = arrayOf("Details")
                    val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                    builder.setTitle(poem.name)
                    builder.setItems(fonts) { _, _ ->
                        findNavController().navigate(
                            PoemFragmentDirections.actionPoemFragmentToDetails(
                                "Poem",
                                poem.id,
                                poem
                            )
                        )
                    }
                    builder.show()
                    true
                }

                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

////            layoutParams.gravity = Gravity.CENTER
                layoutParams.weight = 1F
                layoutParams.rightMargin = 8
                layoutParams.leftMargin = 8
                layoutParams.gravity = Gravity.CENTER
                v.layoutParams = layoutParams
                v.setPadding(2, 2, 2, 2)

                poemList.addView(row)
                row = getNewRow()
                count = 0
                row.addView(v)
                count++
            }
            poemList.addView(row)
            uptoddDialogs.dismissDialog()
        }
    }

    fun getNewRow(): LinearLayout {
        val row = LinearLayout(requireContext())
        row.orientation = LinearLayout.HORIZONTAL
        val linearLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        linearLayoutParams.gravity = Gravity.CENTER
        linearLayoutParams.setMargins(8, 8, 8, 8)
        row.layoutParams = linearLayoutParams
        row.weightSum = 1f
        row.gravity = Gravity.CENTER
        return row
    }

    private fun setTimer(binding: MemoryBoosterFragmentBinding) {

        val dropDownMenu = PopupMenu(requireContext(), binding.musicTimer)
        val menu = dropDownMenu.menu
        menu.add(0, 0, 0, "15m")
        menu.add(0, 1, 0, "30m")
        menu.add(0, 2, 0, "45m")

        dropDownMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                0 -> {
                    if (UpToddMediaPlayer.songPlaying.name != null) {
                        UpToddMediaPlayer.timer = System.currentTimeMillis() + (15 * 60 * 1000)
                        UptoddNotificationUtilities.setAlarm(
                            requireContext(),
                            UpToddMediaPlayer.timer!!,
                            100,
                            MediaStopReceiver::class.java
                        )
                        Snackbar.make(
                            requireView(),
                            getString(R.string.timer_is_set_for_15_minutes),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    true
                }
                1 -> {
                    if (UpToddMediaPlayer.songPlaying.name != null) {
                        UpToddMediaPlayer.timer = System.currentTimeMillis() + (30 * 60 * 1000)
                        UptoddNotificationUtilities.setAlarm(
                            requireContext(),
                            UpToddMediaPlayer.timer!!,
                            100,
                            MediaStopReceiver::class.java
                        )
                        Snackbar.make(
                            requireView(),
                            getString(R.string.timer_is_set_for_30_minutes),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    true
                }
                2 -> {
                    if (UpToddMediaPlayer.songPlaying.name != null) {
                        UpToddMediaPlayer.timer = System.currentTimeMillis() + (45 * 60 * 1000)
                        UptoddNotificationUtilities.setAlarm(
                            requireContext(),
                            UpToddMediaPlayer.timer!!,
                            100,
                            MediaStopReceiver::class.java
                        )
                        Snackbar.make(
                            requireView(),
                            getString(R.string.timer_is_set_for_45_minutes),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    true
                }
                else -> false
            }
        }

        binding.musicTimer.setOnClickListener {
            //If timer is not set already
            if (UpToddMediaPlayer.timer == null) {
                dropDownMenu.show()
            }
            //Else cancel the timer
            else {
                UptoddNotificationUtilities.cancelAlarm(
                    requireContext(),
                    100,
                    MediaStopReceiver::class.java
                )
                UpToddMediaPlayer.timer = null
                Snackbar.make(
                    requireView(),
                    getString(R.string.timer_cancelled),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun onPause() {
        super.onPause()
        /*
        val intent = Intent(requireContext(), BackgroundPlayer::class.java)
        intent.putExtra("toRun", true)
        intent.putExtra("musicType", "poem")
        requireContext().sendBroadcast(intent)
         */
    }

    override fun onResume() {
        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT       //to restrict landscape orientation

        super.onResume()
        /*
        intent.putExtra("toRun", false)
        intent.putExtra("musicType", "poem")
        requireContext().sendBroadcast(intent)
         */
    }

    private fun askPermissions() {
        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
    }

    override fun onClickPoem(poem: MusicFiles, position: Int) {
        //if time is already set and the user changes music, cancel the timer
        val handler = Handler(Looper.getMainLooper())
        var isClickedPoem = false
        handler.post {
            if (!isClickedPoem) {
                isClickedPoem = true
                if (UpToddMediaPlayer.isPlaying) {
                    if (UpToddMediaPlayer.songPlaying.id != poem.id) {
                        UpToddMediaPlayer.isPlaying = false
                    }
                }
                preferences.edit().putInt("currentFileIndex", position).apply()

                try {
                    Navigation.findNavController(requireView())
                        .navigate(R.id.action_speedBoosterFragment_to_memoryBoosterDetailsFragment)
                } catch (e: Exception) {
                    activity?.let { act ->
                        Toast.makeText(act, "Please Try Again", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    private fun requestWorkManager() {

        val check = preferences.getInt("ALREADY_REQUESTED", 0)

        if (check == 0) {
            var workRequest = OneTimeWorkRequest.Builder(
                CheckMemoryBoosterWorkManager::class.java
            ).build()

            context?.let { WorkManager.getInstance(it).enqueue(workRequest) }

            preferences.edit().putInt("ALREADY_REQUESTED", 1).apply()
        }
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