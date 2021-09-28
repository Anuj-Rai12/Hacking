package com.example.hackerstudent.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.hackerstudent.R
import com.example.hackerstudent.TAG
import com.example.hackerstudent.databinding.AddCartLayoutBinding
import com.example.hackerstudent.recycle.addcart.AddCartAdaptor
import com.example.hackerstudent.utils.*
import com.example.hackerstudent.viewmodels.CourseViewModel
import com.example.hackerstudent.viewmodels.PrimaryViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddCartFragment : Fragment(R.layout.add_cart_layout) {
    private lateinit var binding: AddCartLayoutBinding
    private val viewModel: CourseViewModel by viewModels()
    private val course: MutableList<UploadFireBaseData> = mutableListOf()
    private val primaryViewModel: PrimaryViewModel by viewModels()
    private var stringFlag: String? = null
    private var addCartAdaptor: AddCartAdaptor? = null

    @Inject
    lateinit var networkUtils: NetworkUtils

    @Inject
    lateinit var customProgress: CustomProgress

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.changeStatusBarColor()
        showBottomNavBar()
        binding = AddCartLayoutBinding.bind(view)
        savedInstanceState?.let {
            stringFlag = it.getString(GetConstStringObj.NO_INTERNET)
        }
        setUpRecycleView()
        when {
            stringFlag != null -> {
                addToCart()
            }
            networkUtils.isConnected() -> {
                showInternet()
                getData()
            }
            else -> {
                noInternet()
                activity?.msg(GetConstStringObj.NO_INTERNET, GetConstStringObj.RETRY, {
                    if (networkUtils.isConnected()) {
                        showInternet()
                        getData()
                    }
                })
            }
        }
        binding.arrowImg.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.searchQuery.asLiveData().observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty() && !it.isNullOrBlank()) {
                deleteAddCartFunction(it)
            }
        }

    }

    private fun deleteAddCartFunction(s: String) {
        viewModel.deleteCourse(s).observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    noInternet()
                    hideLoading()
                    dir(message = it.exception?.localizedMessage ?: GetConstStringObj.UN_WANTED)
                    viewModel.searchQuery.value = ""
                }
                is MySealed.Loading -> {
                    showInternet()
                    showLoading(it.data as String)
                }
                is MySealed.Success -> {
                    showInternet()
                    hideLoading()
                    dir(title = "Success", message = it.data!!)
                    viewModel.searchQuery.value = ""
                }
            }
        }
    }

    private fun setUpRecycleView() {
        binding.courseLayoutRecycle.apply {
            setHasFixedSize(true)
            addCartAdaptor = AddCartAdaptor(requireContext()) {
                dir(9, uploadedData = it)
            }
            adapter = addCartAdaptor
        }
        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val getSelectedItem =
                    addCartAdaptor?.currentList?.get(viewHolder.absoluteAdapterPosition)
                getSelectedItem?.let {
                    viewModel.searchQuery.value = it.fireBaseCourseTitle?.coursename!!
                }
            }
        }).attachToRecyclerView(binding.courseLayoutRecycle)

    }

    private fun getData() {
        primaryViewModel.userInfo.observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    noInternet()
                    dir(message = "${it.exception?.localizedMessage}")
                }
                is MySealed.Loading -> {
                    showInternet()
                    showLoading()
                }
                is MySealed.Success -> {
                    showInternet()
                    val info = it.data as CreateUserAccount?
                    info?.let { createData ->
                        Log.i(TAG, "getData: Create User Fragment $createData")
                        if (createData.bookmarks?.isEmpty() == true || createData.bookmarks == null) {
                            hideLoading()
                            stringFlag = "Wishlist is loading.."
                            addToCart()
                        } else {
                            Log.i(TAG, "getData: Loading Cart...")

                            createData.bookmarks.values.forEach { purchase ->
                                val flag =
                                    purchase.course == createData.bookmarks.values.last().course

                                getAddCart(purchase.course!!, flag) { list ->
                                    hideLoading()
                                    Log.i(TAG, "getData: $list")
                                    addCartAdaptor?.submitList(list)
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    private fun getAddCart(
        value: String,
        flag: Boolean,
        whenAllLoadingComplete: (List<UploadFireBaseData>) -> Unit
    ) {
        viewModel.courseID(value).observe(viewLifecycleOwner) { data ->
            when (data) {

                is MySealed.Error -> {
                    noInternet()
                    hideLoading()
                    dir(message = "${data.exception?.localizedMessage}")
                }

                is MySealed.Loading -> {
                    Log.i(TAG, "getAddCart: Loading Cart Data.. $value")
                    showInternet()
                }

                is MySealed.Success -> {
                    showInternet()
                    val info = data.data as UploadFireBaseData?

                    Log.i(TAG, "getAddCart: Call is Success -> $info")
                    info?.let { task ->
                        course.add(task)
                        Log.i(TAG, "getAddCart: ${course.size}")
                        if (flag) {
                            whenAllLoadingComplete(course)
                        }
                    }
                }
            }

        }
    }

    private fun addToCart() {
        binding.courseLayoutRecycle.hide()
        binding.courseLottie.show()
        binding.courseLottie.setAnimation(R.raw.add_cart)
    }

    private fun dir(
        choose: Int = 0,
        title: String = "Error",
        message: String = "",
        uploadedData: UploadFireBaseData? = null
    ) {
        val action = when (choose) {
            0 -> AddCartFragmentDirections.actionGlobalPasswordDialog2(title, message)
            else -> {
                val sendSelectedCourse = SendSelectedCourse(
                    courselevel = uploadedData?.fireBaseCourseTitle?.courselevel,
                    thumbnail = uploadedData?.thumbnail,
                    previewvideo = uploadedData?.previewvideo,
                    requirement = uploadedData?.fireBaseCourseTitle?.requirement,
                    totalhrs = uploadedData?.fireBaseCourseTitle?.totalhrs,
                    lastdate = uploadedData?.fireBaseCourseTitle?.lastdate,
                    totalprice = uploadedData?.fireBaseCourseTitle?.totalprice,
                    review = uploadedData?.fireBaseCourseTitle?.review,
                    targetaudience = uploadedData?.fireBaseCourseTitle?.targetaudience,
                    currentprice = uploadedData?.fireBaseCourseTitle?.currentprice,
                    category = uploadedData?.fireBaseCourseTitle?.category,
                    coursename = uploadedData?.fireBaseCourseTitle?.coursename
                )
                AddCartFragmentDirections.actionGlobalCourseViewFragment(
                    sendSelectedCourse,
                    uploadedData?.id!!
                )
            }
        }
        findNavController().navigate(action)
    }

    private fun showInternet() {
        binding.courseLayoutRecycle.show()
        binding.courseLottie.hide()
    }

    private fun noInternet() {
        binding.courseLottie.show()
        binding.courseLayoutRecycle.hide()
        binding.courseLottie.setAnimation(R.raw.no_connection)
    }

    private fun hideLoading() = customProgress.hideLoading()

    private fun showLoading(string: String = "Wishlist is loading..") =
        customProgress.showLoading(requireActivity(), string)


    override fun onPause() {
        super.onPause()
        hideLoading()
        course.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        stringFlag?.let {
            outState.putString(GetConstStringObj.NO_INTERNET, it)
        }
    }
}