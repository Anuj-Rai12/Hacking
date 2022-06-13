package com.uptodd.uptoddapp.ui.nanny.login

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.logindetails.UserInfo
import com.uptodd.uptoddapp.databinding.FragmentNannyLoginBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.todoScreens.TodosListActivity
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import java.text.SimpleDateFormat

class NannyLoginFragment : Fragment() {

    private lateinit var binding: FragmentNannyLoginBinding
    private lateinit var viewModel: NannyLoginViewModel

    var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

    private lateinit var shakeAnimation: Animation

    private var passwordEye = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_nanny_login, container, false)
        binding.lifecycleOwner = this

        shakeAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)

        viewModel = ViewModelProvider(this).get(NannyLoginViewModel::class.java)
        binding.nannyViewModel = viewModel

        initObservers()

        preferences = requireActivity().getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        editor = preferences?.edit()

//        binding.editTextPassword.transformationMethod = MyPasswordTransformationMethod()

//        binding.mainForgotPassword.setOnClickListener {
//            view?.findNavController()
//                ?.navigate(R.id.action_nannyLoginFragment_to_forgetPasswordFragment2)
//        }
//        binding.mainLoginButton.setOnClickListener { onClickLogin() }
//        binding.imageButtonPasswordEye.setOnClickListener { onClickEye() }


        binding.nannyLoginId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                binding.editTextLoginId.isErrorEnabled = false
                binding.editTextLoginId.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.nannyPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                binding.editTextPassword.isErrorEnabled = false
                binding.editTextPassword.error = null
                binding.editTextPassword.isPasswordVisibilityToggleEnabled = true
            }

            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // click listener for back button
        binding.goBackNanny.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    private fun initObservers() {

        viewModel.incorrectLoginId.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.editTextLoginId.error = viewModel.loginIdMsg
                binding.editTextLoginId.startAnimation(shakeAnimation)
                viewModel.incorrectLoginId.value = false
            }
        })

        viewModel.incorrectPassword.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.editTextPassword.error = viewModel.passwordMsg
                binding.editTextPassword.isPasswordVisibilityToggleEnabled = false
                binding.editTextPassword.startAnimation(shakeAnimation)
                viewModel.incorrectPassword.value = false
            }
        })

        viewModel.loginResponse.observe(viewLifecycleOwner, Observer { userInfo ->
            userInfo?.let {
                UptoddSharedPreferences.getInstance(requireContext()).saveAppExpiryDate(viewModel.appAccessingDate)
                UptoddSharedPreferences.getInstance(requireContext()).saveLoginInfo(userInfo)
                if(viewModel.motherStage=="pre birth")
                {
                    viewModel.motherStage="prenatal"
                }
                else if(viewModel.motherStage=="post birth")
                {
                    viewModel.motherStage="postnatal"
                }


                UptoddSharedPreferences.getInstance(requireContext()).savePhone(viewModel.phoneNo)
                UptoddSharedPreferences.getInstance(requireContext())
                    .saveStage(viewModel.motherStage)
                UptoddSharedPreferences.getInstance(requireContext())
                    .saveSubStartDate(viewModel.subscriptionStartDate)
                UptoddSharedPreferences.getInstance(requireContext())
                    .saveSubEndDate(viewModel.subsriptionEndDate)

                val start = SimpleDateFormat("yyyy-MM-dd").parse(viewModel.subscriptionStartDate)
                val end = SimpleDateFormat("yyyy-MM-dd").parse(viewModel.subsriptionEndDate)

                val months= AllUtil.getDifferenceMonth(start.time,end.time)
                UptoddSharedPreferences.getInstance(requireContext()).saveCurrentSubPlan(months)

                val difference = AllUtil.getDifferenceDay(start.time, end.time)


                if (difference < 30) {
                    UptoddSharedPreferences.getInstance(requireContext()).saveUserType("nonPremium")

                    viewModel.getNPDetails(requireContext())
                    viewModel.iSNPNew.observe(viewLifecycleOwner, Observer {

                        if (it) {
                            view?.findNavController()?.navigate(NannyLoginFragmentDirections.actionNannyLoginFragmentToSelectParentFragment())
                        }
                        else
                        {
                            preferences?.edit()?.putBoolean(UserInfo::isNewUser.name,false)?.apply()
                            startActivity(
                                Intent(activity, TodosListActivity::class.java)
                            )
                            activity?.finish()
                        }
                        Log.d("subscription"," not ended")

                        /*
                         if(!AllUtil.isSubscriptionOver(end))
                         {
                             UpgradeViewModel.isFromLogin=true
                             preferences?.edit()?.putBoolean(UserInfo::isNewUser.name,false)?.apply()
                             view?.findNavController()?.navigate(R.id.action_loginFragment_to_upgradeFragment2)
                             Log.d("subscription","ended")
                         }
                         else
                         {
                         }
                         */
                    })
                } else {
                    UptoddSharedPreferences.getInstance(requireContext()).saveUserType("premium")

                    val country=if(viewModel.phoneNo?.startsWith("+91")!!)
                        "india"
                    else
                        "row"

                    AllUtil.registerToken("normal")
                    if(AllUtil.isSubscriptionOverActive(requireContext()))
                    {
                        AllUtil.logoutOnly(requireContext())
                        val upToddDialogs = UpToddDialogs(requireContext())
                        upToddDialogs.showInfoDialog("Your Premium Subscription is ended now","Close",
                            object :UpToddDialogs.UpToddDialogListener {
                                override fun onDialogButtonClicked(dialog: Dialog) {
                                    dialog.dismiss()
                                }
                            }
                        )

                    }
                    else if (userInfo.isNewUser) {
                        view?.findNavController()
                            ?.navigate(R.id.action_nannyLoginFragment_to_babyGenderFragment)
                    } else {
                        if((userInfo.kidsDob==null || userInfo.kidsDob=="null")&&viewModel.motherStage=="postnatal")
                        {

                            view?.findNavController()
                                ?.navigate(R.id.action_nannyLoginFragment_to_dobFragment)
                        }
                        else if((userInfo.address==null ||userInfo.address=="null") && country=="india")
                        {

                            view?.findNavController()
                                ?.navigate(R.id.action_nannyLoginFragment_to_addressFragment)
                        }
                        else {

                            startActivity(
                                Intent(activity, TodosListActivity::class.java)
                            )
                            activity?.finish()

                        }
                    }
                }
            }
        })

        viewModel.errorFromApiResponse.observe(viewLifecycleOwner, Observer {
            UpToddDialogs(requireContext()).showDialog(R.drawable.network_error,
                it,
                getString(R.string.close),
                object : UpToddDialogs.UpToddDialogListener {
                    override fun onDialogButtonClicked(dialog: Dialog) {
                        dialog.dismiss()
                    }
                })
        })


//        viewModel.isValidatingEmailPassword.observe(viewLifecycleOwner, Observer {
//            if (!it && !viewModel.isEmailPasswordCorrect) {
//                if (viewModel.errorFromApiResponse != "") {
//                    UpToddDialogs(requireContext()).showDialog(R.drawable.network_error,
//                        viewModel.errorFromApiResponse,
//                        getString(R.string.close),
//                        object : UpToddDialogs.UpToddDialogListener {
//                            override fun onDialogButtonClicked(dialog: Dialog) {
//                                dialog.dismiss()
//                            }
//                        })
//                } else {
//                    UpToddDialogs(requireContext()).showDialog(R.drawable.network_error,
//                        getString(R.string.something_went_wrong),
//                        getString(R.string.close),
//                        object : UpToddDialogs.UpToddDialogListener {
//                            override fun onDialogButtonClicked(dialog: Dialog) {
//                                dialog.dismiss()
//                            }
//                        })
//                }
//            }
//        })
    }

//    private fun onClickEye() {
//        passwordEye = !passwordEye
//        if (passwordEye)
//            binding.editTextPassword.transformationMethod = null
//        else
//            binding.editTextPassword.transformationMethod = MyPasswordTransformationMethod()
//    }

//    private fun onClickLogin() {
//        val shake: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
//        Log.d("div", "NannyLoginFragment L67")
//        if (binding.editTextLoginId.text.isEmpty())
//            binding.textViewPasswordError.text = getString(R.string.enter_email)
//        else if (binding.editTextPassword.text.isEmpty())
//            binding.textViewPasswordError.text = getString(R.string.enter_password)
//        else {
//            if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
//                Log.d("div", "NannyLoginFragment L77")
//                viewModel.errorFromApiResponse = ""
//                viewModel.isValidatingEmailPassword.value = true
//                viewModel.isEmailPasswordCorrect = false
//                showLoadingDialog()
//                viewModel.checkEmailAndPassword(
//                    binding.editTextLoginId.text.toString(),
//                    binding.editTextPassword.text.toString()
//                )
//                viewModel.isValidatingEmailPassword.observe(viewLifecycleOwner, Observer {
//                    if (!it) {
//                        if (viewModel.isEmailPasswordCorrect) {
//                            Log.d("div", "NannyLoginFragment L87")
//                            if (viewModel.isNewUser) {
//                                Log.d("div", "NannyLoginFragment L89")
//                                editor?.putBoolean("isNewUser", true)
//                                editor?.putString("userType", "Nanny")
//                                editor?.putString("uid", viewModel.uid)
//                                editor?.putString("loginMethod", viewModel.loginMethod)
//                                editor?.putString("email", viewModel.email)
//                                editor?.putString("kidsDob", viewModel.kidsDob)
//                                editor?.putString("babyName", viewModel.babyName)
//                                editor?.putString("profileImageUrl", viewModel.profileImageUrl)
//                                editor?.putLong("babyDOB", viewModel.babyDOB)
//                                editor?.putBoolean("isPaid", false)
//                                editor?.putLong("loginTime", System.currentTimeMillis())
//                                editor?.putString("token", viewModel.tokenHeader)
//                                editor?.commit()
//
//                                view?.findNavController()
//                                    ?.navigate(LoginFragmentDirections.actionLoginFragmentToSelectParentFragment())
//                            } else {
//                                Log.d("div", "NannyLoginFragment L104")
//                                editor?.putBoolean("loggedIn", true)
//                                editor?.putBoolean("isNewUser", false)
//                                editor?.putString("userType", "Nanny")
//                                editor?.putString("uid", viewModel.uid)
//                                editor?.putString("loginMethod", viewModel.loginMethod)
//                                editor?.putString("email", viewModel.email)
//                                editor?.putString("kidsDob", viewModel.kidsDob)
//                                editor?.putString("babyName", viewModel.babyName)
//                                editor?.putString("profileImageUrl", viewModel.profileImageUrl)
//                                editor?.putLong("babyDOB", viewModel.babyDOB)
//                                editor?.putBoolean("isPaid", false)
//                                editor?.putLong("loginTime", System.currentTimeMillis())
//                                editor?.putString("token", viewModel.tokenHeader)
//                                editor?.commit()
//                                Log.d(
//                                    "div",
//                                    "LoginFragment L402 ${preferences!!.getString("kidsDob", "")}"
//                                )
//                                //view?.findNavController()?.navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
//                                startActivity(Intent(activity, TodosListActivity::class.java))
//                                activity?.finish()
//                            }
//                            AllUtil.registerToken("nanny")
//                        } else if (viewModel.errorFromApiResponse != "") {
//                            binding.textViewPasswordError.text = viewModel.errorFromApiResponse
//                            binding.editTextLoginId.startAnimation(shake)
//                            binding.editTextPassword.startAnimation(shake)
//                            vibrate()
//
//                            //Dialog shown in initObservers()
//                        } else {
//                            binding.textViewPasswordError.text =
//                                getString(R.string.something_went_wrong)
//                            binding.editTextLoginId.startAnimation(shake)
//                            binding.editTextPassword.startAnimation(shake)
//                            vibrate()
//
//                            //Dialog shown in initObservers()
//                        }
//                    }
//                })
//            } else {
//                //showInternetNotConnectedDialog()
//                Snackbar.make(
//                    binding.layout,
//                    getString(R.string.no_internet_connection),
//                    Snackbar.LENGTH_LONG
//                ).show()
//            }
//        }
//    }

//    private fun vibrate() {
//        val v = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            v!!.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
//        } else {
//            //deprecated in API 26
//            v!!.vibrate(500)
//        }
//    }

//    override fun onStart() {
//        super.onStart()
//        Log.d("div", "LoginFragment L454 onStartCalled")
//        if (preferences != null && preferences!!.contains("isNewUser") && preferences!!.getBoolean(
//                "isNewUser",
//                true
//            )
//        )
//            view?.findNavController()?.navigate(R.id.action_loginFragment_to_selectParentFragment)
//    }
//
//    private fun showInternetNotConnectedDialog() {
//        val upToddDialogs = UpToddDialogs(requireContext())
//        upToddDialogs.showDialog(R.drawable.gif_loading,
//            getString(R.string.no_internet_connection),
//            getString(R.string.back),
//            object : UpToddDialogs.UpToddDialogListener {
//                override fun onDialogButtonClicked(dialog: Dialog) {
//                    dialog.dismiss()
//                }
//            })
//    }

    private fun showLoadingDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_loading,
            getString(R.string.loading_please_wait),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }
            })
        viewModel.isValidatingEmailPassword.observe(viewLifecycleOwner, Observer {
            if (!it) {
                upToddDialogs.dismissDialog()
            }
        })
        val handler = Handler()
        handler.postDelayed({
            upToddDialogs.dismissDialog()
        }, R.string.loadingDuarationInMillis.toLong())
    }


}