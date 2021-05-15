package com.uptodd.uptoddapp.ui.todoScreens.todoDetailsScreen

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentTodoDetailsBinding
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.KidsPeriod
import com.uptodd.uptoddapp.utilities.ScreenDpi
import com.uptodd.uptoddapp.utilities.UpToddDialogs

// data has directly been bound in the layout
//
class TodoDetailsFragment : Fragment() {

    private lateinit var binding: FragmentTodoDetailsBinding
    private lateinit var viewModel: TodoDetailsViewModel

    private val isLoadingDialogVisible = MutableLiveData<Boolean>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        showLoadingDialog()
        isLoadingDialogVisible.value = true
        initialiseBindingAndViewModel(inflater, container)

        val arguments = TodoDetailsFragmentArgs.fromBundle(requireArguments())


        binding.btnEditTime.setOnClickListener {

            findNavController().navigate(
                TodoDetailsFragmentDirections.actionTodoDetailsFragmentToEditAlarmTimeFragment(
                    arguments.todoId
                )
            )
        }

        viewModel.imageUrl.observe(viewLifecycleOwner, Observer { imageUrl ->
            imageUrl?.let {
                val period = KidsPeriod(requireActivity()).getPeriod()
                val dpi = ScreenDpi(requireContext()).getScreenDrawableType()
                val appendable =
                    "https://uptodd.com/images/app/android/details/activities/$period/$dpi/"

                Glide.with(this)
                    .load("$appendable$imageUrl.webp")
                    .into(binding.todoImageView)
                    .onLoadStarted(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.loading_animation
                        )
                    )

                isLoadingDialogVisible.value = false
            }
        })


        return binding.root
    }

    private fun initialiseBindingAndViewModel(inflater: LayoutInflater, container: ViewGroup?) {

        val arguments = TodoDetailsFragmentArgs.fromBundle(requireArguments())

        viewModel = ViewModelProvider(
            this
        ).get(TodoDetailsViewModel::class.java)
        viewModel.fetchTodoDetailsFromGetApi(arguments.todoId)

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_todo_details, container, false)

        binding.todoDetailsViewModel = viewModel
        binding.lifecycleOwner = this


    }

    private fun showLoadingDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_loading,
            "Loading, please wait",
            "Back",
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }
            })
        isLoadingDialogVisible.observe(viewLifecycleOwner, Observer {
            if (!it) {
                upToddDialogs.dismissDialog()
            }
        })
        val handler = Handler()
        handler.postDelayed({
            upToddDialogs.dismissDialog()
        }, R.string.loadingDuarationInMillis.toLong())

    }

}
