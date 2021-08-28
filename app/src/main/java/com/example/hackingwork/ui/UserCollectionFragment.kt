package com.example.hackingwork.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.hackingwork.R
import com.example.hackingwork.databinding.UsersCollectionLayoutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserCollectionFragment : Fragment(R.layout.users_collection_layout) {
    private lateinit var binding:UsersCollectionLayoutBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= UsersCollectionLayoutBinding.bind(view)
    }
}