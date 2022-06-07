package com.uptodd.uptoddapp.ui.expertCounselling


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.LayoutFullScreenDialogBinding
import com.uptodd.uptoddapp.databinding.LayoutTermsConditionBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences

class TermsAndConditions(val info: String, val link: String? = null) : DialogFragment() {

    lateinit var binding: LayoutTermsConditionBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutTermsConditionBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textInfo.text = info

        if (link != null) {
            binding.texthead.text = "Notice"
            if ((!TextUtils.isEmpty(link) && link.startsWith("http"))) {
                binding.okButton.text = "Fill now"
            } else if (link == "navigateToSession") {
                binding.okButton.text = "Book session"
            } else if (link == "navigateToDevelopment") {
                binding.texthead.text = "Development form"
                binding.okButton.text = "Fill now"
            }

        }
        binding.okButton.setOnClickListener {
            dismiss()

            if (link != null) {

                if (!TextUtils.isEmpty(link) && link.startsWith("http")) {
                    val intent = Intent(
                        Intent.ACTION_VIEW, Uri.parse(
                            link
                        )
                    )
                    startActivity(intent)
                } else if (link == "navigateToSession") {
                    try {
                        findNavController().navigate(R.id.action_homePageFragment_to_homeExpertCounselling)
                    } catch (e: Exception) {
                        activity?.let {
                            Toast.makeText(it, "Unknown Error", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else if (link == "navigateToDevelopment") {
                    try {
                        findNavController().navigate(R.id.action_homePageFragment_to_developmentTrackerFragment)
                    } catch (e: Exception) {
                        activity?.let {
                            Toast.makeText(it, "Unknown Error", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        binding.cancelDialog.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        fun show(info: String, fragmentManager: FragmentManager) {
            val dialog = TermsAndConditions(info)
            dialog.show(fragmentManager, TermsAndConditions::class.java.name)
        }


    }

    fun handleClick() {
        val sharedPreferences = UptoddSharedPreferences.getInstance(requireContext())
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(sharedPreferences.getOnboardingLink())
        )
        startActivity(intent)
    }


}