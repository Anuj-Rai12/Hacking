package com.uptodd.uptoddapp.ui.freeparenting.purchase.tabs

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.PurchasePlanTabsFragmentsBinding
import com.uptodd.uptoddapp.ui.freeparenting.daily_book.repo.VideoContentRepository
import com.uptodd.uptoddapp.ui.freeparenting.purchase.adaptor.CoursePurchaseDescAdaptor
import com.uptodd.uptoddapp.ui.freeparenting.purchase.viewpager.ViewPagerAdapter
import com.uptodd.uptoddapp.utils.getEmojiByUnicode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PurchasePlanTabsFragment :
    Fragment(R.layout.purchase_plan_tabs_fragments) {
    private lateinit var binding: PurchasePlanTabsFragmentsBinding
    private lateinit var adaptor: CoursePurchaseDescAdaptor
    private lateinit var whyUptoddAdaptor: CoursePurchaseDescAdaptor
    private lateinit var viewpagerAdaptor: ViewPagerAdapter

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = PurchasePlanTabsFragmentsBinding.bind(view)
        setAdaptor()
        binding.courseDesc.text =
            "Powerful Personalised Parenting Program as per your " +
                    "child’s developmental needs, completely customised" +
                    " program as per your child’s requirements to ensure" +
                    " you Raise a Genius\n"
        binding.upToddDesc.text = "Why UpTodd?"

        binding.courseDuration.text =
            Html.fromHtml("Total Access : <font color='#ff0000'><b>60 Days<b></font>")
        binding.contactDetail.text =
            Html.fromHtml("Start Anytime, Mail : <font color='#2ba0c4'><b>support@uptodd.com<b></font>")
        setData()
        setViewPagerAdaptor()
        viewpagerAdaptor.setFragment(
            CourseVideoTabsFragment(
                "uSTCoECm3TA",
                VideoContentRepository.Companion.ItemType.VIDEO.name
            )
        )
        viewpagerAdaptor.setFragment(
            CourseVideoTabsFragment(
                "CV5BYQbeF-o",
                VideoContentRepository.Companion.ItemType.MUSIC.name
            )
        )
        viewpagerAdaptor.setFragment(
            CourseVideoTabsFragment(
                "KXwn6xjQyVk",
                VideoContentRepository.Companion.ItemType.MUSIC.name
            )
        )

        TabLayoutMediator(
            binding.tabs,
            binding.viewPagerForCourse
        ) { _, _ -> }.attach()

        movingOnScreenToAnother()
    }


    private fun movingOnScreenToAnother() {
        lifecycleScope.launch {
            var pos = 0
            while (true) {
                delay(3000)
                pos = (pos % 3)
                binding.viewPagerForCourse.currentItem = pos
                pos++
            }
        }
    }


    private fun setData() {
        val list = listOf(
            "28 days Program with Personalised 24*7 R&D team support at Fingertip | Monthly Developmental form based Tracking | 1-1 Master On boarding Video call",
            "2 Mega Sets of Memory & Sensory cards with App enabled guide, Parenting Coaches + R&D team + Community Access",
            "4 App powered Learning Books & 4 Story Builders to implant creativity & imagination power",
            "1 Mega Special personalised Wooden/Paper Montessori developmental Toy with 2 Months of Total App Access",
            "100+ Personalised Routine planners with 100s of Neural Musics & Natural Boosters",
            "Personalised App with 100s of Activities, Sessions, Boosters, Musics, Audio Stories etc. like 30+ Personalised App Features to boost 5X development"
        )

        val listOfTwo = listOf(
            "100% Transparency of FAQs, Team & Details clearly listed clearly on the website",
            "Research backed platform by top rated teams with 50+ years of experience",
            "Only program with 1-1 video session + complete personalisation",
            "Only program with KIT of TOY, Sensory & Educational Cards, Audio Story Books, Audio Story Builders etc."
        )

        whyUptoddAdaptor.submitList(listOfTwo)
        adaptor.submitList(list)
    }

    private fun setAdaptor() {
        binding.recycleCourseDescription.apply {
            adaptor = CoursePurchaseDescAdaptor()
            this.adapter = adaptor
        }
        binding.recycleUptoddDescription.apply {
            whyUptoddAdaptor = CoursePurchaseDescAdaptor()
            this.adapter = whyUptoddAdaptor
        }
    }

    private fun setViewPagerAdaptor() {
        viewpagerAdaptor = ViewPagerAdapter(childFragmentManager, lifecycle)
        binding.viewPagerForCourse.adapter = viewpagerAdaptor
    }

    fun get(index: Int) = "${getEmojiByUnicode(0x2705)} This is $index st feature\n\n"
}