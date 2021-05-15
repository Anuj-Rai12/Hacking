package com.uptodd.uptoddapp.ui.capturemoments.selecttype

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
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
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.UptoddViewModelFactory
import com.uptodd.uptoddapp.databinding.FragmentSelectTypeBinding
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.ScreenDpi
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import kotlinx.android.synthetic.main.select_type_item.view.*
import java.io.File
import java.lang.Math.abs


class SelectTypeFragment : Fragment(), SelectTypeAdapter.OnClickListener {

    private lateinit var binding: FragmentSelectTypeBinding

    private var imagePath: String? = null
    private var photoType: String? = null

    private var backgroundBitmap: Bitmap? = null
    private lateinit var viewModel: SelectTypeViewModel

    private lateinit var adapter: SelectTypeAdapter

    var preferences: SharedPreferences? = null
    var token: String? = null

    var alreadyInflated = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        arguments?.let {
            imagePath = it.getString("imagePath")
            Log.d("div", "SelectTypeFragment L19 $imagePath")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_type, container, false)
        binding.lifecycleOwner = this

        alreadyInflated = false

        preferences = activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        if (preferences!!.contains("token"))
            token = preferences!!.getString("token", "")

        val viewModelFactory = UptoddViewModelFactory.getInstance(requireActivity().application)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(SelectTypeViewModel::class.java)

        (activity as AppCompatActivity?)?.supportActionBar?.title =
            getString(R.string.capture_moments)
        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        //viewModel.getAllPhotoTypes()

        Log.d(
            "div",
            "${ScreenDpi(requireContext()).getScreenDpiRatio()} ${ScreenDpi(requireContext()).getScreenDrawableType()}"
        )

        setLayoutBackground()
        adapter = SelectTypeAdapter(emptyList(), this.requireContext(), this)

        viewModel.photoTypeList.observe(viewLifecycleOwner, Observer {
            Log.d("div", "SelectTypeFragment L73 ${viewModel.photoTypeList.value}")
            if (viewModel.photoTypeList.value == null || viewModel.photoTypeList.value!!.isEmpty()) {
                viewModel.isRepositoryEmpty.value = true
                loadData()

            } else
                viewModel.isRepositoryEmpty.value = false
            if (viewModel.photoTypeList.value!!.isNotEmpty()) {
                Log.d("div", "SelectTypeFragment L69 ${viewModel.photoTypeList.value!![0]}")
                inflatePhotoTypes()
                /*adapter = SelectTypeAdapter(viewModel.photoTypeList.value!!, this.requireContext(), this)
                binding.viewPager.adapter = adapter*/
            }
        })


        /*binding.viewPager.adapter=adapter
        formatViewPager(binding.viewPager)
        binding.viewPager.setOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int ) {
                if(position<viewModel.photoTypeList.value!!.size)
                    setLayoutBackground()
            }
            override fun onPageSelected(position: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
        })
        binding.tabLayout.setupWithViewPager(binding.viewPager, true);*/

        //inflatePhotoTypes()

        return binding.root
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

    private fun setLayoutBackground() {
        val imgFile = File(imagePath)
        if (imgFile.exists()) {
            backgroundBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
            val drawable: Drawable = BitmapDrawable(resources, backgroundBitmap)
            drawable.alpha = 100
            binding.scrollView.background = drawable
            /*binding.viewPager.background=drawable
            binding.viewPager.background.alpha=100*/
        } else {
            Toast.makeText(
                activity,
                getString(R.string.image_not_found_in_storage),
                Toast.LENGTH_LONG
            ).show()
            requireActivity().finish()
        }
    }

    private fun inflatePhotoTypes() {
        if (!alreadyInflated) {
            var i: Int = 0
            while (i < viewModel.photoTypeList.value!!.size) {
                val imgUrl = viewModel.photoTypeList.value!![i].imageURL
                var childView: View
                val inflater =
                    activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                childView = inflater.inflate(R.layout.select_type_item, null)
                val imageView = childView.imageView
                imgUrl.let {
                    val dpi = ScreenDpi(requireContext()).getScreenDrawableType()
                    val appendable =
                        "https://uptodd.com/images/app/android/details/cards_category/$dpi/"
                    val url = appendable + imgUrl + ".webp"
                    Glide.with(imageView.context)
                        .load(url)
                        .apply(
                            RequestOptions()
                                .placeholder(R.drawable.loading_animation)
                                .error(R.drawable.ic_broken_image)
                        )
                        .into(imageView)
                }
                val text = viewModel.photoTypeList.value!![i].title
                childView.textView.text = text
                childView.textView.setOnClickListener { onClickType(text) }
                childView.imageView.setOnClickListener { onClickType(text) }
                binding.linearLayout.addView(childView)
                i++
            }
        }
        alreadyInflated = true
    }

    override fun onClickType(type: String?) {
        val bundle = Bundle()
        bundle.putString("photoType", type)
        bundle.putString("imagePath", imagePath)
        view?.findNavController()
            ?.navigate(R.id.action_selectTypeFragment_to_generateCardFragment, bundle)
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
            page.scaleY = 1 - (0.25f * abs(position))
            // If you want a fading effect uncomment the next line:
            page.alpha = 0.75f + (1 - abs(position))
        }
        viewPager.setPageTransformer(true, pageTransformer)

        //val itemDecoration = HorizontalMarginItemDecoration(requireContext(),50)
        //viewPager.addItemDecoration(itemDecoration)

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
        }, 10000)

    }

}