package com.example.hackingwork.recycle.paginate

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.hackingwork.databinding.UserItemBinding
import com.example.hackingwork.utils.CreateUserAccount

class UserDetailAdaptor(private val userInfo: (CreateUserAccount) -> Unit) :
    PagingDataAdapter<CreateUserAccount, UserDetailAdaptor.UserPagingDetail>(diffUtil) {

    inner class UserPagingDetail(private val binding: UserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bindIt(createUserAccount: CreateUserAccount, userInfo: (CreateUserAccount) -> Unit) {
            binding.apply {
                usernameTxt.text = "${createUserAccount.firstname} ${createUserAccount.lastname}"
                userMobileTxt.text = "${createUserAccount.phone}"
                userEmailTxt.text = createUserAccount.email
                binding.root.setOnClickListener {
                    userInfo(createUserAccount)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: UserPagingDetail, position: Int) {
        val current = getItem(position)
        current?.let {
            holder.bindIt(it, userInfo)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPagingDetail {
        val binding = UserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserPagingDetail(binding)
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<CreateUserAccount>() {
            override fun areItemsTheSame(
                oldItem: CreateUserAccount,
                newItem: CreateUserAccount
            ): Boolean {
                return oldItem.phone == newItem.phone
            }

            override fun areContentsTheSame(
                oldItem: CreateUserAccount,
                newItem: CreateUserAccount
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}
