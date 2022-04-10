package com.uptodd.uptoddapp.ui.monthlyDevelopment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.databinding.QuestionFormFragmentBinding
import com.uptodd.uptoddapp.ui.monthlyDevelopment.adapters.QuestionsFormAdapter
import com.uptodd.uptoddapp.ui.monthlyDevelopment.models.Response
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.ToolbarUtils
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import kotlinx.android.synthetic.main.question_form_fragment.*

class QuestionsFormFragment :Fragment() {

    var binding:QuestionFormFragmentBinding?=null
    var viewModel:DevelopmentTrackerViewModel?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = QuestionFormFragmentBinding.inflate(inflater)

        binding?.toolbar?.let {
            ToolbarUtils.initNCToolbar(requireActivity(),"Monthly Form",
                it,findNavController())
        }
        viewModel = ViewModelProvider(requireActivity())[DevelopmentTrackerViewModel::class.java]


        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = QuestionsFormFragmentArgs.fromBundle(requireArguments())
        val adapter=QuestionsFormAdapter()
        adapter.list= args.questionForm?.response!!
        binding?.questionFormRecyclerview?.adapter=adapter

        val uptoddDialog = UpToddDialogs(requireContext())
        uptoddDialog.dismissDialog()

        binding?.submitForm?.setOnClickListener {
            if(adapter.checkValidationOfForm()){
                uptoddDialog.showLoadingDialog(findNavController(),false)
                viewModel?.submitForm(adapter.list as ArrayList<Response>)
            } else {
                Toast.makeText(requireContext(),"Please answer all the questions",
                    Toast.LENGTH_LONG).show()
            }

        }



        viewModel?.formSubmitted?.observe(viewLifecycleOwner, Observer {
            uptoddDialog.dismissDialog()
            if(it){
                Toast.makeText(requireContext(),"Form submitted successfully",
                    Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(),"Please try again",
                    Toast.LENGTH_LONG).show()
            }
        })




    }



}