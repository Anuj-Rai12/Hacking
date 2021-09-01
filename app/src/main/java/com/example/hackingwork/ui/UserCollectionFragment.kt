package com.example.hackingwork.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hackingwork.R
import com.example.hackingwork.databinding.UsersCollectionLayoutBinding
import com.example.hackingwork.recycle.paginate.UserDetailAdaptor
import com.example.hackingwork.recycle.paginate.header.HeaderAndFooterAdaptor
import com.example.hackingwork.utils.CreateUserAccount
import com.example.hackingwork.utils.CustomProgress
import com.example.hackingwork.utils.DialogsForUser
import com.example.hackingwork.utils.GetConstStringObj
import com.example.hackingwork.viewmodels.AdminViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class UserCollectionFragment : Fragment(R.layout.users_collection_layout) {
    private lateinit var binding: UsersCollectionLayoutBinding
    private val viewModel: AdminViewModel by viewModels()
    private var dialogsForUser: DialogsForUser? = null

    @Inject
    lateinit var customProgress: CustomProgress
    private var userDetailAdaptor: UserDetailAdaptor? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = UsersCollectionLayoutBinding.bind(view)
        showLoading()
        setUpRecycleView()
        setUpData()
        binding.root.setOnRefreshListener {
            binding.root.isRefreshing = true
            userDetailAdaptor?.refresh()
        }
    }

    private fun setUpData() {
        lifecycleScope.launchWhenStarted {
            viewModel.userFlow.collectLatest {
                hideLoading()
                binding.root.isRefreshing = false
                userDetailAdaptor?.submitData(it)
            }
        }
    }

    private fun setUpRecycleView() {
        binding.userRecycleView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            userDetailAdaptor = UserDetailAdaptor {
                itemOnClicked(it)
            }
            adapter = userDetailAdaptor?.withLoadStateHeaderAndFooter(
                header = HeaderAndFooterAdaptor({ error ->
                    dir(message = error)
                }, {
                    userDetailAdaptor?.retry()
                }),
                footer = HeaderAndFooterAdaptor({ error ->
                    dir(message = error)
                }, {
                    userDetailAdaptor?.retry()
                }),
            )
        }
    }

    private fun itemOnClicked(it: CreateUserAccount) {
        it.also { user ->
            dialogsForUser =
                DialogsForUser(phone = user.phone, udi = user.id, token = "My Token") {
                    copyText(it)
                }
            dialogsForUser?.show(childFragmentManager, GetConstStringObj.TOKEN)
        }
    }

    private fun copyText(udi: String) {
        dialogsForUser?.dismiss()
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Source Udi", udi)
        clipboard.setPrimaryClip(clip)
        Snackbar.make(requireView(), "$udi is COPIED", Snackbar.LENGTH_SHORT).show()
    }

    private fun dir(title: String = "Error", message: String) {
        val action = UserCollectionFragmentDirections.actionGlobalPasswordDialog2(message, title)
        findNavController().navigate(action)
    }

    private fun showLoading() =
        customProgress.showLoading(requireActivity(), "Users All Loading..")

    private fun hideLoading() = customProgress.hideLoading()
    override fun onPause() {
        super.onPause()
        hideLoading()
        dialogsForUser?.dismiss()
    }
}