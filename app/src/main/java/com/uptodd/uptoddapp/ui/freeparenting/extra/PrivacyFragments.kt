package com.uptodd.uptoddapp.ui.freeparenting.extra

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.FreeParentingDemoActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.PolicyFragmentsLayoutBinding
import com.uptodd.uptoddapp.utils.FilesUtils
import com.uptodd.uptoddapp.utils.isNetworkAvailable
import com.uptodd.uptoddapp.utils.show
import com.uptodd.uptoddapp.utils.showSnackbar

class PrivacyFragments : Fragment(R.layout.policy_fragments_layout) {
    private lateinit var binding: PolicyFragmentsLayoutBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = PolicyFragmentsLayoutBinding.bind(view)
        if (requireActivity().application.isNetworkAvailable()) {
            binding.webView.show()
            setWebSiteData(FilesUtils.PrivacyUrl)
            listenForProgress()
        } else {
            binding.progressbar.visibility = View.INVISIBLE
            binding.progressbar.isIndeterminate = true
            binding.root.showSnackbar("No Internet Connection found!!")
        }

        binding.toolbarNav.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebSiteData(url: String) {
        binding.webView.apply {
            settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                javaScriptCanOpenWindowsAutomatically = true
                loadsImagesAutomatically = true
                setSupportMultipleWindows(true)
            }
            post {
                loadUrl(url)
            }
        }
    }

    private fun listenForProgress() {
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                binding.progressbar.progress = newProgress
                if (newProgress == 100) {
                    binding.progressbar.progress = 0
                }
                super.onProgressChanged(view, newProgress)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        (activity as FreeParentingDemoActivity?)?.showBottomNavBar()
    }


    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        (activity as FreeParentingDemoActivity?)?.hideBottomNavBar()
        binding.toolbarNav.titleTxt.text = "Privacy Policy"
        binding.toolbarNav.titleTxt.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        binding.toolbarNav.topAppBar.setNavigationIcon(R.drawable.arrow)
    }
}