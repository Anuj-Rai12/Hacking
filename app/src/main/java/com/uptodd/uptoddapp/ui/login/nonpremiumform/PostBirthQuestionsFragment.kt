package com.uptodd.uptoddapp.ui.login.nonpremiumform

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.api.getUserId
import com.uptodd.uptoddapp.database.nonpremium.NonPremiumAccount
import com.uptodd.uptoddapp.databinding.FragmentPostBirthQuestionsBinding
import com.uptodd.uptoddapp.ui.todoScreens.TodosListActivity
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import kotlinx.android.synthetic.main.fragment_baby_gender.*

class PostBirthQuestionsFragment :Fragment()
{
    lateinit var binding:FragmentPostBirthQuestionsBinding
    var viewModel:BirthViewModel?=null
    var editor: SharedPreferences.Editor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentPostBirthQuestionsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        val preferences = activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        editor = preferences!!.edit()
        viewModel=ViewModelProvider(this)[BirthViewModel::class.java]
        binding.finish.setOnClickListener {

            if (binding.name.text.toString().isNotEmpty() && binding.babyname.text.toString()
                    .isNotEmpty() && binding.babydob.text.toString().isNotEmpty()&&
            binding.toys.text.toString().isNotEmpty() && binding.minutesTime.text.toString()
                .isNotEmpty()
                    && binding.majorObj.text.toString()
                .isNotEmpty() && binding.specialNeeds.text.toString().isNotEmpty()
            )
            {
                val nonPremiumAccount=NonPremiumAccount(getUserId(requireContext())!!,binding.name.text.toString()
                ,binding.babydob.text.toString(),binding.babyname.text.toString(),binding.toys.text.toString()
                ,binding.minutesTime.text.toString().toInt(),"post birth",binding.specialNeeds.text.toString(),
                    binding.majorObj.text.toString(),"","")
                showLoadingDialog()

            }
            else
                Toast.makeText(requireContext(),"Field cannot be empty",Toast.LENGTH_LONG).show()


        }
    }
    private fun showLoadingDialog() {

        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showSetUpsDialog(
            R.drawable.gif_upload,
            getString(R.string.loading_please_wait),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {

                }

                override fun onDialogDismiss() {
                    if (viewModel?.isDataLoadedToDatabase!!) {
                        editor?.putBoolean("loggedIn", true)
                        editor?.putBoolean("isNewUser", false)
                        editor?.commit()
                        //view?.findNavController()?.navigate(R.id.action_stageFragment_to_homeFragment)
                        startActivity(Intent(activity, TodosListActivity::class.java))
                        activity?.finish()
                    } else {
                        Toast.makeText(
                            activity,
                            getString(R.string.failed_to_load_to_database),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
        viewModel?.isLoadingDialogVisible?.observe(viewLifecycleOwner, Observer {
            if (!it) {
            }
        })
        val handler = Handler()
        handler.postDelayed({
            upToddDialogs.dismissDialog()
        }, R.string.loadingDuarationInMillis.toLong())

    }


}