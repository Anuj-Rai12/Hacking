package com.uptodd.uptoddapp.ui.capturemoments.generatecard

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.androidnetworking.AndroidNetworking
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.capturemoments.generatecard.FinalCard
import com.uptodd.uptoddapp.databinding.FragmentGenerateCard1Binding
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.ScreenDpi
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.sql.Timestamp
import java.util.*


class GenerateCardFragment : Fragment() {

    var preferences: SharedPreferences? = null
    var token: String? = null

    private var imagePath: String? = null
    private var type: String? = null


    private lateinit var binding: FragmentGenerateCard1Binding
    private lateinit var viewModel: GenerateCardViewModel

    private val requiredBitmapSize: Int = 900
    private val textSize: Float = 75f
    private val lineDifference =
        75f              //lineDifference>=textSize and lineDifference should not be too much
    private val cardListMarginInDP = 5f
    private val cardWidthToHeightRatio = 0.5625
    private val paddingLeftRightText = 50
    private val imageTopPadding = 50f
    private val lowerBitmapHeight = 300

    private var imageBitmap: Bitmap? = null
    private var rescaledBitmap: Bitmap? = null
    private var roundedCornerBitmap: Bitmap? = null
    //private var finalBitmap:Bitmap?=null

    private lateinit var adapter: GenerateCardAdapter

    private val REQUEST_CODE_SHARE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        arguments?.let {
            imagePath = it.getString("imagePath")
            type = it.getString("photoType")
            Log.d("div", "GenerateCardFragment L61 $imagePath $type")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_generate_card_1,
            container,
            false
        )
        binding.lifecycleOwner = this

        //The StrictMode policy has been changed to remove the warning while sharing the files on phones with API>24
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        preferences = activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        if (preferences!!.contains("token"))
            token = preferences!!.getString("token", "")

        viewModel = ViewModelProvider(
            this,
            GenerateCardViewModelFactory(requireActivity().application, type, token)
        )
            .get(GenerateCardViewModel::class.java)
        viewModel.imagePath = imagePath

        if (preferences!!.contains("uid"))
            viewModel.uid = preferences!!.getString("uid", "")
        if (preferences!!.contains("babyName"))
            viewModel.babyName = preferences!!.getString("babyName", "baby").toString()
        //viewModel.type = type
        //viewModel.refreshDatabase(type)
        //viewModel.getFromDatabase()

        (activity as AppCompatActivity?)?.supportActionBar?.title = getString(R.string.choose_Card)
        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        Log.d("div", "GenerateCardFragment L66 $requiredBitmapSize ")

        getImageFromStorage()
        rescaleBitmap()
        roundedCornersBitmap()

        initObservers()


        setUpViewPager(emptyList())


        binding.buttonShare.setOnClickListener { onClickShare() }
        binding.buttonSave.setOnClickListener { onClickSave() }


        return binding.root
    }

    private fun initObservers() {
        //observer for cards loading
        viewModel.cardTypeList.observe(viewLifecycleOwner, Observer {
            Log.d("div", "GenerateCardFragment L111 ${viewModel.cardTypeList.value}")
            if (viewModel.cardTypeList.value == null || viewModel.cardTypeList.value!!.isEmpty()) {
                viewModel.isRepositoryEmpty.value = true
                loadData()
            } else
                viewModel.isRepositoryEmpty.value = false
            if (viewModel.cardTypeList.value != null && !viewModel.isCardsLoaded && viewModel.cardTypeList.value!!.isNotEmpty()) {
                Log.d("div", "GenerateCardFragment L101 ${viewModel.cardTypeList.value!![0]}")
                addCardsToList()
                setUpViewPager(viewModel.finalCards)
                viewModel.isCardsLoaded = true
            }
        })

        //observer for card saved to online database
        viewModel.isSavingToDatabase.observe(viewLifecycleOwner, Observer {
            if (!it) {
                if (viewModel.isCardSavedToDatabase) {

                    saveFileToLocal(viewModel.finalBitmap!!)
                } else if (!viewModel.isCancelled) {
                    Toast.makeText(
                        activity,
                        getString(R.string.error_saving_to_database),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        })

        //observe for card saved to both local and online database
        viewModel.isSavedToLocal.observe(viewLifecycleOwner, Observer {
            Log.d("div", "GenerateCardFragment L151 ${viewModel.isSavedToLocal.value}")
            if (it) {
                UpToddDialogs(requireContext()).showDialog(R.drawable.gif_done,
                    getString(R.string.lovely_moment_ofsuper_baby_and_super_parent_is_saved_in_your_gallery),
                    getString(R.string.close),
                    object : UpToddDialogs.UpToddDialogListener {
                        override fun onDialogButtonClicked(dialog: Dialog) {
                            dialog.dismiss()
                        }
                    })
            }
        })
    }

    private fun loadData() {
        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
            showLoadingDialog()
            viewModel.refresh()
        } else {
            val snackbar = Snackbar.make(
                binding.layout,
                getString(R.string.no_internet_connection),
                Snackbar.LENGTH_LONG
            )
                .setAction(getString(R.string.retry)) {
                    loadData()
                }
            snackbar.show()
        }
    }


    private fun getImageFromStorage() {
        val imgFile = File(viewModel.imagePath!!)
        if (imgFile.exists()) {
            imageBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
        } else {
            Toast.makeText(
                activity,
                getString(R.string.image_not_found_in_storage),
                Toast.LENGTH_LONG
            ).show()
            requireActivity().finish()
        }
    }

    private fun rescaleBitmap() {
        /*val largerDimension=if(imageBitmap!!.width>imageBitmap!!.height) imageBitmap!!.width else imageBitmap!!.height
        val scalingFactor:Float=requiredBitmapSize.toFloat()/largerDimension.toFloat()

        val scaledWidth: Float =imageBitmap!!.width * scalingFactor
        val scaledHeight: Float = imageBitmap!!.height * scalingFactor
        rescaledBitmap=Bitmap.createScaledBitmap(
            imageBitmap!!,
            scaledWidth.toInt(),
            scaledHeight.toInt(),
            false
        )*/

        /*Log.d("div","GenerateCardFragment L242 ${imageBitmap!!.height} ${imageBitmap!!.width}")
        val scalingFactor:Float=requiredBitmapSize.toFloat()/imageBitmap!!.width

        val scaledWidth: Float =imageBitmap!!.width * scalingFactor
        val scaledHeight: Float = imageBitmap!!.height * scalingFactor
        rescaledBitmap=Bitmap.createScaledBitmap(
            imageBitmap!!,
            scaledWidth.toInt(),
            scaledHeight.toInt(),
            false
        )
        Log.d("div","GenerateCardFragment L253 ${rescaledBitmap!!.height} ${rescaledBitmap!!.width}")*/

        rescaledBitmap =
            imageBitmap!!        //The rescaling is being done in CaptureImageFragment in resolveRotationErrorAndRescaleImage function

    }

    private fun roundedCornersBitmap() {
        val rounder = Bitmap.createBitmap(
            rescaledBitmap!!.width,
            rescaledBitmap!!.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(rounder)
        val paint = Paint()
        canvas.drawRoundRect(
            RectF(
                0f,
                0f,
                rescaledBitmap!!.width.toFloat(),
                rescaledBitmap!!.height.toFloat()
            ), 20.0f, 20.0f, paint
        )
        canvas.drawBitmap(rescaledBitmap!!, 0f, 0f, null)
        rescaledBitmap = rounder
    }

    private fun addCardsToList() {

        for ((i, imgUrl) in viewModel.cardTypeList.value!!.withIndex()) {
            imgUrl.let {
                val dpi = ScreenDpi(requireContext()).getScreenDrawableType()
                val appendable = "https://www.uptodd.com/images/app/android/details/cards/$dpi/"
                Log.d("div", "GenerateCardFragment L243 ${appendable + imgUrl.imageURL + ".webp"}")
                var cardBitmap: Bitmap?
                Glide.with(this).asBitmap().load(appendable + imgUrl.imageURL + ".webp")
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?,
                        ) {
                            cardBitmap = resource

                            //These 4 lines are written to to avoid same bitmap being created for same image url
                            val bmOverlay = Bitmap.createBitmap(
                                cardBitmap!!.width,
                                cardBitmap!!.height,
                                cardBitmap!!.config
                            )
                            val canvas = Canvas(bmOverlay)
                            canvas.drawBitmap(cardBitmap!!, Matrix(), null)
                            cardBitmap = bmOverlay
                            Log.d(
                                "div",
                                "GenerateCardFragment L255 $resource \t $cardBitmap \t $bmOverlay"
                            )

                            if (cardBitmap != null && rescaledBitmap != null) {
                                /* Uncomment this for previous version of cards*/
                                /*val canvas = Canvas(cardBitmap!!)
                                rescaledBitmap?.let {
                                    canvas.drawBitmap(it, (cardBitmap!!.width - rescaledBitmap!!.width) / 2f, imageTopPadding, null)
                                    addTextToBitmap(cardBitmap!!, viewModel.cardTypeList.value!![i].text.toString())
                                }*/
                                cardBitmap = Bitmap.createScaledBitmap(
                                    cardBitmap!!, requiredBitmapSize,
                                    lowerBitmapHeight, true
                                )
                                Log.d(
                                    "div",
                                    "GenerateCardFragment L313 ${cardBitmap!!.width} ${cardBitmap!!.height}"
                                )
                                val newBitmap = Bitmap.createBitmap(
                                    requiredBitmapSize,
                                    rescaledBitmap!!.height + lowerBitmapHeight,
                                    Bitmap.Config.ARGB_8888
                                )
                                Log.d(
                                    "div",
                                    "GenerateCardFragment L291 ${newBitmap.height} ${newBitmap.width}"
                                )
                                val canvas = Canvas(newBitmap)
                                canvas.drawColor(0x00000000)
                                canvas.drawBitmap(rescaledBitmap!!, 0f, 0f, null)
                                canvas.drawBitmap(
                                    cardBitmap!!,
                                    0f,
                                    rescaledBitmap!!.height.toFloat(),
                                    null
                                )
                                val card = FinalCard(
                                    System.currentTimeMillis(),
                                    newBitmap,
                                    viewModel.cardTypeList.value!![i].cardId
                                )
                                viewModel.finalCards.add(card)
                                binding.viewPager.adapter!!.notifyDataSetChanged()
                                Log.d(
                                    "div",
                                    "GenerateCardFragment L162 ${viewModel.finalCards.size}"
                                )
                            } else {
                                Toast.makeText(
                                    activity,
                                    getString(R.string.unable_to_load_image),
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            Log.d("div", "GenerateCardFragment L190 $placeholder")
                        }
                    })

            }
        }
    }

    private fun addTextToBitmap(cardBitmap: Bitmap, text1: String) {
        Log.d("div", "GenerateCardFragment L291 $cardBitmap")
        var text = text1.replace("<Baby Name>", viewModel.babyName)
        text = text.replace("<baby>", viewModel.babyName)
        text = text.replace("<Babe Name>", viewModel.babyName)
        text = text.replace("<Baby name>", viewModel.babyName)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Log.d("div", "GenerateCardFragment L294 $cardBitmap")
            val canvas = Canvas(cardBitmap)
            val textPaint = TextPaint()
            textPaint.textSize = textSize
            textPaint.color = Color.rgb(66, 66, 66)                          //textColor
            textPaint.style = Paint.Style.FILL
            val staticLayout = StaticLayout(
                text,
                textPaint,
                requiredBitmapSize,
                Layout.Alignment.ALIGN_CENTER,
                1.0f,
                0.0f,
                false
            )
            canvas.translate(
                ((canvas.width - staticLayout.width) / 2).toFloat(),
                (requiredBitmapSize + (cardBitmap.height - requiredBitmapSize - staticLayout.height) / 2).toFloat()
            )
            staticLayout.draw(canvas)
        } else {
            Log.d("div", "GenerateCardFragment L305 $cardBitmap")
            val lineText = DivideIntoLines(text)
            val canvas = Canvas(cardBitmap)
            val textPaint = Paint()
            textPaint.textSize = textSize
            textPaint.color = Color.WHITE                          //textColor
            textPaint.style = Paint.Style.FILL
            val bounds = Rect()
            //val height: Float = textPaint.measureText("yY")
            //val width: Float = textPaint.measureText(text)
            var y: Float = (requiredBitmapSize + 100).toFloat()
            for (line in lineText.split("\n")) {
                textPaint.getTextBounds(lineText, 0, line.length, bounds)
                val x: Float = ((cardBitmap.width - bounds.width()) / 2).toFloat()
                Log.d(
                    "div",
                    "GenerateCradFragment L221 ${cardBitmap.width} ${line.length} ${bounds.width()}"
                )
                y += lineDifference
                canvas.drawText(line, x, y, textPaint)
            }
        }
    }

    private fun DivideIntoLines(text: String): String {
        var textCopy = text
        var word: String = ""
        var finalString = ""
        textCopy = "$textCopy "
        var i = 0
        var lineLength = -1
        while (i < textCopy.length) {
            if (textCopy[i] != ' ')
                word += textCopy[i]
            else {
                if (lineLength == -1 || lineLength + word.length < ((rescaledBitmap!!.width - paddingLeftRightText * 2) / 40)) {
                    if (lineLength == -1) lineLength = 0
                    finalString += "$word "
                    lineLength += word.length
                } else {
                    finalString += "\n$word "
                    lineLength = 0
                }
                word = ""
            }
            i++
        }
        Log.d("div", "GenerateCardFragment L222 $finalString ${rescaledBitmap!!.width}")
        finalString.trim()
        return finalString
    }

    private fun onClickSave() {
        viewModel.isSavedToLocal.value = false
        Log.d("div", "GenerateCardFragment L292 ${adapter.getCardId()}")
        if (viewModel.finalCards.isEmpty()) {
            activity?.let {
                Toast.makeText(it, "Unknown Error", Toast.LENGTH_SHORT).show()
            }
            return
        }
        val finalCard = viewModel.getBitmapById(adapter.getCardId())
        viewModel.finalBitmap = finalCard.finalCard
        if (viewModel.finalBitmap != null) {
            if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
                viewModel.isSavingToDatabase.value = true
                viewModel.isCardSavedToDatabase = false
                viewModel.isCancelled = false
                showUploadingDialog()
                viewModel.isSavedToLocalCache = false
                val file = saveFileToLocalCache(viewModel.finalBitmap)
                if (file != null && file.exists() && viewModel.isSavedToLocalCache) {
                    viewModel.saveFinalCardToDatabase(finalCard, type!!, imagePath)
                    viewModel.isSavingToDatabase.observe(viewLifecycleOwner, Observer {
                        /*if (!it) {
                            if (viewModel.isCardSavedToDatabase) {

                                saveFileToLocal(finalBitmap)
                            } else {
                                Toast.makeText(activity,
                                    "Error saving to database",
                                    Toast.LENGTH_LONG)
                                    .show()
                            }
                        }*/
                        file.delete()
                    })
                } else
                    Toast.makeText(
                        activity,
                        getString(R.string.error_caching_image),
                        Toast.LENGTH_LONG
                    ).show()
            } else {
                //showInternetNotConnectedDialog()
                Snackbar.make(
                    binding.layout,
                    getString(R.string.no_internet_connection),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(getString(R.string.retry)) {
                        onClickSave()
                    }.show()
            }
        }
    }

    private fun saveFileToLocalCache(finalBitmap: Bitmap?): File? {
        try {
            /*var imageFile: File? = null
            val state: String = Environment.getExternalStorageState()
            var folder: File? = null
            folder = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/UpToddCards")
            var success = true
            if (!folder.exists()) {
                success = folder.mkdirs()
            }
            if (success) {
                imageFile = File(folder.absolutePath+File.separator+"CachedImage")             //overwrite to capturedImage in CaptureImageFragment
                imageFile.createNewFile()
            } else {
                Toast.makeText(activity, "Image Not saved", Toast.LENGTH_SHORT).show()
                return null
            }
            val ostream = ByteArrayOutputStream()

            finalBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, ostream)
            val fout = FileOutputStream(imageFile)
            fout.write(ostream.toByteArray())
            fout.close()
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            values.put(MediaStore.MediaColumns.DATA, imageFile.absolutePath)
            activity?.contentResolver?.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )*/
            val file = File(activity?.externalCacheDir, "card.png")
            val fOut = FileOutputStream(file)
            finalBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.flush()
            fOut.close()
            //file.setReadable(true, false)
            viewModel.isSavedToLocalCache = true
            return file

        } catch (e: Exception) {
            Log.d("div", "GenerateCardFragment L358 $e")
            return null
        }
    }

    private fun saveFileToLocal(finalBitmap: Bitmap): File? {
        var imageFile: File? = null

        try {
            //val state: String = Environment.getExternalStorageState()
            var folder: File?
            folder =
                File(Environment.getExternalStorageDirectory().toString() + "/UpToddCards")

            var success = true
            if (!folder.exists()) {
                success = folder.mkdirs()
                if (!success) {
                    folder = File(
                        requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                            .toString() + "/UpToddCards"
                    )
                    success = folder.mkdirs()
                }

            }
            if (success) {
                //imageFile = File(imagePath!!)             //overwrite to capturedImage in CaptureImageFragment
                val date = Date()
                imageFile = File(
                    folder.absolutePath + File.separator + Timestamp(date.time).time + "Card.jpg"
                )
                imageFile.createNewFile()
            } else {
                Toast.makeText(activity, getString(R.string.image_not_saved), Toast.LENGTH_SHORT)
                    .show()

            }
            val ostream = ByteArrayOutputStream()

            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream)
            val fout = FileOutputStream(imageFile)
            fout.write(ostream.toByteArray())
            fout.close()
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            values.put(MediaStore.MediaColumns.DATA, imageFile?.absolutePath)
            activity?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            //activity?.contentResolver?.openOutputStream(Uri.fromFile(imageFile))
            //Toast.makeText(activity, "Saved", Toast.LENGTH_LONG).show()

            viewModel.isSavedToLocal.value = true

        } catch (e: Exception) {
            Log.d("div", "GenerateCardFragment L408 $e")
        }
        return imageFile
    }


    private fun onClickShare() {
        var finalBitmap: Bitmap? = null
        try {
            if (viewModel.finalCards.isEmpty()) {
                activity?.let {
                    Toast.makeText(it, "UnKnown Error", Toast.LENGTH_SHORT).show()
                }
                return
            }
            finalBitmap = viewModel.getBitmapById(adapter.getCardId()).finalCard
        } catch (e: Exception) {

        }

        Log.d("div", "GenerateCardFragment L539 ")
        if (finalBitmap != null) {
            try {

                Log.d("div", "GenerateCardFragment L543 ")
                viewModel.isSavedToLocalCache = false
                val pathBitmap = MediaStore.Images.Media.insertImage(
                    requireContext().contentResolver,
                    finalBitmap,
                    "${System.currentTimeMillis()}",
                    null
                )
                Log.d("div", "GenerateCardFragment L546 ")
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "image/*"
                Log.d("div", "GenerateCardFragment L548 ")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                Log.d("div", "GenerateCardFragment L550 ")
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(pathBitmap))
                Log.d("div", "GenerateCardFragment L552 ")

                Log.d("div", "GenerateCardFragment L554 ")
                startActivity(Intent.createChooser(intent, "Share image via"))
            } catch (e: Exception) {
                Log.d("div", "GenerateCardFragment L577 $e")
                e.printStackTrace()
            }
        } else {
            Toast.makeText(activity, getString(R.string.wait_card_not_ready), Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun formatViewPager(viewPager: ViewPager) {
        viewPager.clipToPadding = false
        viewPager.pageMargin = 50
        viewPager.setPadding(100, 0, 100, 0)
        val nextItemVisiblePx = 50
        val currentItemHorizontalMarginPx = 50
        val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
        val pageTransformer = ViewPager.PageTransformer { page: View, position: Float ->
            page.translationX = -pageTranslationX * position
            // Next line scales the item's height. You can remove it if you don't want this effect
            page.scaleY = 1 - (0.25f * Math.abs(position))
            // If you want a fading effect uncomment the next line:
            page.alpha = 0.75f + (1 - Math.abs(position))
        }
        viewPager.setPageTransformer(true, pageTransformer)

        //val itemDecoration = HorizontalMarginItemDecoration(requireContext(),50)
        //viewPager.addItemDecoration(itemDecoration)

    }

    private fun setUpViewPager(list: List<FinalCard>) {
        adapter = GenerateCardAdapter(list, this.requireContext())
        binding.viewPager.adapter = adapter
        formatViewPager(binding.viewPager)
        onPageChangeListener()
        Log.d("div", "GenerateCardFragment L311 ViewPager set up")
    }

    private fun onPageChangeListener() {
        binding.viewPager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
                if (position < viewModel.finalCards.size)
                    setLayoutBackground()
            }

            override fun onPageSelected(position: Int) {
                adapter.setCardId(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun setLayoutBackground() {
        val drawable: Drawable = BitmapDrawable(resources, imageBitmap)
        drawable.alpha = 100
        binding.viewPager.background = drawable
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showInternetNotConnectedDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_upload,
            getString(R.string.no_internet_connection),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                }
            })
    }

    private fun showUploadingDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_upload,
            getString(R.string.downloading_please_wait),
            getString(R.string.cancel),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    viewModel.isCancelled = true
                    AndroidNetworking.cancel("save") // All the requests with the given tag will be cancelled.
                    AndroidNetworking.forceCancel("save")
                    dialog.dismiss()
                    //findNavController().navigateUp()
                }
            })
        viewModel.isSavingToDatabase.observe(viewLifecycleOwner, Observer {
            if (!it) {
                upToddDialogs.dismissDialog()
            }
        })
        val handler = Handler()
        handler.postDelayed({
            upToddDialogs.dismissDialog()
        }, R.string.loadingDuarationInMillis.toLong())

    }

    private fun showLoadingDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_loading,
            getString(R.string.loading_please_wait),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }
            })
        viewModel.isRepositoryEmpty.observe(viewLifecycleOwner, Observer {
            if (!it) {
                upToddDialogs.dismissDialog()
            }
        })
        val handler = Handler()
        handler.postDelayed({
            upToddDialogs.dismissDialog()
        }, R.string.loadingDuarationInMillis.toLong())

    }

    override fun onResume() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onResume()
    }

    override fun onPause() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        super.onPause()
    }
}