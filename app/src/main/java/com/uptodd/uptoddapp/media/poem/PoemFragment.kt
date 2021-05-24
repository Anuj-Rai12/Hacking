package com.uptodd.uptoddapp.media.poem

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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.isVisible
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
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.media.music.MusicFiles
import com.uptodd.uptoddapp.databinding.PoemFragmentBinding
import com.uptodd.uptoddapp.media.player.BackgroundPlayer
import com.uptodd.uptoddapp.media.player.MediaStopReceiver
import com.uptodd.uptoddapp.utilities.*
import com.uptodd.uptoddapp.utilities.downloadmanager.UpToddDownloadManager
import java.util.*


class PoemFragment : Fragment(), PoemAdapterInterface {

    private lateinit var binding: PoemFragmentBinding
    private lateinit var downloadManager: UpToddDownloadManager
    private lateinit var uptoddDialogs: UpToddDialogs
    private lateinit var viewModel: PoemViewModel
    private lateinit var preferences: SharedPreferences

    private val adapter = PoemAdapter(this)
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
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.poem_fragment,
            container,
            false
        )

        preferences = requireActivity().getSharedPreferences("POEM", Context.MODE_PRIVATE)

        uptoddDialogs = UpToddDialogs(requireContext())

        downloadManager = UpToddDownloadManager(requireContext())

        binding.lifecycleOwner = this

        // setup recyclerView

        binding.poemListRecyclerView.adapter = adapter

        val dataSource = UptoddDatabase.getInstance(requireContext())
        val viewModelFactory =
            UptoddViewModelFactory.getInstance(requireActivity().application)

        viewModel = ViewModelProvider(this, viewModelFactory).get(PoemViewModel::class.java)
        binding.poemViewModel = viewModel

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
            viewModel.initializeOffline()
        }


//        if (AllUtil.isNetworkAvailable(requireContext()))
//            viewModel.initializeAll()
//        else
//            viewModel.initializeOffline()

        setTimer(binding)

        if (!UpToddMediaPlayer.isPlaying || UpToddMediaPlayer.isMemoryBooster!!) {
            binding.musicPlayerLayout.visibility = View.GONE
        }


        viewModel.poems.observe(viewLifecycleOwner, Observer { poems ->
            Log.i("update", "$poems")
//            redrawList(poems, binding)
            if (poems.isEmpty()) {
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
                    uptoddDialogs.dismissDialog()
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

                }
            }
        })

        binding.poemRefresh.setOnRefreshListener {
            updatePoems(today)
        }

        return binding.root
    }

    private fun updatePoems(today: Calendar) {
        if (AllUtil.isNetworkAvailable(requireContext())) {
            viewModel.initializeAll()
            preferences.edit {
                putString("last_updated", today.timeInMillis.toString())
                apply()
            }
        }
        binding.poemRefresh.isRefreshing = false
    }

    private fun initializeObservers(binding: PoemFragmentBinding) {

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
        binding: PoemFragmentBinding,
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

                if (count == 2) {
                    poemList.addView(row)
                    row = getNewRow()
                    count = 0
                }
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
        row.weightSum = 2f
        row.gravity = Gravity.CENTER
        return row
    }

    private fun setTimer(binding: PoemFragmentBinding) {

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
        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR       //to restrict landscape orientation

        super.onPause()
        val intent = Intent(requireContext(), BackgroundPlayer::class.java)
        intent.putExtra("toRun", true)
        intent.putExtra("musicType", "poem")
        requireContext().sendBroadcast(intent)
    }

    override fun onResume() {
        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT       //to restrict landscape orientation

        super.onResume()
        val supportActionBar = (requireActivity() as AppCompatActivity).supportActionBar!!
        supportActionBar.title = getString(R.string.poem)
        supportActionBar.setHomeButtonEnabled(true)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        val intent = Intent(requireContext(), BackgroundPlayer::class.java)
        intent.putExtra("toRun", false)
        intent.putExtra("musicType", "poem")
        requireContext().sendBroadcast(intent)
    }

    private fun askPermissions() {
        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
    }

    override fun onClickPoem(poem: MusicFiles) {
        viewModel.playFile(poem)
        binding.musicTitle.text = poem.name
        binding.musicPlayerLayout.visibility = View.VISIBLE
        //if time is already set and the user changes music, cancel the timer
        if (UpToddMediaPlayer.timer != null)
            binding.musicTimer.performClick()
    }

}