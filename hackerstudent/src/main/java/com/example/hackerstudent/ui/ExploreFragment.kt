package com.example.hackerstudent.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hackerstudent.R
import com.example.hackerstudent.TAG
import com.example.hackerstudent.databinding.ExploreFragmentBinding
import com.example.hackerstudent.paginate.HeaderAndFooterAdaptor
import com.example.hackerstudent.paginate.PaginationAdaptor
import com.example.hackerstudent.utils.*
import com.example.hackerstudent.viewmodels.CourseViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val LOADING = "Course is Loading"

@AndroidEntryPoint
class ExploreFragment : Fragment(R.layout.explore_fragment) {
    private lateinit var binding: ExploreFragmentBinding
    private val courseViewModel: CourseViewModel by viewModels()
    private val disposables = CompositeDisposable()
    private var stringFlag: String? = null
    private var paginationAdaptor: PaginationAdaptor? = null
    private val enterAnim by lazy {
        AnimationUtils.loadAnimation(context, R.anim.ente_anim)
    }

    @Inject
    lateinit var customProgress: CustomProgress

    @Inject
    lateinit var networkUtils: NetworkUtils

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity?)!!.hide()
        showBottomNavBar()
        binding = ExploreFragmentBinding.bind(view)
        binding.arrowImg.setOnClickListener {
            if (stringFlag == null)
                findNavController().popBackStack()
            else {
                binding.searchViewEd.setText("")
                stringFlag = null
            }
        }
        savedInstanceState?.let {
            stringFlag = it.getString(GetConstStringObj.Create_Module)
        }
        binding.searchViewEd.startAnimation(enterAnim)
        setUpRecycleView()
        if (stringFlag == null && networkUtils.isConnected()) {
            showLoading()
            hide()
            getUpData(null)
        } else if (stringFlag == null && !networkUtils.isConnected()) {
            showNoConnection()
            activity?.msg(GetConstStringObj.NO_INTERNET, GetConstStringObj.RETRY, {
                if (networkUtils.isConnected()) {
                    hide()
                    getUpData(null)
                }
            })
        }

        val observable = Observable.create<String> { emitter ->
            binding.searchViewEd.doOnTextChanged { text, _, _, _ ->
                if (!emitter.isDisposed) {
                    Log.i(TAG, "onViewCreated: $text")
                    val str = text ?: " "
                    emitter.onNext(str.toString())
                }
            }
        }.debounce(GetConstStringObj.timeToSearch.toLong(), TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        observed(observable)

        courseViewModel.searchQuery.asLiveData().observe(viewLifecycleOwner) {
            if (it.isNotBlank() && it.isNotEmpty() && !it.isNullOrBlank() && networkUtils.isConnected()) {
                Log.i(TAG, "onViewCreated: Search Query -> $it")
                showLoading()
                getUpData(it)
                hide()
                stringFlag = it
                getCount()
            } else if (it.isNotBlank() && it.isNotEmpty() && !it.isNullOrBlank() && !networkUtils.isConnected()) {
                showNoConnection()
                activity?.msg(GetConstStringObj.NO_INTERNET, GetConstStringObj.RETRY, {
                    if (networkUtils.isConnected()) {
                        showLoading()
                        getUpData(it)
                        stringFlag = it
                        hide()
                        getCount()
                    }
                })
            }
        }
    }

    private fun hide() {
        binding.courseLottie.hide()
        binding.courseLayoutRecycle.show()
    }

    private fun showNoConnection() {
        binding.courseLottie.show()
        binding.courseLottie.setAnimation(R.raw.no_connection)
        binding.courseLayoutRecycle.hide()
    }

    private fun setUpRecycleView() {
        binding.courseLayoutRecycle.apply {
            setHasFixedSize(true)
            paginationAdaptor = PaginationAdaptor({
                dir(uploadFireBaseData = it)
            }, context)
            adapter = paginationAdaptor?.withLoadStateHeaderAndFooter(
                header = HeaderAndFooterAdaptor({
                    dir(msg = it)
                }, {
                    paginationAdaptor?.retry()
                }),
                footer = HeaderAndFooterAdaptor({
                    dir(msg = it)
                }, {
                    paginationAdaptor?.retry()
                })
            )
        }
    }

    private fun showLoading() = customProgress.showLoading(requireActivity(), LOADING)
    private fun hideLoading() = customProgress.hideLoading()

    private fun getUpData(searchQuery: String?) {
        lifecycleScope.launch {
            courseViewModel.getSearchQuery(searchQuery).collectLatest {
                hideLoading()
                paginationAdaptor?.submitData(it)
            }
        }
    }

    private fun getCount() {
        paginationAdaptor?.let {
            binding.srcResultLayout.show()
            binding.resultSize.text = it.itemCount.toString()
        }
    }

    private fun observed(observable: @NonNull Observable<String>) {
        observable.subscribe(object : Observer<String> {
            override fun onSubscribe(d: Disposable?) {
                d?.let {
                    disposables.add(it)
                    Log.i(TAG, "onSubscribe: Disposable Is Added")
                }
            }

            override fun onNext(s: String) {
                if (s.isBlank() || s.isEmpty()) {
                    showLoading()
                    stringFlag = null
                    getUpData(null)
                    return
                }
                courseViewModel.searchQuery.value = s
            }

            override fun onError(e: Throwable?) {
                e?.localizedMessage?.let {
                    dir(msg = it)
                }
            }

            override fun onComplete() {
                Log.i(TAG, "onComplete: Work Completed")
            }
        })
    }

    private fun dir(
        title: String = "Error",
        msg: String = "",
        uploadFireBaseData: UploadFireBaseData? = null
    ) {
        uploadFireBaseData?.let { uploadedData ->
            val sendSelectedCourse = SendSelectedCourse(
                courselevel = uploadedData.fireBaseCourseTitle?.courselevel,
                thumbnail = uploadedData.thumbnail,
                previewvideo = uploadedData.previewvideo,
                requirement = uploadedData.fireBaseCourseTitle?.requirement,
                totalhrs = uploadedData.fireBaseCourseTitle?.totalhrs,
                lastdate = uploadedData.fireBaseCourseTitle?.lastdate,
                totalprice = uploadedData.fireBaseCourseTitle?.totalprice,
                review = uploadedData.fireBaseCourseTitle?.review,
                targetaudience = uploadedData.fireBaseCourseTitle?.targetaudience,
                currentprice = uploadedData.fireBaseCourseTitle?.currentprice,
                category = uploadedData.fireBaseCourseTitle?.category,
                coursename = uploadedData.fireBaseCourseTitle?.coursename
            )
            val action =
                ExploreFragmentDirections.actionGlobalCourseViewFragment(sendSelectedCourse)
            findNavController().navigate(action)
            return
        }
        val action = ExploreFragmentDirections.actionGlobalPasswordDialog2(title, msg)
        findNavController().navigate(action)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    override fun onPause() {
        super.onPause()
        hideLoading()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        stringFlag?.let {
            outState.putString(GetConstStringObj.Create_Module, it)
        }
    }
}
