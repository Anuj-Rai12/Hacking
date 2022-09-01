package com.uptodd.uptoddapp.ui.home.homePage.adapter.models

import com.uptodd.uptoddapp.R

data class OptionsItem(var navId:Int,var icon:Int,var title:String){

    companion object{

        fun getPersonalizedList():ArrayList<OptionsItem>{
           val optionsItem= arrayListOf<OptionsItem>()

            optionsItem.apply {
                add(OptionsItem(R.id.action_homePageFragment_to_activityPodcastFragment,
                    R.drawable.ic_act_podcast, "Activity Podcast"))

                add(OptionsItem(R.id.action_homePageFragment_to_activitySampleFragment,
                    R.drawable.ic_sessions,"Session"))

                add(OptionsItem(R.id.action_homePageFragment_to_speedBoosterFragment,
                    R.drawable.ic_brain_booster,"Brain booster"))

                add(OptionsItem(R.id.action_homePageFragment_to_allTodosViewPagerFragment,
                    R.drawable.ic_routines,"Routines"))


                add(OptionsItem(R.id.action_homePageFragment_to_remediesFragment,
                    R.drawable.ic_gradma_tip,"Grandma Tips"))

                add(OptionsItem(R.id.action_homePageFragment_to_music,
                    R.drawable.ic_neural_music,"Neural music"))



            }

            return optionsItem
        }

        fun addKitTutorial(optionsItems:ArrayList<OptionsItem>):ArrayList<OptionsItem>{
            optionsItems.add(OptionsItem(R.id.action_homePageFragment_to_kitTutorialFragment,
                R.drawable.ic_kit_tutorial,"Kit tutorial"))
            return optionsItems
        }

        fun getPremiumList():ArrayList<OptionsItem>{
            val optionsItem= arrayListOf<OptionsItem>()

            optionsItem.apply {
                add(OptionsItem(R.id.action_homePageFragment_to_homeExpertCounselling,
                    R.drawable.ic_counselling, "Counselling"))

                add(OptionsItem(R.id.action_homePageFragment_to_referFragment,
                    R.drawable.ic_refer_n_earn,"Refer & earn"))

                add(OptionsItem(R.id.action_homePageFragment_to_tutorialFragment,
                    R.drawable.ic_app_tutorial,"App Tutorials"))

               add(OptionsItem(R.id.action_homePageFragment_to_webinarFragment,
                    R.drawable.ic_webinar,"Webinar"))

                add(OptionsItem(R.id.action_homePageFragment_to_recipeFragment,
                    R.drawable.recepie_icon,"Recipe"))

                add(OptionsItem(R.id.action_homePageFragment_to_developmentTrackerFragment,
                    R.drawable.development_form,"Dev. Tracker"))


            }

            return optionsItem
        }

        fun getParentToolList():ArrayList<OptionsItem>{
            val optionsItem= arrayListOf<OptionsItem>()

            optionsItem.apply {
                add(OptionsItem(R.id.action_homePageFragment_to_poemFragment,
                    R.drawable.ic_poem, "Poem"))

                add(OptionsItem(R.id.action_homePageFragment_to_storiesFragment,
                    R.drawable.ic_stories,"Stories"))

                add(OptionsItem(R.id.action_homePageFragment_to_dietFragment,
                    R.drawable.ic_diet,"Diet"))

                add(OptionsItem(R.id.action_homePageFragment_to_toysFragment,
                    R.drawable.ic_toy_suggestions,"Toys suggestions"))

                add(OptionsItem(R.id.action_homePageFragment_to_expectedOutcomesFragment3,
                    R.drawable.ic_milestone_tracker,"Milestone tracker"))

                add(OptionsItem(R.id.action_homePageFragment_to_editAlarmsViewPagerFragment,
                    R.drawable.ic_routine_alaram,"Routine Alarm"))
                add(OptionsItem(R.id.action_homePageFragment_to_allTicketsFragment,
                    R.drawable.ic_support,"Support"))
                add(OptionsItem(R.id.action_homePageFragment_to_blogsFragment,
                    R.drawable.ic_blog,"Blog"))
            }

            return optionsItem
        }

    }
}
