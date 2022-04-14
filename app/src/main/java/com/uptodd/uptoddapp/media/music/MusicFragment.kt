package com.uptodd.uptoddapp.media.music

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.UptoddViewModelFactory
import com.uptodd.uptoddapp.alarmsAndNotifications.UptoddAlarm
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.media.music.MusicFiles
import com.uptodd.uptoddapp.databinding.MusicFragmentBinding
import com.uptodd.uptoddapp.media.player.BackgroundPlayer
import com.uptodd.uptoddapp.media.player.MediaStopReceiver
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.*
import com.uptodd.uptoddapp.utilities.downloadmanager.UpToddDownloadManager
import java.util.*

import com.erkutaras.showcaseview.ShowcaseManager
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.models.SuggestedVideosModel
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.models.VideosUrlResponse
import com.uptodd.uptoddapp.ui.webinars.podcastwebinar.PodcastWebinarActivity
import org.json.JSONObject


private const val musicTimerCode = 2402

class MusicFragment : Fragment() {

    private lateinit var binding: MusicFragmentBinding
    private lateinit var viewModel: MusicViewModel
    private lateinit var appContext: Context

    private lateinit var downloadManager: UpToddDownloadManager
    private lateinit var uptoddDialogs: UpToddDialogs

    private lateinit var preferences: SharedPreferences

    companion object
    {
        var  dpi=""
    }
    private var videosRespons: VideosUrlResponse?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fadeThrough = MaterialFadeThrough().apply {
            duration = 500
        }
        enterTransition = fadeThrough
        reenterTransition = fadeThrough
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ChangeLanguage(requireContext()).setLanguage()

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.music_fragment,
            container,
            false
        )
        ToolbarUtils.initToolbar(
            requireActivity(), binding.collapseToolbar,
            findNavController(),getString(R.string.music),"Curated in UpTodd's Lab",
            R.drawable.music_icon
        )


        fetchTutorials(requireContext())

        binding.collapseToolbar.playTutorialIcon.setOnClickListener {

            fragmentManager?.let { it1 ->
                val intent = Intent(context, PodcastWebinarActivity::class.java)
                intent.putExtra("url", videosRespons?.music)
                intent.putExtra("title", "Music")
                intent.putExtra("kit_content","")
                intent.putExtra("description","")
                startActivity(intent)
            }


        }

        binding.collapseToolbar.playTutorialIcon.visibility=View.VISIBLE

        if(AllUtil.isUserPremium(requireContext()))
        {
            if(!AllUtil.isSubscriptionOverActive(requireContext()))
            {
                binding.upgradeButton.visibility= GONE
            }
        }
        binding.upgradeButton.setOnClickListener {

            it.findNavController().navigate(R.id.action_music_to_upgradeFragment)
        }


        preferences = requireActivity().getSharedPreferences("MUSIC", Context.MODE_PRIVATE)

        uptoddDialogs = UpToddDialogs(requireContext())

        downloadManager = UpToddDownloadManager(requireContext())

        binding.lifecycleOwner = this


        val dataSource = UptoddDatabase.getInstance(requireContext())
//        val viewModelFactory = MusicViewModelFactory(dataSource, requireActivity().application)

        val factory = UptoddViewModelFactory.getInstance(requireActivity().application)

        viewModel = ViewModelProvider(this, factory).get(MusicViewModel::class.java)
        binding.musicViewModel = viewModel
        viewModel.setDpi(ScreenDpi(requireContext()).getScreenDrawableType())
        viewModel.recheckState()


        val lastUpdated: String = preferences.getString("last_updated", "")!!

        val today = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (lastUpdated.isBlank() || UptoddSharedPreferences.getInstance(requireContext()).getMDownStatus()!!) {
            updateMusic(today)
        } else if (lastUpdated.toLong() < today.timeInMillis ) {
            updateMusic(today)
        } else {
            if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
                updateMusic(today)
            } else{
                viewModel.initializeOffline()
            }
        }

        setTimer(binding)

        if (!UpToddMediaPlayer.isPlaying || UpToddMediaPlayer.isMemoryBooster!!) {
            binding.musicPlayerLayout.visibility = View.GONE

        }
        else
        {
            Picasso.get()
                .load(AllUtil.getMusicImage(UpToddMediaPlayer.songPlaying, viewModel.getDpi()))
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.default_set_android_thumbnail)
                .into(binding.musicIcon)

        }
        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            when (it) {
                1 -> {
                    uptoddDialogs.showLoadingDialog(findNavController())
                }
                0 -> {
                    uptoddDialogs.dismissDialog()
                    redrawList(viewModel.musicFiles, binding)
                    initializeObservers(binding)
                }
                -1 -> {
                    uptoddDialogs.dismissDialog()
                    uptoddDialogs.showDialog(R.drawable.network_error,
                        getString(R.string.an_error_has_occurred) + { viewModel.apiError },
                        getString(R.string.close),
                        object : UpToddDialogs.UpToddDialogListener {
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                uptoddDialogs.dismissDialog()
                                findNavController().navigateUp()
                            }
                        })

                }
                else -> {
                    uptoddDialogs.dismissDialog()
                }
            }
        })


        return binding.root
    }

    private fun showHint(view:View){
        val builder =ShowcaseManager.Builder()
        builder.context(requireContext())
            .key("${AllUtil.getUserId()}")
            .view(view)
            .descriptionImageRes(R.mipmap.ic_launcher_round)
            .descriptionTitle("Music")
            .descriptionText(getString(R.string.screen_music))
            .buttonText("Done")
            .buttonVisibility(true)
            .cancelButtonVisibility(false)
            .roundedRectangle()
            .add()
            .build()
            .show()
    }


    private fun updateMusic(today: Calendar) {
        if (AllUtil.isNetworkAvailable(requireContext()))
            viewModel.initializeAll(requireContext())
        else
        {
            viewModel.initializeOffline()
        }
        preferences.edit {

            putString(
                "last_updated",
                today.timeInMillis.toString()
            )
            apply()
        }


    }


    private fun setTimer(binding: MusicFragmentBinding) {

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
                        UpToddMediaPlayer.mediaPlayer.isLooping = true
                        UptoddAlarm.setAlarm(
                            requireActivity(),
                            UpToddMediaPlayer.timer!!,
                            musicTimerCode,
                            name = "MusicStopAlarm",
                            broadcastReceiver = MediaStopReceiver::class.java
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
                        UpToddMediaPlayer.mediaPlayer.isLooping = true
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
                        UpToddMediaPlayer.mediaPlayer.isLooping = true
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
                UpToddMediaPlayer.mediaPlayer.isLooping = false
                Snackbar.make(
                    requireView(),
                    getString(R.string.timer_cancelled),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun initializeObservers(binding: MusicFragmentBinding) {


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

        viewModel.presetTimer.observe(viewLifecycleOwner, Observer {
            it.let {
                if (it != -1) {
                    UpToddMediaPlayer.timer = System.currentTimeMillis() + (it * 60 * 1000)
                    UpToddMediaPlayer.mediaPlayer.isLooping = true
                    UptoddNotificationUtilities.setAlarm(
                        requireContext(),
                        UpToddMediaPlayer.timer!!,
                        100,
                        MediaStopReceiver::class.java
                    )
                    Snackbar.make(
                        requireView(),
                        getString(R.string.we_ll_play_this_for) + " $it " + getString(R.string.minutes),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    viewModel.resetPresetTimer()
                }
            }
        })

        viewModel.isPlaying.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.musicPlay.setImageResource(R.drawable.material_pause)
                val intent = Intent(requireContext(), BackgroundPlayer::class.java)
                intent.putExtra("toRun", true)
                intent.putExtra("musicType", "poem")
                requireContext().sendBroadcast(intent)
            } else {
                val intent = Intent(requireContext(), BackgroundPlayer::class.java)
                intent.putExtra("toRun", true)
                intent.putExtra("musicType", "poem")
                requireContext().sendBroadcast(intent)
                binding.musicPlay.setImageResource(R.drawable.material_play)
            }
        })

        viewModel.image.observe(viewLifecycleOwner, Observer {
            if (it != "")
                Picasso.get()
                    .load(viewModel.image.value)
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.app_icon)
                    .resize(
                Conversion.convertDpToPixel(64F, requireContext()),
                Conversion.convertDpToPixel(64F, requireContext())
            )
                    .into(binding.musicIcon)

            Log.d("url",it)
        })

        viewModel.title.observe(viewLifecycleOwner, Observer {
            if (it != "")
                binding.musicTitle.text = viewModel.title.value
        })
        viewModel.isDownloaded.observe(viewLifecycleOwner, Observer {
            if(it){

            }else{
                ShowInfoDialog.showInfo("Musics are Downloading will add one by one till it is completed",
                requireActivity().supportFragmentManager);
            }
        })

    }

    private fun redrawList(
        list: HashMap<String, ArrayList<MusicFiles>>,
        binding: MusicFragmentBinding
    ) {

        if (list.isNotEmpty()) {
            viewModel.showLoading()
            val musicList = binding.musicList
            musicList.removeAllViews()
            Log.i("redraw", "redrawing")
            list.forEach {
                val inflater = LayoutInflater.from(requireContext())

                val v = inflater.inflate(R.layout.music_list_item, null)
                val musicCategoryTitle: TextView = v.findViewById(R.id.music_item_category_name)
                val musicCategoryList: LinearLayout = v.findViewById(R.id.music_item_layout)
                musicCategoryTitle.text = it.key

                musicCategoryList.removeAllViews()

                //for each category, add its music files
                it.value.forEach { music ->
                    val inflater1 =
                        requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val musicItemView = inflater1.inflate(R.layout.music_item, null)
                    val musicTitle: TextView = musicItemView.findViewById(R.id.music_item_title)
                    val musicImage: RoundedImageView =
                        musicItemView.findViewById(R.id.music_item_image)



                    musicTitle.text = music.name

                    Picasso.get()
                        .load(AllUtil.getMusicImage(music, viewModel.getDpi()))
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.default_set_android_thumbnail)
                        .into(musicImage)

                    musicItemView.setOnClickListener { _ ->
                        //if time is already set and the user changes music, cancel the timer
                        if (UpToddMediaPlayer.timer != null)
                            binding.musicTimer.performClick()
                        viewModel.playFile(music, it.key)
                        binding.musicTitle.text = music.name
                        binding.musicPlayerLayout.visibility = View.VISIBLE
                    }
                    musicItemView.setOnLongClickListener {
                        val fonts: Array<String> = arrayOf("Details")
                        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                        builder.setTitle(music.name)
                        builder.setItems(fonts) { _, _ ->
                            findNavController().navigate(
                                MusicFragmentDirections.actionMusicToDetails(
                                    "Music",
                                    music.id,
                                    music
                                )
                            )
                        }
                        builder.show()
                        true
                    }
                    val layoutParams =
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                    layoutParams.gravity = Gravity.CENTER
                    layoutParams.weight = 1F
                    layoutParams.rightMargin = 8
                    layoutParams.leftMargin = 8
                    layoutParams.gravity = Gravity.CENTER
                    musicItemView.layoutParams = layoutParams

                    musicCategoryList.addView(musicItemView)
                }
                musicList.addView(v)
            }

            viewModel.doneLoading()

            binding?.collapseToolbar?.tvLayout?.let { showHint(it) }
        }
        else
        {
            if (AppNetworkStatus.getInstance(requireContext()).isOnline && viewModel.notActive) {
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
    }

    private fun askPermissions() {
        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appContext = context
    }

    override fun onPause() {
        super.onPause()
        /*
        val intent = Intent(requireContext(), BackgroundPlayer::class.java)
        intent.putExtra("toRun", true)
        intent.putExtra("musicType", "music")
        requireContext().sendBroadcast(intent)
         */
    }

    override fun onResume() {
        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT       //to restrict landscape orientation

        super.onResume()
        val supportActionBar = (requireActivity() as AppCompatActivity).supportActionBar!!
        supportActionBar.title = getString(R.string.music)
        supportActionBar.setHomeButtonEnabled(true)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        /*
        val intent = Intent(requireContext(), BackgroundPlayer::class.java)
        intent.putExtra("toRun", false)
        intent.putExtra("musicType", "music")
        requireContext().sendBroadcast(intent)

         */
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