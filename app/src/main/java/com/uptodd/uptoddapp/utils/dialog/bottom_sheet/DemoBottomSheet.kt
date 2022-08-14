package com.uptodd.uptoddapp.utils.dialog.bottom_sheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.BottomSheetLayoutBinding
import com.uptodd.uptoddapp.ui.freeparenting.daily_book.adaptor.DailyContentAdaptor
import com.uptodd.uptoddapp.utils.OnBottomClick

class DemoBottomSheet(private val title: String) : BottomSheetDialogFragment(), OnBottomClick {

    private lateinit var binding: BottomSheetLayoutBinding
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null
    private lateinit var dailVideoAdaptor: DailyContentAdaptor

    var onListener: OnBottomClick? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(context, R.layout.bottom_sheet_layout, null)
        binding = BottomSheetLayoutBinding.bind(view)
        bottomSheet.setContentView(view)
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)

        //setting Peek at the 16:9 ratio key-line of its parent.
        bottomSheetBehavior?.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO

        binding.nameToolbar.text = title
        setAdaptor()
        binding.cancelBtn.setOnClickListener {
            dismiss()
        }

        setCallBack()

        return bottomSheet

    }

    private fun setAdaptor() {
        binding.viewVideoContentLayout.apply {
            setHasFixedSize(true)
            dailVideoAdaptor = DailyContentAdaptor()
            dailVideoAdaptor.itemClickListener = this@DemoBottomSheet
            adapter = dailVideoAdaptor
        }
        // Set New DATA
    // dailVideoAdaptor.submitList(DailyCheckData.list)
    }


    private fun setCallBack() {
        bottomSheetBehavior?.addBottomSheetCallback(
            object :
                BottomSheetBehavior.BottomSheetCallback() {

                @SuppressLint("SwitchIntDef")
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            //binding.appBarLayout.hide()
                            //binding.imageUp.show()
                            //setLogCat("","")
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            //binding.appBarLayout.show()
                            //binding.imageUp.hide()
                        }
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            dismiss()
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
    }


    override fun onStart() {
        super.onStart()
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        //binding.imageUp.show()
    }


//    companion object {
//        enum class OptionSelection {
//            RESUME,
//            NEXT
//        }
//    }

    override fun getTheme() = R.style.SheetDialog

    override fun <T> onClickListener(res: T) {
        onListener?.onClickListener(res)
        dismiss()
    }
}