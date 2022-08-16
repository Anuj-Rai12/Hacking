package com.uptodd.uptoddapp.ui.freeparenting.purchase.tabs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.PurchasePlanTabsFragmentsBinding
import com.uptodd.uptoddapp.ui.freeparenting.purchase.adaptor.CoursePurchaseDescAdaptor
import com.uptodd.uptoddapp.utils.getEmojiByUnicode

class PurchasePlanTabsFragment :
    Fragment(R.layout.purchase_plan_tabs_fragments) {
    private lateinit var binding: PurchasePlanTabsFragmentsBinding
    private lateinit var adaptor: CoursePurchaseDescAdaptor

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = PurchasePlanTabsFragmentsBinding.bind(view)
        setAdaptor()
        binding.courseDesc.text =
            "Lorem ipsum dolor sit amet consectetur adipisicing elit. Maxime mollitia,\n" +
                    "molestiae quas vel sint commodi repudiandae consequuntur voluptatum laborum\n" +
                    "numquam blanditiis harum quisquam eius sed odit fugiat iusto fuga praesentium\n" +
                    "optio, eaque rerum! Provident similique accusantium nemo autem. Veritatis\n" +
                    "obcaecati tenetur iure eius earum ut molestias architecto voluptate aliquam\n" +
                    "nihil, eveniet aliquid culpa officia aut! Impedit sit sunt quaerat, odit,\n" +
                    "tenetur error, harum nesciunt ipsum debitis quas aliquid. Reprehenderit,\n" +
                    "quia. Quo neque error repudiandae fuga? Ipsa laudantium molestias eos \n" +
                    "sapiente officiis modi at sunt excepturi expedita sint? Sed quibusdam\n" +
                    "recusandae alias error harum maxime adipisci amet laborum. "
        setData()

    }

    private fun setData() {
        val list = listOf(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            "Nullam fringilla est sit amet facilisis pretium.",
            "Aliquam egestas mauris ut dolor tincidunt cursus.",
            "Sed porttitor est id massa sodales rhoncus.",
            "Quisque vitae nunc tempus, egestas lorem nec, bibendum arcu.",
            "Mauris vel lacus facilisis, laoreet ligula vel, malesuada dolor.",
            "Vivamus hendrerit felis non sagittis pharetra.",
            "Donec posuere metus vitae urna auctor, a dignissim diam sodales.",
            "Vestibulum suscipit ante vitae libero hendrerit, a sagittis nunc ultrices.",
            "Vestibulum eget magna at diam faucibus posuere.",
            "Etiam interdum tortor pharetra, scelerisque magna in, fringilla leo.",
            "Ut consectetur leo id nisi mattis, id aliquam libero dignissim.",
            "Ut scelerisque nunc pellentesque, convallis enim porttitor, egestas nisl.",
            "Nunc in urna id nibh imperdiet posuere.",
            "Duis suscipit ex id ex convallis iaculis.",
            "Donec ut leo sit amet massa accumsan congue.",
            "Vivamus semper velit eget nisl rutrum scelerisque.",
            "Pellentesque euismod purus eget ultricies placerat."
        )
        adaptor.submitList(list)
    }

    private fun setAdaptor() {
        binding.recycleCourseDescription.apply {
            adaptor = CoursePurchaseDescAdaptor()
            this.adapter = adaptor
        }
    }

    fun get(index: Int) = "${getEmojiByUnicode(0x2705)} This is $index st feature\n\n"
}