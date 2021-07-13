package com.uptodd.uptoddapp.media.music

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
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
import androidx.navigation.fragment.findNavController
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
import com.uptodd.uptoddapp.utilities.*
import com.uptodd.uptoddapp.utilities.downloadmanager.UpToddDownloadManager
import java.util.*

private const val musicTimerCode = 2402

class MusicFragment : Fragment() {

    private lateinit var binding: MusicFragmentBinding
    private lateinit var viewModel: MusicViewModel
    private lateinit var appContext: Context

    private lateinit var downloadManager: UpToddDownloadManager
    private lateinit var uptoddDialogs: UpToddDialogs

    private lateinit var preferences: SharedPreferences

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
        savedInstanceState: Bundle?,
    ): View {
        ChangeLanguage(requireContext()).setLanguage()

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.music_fragment,
            container,
            false
        )

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

        val lastUpdated: String = preferences.getString("last_updated", "")!!

        val today = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (lastUpdated.isBlank()) {
            updateMusic(today)
        } else if (lastUpdated.toLong() < today.timeInMillis) {
                updateMusic(today)
        } else {
            viewModel.initializeOffline()
        }

        setTimer(binding)

        if (!UpToddMediaPlayer.isPlaying || UpToddMediaPlayer.isMemoryBooster!!) {
            binding.musicPlayerLayout.visibility = View.GONE
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

    private fun updateMusic(today: Calendar) {
        if (AllUtil.isNetworkAvailable(requireContext()))
            viewModel.initializeAll()
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
            } else {
                binding.musicPlay.setImageResource(R.drawable.material_play)
            }
            val intent = Intent(requireContext(), BackgroundPlayer::class.java)
            intent.putExtra("toRun", true)
            intent.putExtra("musicType", "poem")
            requireContext().sendBroadcast(intent)
        })

        viewModel.image.observe(viewLifecycleOwner, Observer {
            if (it != "")
                Picasso.get()
                    .load(viewModel.image.value)
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
        list: HashMap<String, ArrayList<MusicFiles>>,
        binding: MusicFragmentBinding,
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
        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR       //to restrict landscape orientation

        super.onPause()
        val intent = Intent(requireContext(), BackgroundPlayer::class.java)
        intent.putExtra("toRun", true)
        intent.putExtra("musicType", "music")
        requireContext().sendBroadcast(intent)
    }

    override fun onResume() {
        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT       //to restrict landscape orientation

        super.onResume()
        val supportActionBar = (requireActivity() as AppCompatActivity).supportActionBar!!
        supportActionBar.title = getString(R.string.music)
        supportActionBar.setHomeButtonEnabled(true)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        val intent = Intent(requireContext(), BackgroundPlayer::class.java)
        intent.putExtra("toRun", false)
        intent.putExtra("musicType", "music")
        requireContext().sendBroadcast(intent)
    }

}