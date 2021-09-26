package com.example.hackerstudent.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hackerstudent.R
import com.example.hackerstudent.TAG
import com.example.hackerstudent.databinding.PdfLayoutFragmentBinding
import com.example.hackerstudent.repos.FileSource
import com.example.hackerstudent.utils.*
import com.example.hackerstudent.viewmodels.CourseViewModel
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class PdfFileViewFragment : Fragment(R.layout.pdf_layout_fragment) {
    private lateinit var binding: PdfLayoutFragmentBinding
    private val args: PdfFileViewFragmentArgs by navArgs()
    private val viewModel: CourseViewModel by viewModels()
    private var getDefaultPage: Int? = null

    @Inject
    lateinit var networkUtils: NetworkUtils

    @Inject
    lateinit var customProgress: CustomProgress

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.changeStatusBarColor(R.color.pdf_color)
        hideBottomNavBar()
        activity?.preventScreenShotOrVideoRecoding()
        binding = PdfLayoutFragmentBinding.bind(view)
        savedInstanceState?.let {
            getDefaultPage = it.getInt(GetConstStringObj.Create_Module)
        }
        binding.categoryTitle.text = args.title
        binding.arrowImg.setOnClickListener {

            lifecycleScope.launch {
                viewModel.fileStore.collectLatest {
                    it?.let { file ->
                        file.file.delete()
                        Log.i(TAG, "onViewCreated: file Deleted status:Successfully")
                    }
                }
            }
            activity?.removedScreenShotFlagOrVideoRecoding()
            findNavController().popBackStack()
        }
        onBackPressed()
        viewModel.fileStore.asLiveData().observe(viewLifecycleOwner) {
            if (it != null) {
                showPdf(it.file)
            } else if (networkUtils.isConnected()) {
                downloadPdf()
            } else if (!networkUtils.isConnected()) {
                noInterNetConnection()
                activity?.msg(GetConstStringObj.NO_INTERNET, GetConstStringObj.RETRY, {
                    if (networkUtils.isConnected()) {
                        deviceConnected()
                        downloadPdf()
                    }
                })
            }
        }
    }

    private fun hideLoading() = customProgress.hideLoading()
    private fun showLoading(string: String) = customProgress.showLoading(requireActivity(), string)

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Log.i(TAG, "onBackPressed: on Back Pressed")
        }.handleOnBackPressed()
    }

    private fun noInterNetConnection() {
        binding.showPdfFile.hide()
        binding.courseLottie.show()
        binding.courseLottie.setAnimation(R.raw.no_connection)
    }

    private fun deviceConnected() {
        binding.courseLottie.hide()
        binding.showPdfFile.show()
    }

    override fun onPause() {
        super.onPause()
        hideLoading()
    }

    private fun downloadPdf() {
        viewModel.getDownloadFile(args.title, args.url, requireContext())
            .observe(viewLifecycleOwner) {
                when (it) {
                    is MySealed.Error -> {
                        hideLoading()
                        noInterNetConnection()
                        dir(msg = "${it.exception?.localizedMessage}")
                    }
                    is MySealed.Loading -> {
                        deviceConnected()
                        showLoading(it.data as String)
                    }
                    is MySealed.Success -> {
                        deviceConnected()
                        hideLoading()
                        val info = it.data as FileSource
                        Log.i(TAG, "downloadPdf:File -> ${info.file}")
                        Log.i(TAG, "downloadPdf: Total file Information-> ${info.info}")
                        viewModel.fileStore.value = info
                    }
                }
            }
    }

    private fun showPdf(url: File?) {
        url?.let { uri ->
            binding.showPdfFile.fromFile(uri).defaultPage(getDefaultPage ?: 0)
                .enableSwipe(true)
                .enableDoubletap(true)
                .scrollHandle(object : DefaultScrollHandle(activity) {})
                .onPageChange { page, _ ->
                    getDefaultPage = page
                }.spacing(2)
                .onError {
                    dir("Error File", msg = it.localizedMessage!!)
                }
                .nightMode(requireActivity().getNightMode())
                .enableAnnotationRendering(true)
                .load()
            Log.i(TAG, "showPdf: ${binding.showPdfFile.currentPage}")
        }
    }

    private fun dir(title: String = "Error", msg: String = "") {
        val action = ProfileFragmentDirections.actionGlobalPasswordDialog2(title, msg)
        findNavController().navigate(action)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        getDefaultPage?.let {
            outState.putInt(GetConstStringObj.Create_Module, it)
        }
    }
}