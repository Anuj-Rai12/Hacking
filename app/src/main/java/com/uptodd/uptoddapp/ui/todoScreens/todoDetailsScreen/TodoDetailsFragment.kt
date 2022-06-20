package com.uptodd.uptoddapp.ui.todoScreens.todoDetailsScreen

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.uptodd.uptoddapp.utilities.*

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

        ToolbarUtils.initNCToolbar(
            requireActivity(), "Details", binding.toolbar,
            findNavController()
        )

        binding.btnEditTime.setOnClickListener {

            if (AllUtil.isUserPremium(requireContext())) {
                findNavController().navigate(
                    TodoDetailsFragmentDirections.actionTodoDetailsFragmentToEditAlarmTimeFragment(
                        arguments.todoId
                    )
                )
            } else {
                val upToddDialogs = UpToddDialogs(requireContext())
                upToddDialogs.showInfoDialog("This feature is only for Premium Subscribers",
                    "Close",
                    object : UpToddDialogs.UpToddDialogListener {
                        override fun onDialogButtonClicked(dialog: Dialog) {
                            dialog.dismiss()
                        }

                    }
                )
            }
        }

        viewModel.imageUrl.observe(viewLifecycleOwner, Observer { imageUrl ->
            imageUrl?.let {
                val period = KidsPeriod(requireActivity()).getPeriod()
                val dpi = ScreenDpi(requireContext()).getScreenDrawableType()
                val appendable =
                    "https://www.uptodd.com/images/app/android/details/activities/$period/$dpi/"


                Log.d("thumbnail details", "$appendable$imageUrl.webp")
                Glide.with(this)
                    .load("$appendable$imageUrl.webp")
                    .error(R.drawable.default_set_android_thumbnail)
                    .placeholder(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.loading_animation
                        )
                    )
                    .into(binding.todoImageView)

                isLoadingDialogVisible.value = false
            }
        })

        viewModel?.title?.observe(viewLifecycleOwner, Observer {

            it.let {
                binding.todoTaskName.text = it
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
                    try {
                        dialog.dismiss()
                        findNavController().navigateUp()
                    } catch (e: Exception) {
                        activity?.let { act ->
                            Toast.makeText(
                                act,
                                "Please Try Again",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
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
