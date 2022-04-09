package com.uptodd.uptoddapp.ui.monthlyDevelopment.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facebook.all.All
import com.uptodd.uptoddapp.database.expertCounselling.ExpertCounselling
import com.uptodd.uptoddapp.databinding.QuestionFormRecyclerviewItemBinding
import com.uptodd.uptoddapp.databinding.QuestionRecyclerViewItemBinding
import com.uptodd.uptoddapp.ui.monthlyDevelopment.models.AllResponse
import com.uptodd.uptoddapp.ui.monthlyDevelopment.models.Question
import com.uptodd.uptoddapp.ui.monthlyDevelopment.models.Response

class QuestionsFormAdapter(val clickListener: UpcomingSessionInterface?=null) :
    RecyclerView.Adapter<QuestionsFormAdapter.QuestionsViewHolder>() {

    var list = listOf<Response>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return QuestionsViewHolder(
            QuestionFormRecyclerviewItemBinding.inflate(
                inflater
            ))
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: QuestionsViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class QuestionsViewHolder(val binding: QuestionFormRecyclerviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(response: Response) {
            binding.type.text=response.type
            val adapter=QuestionItemAdapter()
            adapter.list=response.questions
            binding.questionRecyclerviewItem.adapter=adapter
        }

    }
}

interface QuestionsFormInterface {
    fun onClick(upcomingSession:ExpertCounselling)
}
