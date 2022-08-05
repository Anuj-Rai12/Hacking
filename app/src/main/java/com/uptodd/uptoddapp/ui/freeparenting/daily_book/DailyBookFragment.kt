package com.uptodd.uptoddapp.ui.freeparenting.daily_book

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DailyBookLayoutBinding


class DailyBookFragment : Fragment(R.layout.daily_book_layout) {
    private lateinit var binding: DailyBookLayoutBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DailyBookLayoutBinding.bind(view)
    }


}