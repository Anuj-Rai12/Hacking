package com.uptodd.uptoddapp.utils.dialog.bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.BottomSheetLayoutBinding
import com.uptodd.uptoddapp.utils.OnBottomClick

class DemoBottomSheet(private val title: String) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetLayoutBinding

    var onListener: OnBottomClick? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetLayoutBinding.inflate(layoutInflater)
        binding.bottomSheetTxt.text = title

        binding.resumeBtn.setOnClickListener {
            onListener?.onClickListener(OptionSelection.RESUME.name)
            dismiss()
        }
        binding.skipTxt.setOnClickListener {
            onListener?.onClickListener(OptionSelection.NEXT.name)
            dismiss()
        }
        return binding.root
    }

    companion object {
        enum class OptionSelection {
            RESUME,
            NEXT
        }
    }

    override fun getTheme() = R.style.SheetDialog
}