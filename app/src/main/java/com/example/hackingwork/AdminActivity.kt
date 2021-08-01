package com.example.hackingwork

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.hackingwork.databinding.AdminActitvityMainBinding
import com.example.hackingwork.viewmodels.PrimaryViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminActivity : AppCompatActivity() {
    private lateinit var binding: AdminActitvityMainBinding
    private val primaryViewModel: PrimaryViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminActitvityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        primaryViewModel.read.observe(this) {
            Log.i(TAG, "onCreate: From Admin Activity->$it")
        }
        binding.tile.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
        }
    }

    override fun onBackPressed() {
        Log.i(TAG, "onBackPressed: Admin-Side ${MainActivity.emailAuthLink}")
        if (MainActivity.emailAuthLink==null)
        super.onBackPressed()
        else
            finish()
    }
}