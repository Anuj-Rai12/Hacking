package com.uptodd.uptoddapp.ui.home.homePage

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialSharedAxis
import com.squareup.picasso.Picasso
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.adapters.TodoViewPagerAdapter
import com.uptodd.uptoddapp.database.webinars.Webinars
import com.uptodd.uptoddapp.databinding.FragmentHomePageBinding
import com.uptodd.uptoddapp.helperClasses.DateClass
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.blogs.fullblog.FullBlogActivity
import com.uptodd.uptoddapp.ui.home.homePage.childFragments.DailyFragment
import com.uptodd.uptoddapp.ui.home.homePage.childFragments.EssentialsFragment
import com.uptodd.uptoddapp.ui.home.homePage.childFragments.MonthlyFragment
import com.uptodd.uptoddapp.ui.home.homePage.childFragments.WeeklyFragment
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.TodosViewModel
import com.uptodd.uptoddapp.utilities.*
import com.uptodd.uptoddapp.utilities.downloadmanager.JishnuDownloadManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class HomePageFragment : Fragment() {

    private lateinit var uptoddDialogs: UpToddDialogs
    private lateinit var binding: FragmentHomePageBinding
    private val viewModel: TodosViewModel by activityViewModels()
    val dialogs by lazy {
        UpToddDialogs(requireContext())
    }

    var dialogShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        ChangeLanguage(requireContext()).setLanguage()

        Log.d("div", "${System.currentTimeMillis()}")

        initialiseBindingAndViewModel(inflater, container)
        initiateDataRefresh()
        setupViewPager()
        viewModel.loadDailyTodoScore()
        initialiseScoreDisplay()
        initialiseBabyPhoto()
        initialiseOtherInformation()
        loadTodaysTip()

        uptoddDialogs = UpToddDialogs(requireContext())


        // if this is first time login save the launch time
        val preferences = activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        if (!preferences!!.contains("LaunchTime")) {
            preferences.edit()?.let { editor ->
                editor.putLong("LaunchTime", System.currentTimeMillis())
                editor.apply()
            }
        }

        viewModel.dpi = ScreenDpi(requireContext()).getScreenDrawableType()
        viewModel.getWebinars()
        viewModel.showDownloadingFlag.observe(viewLifecycleOwner, Observer {
            if (it) {
                if (!dialogShown) {
                    //Toast.makeText(requireContext(), "Downloading media files.", Toast.LENGTH_SHORT).show()
                    dialogShown = true
                }
            }
        })

//        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
//            it.let {
//                when (it) {
//                    0 -> {
//                        uptoddDialogs.dismissDialog()
//                    }
//                    1 -> {
//                        uptoddDialogs.showLoadingDialog(findNavController())
//                    }
//                    -1 -> {
//                        uptoddDialogs.dismissDialog()
//                        uptoddDialogs.showDialog(R.drawable.network_error,
//                            "An error has occurred: ${viewModel.apiError}.",
//                            "Close",
//                            object : UpToddDialogs.UpToddDialogListener {
//                                override fun onDialogButtonClicked(dialog: Dialog) {
//                                    uptoddDialogs.dismissDialog()
//                                }
//                            })
//                    }
//                }
//            }
//        })

        viewModel?.isDataOutdatedFlag.observe(viewLifecycleOwner, Observer {

            if(it)
            {
               changeToLoading()
            }
            else
            {
                changeToNormalLayout()
            }


        })
        viewModel.notificationIntent.observe(viewLifecycleOwner, Observer {
            when (viewModel.notificationIntent.value) {
                1 -> {
                    viewModel.openDailyTodos()
                    viewModel.notificationIntent.value = DEFAULT_HOMEPAGE_INTENT
                    findNavController().navigate(HomePageFragmentDirections.actionHomePageFragmentToTodosViewPagerFragment())
                }
                2 -> {
                    viewModel.openWeeklyTodos()
                    viewModel.notificationIntent.value = DEFAULT_HOMEPAGE_INTENT
                    findNavController().navigate(HomePageFragmentDirections.actionHomePageFragmentToTodosViewPagerFragment())
                }
                3 -> {
                    viewModel.openMonthlyTodos()
                    viewModel.notificationIntent.value = DEFAULT_HOMEPAGE_INTENT
                    findNavController().navigate(HomePageFragmentDirections.actionHomePageFragmentToTodosViewPagerFragment())
                }
            }
        })

        checkNavigation()

        binding.btnSeeAllActivites.setOnClickListener {
//            val freshness = validateFreshness()
//            val dailyTabIsSelected = viewModel.tabPosition.value == DAILY_TODOS_TAB_POSITION

//            if (!freshness && dailyTabIsSelected) {
//                val snackbar = Snackbar.make(
//                    binding.mainConstraintLayout,
//                    getString(R.string.no_internet_connection),
//                    Snackbar.LENGTH_LONG
//                )
//                snackbar.show()
//
//                return@setOnClickListener
//            } else {
            val direction =
                HomePageFragmentDirections.actionHomePageFragmentToTodosViewPagerFragment()
            findNavController().navigate(direction)
//            }
        }

        binding.appGuidelinesBtn.setOnClickListener {
            downloadGuidelinesPdf()
        }

        viewModel.navigateToAppreciationScreenFlag.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                changeToAppreciationLayout()
            } else {
                changeToNormalLayout()
            }
        })

        binding.imageButtonReload.setOnClickListener { onClickReloadWebinars() }

        return binding.root
    }

    private fun onClickReloadWebinars() {
        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
            viewModel.getWebinars()
        } else {
            val snackbar = Snackbar.make(
                binding.mainConstraintLayout,
                getString(R.string.no_internet_connection),
                Snackbar.LENGTH_LONG
            )
                .setAction(getString(R.string.retry)) {
                    onClickReloadWebinars()
                }
            snackbar.show()
        }
    }

    private fun initialiseOtherInformation() {
        val preferences =
            requireActivity().getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        val parentType = preferences.getString("parentType", "mother")
        binding.introText.text = when (parentType) {
            "mother" -> getString(R.string.hey_mumma)
            "father" -> getString(R.string.hey_dad)
            "guardian" -> getString(R.string.hey)
            else -> getString(R.string.hey_mumma)
        }


        viewModel.updateIdealWeightAndHeight(requireActivity())


        val months = KidsPeriod(requireActivity()).getKidsAge()
        val stage=UptoddSharedPreferences.getInstance(requireContext()).getStage()
        if(stage=="pre birth" ||stage=="prenatal")
        {
             binding.babyAgeView.text = getString(R.string.babyAgePrenatal)
        }
        else if(months==0)
        {
            binding.babyAgeView.text="The Baby is in womb"
        }
        else if (months <12)
            binding.babyAgeView.text = getString(R.string.babyMonthsFormat, months)
        else
            binding.babyAgeView.text=getString(R.string.babyYearFormat,months/12,months%12)
    }


    private fun checkNavigation() {
        Log.i("notificationIntent", "extra searching")
        viewModel.notificationIntentExtras.observe(viewLifecycleOwner, Observer {
            Log.i("notificationIntent", "extra observing")
            if (it != null) {
                Log.i("notificationIntent", "extra not null")
                val navigateTo = it.getString("navigateTo")
                if (navigateTo != null) {
                    Log.i("notificationIntent", "navigate to $navigateTo")
                    when (navigateTo) {
                        "refer" -> {
                            if (!viewModel.isnavigated) {
                                findNavController().navigate(R.id.referFragment)
                                viewModel.isnavigated = true
                            }
                        }
                        "referralList" -> {
                            if (!viewModel.isnavigated) {
                                findNavController().navigate(R.id.referListFragment)
                                viewModel.isnavigated = true
                            }
                        }
                        "orders" -> {
                            if (!viewModel.isnavigated) {
                                findNavController().navigate(R.id.orderListFragment)
                                viewModel.isnavigated = true
                            }
                        }
                        "music" -> {
                            if (!viewModel.isnavigated) {
                                findNavController().navigate(R.id.music)
                                viewModel.isnavigated = true
                            }
                        }
                        "poem" -> {
                            if (!viewModel.isnavigated) {
                                findNavController().navigate(R.id.poemFragment)
                                viewModel.isnavigated = true
                            }
                        }
                        "support" -> {
                            if (!viewModel.isnavigated) {
                                findNavController().navigate(R.id.allTicketsFragment)
                                viewModel.isnavigated = true
                            }
                        }
                        "blogs" -> {
                            if (!viewModel.isnavigated) {
                                findNavController().navigate(R.id.blogsFragment)
                                viewModel.isnavigated = true
                            }
                        }
                        "webinars" -> {
                            if (!viewModel.isnavigated) {
                                findNavController().navigate(R.id.webinarsFragment)
                                viewModel.isnavigated = true
                            }
                        }
                        "happy_birthday" -> {
                            if (!viewModel.isnavigated) {
                                findNavController().navigate(R.id.happyBirthdayFragment)
                                viewModel.isnavigated = true
                            }
                        }
                    }
                }
            }
        })

        setUpBlogs()

    }


    private fun setUpBlogs() {
        viewModel.webinars.observe(viewLifecycleOwner, Observer { blogsList ->
            Log.d("div", "HomePageFragment L296 $blogsList ${blogsList.size}")
            when (blogsList.size) {
                0 -> {
                    binding.doctorDashboardWebinar1.visibility = View.INVISIBLE
                    binding.doctorDashboardWebinar2.visibility = View.INVISIBLE
                    binding.doctorDashboardWebinar3.visibility = View.INVISIBLE
                    binding.doctorDashboardWebinar4.visibility = View.INVISIBLE

                }
                1 -> {

                    binding.doctorDashboardWebinar1Text.text = blogsList[0].title

                    Picasso.get()
                        .load(blogsList[0].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.default_set_default_blog)
                        .into(binding.doctorDashboardWebinar1Image)

                    binding.doctorDashboardWatchNow1.setOnClickListener {
                        openBlog(blogsList[0])
                    }


                    binding.doctorDashboardWebinar2.visibility = View.INVISIBLE
                    binding.doctorDashboardWebinar3.visibility = View.INVISIBLE
                    binding.doctorDashboardWebinar4.visibility = View.INVISIBLE
                }
                2 -> {

                    binding.doctorDashboardWebinar1Text.text = blogsList[0].title

                    Picasso.get()
                        .load(blogsList[0].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.default_set_default_blog)
                        .into(binding.doctorDashboardWebinar1Image)

                    binding.doctorDashboardWatchNow1.setOnClickListener {
                        openBlog(blogsList[0])
                    }


                    binding.doctorDashboardWebinar2Text.text = blogsList[1].title

                    Picasso.get()
                        .load(blogsList[1].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.default_set_default_blog)
                        .into(binding.doctorDashboardWebinar2Image)

                    binding.doctorDashboardWatchNow2.setOnClickListener {
                        openBlog(blogsList[1])
                    }

                    binding.doctorDashboardWebinar3.visibility = View.INVISIBLE
                    binding.doctorDashboardWebinar4.visibility = View.INVISIBLE
                }
                3 -> {

                    binding.doctorDashboardWebinar1Text.text = blogsList[0].title

                    Picasso.get()
                        .load(blogsList[0].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.default_set_default_blog)
                        .into(binding.doctorDashboardWebinar1Image)

                    binding.doctorDashboardWatchNow1.setOnClickListener {
                        openBlog(blogsList[0])
                    }


                    binding.doctorDashboardWebinar2Text.text = blogsList[1].title

                    Picasso.get()
                        .load(blogsList[1].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.default_set_default_blog)
                        .into(binding.doctorDashboardWebinar2Image)

                    binding.doctorDashboardWatchNow2.setOnClickListener {
                        openBlog(blogsList[1])
                    }


                    binding.doctorDashboardWebinar3Text.text = blogsList[2].title

                    Picasso.get()
                        .load(blogsList[2].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.default_set_default_blog)
                        .into(binding.doctorDashboardWebinar3Image)

                    binding.doctorDashboardWatchNow3.setOnClickListener {
                        openBlog(blogsList[2])
                    }

                    binding.doctorDashboardWebinar4.visibility = View.INVISIBLE
                }
                else -> {

                    binding.doctorDashboardWebinar1Text.text = blogsList[0].title

                    Picasso.get()
                        .load(blogsList[0].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.default_set_default_blog)
                        .into(binding.doctorDashboardWebinar1Image)

                    binding.doctorDashboardWatchNow1.setOnClickListener {
                        openBlog(blogsList[0])
                    }

                    binding.doctorDashboardWebinar2Text.text = blogsList[1].title

                    Picasso.get()
                        .load(blogsList[1].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.default_set_default_blog)
                        .into(binding.doctorDashboardWebinar2Image)

                    binding.doctorDashboardWatchNow2.setOnClickListener {
                        openBlog(blogsList[1])
                    }

                    binding.doctorDashboardWebinar3Text.text = blogsList[2].title

                    Picasso.get()
                        .load(blogsList[2].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.default_set_default_blog)
                        .into(binding.doctorDashboardWebinar3Image)

                    binding.doctorDashboardWatchNow3.setOnClickListener {
                        openBlog(blogsList[2])
                    }

                    binding.doctorDashboardWebinar4Text.text = blogsList[3].title

                    Picasso.get()
                        .load(blogsList[3].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.default_set_default_blog)
                        .into(binding.doctorDashboardWebinar4Image)

                    binding.doctorDashboardWatchNow4.setOnClickListener {
                        openBlog(blogsList[3])
                    }
                }
            }
        })

    }

    private fun openBlog(webinar: Webinars) {
        val intent = Intent(context, FullBlogActivity::class.java)
        intent.putExtra("url", webinar.webinarURL)
        startActivity(intent)
    }


    private fun initiateDataRefresh() {
        val freshness = validateFreshness()

        if (freshness && !UptoddSharedPreferences.getInstance(requireContext()).getInitSave()) {
            Log.i("h_debug", "Todos are fresh")
            return
        } else {
            Log.i("h_debug", "Refreshing Todos")
            viewModel.refreshDataByCallingApi(requireContext(), requireActivity())
            UptoddSharedPreferences.getInstance(requireContext()).initSave(false)
//            val connection =
//                testInternetConnectionAndRefreshData() // test internet connection and assign a disposable to connection so that we can dispose it after data is refreshed
//
//            viewModel.isDataOutdatedFlag.observe(viewLifecycleOwner, Observer {
//                if (it) { // data is outdated
//                    Log.i("h_debug", "Refreshing Activities")
//                    viewModel.refreshDataByCallingApi(requireContext(), requireActivity())
//                } else {  // data is refreshed
//                    connection?.dispose()
//                }
//
//            })
        }
    }


    private fun initialiseBabyPhoto() {
        try {
            val preferences: SharedPreferences? =
                activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
            var url = ""
            if (preferences!!.contains("profileImageUrl"))
                url = preferences.getString("profileImageUrl", "").toString()
            var stage = "pre"
            if (preferences.contains("babyName") && preferences.getString(
                    "babyName",
                    ""
                ) != "null"
                && preferences.getString("babyName", "") != "baby"
            )
                stage = "post"
            stage= UptoddSharedPreferences.getInstance(requireContext()).getStage()!!
           val res = if (stage == "prenatal" ||stage=="pre birth")
                R.drawable.pre_birth_profile
            else
                R.drawable.post_birth_profile

            if (url == "null" || url == "") {
                binding.profileImage.setImageResource(res)
            } else {
                url = "https:uptodd.com/uploads/$url"


                val imageFile: File?
                val folder =
                    File(
                        requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                            .toString() + "/UpTodd"
                    )
                var success = true
                if (!folder.exists()) {
                    success = folder.mkdirs()
                }
                if (success) {
                    imageFile = File(folder.absolutePath + File.separator + "Profile.jpg")

                    if (!imageFile.exists()) {
                        Log.d("div", "HomePageFragment L205")
                        Glide.with(binding.profileImage.context)
                            .load(url)
                            .apply(
                                RequestOptions()
                                    .placeholder(R.drawable.loading_animation)
                                    .error(res)
                            )
                            .into(binding.profileImage)

                        imageFile.createNewFile()

                        Glide.with(this).asBitmap().load(url)
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                                ) {
                                    binding.profileImage.setImageBitmap(resource)

                                    val ostream = ByteArrayOutputStream()
                                    resource.compress(
                                        Bitmap.CompressFormat.JPEG,
                                        100,
                                        ostream
                                    )
                                    val fout = FileOutputStream(imageFile)
                                    fout.write(ostream.toByteArray())
                                    fout.close()
                                    val values = ContentValues()
                                    values.put(
                                        MediaStore.Images.Media.DATE_TAKEN,
                                        System.currentTimeMillis()
                                    )
                                    values.put(
                                        MediaStore.Images.Media.MIME_TYPE,
                                        "image/jpeg"
                                    )
                                    values.put(
                                        MediaStore.MediaColumns.DATA,
                                        imageFile.absolutePath
                                    )
                                    activity?.contentResolver?.insert(
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        values
                                    )
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                    Log.d("div", "GenerateCardFragment L190 $placeholder")
                                }
                            })
                    } else {
                        binding.profileImage.setImageBitmap(
                            BitmapFactory.decodeFile(
                                imageFile.absolutePath
                            )
                        )
                    }


                } else {
                    Toast.makeText(activity, "Image Not saved", Toast.LENGTH_SHORT).show()
                    return
                }


            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        }


    }

    private fun testInternetConnectionAndRefreshData(): Disposable? {

        return ReactiveNetwork
            .observeNetworkConnectivity(requireContext())
            .subscribeOn(Schedulers.io())
            // anything else what you can do with RxJava
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { connectivity ->
                    // do something with connectivity
                    // you can call connectivity.state();
                    // connectivity.type(); or connectivity.toString();
                    if (connectivity.available()) {

                        viewModel.performAction(requireContext(), requireActivity())


                        // viewModel.uploadMonthlyTodosThroughApi()
//                        Toast.makeText(requireContext(), "Connected", Toast.LENGTH_LONG)
//                            .show()

                    } else {
//                        val snackbar = Snackbar.make(
//                            binding.mainConstraintLayout,
//                            getString(R.string.no_internet_connection),
//                            Snackbar.LENGTH_INDEFINITE
//                        )
////                            .setAction("RETRY") {
////                                initiateDataRefresh()
////                            }
//                        snackbar.show()

                    }

                },
                {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }

            )

    }

// this function was used only during testing -->
//    fun forceDataUpload() {
//        viewModel.uploadWeeklyTodosThroughApi(requireActivity())
//        viewModel.uploadMonthlyTodosThroughApi(requireActivity())
//        viewModel.uploadEssentialsTodosThroughApi(requireActivity())
//        viewModel.refreshDataByCallingApi(requireContext(), requireActivity())
//    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val animationDrawable = binding.btnSeeAllActivites.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(3000)
        animationDrawable.start()

    }

    private fun validateFreshness(): Boolean {
        val lastTodoFetchedOn =
            UptoddSharedPreferences.getInstance(requireContext()).getLastDailyTodoFetchedDate()
        if (lastTodoFetchedOn != null) {
            Log.i("h_debug", lastTodoFetchedOn)
            return DateClass().isTodoFresh(lastTodoFetchedOn)
        } else return false  // return false incase user opens the app for the very first time( last to-do fetched date would be nul in that case)

    }

    private fun setupViewPager() {
        val adapter = TodoViewPagerAdapter(this.requireActivity())
        binding.viewPager.adapter = adapter

        adapter.apply {
            addFragment(DailyFragment())
            addFragment(WeeklyFragment())
            addFragment(MonthlyFragment())
            addFragment(EssentialsFragment())
        }

        val fragmentTitleList = arrayListOf(
            getString(R.string.daily),
            getString(R.string.weekly),
            getString(R.string.monthly),
            getString(R.string.essentials)
        )

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = fragmentTitleList[position]
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {



                when (tab?.position) {
                    0 -> viewModel.loadDailyTodoScore()
                    1 -> viewModel.loadWeeklyTodoScore()
                    2 -> viewModel.loadMonthlyTodoScore()
                    3 -> viewModel.loadEssentialsTodoScore()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // define custom function if needed
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // define custom fun if needed
            }
        })
    }

    private fun initialiseBindingAndViewModel(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_home_page, container, false)

        (requireActivity() as AppCompatActivity).supportActionBar!!.title =
            getString(R.string.home)

        binding.todosViewModel = viewModel
        binding.lifecycleOwner = this

    }


    private fun initialiseScoreDisplay() {

        viewModel.score.observe(viewLifecycleOwner, Observer {

            // spannable is used to give different text formatting in the same text view
            val spannable = SpannableString(viewModel.score.value)
            spannable.setSpan(
                RelativeSizeSpan(1.2f),
                0, viewModel.score.value!!.substringBefore("/").length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.darkBlue
                    )
                ),
                0, viewModel.score.value!!.substringBefore("/").length + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.scoreView.text = spannable
        })
    }


    private fun changeToAppreciationLayout() {
        binding.apply {


            if(!viewModel?.isRefreshing.value!!)
            {
                btnSeeAllActivites.visibility = View.INVISIBLE
                superParentTextView.visibility = View.INVISIBLE
                scoreView.visibility = View.INVISIBLE
                superManImageView.visibility = View.VISIBLE
                youAreASuperParentTextView.visibility = View.VISIBLE
                allActivitiesCompletedTextView.visibility = View.VISIBLE
                confettiImageView.visibility = View.VISIBLE
                Glide.with(requireActivity())
                    .load(R.drawable.superparentgif)
                    .into(confettiImageView)
            }
            else
                changeToLoading()
        }
    }

    private fun changeToNormalLayout() {
        binding.apply {
            btnSeeAllActivites.visibility = View.VISIBLE
            superParentTextView.visibility = View.VISIBLE
            scoreView.visibility = View.VISIBLE

            superManImageView.visibility = View.INVISIBLE
            youAreASuperParentTextView.visibility = View.INVISIBLE
            allActivitiesCompletedTextView.visibility = View.INVISIBLE
            confettiImageView.visibility = View.INVISIBLE

        }
    }


    private fun changeToLoading()
    {
        binding.apply {
            btnSeeAllActivites.visibility = View.INVISIBLE
            superParentTextView.visibility = View.INVISIBLE
            scoreView.visibility = View.INVISIBLE

            superManImageView.visibility = View.VISIBLE
            youAreASuperParentTextView.visibility = View.INVISIBLE
            allActivitiesCompletedTextView.visibility = View.INVISIBLE
            confettiImageView.visibility = View.VISIBLE
            Glide.with(requireActivity()).load(R.drawable.loading_animation)
                .into(confettiImageView)

        }


    }

    private fun downloadGuidelinesPdf() {
        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
            JishnuDownloadManager(
                "https://uptodd.com/resources/user/UserGuide.pdf",
                "UptoddAppGuidelines.pdf",
                File(
                    requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "Downloads"
                ),
                requireContext(),
                requireActivity()
            )
        } else {
            val snackbar = Snackbar.make(
                binding.mainConstraintLayout,
                getString(R.string.no_internet_connection),
                Snackbar.LENGTH_LONG
            )
                .setAction(getString(R.string.retry)) {
                    downloadGuidelinesPdf()
                }
            snackbar.show()
        }

    }

    private fun loadTodaysTip() {
        GlobalScope.launch {
            val preferences: SharedPreferences =
                requireContext().getSharedPreferences("TODAYS_TIP", Context.MODE_PRIVATE)
            val editor = preferences.edit()

            val date = DateClass().getCurrentDateStringAsYYYYMMDD()
            AndroidNetworking.get("http://uptodd.com/api/todaytip/$date")
                .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        response?.let { it ->
                            if (it.getString("data") != "null") {
                                val data = it.get("data") as JSONObject
                                val heading = data.getString("tipHeading")
                                val tip = data.getString("description")

                                editor.putString("tipHeading", heading)
                                editor.putString("description", tip)
                                editor.apply()
                                loadTip()
                            } else {
                                hideTip()
                            }
                        }

                    }

                    override fun onError(error: ANError) {
                        Log.d("tip", error.message.toString())

                        hideTip()
                    }
                })
        }
    }

    private fun loadTip() {
        val preferences: SharedPreferences =
            requireContext().getSharedPreferences("TODAYS_TIP", Context.MODE_PRIVATE)
        val heading = preferences.getString("tipHeading", null)
        val description =
            preferences.getString("description", null)

        if (heading != null && description != null) {
            binding.tagline.text = heading
            binding.tipDescription.text = description
        } else {
            hideTip()
        }

    }

    private fun hideTip() {
        binding.apply {
            tagline.visibility = View.INVISIBLE
            tipDescription.visibility = View.INVISIBLE
            todaysTipTextView.visibility = View.INVISIBLE
            tagline.height = 0
            tipDescription.height = 0
            todaysTipTextView.height = 0
        }
    }
}

