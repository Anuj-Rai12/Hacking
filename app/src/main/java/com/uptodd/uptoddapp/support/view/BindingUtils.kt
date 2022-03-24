package com.uptodd.uptoddapp.support.view

import android.text.util.Linkify
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.Conversion
import de.hdodenhof.circleimageview.CircleImageView

@BindingAdapter("ticketMessage")
fun TextView.setTicketMessage(item: TicketMessage?) {
    item?.let {
        text = item.message
        Linkify.addLinks(this,Linkify.WEB_URLS)
        val layoutParams:LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        if(item.isSenderValue){
            layoutParams.gravity = Gravity.END
            setLayoutParams(layoutParams)
            setBackgroundResource(R.drawable.round_message_sent)
        }
        else{
            layoutParams.gravity = Gravity.START
            setLayoutParams(layoutParams)
            setBackgroundResource(R.drawable.round_message_received)
        }
    }
}

@BindingAdapter("ticketLayout")
fun LinearLayout.setTicketLayout(item: TicketMessage?) {
    item?.let {

        val layoutParams:LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        if(item.isSenderValue){
            layoutParams.gravity = Gravity.END
            setLayoutParams(layoutParams)
            setBackgroundResource(R.drawable.round_message_sent)
        }
        else{
            layoutParams.gravity = Gravity.START
            setLayoutParams(layoutParams)
            setBackgroundResource(R.drawable.round_message_received)
        }
    }
}



@BindingAdapter("ticketDate")
fun TextView.setTicketDate(item: TicketMessage?) {
    item?.let {
        text = AllUtil.getTimeFromMillis(item.time)
    }
}


@BindingAdapter("ticketStatus")
fun ImageView.setTicketStatus(item: TicketMessage?) {
    item?.let {
        if(item.isSenderValue)
        {
            val layoutParams:LinearLayout.LayoutParams = LinearLayout.LayoutParams(Conversion.convertDpToPixel(13F, context), Conversion.convertDpToPixel(13F, context))
            layoutParams.gravity = Gravity.CENTER_VERTICAL
            setLayoutParams(layoutParams)
            setImageResource(R.drawable.material_message_recieved)
        }
        else
        {
            visibility=View.GONE
        }
    }
}

@BindingAdapter("supportStatus")
fun TextView.setSupportStatus(item: TicketMessage?) {
    item?.let {
        if(item.isSenderValue)
        {
            visibility=View.GONE
        }
    }
}


@BindingAdapter("layoutImageStatus")
fun CircleImageView.setLayoutImageStatus(item: TicketMessage?) {

  item.let {
return

  }
}
@BindingAdapter("layoutGravity")
fun LinearLayout.setLayoutGravity(item: TicketMessage?) {

    item?.let {
        val layoutParams:LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        if(item.isSenderValue){
            gravity=Gravity.RIGHT
        }
        else{
           gravity=Gravity.LEFT
        }
    }
}
@BindingAdapter("sendImageStatus")
fun CircleImageView.setSendImageStatus(item: TicketMessage?) {

    item.let {

        if(it!!.isSenderValue){
            visibility=View.VISIBLE
        }
        else{

        }
    }
}


@BindingAdapter("ticketSender")
fun LinearLayout.setTicketSender(item: TicketMessage?) {
    item?.let {
        val layoutParams:LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        if(item.isSenderValue){
            layoutParams.gravity = Gravity.END
            setLayoutParams(layoutParams)
        }
        else{
            layoutParams.gravity = Gravity.START
            setLayoutParams(layoutParams)
        }
    }
}

//
//@BindingAdapter("sleepQualityString")
//fun TextView.setSleepQualityString(item: SleepNight?) {
//    item?.let {
//        text = convertNumericQualityToString(item.sleepQuality, context.resources)
//    }
//}