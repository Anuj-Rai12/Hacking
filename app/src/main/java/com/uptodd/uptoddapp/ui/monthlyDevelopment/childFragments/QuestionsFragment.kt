package com.uptodd.uptoddapp.ui.monthlyDevelopment.childFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.databinding.FragmentQuestionsBinding
import com.uptodd.uptoddapp.ui.monthlyDevelopment.adapters.QuestionsAdapter
import com.uptodd.uptoddapp.ui.monthlyDevelopment.models.Question
import com.uptodd.uptoddapp.ui.monthlyDevelopment.models.Response
import java.util.*

private const val TAG = "ActivitySampleFragment"

class QuestionsFragment : Fragment() {


    private lateinit var binding: FragmentQuestionsBinding
    var response:Response?=null

    private val adapter = QuestionsAdapter()

    companion object {
    fun getInstance(response:Response):QuestionsFragment{
            val fragment=QuestionsFragment()
            val bundle = Bundle()
            bundle.putSerializable("questions",response)
        fragment.arguments=bundle

        return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQuestionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        response = arguments?.getSerializable("questions") as Response
        setupRecyclerView(response!!)
    }


    private fun setupRecyclerView(response: Response) {
        if(response.questions.isEmpty()){
            hideRecyclerView()
            showNoData()

            return
        }
        showRecyclerView()
        hideNodata()
        adapter.list = response.questions
        binding.questionsRecyclerview.adapter = adapter
        showRecyclerView()
    }

    private fun hideNodata() {
        binding.noDataContainer.isVisible = false
    }

    private fun showNoData() {
        binding.noDataContainer.isVisible = true
    }

    private fun showRecyclerView() {
        binding.questionsRecyclerview.isVisible = true
    }

    private fun hideRecyclerView() {
        binding.questionsRecyclerview.isVisible = false
    }


}