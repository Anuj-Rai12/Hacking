package com.uptodd.uptoddapp.doctor.resources

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ViewPDFFragment : Fragment() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =  inflater.inflate(R.layout.fragment_view_pdf, container, false)

        val webView = view.findViewById<WebView>(R.id.view_pdf_web_view)

        val args = ViewPDFFragmentArgs.fromBundle(requireArguments())
        val url = args.url

        webView.webViewClient = WebViewClient()
        webView.settings.setSupportZoom(true)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("https://docs.google.com/gview?embedded=true&url=$url")

        return view
    }

    companion object {
        fun newInstance(param1: String, param2: String) =
            ViewPDFFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}