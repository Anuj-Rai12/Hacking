package com.uptodd.uptoddapp.ui.capturemoments.generatecard

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.capturemoments.generatecard.FinalCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File


class GenerateCardViewModel(application: Application, val type: String?,val token:String?) : AndroidViewModel(
    application)
{

    var isCancelled: Boolean = false
    var uid: String?=null
    var imagePath:String?=null
    var babyName:String="baby"

    /*val card1=Card(1,"http://mars.jpl.nasa.gov/msl-raw-images/msss/01000/mcam/1000MR0044631240503684E03_DXXX.jpg","sdhhjcvsjhvsdjhd kj bdsbiu bk j gd sdhhjcvsjhvsdjhd kj bdsbiu bk j gd")
    val card2=Card(2,"http://www.linkpicture.com/q/frame_1.jpg"," lhifgd k g fekjg jkvuyr fwe lhifgd k g fekjg jkvuyr fwe")
    val card3=Card(3,"http://mars.jpl.nasa.gov/msl-raw-images/msss/01000/mcam/1000MR0044631170503677E03_DXXX.jpg","ejf bjhvmdns jkvq efbbdf m ejf bjhvmdns jkvq efbbdf m")
    val card4=Card(4,"http://mars.jpl.nasa.gov/msl-raw-images/msss/01000/mcam/1000ML0044631120305209E02_DXXX.jpg","lk bkebdk kjg kjv  biebb k bkebdk kjg kjv  biebb")
    val card5=Card(4,"https://www.linkpicture.com/q/card_3.png","“Being a family means you are a part of something very wonderful. It means you will love and be loved for the rest of your life.”")

    var cardTypeList= listOf<Card>(card1,card2,card3,card4,card5)*/


    private val viewModelJob= Job()
    private val viewModelScope= CoroutineScope(viewModelJob + Dispatchers.Main)

    private val database= com.uptodd.uptoddapp.database.capturemoments.generatecard.getDatabase(
        application)
    private val cardsRepository=CardsRepository(database)

    var isRepositoryEmpty=MutableLiveData<Boolean>()

    var isCardsLoaded:Boolean=false

    init {
        Log.d("div", "GenerateCardViewModel L37 $type")
        viewModelScope.launch {
            cardsRepository.refreshCards(type,token)
        }
    }

    fun refresh()
    {
        viewModelScope.launch {
            cardsRepository.refreshCards(type,token)
        }
    }

    val cardTypeList=cardsRepository.getCardsByCategoryKey(type)

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }




    val finalCards=mutableListOf<FinalCard>()
    val selectedCard:MutableLiveData<Int> = MutableLiveData(0)

    fun getBitmapById(cardId: Long):FinalCard
    {
        val list:ArrayList<FinalCard> = finalCards as ArrayList<FinalCard>
        for(card in finalCards)
        {
            if(card.cardId==cardId)
                return card

        }
        return finalCards[0]
    }

    var isSavedToLocalCache=false

    var finalBitmap:Bitmap?=null
    var isSavingToDatabase=MutableLiveData<Boolean>()
    var isCardSavedToDatabase:Boolean=false
    var isSavedToLocal=MutableLiveData<Boolean>()
    fun saveFinalCardToDatabase(finalCard: FinalCard, type: String, imagePath: String?) {
        val bitmapFile= File(imagePath)
        AndroidNetworking.upload("https://uptodd.com/api/generatecard")
            .addHeaders("Authorization","Bearer $token")
            .setTag("save")
            .addMultipartFile("generatedCard", bitmapFile)
            .addMultipartParameter("frameId", finalCard.finalCardFrameId.toString())
            .addMultipartParameter("category", type)
            .addMultipartParameter("userId", uid)
            .setPriority(Priority.HIGH)
            .build()
            .setUploadProgressListener { bytesUploaded, totalBytes ->
                // do anything with progress
            }
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    // do anything with response
                    Log.d("div","GenerateCardViewModel L94 $response")
                    if(response.get("status")!=null && response.get("status")=="Success")
                        isCardSavedToDatabase=true
                    isSavingToDatabase.value=false
                }

                override fun onError(error: ANError?) {
                    isSavingToDatabase.value=false
                    Log.d("div", "AccountViewModel L67 $error")
                    if (error!!.getErrorCode() != 0) {
                        Log.d("div", "onError errorCode : " + error.getErrorCode())
                        Log.d("div", "onError errorBody : " + error.getErrorBody())
                        Log.d("div", "onError errorDetail : " + error.getErrorDetail())
                    } else {
                        // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                        Log.d("div", "onError errorDetail : " + error.getErrorDetail())
                    }
                }
            })
    }
}