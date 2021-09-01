package com.example.hackingwork.recycle.paginate.header

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hackingwork.databinding.HeaderFooterLoadingBinding

class HeaderAndFooterAdaptor(private val error: (String) -> Unit, private val retry: () -> Unit) :
    LoadStateAdapter<HeaderAndFooterAdaptor.LoadingViewHolder>() {

    inner class LoadingViewHolder(
        private val binding: HeaderFooterLoadingBinding,
        private val retry: () -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(loadState: LoadState, error: (String) -> Unit) {
            binding.apply {
                progressBar.isVisible = loadState is LoadState.Loading
                retryBtn.isVisible = loadState !is LoadState.Loading
                errorTxt.isVisible = loadState !is LoadState.Loading
                if (errorTxt.isVisible) {
                    val mError = (loadState as LoadState.Error).error.localizedMessage
                    mError?.let {
                        if (it.equals("List is Empty", true)) {
                            errorTxt.isVisible = loadState !is LoadState.Loading
                        } else {
                            retryBtn.isVisible = false
                            errorTxt.isVisible = false
                        }
                    }
                    if (errorTxt.isVisible) {
                        error(mError!!)
                    1}
                }
            }
        }

        init {
            binding.retryBtn.setOnClickListener {
                retry()
            }
        }
    }

    override fun onBindViewHolder(holder: LoadingViewHolder, loadState: LoadState) {
        holder.bind(loadState, error)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadingViewHolder {
        val binding =
            HeaderFooterLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoadingViewHolder(binding, retry)
    }

}