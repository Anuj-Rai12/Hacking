package com.uptodd.uptoddapp.ui.blogs.fullblog

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ActivityFullBlogBinding
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.ChangeLanguage


class FullBlogActivity : AppCompatActivity() {

    lateinit var binding: ActivityFullBlogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ChangeLanguage(this).setLanguage()
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_full_blog
        )
        binding.lifecycleOwner = this
        supportActionBar?.title=getString(R.string.blogs)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if(AppNetworkStatus.getInstance(this).isOnline) {
            load()
        }
        else{
            val snackbar = Snackbar.make(binding.webView, getString(R.string.no_internet_connection), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.retry)) {
                    load()
                }
            snackbar.show()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun load() {
        val intent: Intent = intent
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                //Make the bar disappear after URL is loaded, and changes string to Loading...
                title = "Loading..."
                binding.progressBar.setProgress(progress) //Make the bar disappear after URL is loaded

                // Return the app name after finish loading
                if (progress == 100)
                {
                    binding.progressBar.visibility= View.INVISIBLE
                }
            }
        }
        intent.getStringExtra("url")?.let { binding.webView.loadUrl(it) }
        binding.webView.settings.javaScriptEnabled=true


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}