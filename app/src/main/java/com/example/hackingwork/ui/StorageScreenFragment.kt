package com.example.hackingwork.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.hackingwork.R
import com.example.hackingwork.databinding.StorageScreenFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StorageScreenFragment:Fragment(R.layout.storage_screen_fragment) {
    private lateinit var binding: StorageScreenFragmentBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= StorageScreenFragmentBinding.bind(view)
    }
}