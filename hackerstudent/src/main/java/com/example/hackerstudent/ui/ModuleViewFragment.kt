package com.example.hackerstudent.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hackerstudent.R
import com.example.hackerstudent.databinding.ModuleViewFragmentBinding
import com.example.hackerstudent.paginate.HeaderAndFooterAdaptor
import com.example.hackerstudent.recycle.module.ModuleAdaptor
import com.example.hackerstudent.utils.*
import com.example.hackerstudent.viewmodels.CourseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class ModuleViewFragment : Fragment(R.layout.module_view_fragment) {
    private lateinit var binding: ModuleViewFragmentBinding
    private val args: ModuleViewFragmentArgs by navArgs()
    private val courseVideModel: CourseViewModel by viewModels()
    private var moduleAdaptor: ModuleAdaptor? = null

    @Inject
    lateinit var networkUtils: NetworkUtils

    @Inject
    lateinit var customProgress: CustomProgress

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideBottomNavBar()
        activity?.changeStatusBarColor()
        binding = ModuleViewFragmentBinding.bind(view)
        binding.categoryTitle.text=args.title
        showLoading()
        setRecyclerview()
        if (networkUtils.isConnected()) {
            setData()
            deviceConnected()
        } else {
            noInterNet()
            activity?.msg(GetConstStringObj.NO_INTERNET, GetConstStringObj.RETRY, {
                if (networkUtils.isConnected()) {
                    setData()
                    deviceConnected()
                }
            })
        }

        binding.arrowImg.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun noInterNet() {
        binding.courseLottie.show()
        binding.courseLayoutRecycle.hide()
        binding.courseLottie.setAnimation(R.raw.no_connection)
    }

    private fun deviceConnected() {
        binding.courseLayoutRecycle.show()
        binding.courseLottie.hide()
    }

    private fun setData() {
        lifecycleScope.launchWhenStarted {
            courseVideModel.getModule(args.title).collectLatest {
                hideLoading()
                moduleAdaptor?.submitData(it)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        hideLoading()
    }

    private fun showLoading() = customProgress.showLoading(requireActivity(), "Loading Course")
    private fun hideLoading() = customProgress.hideLoading()
    private fun setRecyclerview() {
        binding.courseLayoutRecycle.apply {
            setHasFixedSize(true)
            moduleAdaptor = ModuleAdaptor({ title, link ->//Assignment
                context?.msg("Assignment \n$title and Link \n $link")

            }, { title, link ->//Video
                context?.msg("$title and Link \n $link")
            })
            adapter = moduleAdaptor?.withLoadStateHeaderAndFooter(
                header = HeaderAndFooterAdaptor({
                    dir(msg = it)
                }, {
                    moduleAdaptor?.retry()
                }),
                footer = HeaderAndFooterAdaptor({
                    dir(msg = it)
                }, {
                    moduleAdaptor?.retry()
                })
            )
        }
    }

    private fun dir(choose: Int = 0, msg: String = "", title: String = "Error") {
        when (choose) {
            0 -> {
                val action = ModuleViewFragmentDirections.actionGlobalPasswordDialog2(title, msg)
                findNavController().navigate(action)
            }
            else -> {

            }
        }
    }
}