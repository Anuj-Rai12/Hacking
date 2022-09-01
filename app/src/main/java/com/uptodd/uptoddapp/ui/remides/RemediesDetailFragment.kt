package com.uptodd.uptoddapp.ui.remides

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.RemediesDetailLayoutFragmentBinding
import com.uptodd.uptoddapp.utils.*

class RemediesDetailFragment : Fragment(R.layout.remedies_detail_layout_fragment) {

    private lateinit var binding: RemediesDetailLayoutFragmentBinding
    private val args: RemediesDetailFragmentArgs by navArgs()
    private var isRemediesDesc = false
    private var isSymptomsDesc = false

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = RemediesDetailLayoutFragmentBinding.bind(view)

        getRandomBgColor.apply {
            activity?.changeStatusBarColor(this.first)
            binding.videoViewLayout.setBackgroundResource(this.second)
        }

        binding.title.text = args.res.name
        binding.descriptionTxt.text = (args.res.definition + "\n\nCheck the ")
        val ageTxt = "<font color='#2ba0c4'><b>Age Limit<b></font>"
        val age = "Minimum Age: <font color='#269B32'><b>${args.res.minAge} yrs<b></font>"
        val maxAge = "Maximum Age: <font color='#ff0000'><b>${args.res.maxAge} yrs<b></font>"

        binding.descriptionTxt.append(Html.fromHtml(ageTxt))
        binding.descriptionTxt.append("\n")
        binding.descriptionTxt.append(Html.fromHtml(age))
        binding.descriptionTxt.append("\n")
        binding.descriptionTxt.append(Html.fromHtml(maxAge))

        if (args.res.remedies.isEmpty() || args.res.remedies.isBlank()) {
            binding.remidesLayout.root.hide()
        } else
            binding.remidesLayout.titleDesc.text = args.res.remedies

        if (args.res.symptoms.isEmpty() || args.res.symptoms.isBlank()) {
            binding.symptonsLayout.root.hide()
        } else
            binding.symptonsLayout.titleDesc.text = args.res.symptoms

        "https://img.youtube.com/vi/${args.res.link}/mqdefault.jpg".also { url ->
            Glide.with(this)
                .load(Uri.parse(url))
                .transform(CenterCrop(), RoundedCorners(20))
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.default_set_android_thumbnail)
                .into(binding.videoThumbnail)
        }
        binding.symptonsLayout.titleForTxt.text = "Symptoms"

        binding.remidesLayout.titleForTxt.text = "Remedies"

        binding.mainImageLayout.setOnClickListener {
            binding.videoThumbnail.invisible()
            binding.darkColorRecycle.hide()
            binding.webViewPlayer.setBackgroundColor(Color.TRANSPARENT)
            binding.mainConstraintHolder.setPadding(0)
            setWebViewSetUp()
        }

        listenForProgress()


        binding.bckArrow.setOnClickListener {
            findNavController().popBackStack()
        }


        binding.remidesLayout.root.setOnClickListener {
            if (!isRemediesDesc) {
                showDecsTxt(binding.remidesLayout.titleDesc, binding.remidesLayout.titleForTxt)
            } else {
                hideDecsTxt(binding.remidesLayout.titleDesc, binding.remidesLayout.titleForTxt)
            }
            isRemediesDesc = !isRemediesDesc
        }

        binding.symptonsLayout.root.setOnClickListener {
            if (!isSymptomsDesc) {
                showDecsTxt(binding.symptonsLayout.titleDesc, binding.symptonsLayout.titleForTxt)
            } else {
                hideDecsTxt(binding.symptonsLayout.titleDesc, binding.symptonsLayout.titleForTxt)
            }
            isSymptomsDesc = !isSymptomsDesc
        }

    }


    private fun showDecsTxt(view: TextView, changeTextView: TextView) {
        changeTextView.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.ic_keyboard_arrow_up,
            0
        )
        view.show()
    }

    private fun hideDecsTxt(view: TextView, changeTextView: TextView) {
        changeTextView.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.ic_keyboard_arrow_down,
            0
        )
        view.hide()
    }

    override fun onPause() {
        super.onPause()
        activity?.changeStatusBarColor(R.color.colorPrimary)
    }

    private fun listenForProgress() {
        binding.webViewPlayer.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                Log.i("PodcastWebinarActivity", "onProgressChanged: $newProgress")
                binding.progressCircle.show()
                if (newProgress == 100) {
                    binding.mainImageLayout.hide()
                    binding.progressCircle.hide()
                }
                super.onProgressChanged(view, newProgress)
            }
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebViewSetUp() {
        binding.webViewPlayer.apply {
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

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return false
                }
            }
            this.loadUrl("https://uptodd.com/playYoutubeVideos/${args.res.link}?fs=0")//uxSh8svEoZQ
        }
    }

}