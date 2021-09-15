package com.example.hackerstudent.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
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

    @Inject
    lateinit var customProgress: CustomProgress

    @Inject
    lateinit var networkUtils: NetworkUtils

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity?)!!.hide()
        binding = ExploreFragmentBinding.bind(view)
        binding.arrowImg.setOnClickListener {
            findNavController().popBackStack()
        }
        savedInstanceState?.let {
            stringFlag = it.getString(GetConstStringObj.Create_Module)
        }
        setUpRecycleView()
        if (stringFlag == null && networkUtils.isConnected()) {
            showLoading()
            hide()
            getUpData(stringFlag)
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
                if (text.isNullOrBlank()) {
                    showLoading()
                    getUpData(null)
                    Log.i(TAG, "onViewCreated: DoOnTextChange Text Empty Calls ")
                } else {
                    if (!emitter.isDisposed && text.isNotEmpty()) {
                        Log.i(TAG, "onViewCreated: Query Length -> ${text.length}")
                        emitter.onNext(text.toString())
                    }
                }
            }
        }.debounce(GetConstStringObj.timeToSearch.toLong(), TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
        observed(observable)

        courseViewModel.searchQuery.asLiveData().observe(viewLifecycleOwner) {
            if (it.isNotBlank() && it.isNotEmpty() && !it.isNullOrBlank() && networkUtils.isConnected()) {
                Log.i(TAG, "onViewCreated: Search Query -> $it")
                showLoading()
                getUpData(it)
                hide()
                getCount()
            } else if (it.isNotBlank() && it.isNotEmpty() && !it.isNullOrBlank() && !networkUtils.isConnected()) {
                showNoConnection()
                activity?.msg(GetConstStringObj.NO_INTERNET, GetConstStringObj.RETRY, {
                    if (networkUtils.isConnected()) {
                        showLoading()
                        getUpData(it)
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
            paginationAdaptor = PaginationAdaptor {
                context.msg("got it")
                Log.i(TAG, "setUpRecycleView: $it")
            }
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
                if (s.isBlank() || s.isEmpty())
                    return
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

    private fun dir(title: String = "Error", msg: String = "") {
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