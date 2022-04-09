package com.uptodd.uptoddapp.ui.monthlyDevelopment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.databinding.QuestionFormFragmentBinding
import com.uptodd.uptoddapp.ui.monthlyDevelopment.adapters.QuestionsFormAdapter
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.ToolbarUtils

class QuestionsFormFragment :Fragment() {

    var binding:QuestionFormFragmentBinding?=null

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
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adpater = QuestionsFormAdapter()
        adpater.list=AllUtil.getAllQuestions()
        binding?.questionFormRecyclerview?.adapter=adpater
    }



}