package com.uptodd.uptoddapp.ui.expertCounselling

import com.uptodd.uptoddapp.ui.todoScreens.todoDetailsScreen.TodoDetailsFragmentArgs
import com.uptodd.uptoddapp.ui.todoScreens.todoDetailsScreen.TodoDetailsFragmentDirections
import com.uptodd.uptoddapp.ui.todoScreens.todoDetailsScreen.TodoDetailsViewModel


import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ExpertSuggestionFragmentBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.*
import kotlinx.android.synthetic.main.expert_suggestion_fragment.*
import java.util.zip.GZIPOutputStream

// data has directly been bound in the layout
//
class ExpertSuggestionsFragment : Fragment() {

    private lateinit var binding: ExpertSuggestionFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()
        initialiseBindingAndViewModel(inflater, container)

        return binding.root
    }

    private fun initialiseBindingAndViewModel(inflater: LayoutInflater, container: ViewGroup?) {

        val arguments = ExpertSuggestionsFragmentArgs.fromBundle(requireArguments())


        binding =
            DataBindingUtil.inflate(inflater, R.layout.expert_suggestion_fragment, container, false)
        binding.todoTaskName.text=arguments.expCounselling.name

        if(!arguments.expCounselling.tips.isNullOrEmpty()) {
            binding.description.text = arguments.expCounselling.tips
        }
        else
            binding.tipsLayout.visibility=View.GONE
        arguments.expCounselling.apply {

            if(!duration.isNullOrEmpty())
            binding.dateDuration.text = duration
            else
                binding.dateDuration.visibility=View.GONE;


            if(!startTime.isNullOrEmpty())
                binding.startTime.text = "$startTime - $endTime"
            else
                binding.startTimeLayout.visibility=View.GONE;



            if(!joiningLink.isNullOrEmpty()) {
                binding.joinLink.text = joiningLink
                Linkify.addLinks(binding.joinLink,Linkify.WEB_URLS);
                binding.joinButton.visibility=View.VISIBLE
                binding.joinButton.setOnClickListener {
                    val intent= Intent(Intent.ACTION_VIEW, Uri.parse(joiningLink))
                    startActivity(intent)
                }
            }
            else {
                binding.joinNowLayout.visibility = View.GONE
                binding.joinButton.visibility=View.GONE
            }


            if(!rescheduleLink.isNullOrEmpty()) {
                binding.rescheduleLink.text = rescheduleLink
                Linkify.addLinks(binding.rescheduleLink,Linkify.WEB_URLS);
                binding.rescheduleButton.setOnClickListener {
                    val intent= Intent(Intent.ACTION_VIEW, Uri.parse(rescheduleLink))
                    startActivity(intent)
                }
            }
            else {
                binding.rescheduleLinkLayout.visibility = View.GONE;
                binding.rescheduleButton.visibility=View.GONE
            }

            if(!note.isNullOrEmpty()){
                binding.note.text=note
            }
            else
                binding.noteLayout.visibility=View.GONE

            if(!status.isNullOrEmpty())
                binding.statusMeet.text =status
            else
                binding.statusLayout.visibility=View.GONE;

            if(!expectedDate.isNullOrEmpty() && status=="Missed")
                binding.expectedDate.text =expectedDate
            else
                binding.expectedDateLayout.visibility=View.GONE;

            if(!date.isNullOrEmpty()) {
                binding.date.text=date
            }
            else{
                binding.dateLayout.visibility=View.GONE
            }

            if(status=="Missed")
            {
                binding.statusIcon.setImageResource(R.drawable.ic_baseline_missed_video_call_24)
            }

        }

    }

}
