package com.uptodd.uptoddapp.utils

import android.app.Activity
import android.app.Dialog
import android.view.Window
import android.widget.ImageButton
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.AppRatingLayoutBinding


fun Activity.rateUsDialog(title: String, desc: String) {

    val binding = AppRatingLayoutBinding.inflate(layoutInflater)


    val ids = arrayListOf(
        binding.star1,
        binding.star2,
        binding.star3,
        binding.star4,
        binding.star5
    )

    val ratingTxt = arrayListOf("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!")
    val dialog = Dialog(
        this,
        android.R.style.Theme_Translucent_NoTitleBar_Fullscreen
    )
    onClickStar(0, binding.star1, binding, ids, ratingTxt)
    onClickStar(1, binding.star2, binding, ids, ratingTxt)
    onClickStar(2, binding.star3, binding, ids, ratingTxt)
    onClickStar(3, binding.star4, binding, ids, ratingTxt)
    onClickStar(4, binding.star5, binding, ids, ratingTxt)

    binding.dissBtn.setOnClickListener {
        dialog.dismiss()
    }

    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(false)
    dialog.setContentView(binding.root)


    dialog.show()

}

private fun onClickStar(
    index: Int,
    imageButton: ImageButton,
    binding: AppRatingLayoutBinding,
    ids: ArrayList<ImageButton>,
    rating: ArrayList<String>
) {
    imageButton.setOnClickListener {
        clearStart(index, ids)
        setStarItem(index, ids)
        binding.startDesc.text = rating[index]
    }
}

private fun clearStart(index: Int, ids: ArrayList<ImageButton>) {
    for (i in index until ids.size) {
        ids[i].setImageResource(R.drawable.ic_starts_svg_empty)
    }
}

private fun setStarItem(index: Int, ids: ArrayList<ImageButton>) {

    for (i in 0..index) {
        ids[i].setImageResource(R.drawable.ic_starts_svg_full)
    }


}