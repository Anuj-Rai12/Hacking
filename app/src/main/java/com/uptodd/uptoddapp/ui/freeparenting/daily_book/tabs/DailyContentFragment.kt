package com.uptodd.uptoddapp.ui.freeparenting.daily_book.tabs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DailyContentFragmentBinding

class DailyContentFragment : Fragment(R.layout.daily_content_fragment) {

    private lateinit var binding: DailyContentFragmentBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DailyContentFragmentBinding.bind(view)




    }

}