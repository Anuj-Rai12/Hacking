package com.example.hackerstudent.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.hackerstudent.ClientActivity
import com.example.hackerstudent.R
import com.example.hackerstudent.SplashScreen
import com.example.hackerstudent.TAG
import com.example.hackerstudent.databinding.CourseViewFragmentBinding
import com.example.hackerstudent.recycle.preview.AllPreviewAdaptor
import com.example.hackerstudent.utils.*
import com.example.hackerstudent.viewmodels.CourseViewModel
import com.example.hackerstudent.viewmodels.PrimaryViewModel
import com.razorpay.Checkout
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class CourseViewFragment : Fragment(R.layout.course_view_fragment) {
    private lateinit var binding: CourseViewFragmentBinding
    private val list: MutableList<CoursePreview> = mutableListOf()
    private var allPreviewAdaptor: AllPreviewAdaptor? = null
    private val args: CourseViewFragmentArgs by navArgs()
    private val viewModel: PrimaryViewModel by viewModels()
    private val courseViewModel: CourseViewModel by viewModels()
    private var stringPaymentFlag: String? = null

    @Inject
    lateinit var customProgress: CustomProgress

    @Inject
    lateinit var networkUtils: NetworkUtils

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).hide()
        hideBottomNavBar()
        activity?.changeStatusBarColor()
        binding = CourseViewFragmentBinding.bind(view)
        savedInstanceState?.let {
            stringPaymentFlag = it.getString(GetConstStringObj.UN_WANTED)
        }
        Checkout.preload(activity?.applicationContext)
        Log.i(TAG, "onViewCreated: Selected Course id -> ${args.id}")
        binding.arrowImg.setOnClickListener {
            findNavController().popBackStack()
        }
        setUpRecycle()
        if (networkUtils.isConnected()) {
            getData()
        } else {
            hideOffline()
            requireActivity().msg(
                GetConstStringObj.NO_INTERNET,
                setAction = GetConstStringObj.RETRY, {
                    if (networkUtils.isConnected()) {
                        internetConnected()
                        getData()
                    }
                })
        }
        allPreviewAdaptor?.submitList(list)
        //allPreviewAdaptor?.notifyItemRangeInserted(0, list.size)
        binding.shareImg.setOnClickListener {
            lifecycleScope.launchWhenStarted {
                getBitmap()?.let { bitmap ->
                    val url = activity?.bitUrl(bitmap)
                    url?.let {uri: Uri ->
                        stringPaymentFlag="payment success"
                        activity?.shareImage(
                            title = "Share Course :)",
                            message = "Hey check this Course ,\n${args.data.coursename}" +
                                    "\nThis course is for ${args.data.courselevel}" +
                                    "\nJust only for ${GetConstStringObj.Rs}${args.data.currentprice}" +
                                    "\nFor More Detail Check this course preview Video" +
                                    "\n\n${args.data.previewvideo}" +
                                    "\n\nVisit this Course" +
                                    "\n\n${SplashScreen.versionControl?.updateurl ?: "www.google.com"}",
                            uri=uri
                        )
                    }
                }
            }
        }
    }

    private suspend fun getBitmap(): Bitmap? {
        val loading = ImageLoader(requireContext())
        val request = ImageRequest.Builder(requireContext())
            .data(args.data.thumbnail)
            .build()
        return try {
            val result = (loading.execute(request) as SuccessResult).drawable
            (result as BitmapDrawable).bitmap
        } catch (e: Exception) {
            hideLoading()
            dir(message = e.localizedMessage!!)
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpRecycle() {
        binding.courseViewRecycle.apply {
            setHasFixedSize(true)
            allPreviewAdaptor = AllPreviewAdaptor({ courseName, CoursePrice ->
                //Item Buy
                if (networkUtils.isConnected()) {
                    internetConnected()
                    stringPaymentFlag = "payment Mill ga-ya"
                    getUserInfo(courseName, CoursePrice)
                } else {
                    hideOffline()
                    activity?.msg(GetConstStringObj.NO_INTERNET, GetConstStringObj.RETRY, {
                        if (networkUtils.isConnected()) {
                            internetConnected()
                            stringPaymentFlag = "payment Mill ga-ya"
                            getUserInfo(courseName, CoursePrice)
                        }
                    })
                }
            }, { courseName, CoursePrice ->
                //Item Cart
                addItemCart(courseName, CoursePrice)
            }, { teacher ->
                //Navigated to teacher Intro fragment
                context?.msg(teacher)
            }, { goToMoreReview ->
                Log.i(TAG, "setUpRecycle: $goToMoreReview")
                context?.msg("working on Review")
            }, { video ->
                val action = CourseViewFragmentDirections.actionGlobalVideoFragment(
                    video,
                    "${args.data.coursename} Preview"
                )
                findNavController().navigate(action)
            }, requireActivity())
            layoutManager = WrapContentLinearLayoutManager(requireActivity())
            adapter = allPreviewAdaptor
        }
    }

    private fun addItemCart(name: String, price: String) {
        val coursePurchase =
            CoursePurchase(
                course = name,
                data = getDateTime(),
                purchase = price,
                status = "WishList",
            )
        viewModel.addItemCart(coursePurchase).observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    hideOffline()
                    dir(message = it.exception?.localizedMessage ?: GetConstStringObj.UN_WANTED)
                }
                is MySealed.Loading -> {
                    internetConnected()
                    showLoading(it.data as String)
                }
                is MySealed.Success -> {
                    internetConnected()
                    hideLoading()
                    dir(title = "Success", message = "Course is Added to Cart")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getUserInfo(courseName: String, price: String) {
        viewModel.userInfo.observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    hideOffline()
                    hideLoading()
                    dir(message = it.exception?.localizedMessage ?: GetConstStringObj.UN_WANTED)
                }
                is MySealed.Loading -> {
                    showLoading(it.data as String)
                    internetConnected()
                }
                is MySealed.Success -> {
                    hideLoading()
                    internetConnected()
                    val userInfo = it.data as CreateUserAccount?
                    if (userInfo != null) {
                        startPayment(courseName = courseName, CoursePrice = price, userInfo)
                    } else
                        activity?.msg("No User info Found")
                }
            }
        }
    }

    private fun startPayment(courseName: String, CoursePrice: String, userInfo: CreateUserAccount) {
        Log.i(TAG, "startPayment: Real Amount is $CoursePrice")
        Log.i(TAG, "startPayment: Real CourseName is $courseName")
        val co = Checkout()
        if (SplashScreen.versionControl?.razorpay==null) {
            context?.msg("Un-Fortunate Error")
            return
        }
        co.setKeyID(SplashScreen.versionControl?.razorpay!!)
        try {
            val options = JSONObject()
            options.put("name", getString(R.string.app_name))
            options.put("description", courseName)
            co.setImage(R.drawable.hacking_main_icon)
            //You can omit the image option to fetch the image from dashboard
            options.put("image", "${args.data.thumbnail}")
            options.put("theme.color", GetConstStringObj.Payment_COLOR)
            options.put("currency", GetConstStringObj.Currency)
            options.put("amount", "${CoursePrice.toInt() * 100}")

            val retryObj = JSONObject()
            retryObj.put("enabled", false)
//            retryObj.put("max_count", 4)
            options.put("retry", retryObj)

            val prefill = JSONObject()
            prefill.put("email", "${userInfo.email}")
            prefill.put("contact", "${userInfo.phone}")

            options.put("prefill", prefill)
            ClientActivity.courseName = courseName
            ClientActivity.coursePrice = CoursePrice
            activity?.let {
                showPaymentOption(co, it, options)
            }
        } catch (e: Exception) {
            Log.i(TAG, "startPayment: Error ->  $e")
            dir(message = e.localizedMessage!!)
        }
    }

    private fun showPaymentOption(co: Checkout, it: FragmentActivity, options: JSONObject) {
        courseViewModel.showPaymentOption(co, it, options).observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    hideOffline()
                    dir(message = it.exception?.localizedMessage ?: GetConstStringObj.UN_WANTED)
                }
                is MySealed.Loading -> {
                    internetConnected()
                    showLoading(it.data as String)
                }
                is MySealed.Success -> {
                    hideLoading()
                    internetConnected()
                    Log.i(TAG, "showPaymentOption: SuccessFull Open Do it")
                }
            }
        }
    }

    private fun dir(title: String = "Error", message: String = "") {
        val action = CourseViewFragmentDirections.actionGlobalPasswordDialog2(title, message)
        findNavController().navigate(action)
    }

    private fun internetConnected() {
        binding.courseViewRecycle.show()
        binding.errorLottieFile.hide()
    }

    private fun hideOffline() {
        binding.courseViewRecycle.hide()
        binding.errorLottieFile.show()
        binding.errorLottieFile.setAnimation(R.raw.no_connection)
    }

    private fun getData() {
        args.data.let { data ->
            list.add(
                CoursePreview.VideoCourse(
                    videoPreview = data.previewvideo ?: "",
                    title = data.coursename ?: "No Name",
                    thumbnail = data.thumbnail ?: ""
                )
            )
            list.add(
                CoursePreview.CourseRatingAndOther(
                    rating = data.review?.rateing ?: "4.5",
                    totalHrs = data.totalhrs ?: "00"
                )
            )
            list.add(
                CoursePreview.ArrayClass(
                    title = "For whom this course is for,",
                    targetAudience = data.targetaudience
                )
            )
            list.add(
                CoursePreview.ArrayClass(
                    title = "Requirements for this course,",
                    requirement = data.requirement
                )
            )

            list.add(
                CoursePreview.CoursePrice(
                    currAmt = data.currentprice ?: "00",
                    mrp = data.totalprice ?: "00",
                    title = data.coursename ?: ""
                )
            )
            val review = UserViewOnCourse(
                bywhom = data.review?.bywhom ?: "Anuj",
                rateing = data.review?.rateing ?: "4.5",
                description = data.review?.description
                    ?: "Great to Learn And Improve my Knowledge and Assignment are Best i have learn so much by solving them."
            )
            list.add(CoursePreview.ReviewSection(data = review))
        }
    }

    private fun showLoading(string: String) = customProgress.showLoading(requireActivity(), string)
    private fun hideLoading() = customProgress.hideLoading()

    override fun onPause() {
        super.onPause()
        if (stringPaymentFlag == null) {
            allPreviewAdaptor = null
            list.clear()
        } else
            stringPaymentFlag = null
        hideLoading()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        stringPaymentFlag?.let {
            outState.putString(GetConstStringObj.UN_WANTED, it)
        }
    }

}