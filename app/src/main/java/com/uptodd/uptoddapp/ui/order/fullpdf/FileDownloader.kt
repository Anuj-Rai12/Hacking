package com.uptodd.uptoddapp.ui.order.fullpdf

import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class FileDownloader
{
    private val MEGABYTE:Int=1024*1024

    fun downloadFile(fileUrl:String?,directory:File)
    {
        try{
            val url=URL(fileUrl)
            Log.d("div","FileDownloader L1: $url in $directory")
            val urlConnection:HttpURLConnection=url.openConnection() as HttpURLConnection
            Log.d("div","FileDownloader L2")
            urlConnection.connect()
            Log.d("div","FileDownloader L3")

            val inputStream=urlConnection.inputStream
            Log.d("div","FileDownloader L4")

            val  fileOutputStream=FileOutputStream(directory)
            Log.d("div","FileDownloader L5")

            val totalSize=urlConnection.contentLength
            Log.d("div","FileDownloader L6")

            val buffer:ByteArray= ByteArray(MEGABYTE)
            Log.d("div","FileDownloader L7")

            var bufferLength:Int=0
            Log.d("div","FileDownloader L8")

            bufferLength=inputStream.read(buffer)
            Log.d("div","FileDownloader L9")

            while(bufferLength>0)
            {
                fileOutputStream.write(buffer,0,bufferLength)
                Log.d("div","FileDownloader L10")
                bufferLength=inputStream.read(buffer)
                Log.d("div","FileDownloader L11")
            }
            fileOutputStream.close()
            Log.d("div","FileDownloader L12")

        }
        catch (e:MalformedURLException)
        {
            Log.d("div","FileDownloader L41 $e")
        }
        catch(e:IOException)
        {
            Log.d("div","FileDownloader L46 $e")
        }
        catch (e:FileNotFoundException)
        {
            Log.d("div","FileDownloader L50 $e")
        }

    }
}