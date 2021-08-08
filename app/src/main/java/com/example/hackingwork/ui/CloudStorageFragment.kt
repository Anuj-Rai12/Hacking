package com.example.hackingwork.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.hackingwork.R
import com.example.hackingwork.databinding.CloudStorageFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CloudStorageFragment : Fragment(R.layout.cloud_storage_fragment){
    private lateinit var  binding:CloudStorageFragmentBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= CloudStorageFragmentBinding.bind(view)
    }
}