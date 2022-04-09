package com.uptodd.uptoddapp.ui.monthlyDevelopment.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.paypal.pyplcheckout.sca.runOnUiThread
import com.uptodd.uptoddapp.database.expertCounselling.ExpertCounselling
import com.uptodd.uptoddapp.databinding.QuestionFormQuestionItemBinding
import com.uptodd.uptoddapp.databinding.QuestionRecyclerViewItemBinding
import com.uptodd.uptoddapp.ui.monthlyDevelopment.models.Question

class QuestionItemAdapter(val clickListener: QuestionsItemInterface?=null) :
    RecyclerView.Adapter<QuestionItemAdapter.QuestionsViewHolder>() {

    var list = listOf<Question>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return QuestionsViewHolder(
            QuestionFormQuestionItemBinding.inflate(
                inflater
            ))
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: QuestionsViewHolder, position: Int) {
        holder.bind(list[position],position)
    }

    inner class QuestionsViewHolder(val binding: QuestionFormQuestionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(question: Question, position: Int) {
            binding.question.text=question.question
            if(question.options.isEmpty()){
                binding.comments.visibility= View.VISIBLE
                binding.option1.visibility=View.GONE
                binding.option2.visibility=View.GONE
                binding.option3.visibility=View.GONE
                binding.comments.addTextChangedListener {
                    question.answer=it.toString()
                }
            } else {
                binding.comments.visibility= View.GONE
                binding.option1.visibility=View.VISIBLE
                binding.option2.visibility=View.VISIBLE
                binding.option3.visibility=View.VISIBLE

                binding.apply {
                    option1.text=question.options[0]
                    option2.text=question.options[1]
                    option3.text=question.options[2]
                }

                when (question.answer) {
                    binding.option1.text.toString() -> {
                        runOnUiThread {
                            binding.option1.isChecked=true
                        }

                    }
                    binding.option2.text.toString() -> {
                        runOnUiThread {
                            binding.option2.isChecked=true
                        }

                    }
                    binding.option3.text.toString() -> {
                        runOnUiThread {
                            binding.option3.isChecked=true
                        }
                    }
                }
                binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->

                    val option= group.findViewById<AppCompatRadioButton>(checkedId)
                    if(option.isChecked) {
                        question.answer = option.text.toString()
                        notifyItemChanged(position)
                    }
                }

            }
        }

    }
}

interface QuestionsItemInterface {
    fun onClick(upcomingSession:ExpertCounselling)
}
