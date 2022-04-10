package com.uptodd.uptoddapp.ui.monthlyDevelopment.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.database.expertCounselling.ExpertCounselling
import com.uptodd.uptoddapp.databinding.QuestionRecyclerViewItemBinding
import com.uptodd.uptoddapp.ui.monthlyDevelopment.models.Question

class QuestionsAdapter(val clickListener: UpcomingSessionInterface?=null) :
    RecyclerView.Adapter<QuestionsAdapter.QuestionsViewHolder>() {

    var list = listOf<Question>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return QuestionsViewHolder(
            QuestionRecyclerViewItemBinding.inflate(
                inflater
            ))
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: QuestionsViewHolder, position: Int) {
        holder.bind(list[position])
    }


    inner class QuestionsViewHolder(val binding: QuestionRecyclerViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(question: Question) {
            binding.tvTitle.text="Q."+question.question
            binding.tvAns.text="Ans:"+question.answer
        }

    }
}

interface UpcomingSessionInterface {
    fun onClick(upcomingSession:ExpertCounselling)
}
