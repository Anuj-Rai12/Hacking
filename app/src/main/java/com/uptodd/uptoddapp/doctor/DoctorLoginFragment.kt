package com.uptodd.uptoddapp.doctor

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DoctorLoginFragmentBinding
import com.uptodd.uptoddapp.doctor.dashboard.DoctorDashboard
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.UpToddDialogs

class DoctorLoginFragment : Fragment() {

    companion object {
        fun newInstance() = DoctorLoginFragment()
    }

    private lateinit var binding: DoctorLoginFragmentBinding
    private lateinit var viewModel: DoctorLoginViewModel

    private lateinit var shakeAnimation: Animation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.doctor_login_fragment,
            container,
            false
        )
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(DoctorLoginViewModel::class.java)
        binding.doctorViewModel = viewModel

        shakeAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)

        binding.doctorMail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                binding.loginMailDoctor.isErrorEnabled = false
                binding.loginMailDoctor.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                binding.loginPasswordDoctor.isErrorEnabled = false
                binding.loginPasswordDoctor.error = null
                binding.loginPasswordDoctor.isPasswordVisibilityToggleEnabled = true
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        setClickListeners()
        setupObservers()



        return binding.root
    }

    private fun setupObservers() {
        viewModel.incorrectEmail.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.loginMailDoctor.isErrorEnabled = true
                binding.loginMailDoctor.error = viewModel.emailMsg
                binding.loginMailDoctor.startAnimation(shakeAnimation)
                viewModel.incorrectEmail.value = false
            }
        })

        viewModel.incorrectPassword.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.loginPasswordDoctor.isErrorEnabled = true
                binding.loginPasswordDoctor.error = viewModel.passwordMsg
                binding.loginPasswordDoctor.isPasswordVisibilityToggleEnabled = false
                binding.loginPasswordDoctor.startAnimation(shakeAnimation)
                viewModel.incorrectPassword.value = false
            }
        })

        viewModel.apiError.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrBlank()) return@Observer
            it.let {
                UpToddDialogs(requireContext()).showDialog(R.drawable.network_error,
                    "Error: $it",
                    "OK",
                    object : UpToddDialogs.UpToddDialogListener {
                        override fun onDialogButtonClicked(dialog: Dialog) {
                            dialog.dismiss()
                        }
                    })
                viewModel.apiError.value = ""
            }
        })

        viewModel.loginResponse.observe(viewLifecycleOwner, Observer {
            UptoddSharedPreferences.getInstance(requireContext()).saveDoctorLoginInfo(it)
            openDoctorDashboard()
        })
    }

    private fun setClickListeners() {

        binding.forgotPasswordDoctor.setOnClickListener {
            forgotPassword()
        }

        binding.loginAsPatient.setOnClickListener {
            requireActivity().finish()
        }

        // click listener for back button
        binding.goBackDoctor.setOnClickListener {
            requireActivity().finish()
        }
    }

    private fun forgotPassword() {
        //Redirect to forgot password page
        findNavController().navigate(
            DoctorLoginFragmentDirections.actionDoctorLoginFragmentToForgetPasswordFragment2(
                true
            )
        )
    }

//    private fun verifyAndLogin() {
//        val mailID = binding.loginMailDoctor.text.toString()
//        val password = binding.loginPasswordDoctor.text.toString()
//        when {
//            mailID.isEmpty() -> binding.loginMailDoctor.error = "Required"
//            password.isEmpty() -> binding.loginPasswordDoctor.error = "Required"
//            else -> {
//                val json = JSONObject()
//                json.put("mail", mailID)
//                json.put("password", password)
//                val uptoddDialogs = UpToddDialogs(requireContext())
//                uptoddDialogs.showLoginDialog()
//                AndroidNetworking.post("https://uptodd.com/api/doctor")
//                    .addJSONObjectBody(json)
//                    .setPriority(Priority.HIGH)
//                    .build()
//                    .getAsJSONObject(object : JSONObjectRequestListener {
//                        override fun onResponse(response: JSONObject?) {
//                            if (response != null) {
//                                uptoddDialogs.dismissDialog()
//                                Log.i("response", response.toString())
//                                if (response.getString("status") == "Success") {
//                                    saveLoginDetails(response)
//                                    openDoctorDashboard()
//                                } else {
//                                    uptoddDialogs.showDialog(R.drawable.network_error,
//                                        "Error: ${response.getString("message")}",
//                                        "OK",
//                                        object : UpToddDialogs.UpToddDialogListener {
//                                            override fun onDialogButtonClicked(dialog: Dialog) {
//                                                uptoddDialogs.dismissDialog()
//                                            }
//                                        })
//                                }
//                            }
//                        }
//
//                        override fun onError(anError: ANError?) {
//                            uptoddDialogs.dismissDialog()
//                            var apiError = ""
//                            if (anError!!.errorCode == 0)
//                                apiError = "Connection Timeout!"
//                            else
//                                apiError =
//                                    AllUtil.getJsonObject(anError.errorBody).getString("message")
//                                        .toString()
//                            uptoddDialogs.showDialog(R.drawable.network_error,
//                                "Error: $apiError",
//                                "OK",
//                                object : UpToddDialogs.UpToddDialogListener {
//                                    override fun onDialogButtonClicked(dialog: Dialog) {
//                                        uptoddDialogs.dismissDialog()
//                                    }
//                                })
//                            AllUtil.logApiError(anError)
//                        }
//                    })
//            }
//
//        }
//    }

//    private fun saveLoginDetails(response: JSONObject) {
//        val editor =
//            requireActivity().getSharedPreferences("LOGIN_INFO", AppCompatActivity.MODE_PRIVATE)
//                .edit()
//        editor.putBoolean("loggedIn", true)
//        editor.putString("userType", "Doctor")
//        editor.putString(
//            "uid",
//            (response.getJSONObject("data") as JSONObject).getJSONObject("doctor").getString("id")
//        )
//        editor.putString(
//            "email",
//            (response.getJSONObject("data") as JSONObject).getJSONObject("doctor").getString("mail")
//        )
//        editor.putString("token", response.getJSONObject("data").getString("token"))
//        editor.apply()
//
//        AllUtil.registerToken("doctor")
//
//    }


    private fun openDoctorDashboard() {
        val intent = Intent(requireContext(), DoctorDashboard::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        requireActivity().finish()
    }

}