package com.uptodd.uptoddapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.cuneytayyildiz.onboarder.OnboarderActivity
import com.cuneytayyildiz.onboarder.OnboarderPage
import java.util.*


class OnboardingActivity : OnboarderActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pages: List<OnboarderPage> = Arrays.asList(
            OnboarderPage.Builder()
                .title("Welcome to UpTodd")
                .description(" ")
                .imageResourceId(R.drawable.app_icon)
                .backgroundColor(R.color.white)
                .titleColor(R.color.buttonBlue)
                .multilineDescriptionCentered(true)
                .build(),
            OnboarderPage.Builder()
                .title("Notice")
                .description(
                    "You need to wait for around 15 Minutes to let your music and " +
                            "boosters get downloaded in the background. Keep your APP open."
                )
                .imageResourceId(R.drawable.ic_baseline_info_24)
                .backgroundColor(R.color.white)
                .titleColor(R.color.buttonBlue)
                .descriptionColor(R.color.buttonBlue)
                .multilineDescriptionCentered(true)
                .build(),
            OnboarderPage.Builder()
                .title("Sections")
                .description(
                    " Sections Named - Routines for your habits, Session, Activity Podcast, & Boosters are customised sections." +
                            " New Research based contents designed for you, will keep getting added here by your expert\n"
                )
                .imageResourceId(R.drawable.ic_baseline_category_24)
                .backgroundColor(R.color.white)
                .titleColor(R.color.buttonBlue)
                .descriptionColor(R.color.buttonBlue)
                .multilineDescriptionCentered(true)
                .build(),
            OnboarderPage.Builder()
                .title("Music")
                .description("Music is really important section to enhance neural connections of the child")
                .imageResourceId(R.drawable.ic_baseline_library_music_24)
                .backgroundColor(R.color.white)
                .titleColor(R.color.buttonBlue)
                .descriptionColor(R.color.buttonBlue)
                .multilineDescriptionCentered(true)
                .build(),
            OnboarderPage.Builder()
                .title("Support")
                .description("Use Support section of the app to reach our team or your expert by using relevant Tab," +
                        " our team is there to help you - 24*7")
                .imageResourceId(R.drawable.ic_support_icon_blue)
                .backgroundColor(R.color.white)
                .titleColor(R.color.buttonBlue)
                .descriptionColor(R.color.buttonBlue)
                .multilineDescriptionCentered(true)
                .build()


        )
        initOnboardingPages(pages)
        setInactiveIndicatorColor(R.color.grey)
        setActiveIndicatorColor(R.color.buttonBlue)

        val btnFinish = findViewById<Button>(com.cuneytayyildiz.onboarder.R.id.button_finish)
        val btnSkip = findViewById<Button>(com.cuneytayyildiz.onboarder.R.id.button_skip)
        val btnNext = findViewById<ImageButton>(com.cuneytayyildiz.onboarder.R.id.button_next)
        btnNext.setColorFilter(resources.getColor(R.color.buttonBlue))
        btnFinish.setTextColor(resources.getColor(R.color.buttonBlue))
        btnSkip.setTextColor(resources.getColor(R.color.buttonBlue))
    }

    override fun onFinishButtonPressed() {
        // implement your logic, save induction has done to sharedPrefs
        finish()
    }
}

