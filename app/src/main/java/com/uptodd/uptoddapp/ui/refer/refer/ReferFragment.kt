package com.uptodd.uptoddapp.ui.refer.refer

import android.R.attr.label
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.content.ClipData
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentReferBinding
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import com.uptodd.uptoddapp.utilities.downloadmanager.JishnuDownloadManager
import pl.droidsonroids.gif.GifImageView
import java.util.regex.Pattern


class ReferFragment : Fragment() {


    private lateinit var binding: FragmentReferBinding
    private lateinit var viewModel: ReferViewModel

    private lateinit var uptoddDialogs: UpToddDialogs

    var preferences: SharedPreferences? = null

    var countryCodes= arrayListOf("+91","+1")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

    }
    private fun downloadGuidelinesPdf(url:String,name:String) {
        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
            requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.let {
                JishnuDownloadManager(
                    url,
                    name,
                    it, requireContext(),
                    requireActivity()
                )
            }
        } else {
            val snackbar = binding?.root?.let {
                Snackbar.make(
                    it,
                    getString(R.string.no_internet_connection),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(getString(R.string.retry)) {
                        downloadGuidelinesPdf(url,name)
                    }
            }
            snackbar?.show()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        uptoddDialogs = UpToddDialogs(requireContext())

        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_refer, container, false)
        binding.lifecycleOwner = this

        viewModel = ViewModelProvider(this).get(ReferViewModel::class.java)

        preferences = activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        if (preferences!!.contains("uid"))
            viewModel.uid = preferences!!.getString("uid", "")!!
        if (preferences!!.contains("token"))
            viewModel.token = preferences!!.getString("token", "")

        val  adapter= ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,countryCodes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.loginSpinner?.adapter=adapter

        viewModel.getReferProgramDetails(requireContext())

        (requireActivity() as AppCompatActivity?)?.supportActionBar?.title =
            getString(R.string.refer_and_earn)
        (requireActivity() as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        binding.buttonSubmit.setOnClickListener { onClickSubmit() }
        binding.tapToCopyLayoutDash?.setOnClickListener { onTapToCopy() }
        binding.textViewPreviousReferrals.setOnClickListener { onClickPreviousReferrals() }
        binding.textViewCode.text = "${viewModel.code.value}"
        binding.buttonShare.setOnClickListener { onClickShareCode() }
        binding.buttonSave.setOnClickListener { onClickCopyCode() }


        viewModel.code.observe(viewLifecycleOwner, Observer {
            binding.textViewCode.text=it
        })
        viewModel.privacyLink.observe(viewLifecycleOwner, Observer {

            val link=it

            binding.refferalPolicy?.setOnClickListener {
                downloadGuidelinesPdf(link,"ReferralPolicy")
            }
        })


        viewModel.isReferralSentSuccess.observe(viewLifecycleOwner, Observer {
            when (it) {
                0 -> {
                    uptoddDialogs.showUploadDialog()
                }
                1 -> {
                    uptoddDialogs.dismissDialog()
                    uptoddDialogs.showDialog(
                        R.drawable.gif_done,
                        getString(R.string.thank_you_for_your_referral),
                        getString(R.string.ok),
                        object : UpToddDialogs.UpToddDialogListener {
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                uptoddDialogs.dismissDialog()
                                findNavController().navigateUp()
                            }
                        })
                }
                -1 -> {
                    uptoddDialogs.dismissDialog()
                    uptoddDialogs.showDialog(
                        R.drawable.network_error,
                        getString(R.string.an_error_has_occurred) + viewModel.apiError,
                        getString(R.string.close),
                        object : UpToddDialogs.UpToddDialogListener {
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                uptoddDialogs.dismissDialog()
                                findNavController().navigateUp()
                            }
                        })
                }
            }
        })
        if(AllUtil.isRow(requireContext()))
        {

        }

        return binding.root
    }

    private fun onClickCopyCode() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as android.text.ClipboardManager
            clipboard.text = viewModel.code.value
        } else {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", viewModel.code.value)
            clipboard.setPrimaryClip(clip)
        }
        Toast.makeText(activity, getString(R.string.copied_to_clipboard), Toast.LENGTH_LONG).show()
    }

    private fun onClickShareCode() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.referral_code_for_uptodd))
        intent.putExtra(
            Intent.EXTRA_TEXT,
            "The secret of my baby’s success is UpTodd’s help and our effort for best future of the baby\n" +
                    "\n" +
                    "Book the free session from https://www.uptodd.com and give my referral code ${viewModel.code.value} to the counsellor for 5% extra OFF. I highly recommend this for your baby too."
        )
        /*Fire!*/
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    private fun onClickPreviousReferrals() {
        view?.findNavController()?.navigate(R.id.action_referFragment_to_referListFragment)
    }

    private fun resetFields() {
        binding.editTextToys.setText("")
        binding.editTextEmail.setText("")
        binding.editTextPhone.setText("")
    }

    private fun onClickSubmit() {
        if (binding.editTextToys.text.isEmpty())
            binding.textViewError.text = getString(R.string.enter_valid_name)
        else if (binding.editTextEmail.text.isEmpty() || !isEmailValid(binding.editTextEmail.text.toString()))
            binding.textViewError.text = getString(R.string.enter_valid_email)
        else if (binding.editTextPhone.text.length != 10 )
            binding.textViewError.text = getString(R.string.enter_valid_phone)
        else {
            var mNumber="${countryCodes[binding.loginSpinner?.selectedItemPosition!!]}${
                binding.editTextPhone.text.toString()}"
            if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
                viewModel.sendReferral(
                    binding.editTextToys.text.toString(), binding.editTextEmail.text.toString(),
                    mNumber, System.currentTimeMillis()
                )
            } else {
                Snackbar.make(
                    binding.layout,
                    getString(R.string.no_internet_connection),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(getString(R.string.retry)) {
                        onClickSubmit()
                    }.show()
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$"
        val pat: Pattern = Pattern.compile(emailRegex)
        return if (email == null) false else pat.matcher(email).matches()
    }

    @SuppressLint("SetTextI18n")
    private fun showSubmitDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.gif_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<TextView>(R.id.gif_dialog_text).text =
            getString(R.string.thank_you_for_your_referral_we_ll_contact_you_soon)
        dialog.findViewById<GifImageView>(R.id.gif_dialog_gif).setImageResource(R.drawable.gif_done)
        dialog.findViewById<Button>(R.id.gif_dialog_button).setOnClickListener {
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.show()

        val mPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.ting)
        mPlayer.start()
    }

    fun onTapToCopy() {
        val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val textView="My baby is doing wonderful with Uptodd under IITs, AIIMS etc team with complete personalisation, I am referring this code to you so you can visit https://uptodd.com and avail 50% discount to get this in budget price and moreover use interest free EMIs for lifelong foundation of the child\n" +
                "\n" +
                "${binding.textViewCode.text}"
        val clip = ClipData.newPlainText("Copied Code",textView)
        clipboard!!.setPrimaryClip(clip)
        Toast.makeText(requireContext(),"Refferal Code copied",Toast.LENGTH_LONG).show()
    }

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }*/

}