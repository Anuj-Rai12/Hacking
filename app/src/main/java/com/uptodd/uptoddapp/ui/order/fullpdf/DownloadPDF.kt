package com.uptodd.uptoddapp.ui.order.fullpdf

import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException

class DownloadPDF(asynResponse: AsynResponse,val context: Context):AsyncTask<String, Void, Void>()
{
    //lateinit var context:Context

    interface AsynResponse {
        fun processFinish(output: Boolean?)
    }

    var asynResponse: AsynResponse? = null
    init{
        this.asynResponse = asynResponse
    }

    override fun doInBackground(vararg params: String?): Void? {
        var fileUrl=params[0]
        var fileName=params[1]

        //val extStorageDirectory=Environment.getExternalStorageDirectory().toString()

        //val folder=File(extStorageDirectory, "UpTodd Orders")
        val folder=File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/orders")
        folder.mkdir()

        val pdfFile=File(folder, fileName)

        try{
            pdfFile.createNewFile()
        }catch (e: IOException)
        {
            Log.d("div", "DownloadPDF L31 $e")
        }

        FileDownloader().downloadFile(fileUrl, pdfFile)

        return null
    }
    override fun onPostExecute(aVoid: Void?) {
        super.onPostExecute(aVoid)
        asynResponse?.processFinish(true)
    }



}