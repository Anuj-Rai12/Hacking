package com.uptodd.uptoddapp.ui.login.selectlanguage

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentSelectLanguageBinding

class SelectLanguageFragment : Fragment() {

    private lateinit var binding: FragmentSelectLanguageBinding
    lateinit var viewModel: SelectLanguageViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_language, container, false)
        binding.selectLanguageViewModel= SelectLanguageViewModel()
        viewModel= ViewModelProvider(this).get(SelectLanguageViewModel::class.java)
        binding.lifecycleOwner = this

        binding.buttonEnglish.setOnClickListener{
            viewModel.languageSelected.value="english"
        }
        binding.buttonHindi.setOnClickListener{
            viewModel.languageSelected.value="hindi"
        }

        viewModel.languageSelected.observe(viewLifecycleOwner, Observer {
            viewModel.languageSelected.value?.let { it1 -> changeButtonBackground(it1) }
        })

        return binding.root
    }

    private fun changeButtonBackground(languageSelected:String)
    {
        if(languageSelected=="english")
        {
            binding.buttonEnglish.setBackgroundResource(R.drawable.select_language_selected)
            binding.buttonEnglish.setTextColor(resources.getColor(R.color.themeBlue))
            binding.buttonHindi.setBackgroundResource(R.drawable.select_language_unselected)
            binding.buttonHindi.setTextColor(Color.BLACK)
        }
        else
        {
            binding.buttonHindi.setBackgroundResource(R.drawable.select_language_selected)
            binding.buttonHindi.setTextColor(resources.getColor(R.color.themeBlue))
            binding.buttonEnglish.setBackgroundResource(R.drawable.select_language_unselected)
            binding.buttonEnglish.setTextColor(Color.BLACK)
        }
    }

}