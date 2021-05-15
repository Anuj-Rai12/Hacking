package com.uptodd.uptoddapp.doctor.refer.referrals

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.NoteFragmentBinding
import com.uptodd.uptoddapp.utilities.UpToddDialogs

class NoteFragment : Fragment() {

    companion object {
        fun newInstance() = NoteFragment()
    }

    private lateinit var viewModel: NoteViewModel
    private lateinit var uptoddDialogs: UpToddDialogs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        uptoddDialogs = UpToddDialogs(requireContext())

        val binding: NoteFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.note_fragment, container, false)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
        binding.noteFragmentBinding = viewModel

        val args = NoteFragmentArgs.fromBundle(requireArguments())
        val referredPerson = args.referralPersonName
        val referredPersonId = args.referralPersonId
        val isDoctor = args.isDoctor

        viewModel.isLoading.observe(viewLifecycleOwner, {
            it.let {
                when(it){
                    0 -> {
                        uptoddDialogs.dismissDialog()
                        uptoddDialogs.showDialog(R.drawable.gif_done, "Your feedback has been submitted.", "Close", object: UpToddDialogs.UpToddDialogListener{
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                uptoddDialogs.dismissDialog()
                                findNavController().navigateUp()
                            }
                        })
                    }
                    1 -> {
                        uptoddDialogs.showUploadDialog()
                    }
                    -1 ->{
                        uptoddDialogs.showDialog(R.drawable.network_error, "An error has occured: ${viewModel.apiError}", "Close", object: UpToddDialogs.UpToddDialogListener{
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                uptoddDialogs.dismissDialog()
                            }
                        })
                    }
                }
            }
        })

        binding.noteName.text = referredPerson
        binding.noteSubmit.setOnClickListener {
            viewModel.submitNote(isDoctor, referredPersonId, binding.referralNote.text.toString())
            uptoddDialogs.showUploadDialog()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
    }

}