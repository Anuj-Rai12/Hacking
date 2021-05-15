package com.uptodd.uptoddapp.ui.order.fullpdf

import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentFullPdfBinding
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import java.io.File

class FullPdfFragment : Fragment() {

    private lateinit var binding:FragmentFullPdfBinding

    private var pdfPath: String?=null
    private var pdfName: String?=null
    private var detailsUrl:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            detailsUrl=it.getString("url")
            pdfName=it.getString("pdfName")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        binding=DataBindingUtil.inflate(layoutInflater,R.layout.fragment_full_pdf,container,false)
        binding.lifecycleOwner=this

        (requireActivity() as AppCompatActivity?)?.supportActionBar?.title="$pdfName"
        (requireActivity() as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        pdfPath=  requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/orders/" + pdfName

        if(detailsUrl!=null && pdfName!=null)
        {
            //loadPdf()
            if(File(pdfPath).exists())
                displayPdf()
            else
                downloadPdf()
        }
        else
        {
            Toast.makeText(activity,getString(R.string.unable_to_load_pdf), Toast.LENGTH_LONG).show()
        }

        return binding.root
    }

    private fun downloadPdf()
    {
        if(AppNetworkStatus.getInstance(requireContext()).isOnline) {
            val downloadPdf = DownloadPDF(object : DownloadPDF.AsynResponse {
                override fun processFinish(output: Boolean?) {
                    if (output != null && output) {
                        binding.progressBar.visibility = View.INVISIBLE
                        displayPdf()
                    }

                }
            }, requireContext()).execute(detailsUrl, pdfName)

            if (downloadPdf.getStatus() == AsyncTask.Status.PENDING) {
                Toast.makeText(activity, getString(R.string.wait_), Toast.LENGTH_LONG).show()
            }

            if (downloadPdf.getStatus() == AsyncTask.Status.RUNNING) {
                binding.progressBar.visibility = View.VISIBLE
            }
        }
        else
            Snackbar.make(binding.pdfView,getString(R.string.no_internet_connection),Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.retry)){
                    downloadPdf()
                }.show()
    }
    private fun displayPdf() {
        if(pdfPath!=null) {
            val pdfUri = Uri.fromFile(File(pdfPath))
            Log.d("div", "FullPdfActivity L38 $pdfUri")
            binding.pdfView.fromUri(pdfUri).load()
        }
        else
        {
            Toast.makeText(activity,getString(R.string.unable_to_load_pdf),Toast.LENGTH_LONG).show()
        }
    }

    /*private fun loadPdf()
    {
        val url="http://drive.google.com/viewerng/viewer?embedded=true&url=$detailsUrl"
        if(detailsUrl!=null) {
            binding.webView.settings.javaScriptEnabled=true
            binding.webView.settings.builtInZoomControls=true
            binding.webView.webChromeClient = object:WebChromeClient(){
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    if(newProgress==100)
                    {
                        binding.progressBar.visibility=View.GONE
                    }

                }
            }
            binding.webView.loadUrl(url)
        }
    }*/

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}