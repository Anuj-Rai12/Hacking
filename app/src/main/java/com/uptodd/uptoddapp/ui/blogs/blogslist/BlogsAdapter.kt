package com.uptodd.uptoddapp.ui.blogs.blogslist

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.blogs.Blogs
import kotlinx.android.synthetic.main.blog_item_view.view.*


class BlogsAdapter(
    var allBlogsList: List<Blogs?>,
    private val onCompleteClickListener: OnCompleteClickListener,
    var context: FragmentActivity
) :RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    init {
        Log.d("div", "BlogsAdapter L32 $context")
    }

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType==VIEW_TYPE_ITEM)
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.blog_item_view, parent, false)
            return ItemViewHolder(view, onCompleteClickListener, context)
        }
        else
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.progressbar_loading, parent, false)
            return LoadingViewHolder(view, onCompleteClickListener)
        }
    }

    override fun getItemCount(): Int {
        Log.d("div", "BlogsAdapter L25 " + allBlogsList.size)
        return allBlogsList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is ItemViewHolder)
        {
            val card = allBlogsList[position]
            if (card != null) {
                holder.bind(card)
            }
        }
        else if(holder is LoadingViewHolder)
        {
            showLoadingView(holder, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (allBlogsList[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    class ItemViewHolder(
        itemView: View,
        onCompleteClickListener: OnCompleteClickListener,
        var context: Context
    ) :
        RecyclerView.ViewHolder(itemView) {

        init {
            itemView.layout.setOnClickListener {
                onCompleteClickListener.onClickBlog(adapterPosition)
                Log.d("div", "BlogsAdapter L86 ${context.contentResolver}")
            }
        }

        fun bind(blog: Blogs) {
            itemView.textView_title1.text = blog.title.toString()
            bindImage(itemView.imageView1, blog.imageURL, blog.title.toString())
        }
        fun bindImage(imgView: ImageView, imgUrl: String?, description: String) {
            imgUrl?.let {

                /*Glide.
                with(imgView.context).
                asBitmap().
                load(imgUrl).
                into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        Log.d("div", "GenerateCardFragment L185 $resource")
                        imgView.layoutParams.width=imgView.layoutParams.height*(resource.width/resource.height)
                        imgView.setImageBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        Log.d("div", "GenerateCardFragment L190 $placeholder")
                    }
                })*/

                val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
                Glide.with(imgView.context)
                    .load(imgUri)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.loading_animation)
                            .error(R.drawable.default_set_default_blog)
                    )
                    .into(imgView)

            }
        }
        /*fun addTitleToImage(imageUri: Uri, description: String, imgView: ImageView,imgUrl: String)
        {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, imageUri))
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }
            //val bitmap: Bitmap = (imgView.drawable as BitmapDrawable).bitmap
            /*val bitmap=Glide.with()
                .load(imageUri)
                    .asBitmap()
                    .into(100, 100) //width and height
                    .get();*/
            //var bitmap:Bitmap?=null
            //bitmap=getBitmapFromURL(imgUrl)

            val bitmapWithText=Bitmap.createBitmap(
                bitmap!!.width,
                bitmap!!.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas=Canvas(bitmapWithText)

            canvas.drawBitmap(bitmap,0.0f,0.0f,null)

            val tPaint = Paint()
            val textSize:Float=20f                      //textSize
            tPaint.textSize = textSize
            tPaint.color = Color.WHITE                           //textColor
            tPaint.style = Paint.Style.FILL
            canvas.drawText(description, 10f,bitmap!!.height * 0.8f, tPaint)
            imgView.post(Runnable { imgView.setImageBitmap(bitmapWithText)  })
            imgView.postDelayed(Runnable { },1)
            /*val bytes = ByteArrayOutputStream()
            bitmapWithText.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path: String = Images.Media.insertImage(
                context.contentResolver,
                bitmapWithText,
                "Title",
                null
            )
            return Uri.parse(path)*/

        }
        fun getBitmapFromURL(src: String?): Bitmap? {
            return try {
                val url = URL(src)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                // Log exception
                null
            }
        }*/
    }

    private class LoadingViewHolder(
        itemView: View,
        onCompleteClickListener: OnCompleteClickListener
    ) : RecyclerView.ViewHolder(itemView) {
        var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    }

    private fun showLoadingView(viewHolder: LoadingViewHolder, position: Int) {
        //ProgressBar would be displayed
    }

    interface OnCompleteClickListener {
        fun onClickBlog(position: Int)
    }
}

