package com.uptodd.uptoddapp.ui.capturemoments.selecttype

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.capturemoments.selecttype.PhotoType
import com.uptodd.uptoddapp.utilities.ScreenDpi

class SelectTypeAdapter(private var photoTypeList: List<PhotoType>,
                        private var context:Context,
                        private val onClickListener:OnClickListener):PagerAdapter()
{
    private lateinit var layoutInflater:LayoutInflater

    override fun getCount(): Int {
        return photoTypeList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view.equals(`object`)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater= LayoutInflater.from(context)
        val view:View=layoutInflater.inflate(R.layout.select_photo_type_view_pager_item,container,false)

        val imageView:ImageView=view.findViewById(R.id.imageView)
        val title:TextView=view.findViewById(R.id.textView)

        val imgUrl=photoTypeList[position].imageURL
        imgUrl.let {
            //val imgUri = imgUrl?.toUri()?.buildUpon()?.scheme("https")?.build()

            val dpi= ScreenDpi(context).getScreenDrawableType()
            val appendable = "https://uptodd.com/images/app/android/details/cards_category/$dpi/"
            val url=appendable+imgUrl+".webp"
            Glide.with(imageView.context)
                .load(url)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                )
                .into(imageView)
        }
        val text=photoTypeList[position].title
        title.text=text
        Log.d("div","SelectTypeAdapter L56 $text")
        imageView.setOnClickListener{onClickListener.onClickType(text)}
        title.setOnClickListener{onClickListener.onClickType(text)}

        container.addView(view,0)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    interface OnClickListener {
        fun onClickType(type:String?)
    }

}