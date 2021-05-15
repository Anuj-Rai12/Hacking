package com.uptodd.uptoddapp.ui.capturemoments.generatecard

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.capturemoments.generatecard.FinalCard

class GenerateCardAdapter(private var cardList:List<FinalCard>,
                          private var context:Context):PagerAdapter()
{
    private lateinit var layoutInflater:LayoutInflater
    private var cardId:Long=0

    override fun getCount(): Int {
        return cardList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view.equals(`object`)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater= LayoutInflater.from(context)
        val view:View=layoutInflater.inflate(R.layout.generate_card_view_pager_item,container,false)

        val imageView:ImageView=view.findViewById(R.id.imageView)
        Log.d("div","GenerateCardAdapter L32 ${cardList[position]}")
        imageView.setImageBitmap(cardList[position].finalCard)


        container.addView(view,0)

        return view
    }

    override fun getItemPosition(`object`: Any): Int {
        return super.getItemPosition(`object`)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
    fun getCardId():Long
    {return cardId}
    fun setCardId(position: Int)
    {cardId=cardList[position].cardId}

}