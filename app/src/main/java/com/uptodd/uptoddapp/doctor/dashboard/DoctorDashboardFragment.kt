package com.uptodd.uptoddapp.doctor.dashboard

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.transition.MaterialSharedAxis
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.webinars.Webinars
import com.uptodd.uptoddapp.databinding.DoctorDashboardFragmentBinding
import com.uptodd.uptoddapp.ui.blogs.fullblog.FullBlogActivity
import com.uptodd.uptoddapp.utilities.ScreenDpi
import com.uptodd.uptoddapp.utilities.UpToddDialogs


class DoctorDashboardFragment : Fragment(), CarouselAdapter.OnClickListener {

    companion object {
        fun newInstance() = DoctorDashboardFragment()
        fun setImageWithGlideInImageView(view: ImageView, url: String?) {
            Glide.with(view.context)
                .load(url)
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_broken_image)
                .into(view)
        }
    }

    private lateinit var uptoddDialogs: UpToddDialogs
    private lateinit var viewModel: DoctorDashboardViewModel
    private lateinit var adapter: CarouselAdapter

    var currentPage = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        uptoddDialogs = UpToddDialogs(requireContext())

        val binding: DoctorDashboardFragmentBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.doctor_dashboard_fragment,
            container,
            false
        )
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(DoctorDashboardViewModel::class.java)
        binding.doctorDashboardBinding = viewModel

        setClickListeners(binding)

        viewModel.dpi = ScreenDpi(requireContext()).getScreenDrawableType()


        checkForNavigation()


        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            it.let {
                when (it) {
                    0 -> {
                        uptoddDialogs.dismissDialog()
                        viewModel.initializeVariables()
                        adapter = CarouselAdapter(
                            this.requireContext(),
                            this,
                            viewModel.doctorsReferred.value!!,
                            viewModel.doctorsEnrolled.value!!,
                            viewModel.patientsReferred.value!!,
                            viewModel.patientsEnrolled.value!!
                        )
                        binding.carouselView.adapter = adapter
                        formatCarouselView(binding.carouselView)
                        binding.doctorDashboardWelcomeDoctor.text =
                            "Welcome, " + viewModel.doctorName.value
                    }
                    1 -> {
                        uptoddDialogs.showLoadingDialog(findNavController())
                    }
                    -1 -> {
                        uptoddDialogs.dismissDialog()
                        uptoddDialogs.showDialog(R.drawable.network_error,
                            "An error has occurred: ${viewModel.apiError}.",
                            "Close",
                            object : UpToddDialogs.UpToddDialogListener {
                                override fun onDialogButtonClicked(dialog: Dialog) {
                                    uptoddDialogs.dismissDialog()
                                }
                            })
                    }
                    else -> return@Observer
                }
            }
        })

        setUpBlogs(binding)

        binding.carouselViewIndicator.setupWithViewPager(binding.carouselView, true)

        return binding.root
    }

    private fun setUpBlogs(binding: DoctorDashboardFragmentBinding) {
        viewModel.webinars.observe(viewLifecycleOwner, Observer { blogsList ->
            when (blogsList.size) {
                0 -> {
                    binding.doctorDashboardWebinar1.visibility = View.INVISIBLE
                    binding.doctorDashboardWebinar2.visibility = View.INVISIBLE
                    binding.doctorDashboardWebinar3.visibility = View.INVISIBLE
                    binding.doctorDashboardWebinar4.visibility = View.INVISIBLE

                }
                1 -> {

                    binding.doctorDashboardWebinar1Text.text = blogsList[0].title

                    /*Glide.with(binding.doctorDashboardWebinar1Image.context)
                        .load(blogsList[0].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                        .into(binding.doctorDashboardWebinar1Image)*/
                    setImageWithGlideInImageView(
                        binding.doctorDashboardWebinar1Image,
                        blogsList[0].imageURL
                    )
                    /*Picasso.get()
                        .load(blogsList[0].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                        .into(binding.doctorDashboardWebinar1Image)*/

                    binding.doctorDashboardWatchNow1.setOnClickListener {
                        openBlog(blogsList[0])
                    }


                    binding.doctorDashboardWebinar2.visibility = View.INVISIBLE
                    binding.doctorDashboardWebinar3.visibility = View.INVISIBLE
                    binding.doctorDashboardWebinar4.visibility = View.INVISIBLE
                }
                2 -> {

                    binding.doctorDashboardWebinar1Text.text = blogsList[0].title

                    setImageWithGlideInImageView(
                        binding.doctorDashboardWebinar1Image,
                        blogsList[0].imageURL
                    )
                    /*Picasso.get()
                        .load(blogsList[0].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                        .into(binding.doctorDashboardWebinar1Image)*/

                    binding.doctorDashboardWatchNow1.setOnClickListener {
                        openBlog(blogsList[0])
                    }


                    binding.doctorDashboardWebinar2Text.text = blogsList[1].title
                    setImageWithGlideInImageView(
                        binding.doctorDashboardWebinar2Image,
                        blogsList[1].imageURL
                    )
                    /*Picasso.get()
                        .load(blogsList[1].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                        .into(binding.doctorDashboardWebinar2Image)*/

                    binding.doctorDashboardWatchNow2.setOnClickListener {
                        openBlog(blogsList[1])
                    }

                    binding.doctorDashboardWebinar3.visibility = View.INVISIBLE
                    binding.doctorDashboardWebinar4.visibility = View.INVISIBLE
                }
                3 -> {

                    binding.doctorDashboardWebinar1Text.text = blogsList[0].title
                    setImageWithGlideInImageView(
                        binding.doctorDashboardWebinar1Image,
                        blogsList[0].imageURL
                    )
                    /*Picasso.get()
                        .load(blogsList[0].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                        .into(binding.doctorDashboardWebinar1Image)*/

                    binding.doctorDashboardWatchNow1.setOnClickListener {
                        openBlog(blogsList[0])
                    }


                    binding.doctorDashboardWebinar2Text.text = blogsList[1].title
                    setImageWithGlideInImageView(
                        binding.doctorDashboardWebinar2Image,
                        blogsList[1].imageURL
                    )
                    /*Picasso.get()
                        .load(blogsList[1].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                        .into(binding.doctorDashboardWebinar2Image)*/

                    binding.doctorDashboardWatchNow2.setOnClickListener {
                        openBlog(blogsList[1])
                    }


                    binding.doctorDashboardWebinar3Text.text = blogsList[2].title

                    setImageWithGlideInImageView(
                        binding.doctorDashboardWebinar3Image,
                        blogsList[2].imageURL
                    )

                    /*Picasso.get()
                        .load(blogsList[2].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                        .into(binding.doctorDashboardWebinar3Image)*/

                    binding.doctorDashboardWatchNow3.setOnClickListener {
                        openBlog(blogsList[2])
                    }

                    binding.doctorDashboardWebinar4.visibility = View.INVISIBLE
                }
                else -> {

                    binding.doctorDashboardWebinar1Text.text = blogsList[0].title
                    setImageWithGlideInImageView(
                        binding.doctorDashboardWebinar1Image,
                        blogsList[0].imageURL
                    )
                    /*Picasso.get()
                        .load(blogsList[0].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                        .into(binding.doctorDashboardWebinar1Image)*/

                    binding.doctorDashboardWatchNow1.setOnClickListener {
                        openBlog(blogsList[0])
                    }

                    binding.doctorDashboardWebinar2Text.text = blogsList[1].title
                    setImageWithGlideInImageView(
                        binding.doctorDashboardWebinar2Image,
                        blogsList[1].imageURL
                    )
                    /*Picasso.get()
                        .load(blogsList[1].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                        .into(binding.doctorDashboardWebinar2Image)*/

                    binding.doctorDashboardWatchNow2.setOnClickListener {
                        openBlog(blogsList[1])
                    }

                    binding.doctorDashboardWebinar3Text.text = blogsList[2].title
                    setImageWithGlideInImageView(
                        binding.doctorDashboardWebinar3Image,
                        blogsList[2].imageURL
                    )
                    /*Picasso.get()
                        .load(blogsList[2].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                        .into(binding.doctorDashboardWebinar3Image)*/

                    binding.doctorDashboardWatchNow3.setOnClickListener {
                        openBlog(blogsList[2])
                    }

                    binding.doctorDashboardWebinar4Text.text = blogsList[3].title
                    setImageWithGlideInImageView(
                        binding.doctorDashboardWebinar4Image,
                        blogsList[3].imageURL
                    )
                    /*Picasso.get()
                        .load(blogsList[3].imageURL)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                        .into(binding.doctorDashboardWebinar4Image)*/

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

    @SuppressLint("ApplySharedPref")
    private fun checkForNavigation() {
        val preferences = requireContext().getSharedPreferences("navigation", Context.MODE_PRIVATE)
        val isNavigated = preferences.getBoolean("isDoctorDashboardNavigated", false)
        if (!isNavigated) {
            val extras = requireActivity().intent.extras
            if (extras != null) {
                val navigateTo = extras.getString("navigateTo")
                if (navigateTo != null) {
                    preferences.edit().putBoolean("isDoctorDashboardNavigated", true).commit()
                    when (navigateTo) {
                        "referralDoctor" -> {
                            if (!viewModel.isNavigationDone) {
                                findNavController().navigate(R.id.referredListDoctor)
                            }
                        }
                        "referralPatient" -> {
                            if (!viewModel.isNavigationDone) {
                                findNavController().navigate(R.id.referredListPatient)
                            }
                        }
                        "we_miss_you" -> {
                            if (!viewModel.isNavigationDone) {
                                findNavController().navigate(R.id.missYouDoctorFragment)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setClickListeners(binding: DoctorDashboardFragmentBinding) {
        binding.doctorDashboardBookASession.setOnClickListener {
            findNavController().navigate(DoctorDashboardFragmentDirections.actionDoctorDashboardFragmentToBookASession())
//            val sp = requireContext().getSharedPreferences("language", Context.MODE_PRIVATE)
//            sp.edit().putString("lang", "fr").apply()
//            requireActivity().finish()
        }
    }

    private fun formatCarouselView(viewPager: ViewPager) {
        viewPager.clipToPadding = false
        viewPager.pageMargin = 5
        viewPager.setPadding(10, 0, 10, 0)
        val nextItemVisiblePx = 10
        val currentItemHorizontalMarginPx = 10
        val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
        val pageTransformer = ViewPager.PageTransformer { page: View, position: Float ->
            page.translationX = -pageTranslationX * position
            // Next line scales the item's height. You can remove it if you don't want this effect
            page.scaleY = 1 - (0.25f * Math.abs(position))
            // If you want a fading effect uncomment the next line:
            page.alpha = 0.75f + (1 - Math.abs(position))
        }
        viewPager.setCurrentItem(0, true)

        val handler = Handler(Looper.getMainLooper())
        val update = Runnable {
            viewPager.setCurrentItem((currentPage++) % 2, true)
        }
        val recall = Runnable {
            formatCarouselView(viewPager)
        }

        viewPager.setPageTransformer(true, pageTransformer)
        Thread {
            Thread.sleep(3000)
            handler.post(update)
            Thread.sleep(3000)
            handler.post(recall)
        }.start()

    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).menu.getItem(0).isChecked =
            true
        viewModel.reInit()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DoctorDashboardViewModel::class.java)
    }

    override fun onClickDoctorsReferred() {
        findNavController().navigate(
            DoctorDashboardFragmentDirections.actionDoctorDashboardFragmentToReferredListDoctor()
        )
    }

    override fun onClickDoctorsEnrolled() {
        val navigator =
            DoctorDashboardFragmentDirections.actionDoctorDashboardFragmentToReferredListDoctor()
        navigator.filtersStatus = "Success"
        findNavController().navigate(navigator)
    }

    override fun onClickPatientsReferred() {
        findNavController().navigate(
            DoctorDashboardFragmentDirections.actionDoctorDashboardFragmentToReferredListPatient()
        )
    }

    override fun onClickPatientsEnrolled() {
        val navigator =
            DoctorDashboardFragmentDirections.actionDoctorDashboardFragmentToReferredListPatient()
        navigator.filtersStatus = "Success"
        findNavController().navigate(navigator)
    }
}