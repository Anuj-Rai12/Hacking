package com.example.hackingwork

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hackingwork.databinding.AdminActitvityMainBinding

class AdminActivity :AppCompatActivity(){
    private lateinit var binding: AdminActitvityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= AdminActitvityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}