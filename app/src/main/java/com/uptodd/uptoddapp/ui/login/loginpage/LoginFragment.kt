/*package com.uptodd.uptoddapp.ui.login.loginpage

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.facebook.*
import com.facebook.FacebookSdk.getApplicationContext
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.uptodd.uptoddapp.LoginActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentLoginBinding
import java.util.*


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    lateinit var viewModel: LoginViewModel

    lateinit var googleSignInClient: GoogleSignInClient
    val RC_SIGN_IN:Int=1

    lateinit var callbackManager:CallbackManager

    var preferences: SharedPreferences? = null
    var editor: Editor? = null

    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=
            DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.loginViewModel= LoginViewModel()
        viewModel= ViewModelProvider(this).get(LoginViewModel::class.java)
        binding.lifecycleOwner = this

        preferences=activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        editor = preferences!!.edit()

        firebaseAuth=FirebaseAuth.getInstance()

        var gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.googleSignInButton.setOnClickListener {
            googleSignInClient.signOut()
            var intent: Intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }

        //val textView: TextView = binding.googleSignInButton.getChildAt(0) as TextView
        //textView.setText("")
        //binding.googleSignInButton.setStyle(2,0)

        val loginButton:LoginButton=binding.buttonFacebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        //AppEventsLogger.activateApp(activity);
        callbackManager= CallbackManager.Factory.create()
        loginButton.setReadPermissions("email", "public_profile")
        Log.d("div", "LoginFragment L72 Success ")
        loginButton.setOnClickListener{
            Log.d("div", "LoginFragment L148 onActivityResult")
            LoginManager.getInstance().logInWithReadPermissions(this@LoginFragment, Arrays.asList("public_profile", "email"));
        }
        LoginManager.getInstance().registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {
                    Log.d("div", "LoginFragment L75 Success $loginResult")
                    handleFacebookToken(loginResult!!.accessToken)
                }

                override fun onCancel() {
                    Log.d("div", "LoginFragment L75 Cancel")
                }

                override fun onError(exception: FacebookException?) {
                    Log.d("div", "LoginFragment L75 exception")
                }
            })

        return binding.root
    }

    //L90 to L Facebook signin


    fun handleFacebookToken(token: AccessToken)
    {
        Log.d("div", "LoginFragment L90 $token")
        val facebookCredential=FacebookAuthProvider.getCredential(token.token)
        firebaseAuth.signInWithCredential(facebookCredential).addOnCompleteListener(requireActivity(),object : OnCompleteListener<AuthResult> {
            override fun onComplete(task: Task<AuthResult>) {
                if(task.isSuccessful)
                {
                    val user:FirebaseUser?=firebaseAuth.currentUser
                    Toast.makeText(activity, "Log in completed $user",Toast.LENGTH_LONG).show()
                }
                else
                {
                    Toast.makeText(activity,"Log in failed",Toast.LENGTH_LONG).show()
                }
            }

        })

        viewModel.signInWithFacebook(facebookCredential)
        viewModel.loginMethod="facebook"
        viewModel.authenticatedUserLiveData?.observe(this, Observer {
            viewModel.isNewUser = it[0] == 'T'
            viewModel.uid = it.subSequence(1, it.indexOf('?')).toString()
            viewModel.email = it.subSequence(it.indexOf('?') + 1, it.length).toString()
            Log.d("div", "Login Fragment L94 ${viewModel.loginMethod} ${viewModel.isNewUser} ${viewModel.uid} ${viewModel.email}")
            editor?.putBoolean("isNewUser",viewModel.isNewUser)
            editor?.putString("uid",viewModel.uid)
            editor?.putString("loginMethod",viewModel.loginMethod)
            editor?.putString("email",viewModel.email)
            editor?.commit()


            if (viewModel.isNewUser)
                view?.findNavController()
                    ?.navigate(LoginFragmentDirections.actionLoginFragmentToSelectParentFragment())
            else
                view?.findNavController()
                    ?.navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
        })
    }

    //L63 to L100 = Google signin
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("div", "LoginFragment L127 onActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== RC_SIGN_IN)
        {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            googleSignInResult(task)
        }
        Log.d("div", "LoginFragment L148 onActivityResult")
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
    fun googleSignInResult(task: Task<GoogleSignInAccount>)
    {
        try{
            val account:GoogleSignInAccount = task.getResult(ApiException::class.java)!!
            viewModel.email= account.email.toString()
            viewModel.uid=account.id.toString()
            //If data found in database then goto FirebaseGoogleAuth function otherwise go to signUp page
            Toast.makeText(activity, viewModel.uid, Toast.LENGTH_LONG).show()
            val authCredential: AuthCredential =
                GoogleAuthProvider.getCredential(account.idToken, null)
            signInWithGoogleAuthCredential(authCredential)
        }
        catch (e: ApiException)
        {
            Toast.makeText(activity, "Login Failed", Toast.LENGTH_LONG).show()
        }
    }
    private fun signInWithGoogleAuthCredential(googleAuthCredential: AuthCredential) {
        viewModel.signInWithGoogle(googleAuthCredential)
        viewModel.loginMethod="google"
        viewModel.authenticatedUserLiveData?.observe(this, Observer {
            viewModel.isNewUser = it[0] == 'T'
            viewModel.uid = it.subSequence(1, it.indexOf('?')).toString()
            viewModel.email = it.subSequence(it.indexOf('?') + 1, it.length).toString()
            Log.d(
                "div",
                "Login Fragment L94 ${viewModel.loginMethod} ${viewModel.isNewUser} ${viewModel.uid} ${viewModel.email}"
            )
            if (viewModel.isNewUser) {
                editor?.putBoolean("isNewUser",viewModel.isNewUser)
                editor?.putString("uid",viewModel.uid)
                editor?.putString("loginMethod",viewModel.loginMethod)
                editor?.putString("email",viewModel.email)
                editor?.commit()
                view?.findNavController()
                    ?.navigate(LoginFragmentDirections.actionLoginFragmentToSelectParentFragment())
            }
            else
                view?.findNavController()
                    ?.navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
        })
    }

    override fun onStart() {
        super.onStart()

        val currentUser: FirebaseUser?=firebaseAuth.currentUser

        if(currentUser!=null)
        {
            Toast.makeText(activity,"Logged in",Toast.LENGTH_LONG).show()
        }
        else{
            Toast.makeText(activity,"Not logged in",Toast.LENGTH_LONG).show()
        }
    }
}*/

package com.uptodd.uptoddapp.ui.login.loginpage

import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.interceptors.HttpLoggingInterceptor
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.uptodd.uptoddapp.FreeParentingDemoActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.SplashScreenActivity
import com.uptodd.uptoddapp.UptoddWebsiteActivity
import com.uptodd.uptoddapp.database.logindetails.UserInfo
import com.uptodd.uptoddapp.databinding.FragmentLoginBinding
import com.uptodd.uptoddapp.doctor.DoctorLogin
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.login.facebooklogin.FacebookLoginActivity
import com.uptodd.uptoddapp.ui.todoScreens.TodosListActivity
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import com.uptodd.uptoddapp.utils.toastMsg
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    lateinit var viewModel: LoginViewModel

    lateinit var googleSignInClient: GoogleSignInClient
    val RC_SIGN_IN: Int = 1

    lateinit var callbackManager: CallbackManager

    var preferences: SharedPreferences? = null
    var editor: Editor? = null


    private var passwordEye = false

    lateinit var firebaseAuth: FirebaseAuth

    private lateinit var shakeAnimation: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        binding.loginViewModel = viewModel
        binding.lifecycleOwner = this

        shakeAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)

        preferences = activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        editor = preferences!!.edit()

        firebaseAuth = FirebaseAuth.getInstance()

        var gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.mainGoogleLogin.setOnClickListener {
            googleSignInClient.signOut()
            var intent: Intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }

        initObservers()


        //val textView: TextView = binding.googleSignInButton.getChildAt(0) as TextView
        //textView.setText("")
        //binding.googleSignInButton.setStyle(2,0)

        val loginButton: ImageButton = binding.mainFacebookLogin
        /*FacebookSdk.sdkInitialize(getApplicationContext());
        //AppEventsLogger.activateApp(activity);
        callbackManager= CallbackManager.Factory.create()
        loginButton.setReadPermissions("email", "public_profile")
        Log.d("div", "LoginFragment L72 Success ")*/
        loginButton.setOnClickListener {
            Log.d("div", "LoginFragment L148 button clicked")
            startActivity(Intent(activity, FacebookLoginActivity::class.java))
            //requireActivity().finish()
            //activity?.finish()
            //LoginManager.getInstance().logInWithReadPermissions(this@LoginFragment, Arrays.asList("public_profile", "email"));
        }

        binding.buttonDoctor.setOnClickListener {
            startActivity(Intent(activity, DoctorLogin::class.java))
        }

        viewModel.incorrectEmail.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.mainLoginPassword.isErrorEnabled = true
                binding.mainLoginMail.error = viewModel.incorrectEmailMsg
                binding.mainLoginMail.startAnimation(shakeAnimation)
                viewModel.incorrectEmail.value = false
            }
        })

        viewModel.incorrectPassword.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.mainLoginPassword.isErrorEnabled = true
                binding.mainLoginPassword.error = viewModel.incorrectPasswordMsg
                binding.mainLoginPassword.startAnimation(shakeAnimation)
                viewModel.incorrectPassword.value = false
            }
        })

        binding.buttonNanny.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_loginFragment_to_nannyLoginFragment)
        }


        binding.emailId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                binding.mainLoginMail.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                binding.mainLoginPassword.isPasswordVisibilityToggleEnabled = true
                binding.mainLoginPassword.error = null
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        /*LoginManager.getInstance().registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {
                    Log.d("div", "LoginFragment L75 Success $loginResult")
                    handleFacebookToken(loginResult!!.accessToken)
                }

                override fun onCancel() {
                    Log.d("div", "LoginFragment L75 Cancel")
                }

                override fun onError(exception: FacebookException?) {
                    Log.d("div", "LoginFragment L75 exception")
                }
            })*/

//        binding.mainLoginPassword.transformationMethod=MyPasswordTransformationMethod()

//        binding.mainLoginButton.setOnClickListener{loginWithEmailAndPassword()}
//        binding.imageButtonPasswordEye.setOnClickListener { onClickEye() }

        binding.mainForgotPassword.setOnClickListener {
            view?.findNavController()?.navigate(
                LoginFragmentDirections.actionLoginFragmentToForgetPasswordFragment2(false)
            )
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val isNew = activity?.intent?.getIntExtra(SplashScreenActivity.KEY_NEW, -1)




        when (isNew) {
            0 -> {
                view?.findNavController()
                    ?.navigate(LoginFragmentDirections.actionLoginFragmentToSelectParentFragment())
            }
            1 -> {
                view?.findNavController()
                    ?.navigate(R.id.action_loginFragment_to_dobFragment)
            }
            2 -> {
                view?.findNavController()
                    ?.navigate(R.id.action_loginFragment_to_babyGenderFragment)
            }
            3 -> {
                view?.findNavController()
                    ?.navigate(R.id.action_loginFragment_to_addressFragment)
            }

        }
    }

    private fun initObservers() {

        viewModel.errorFromApiResponse.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrBlank()) return@Observer
            UpToddDialogs(requireContext()).showDialog(R.drawable.network_error,
                it,
                getString(R.string.close),
                object : UpToddDialogs.UpToddDialogListener {
                    override fun onDialogButtonClicked(dialog: Dialog) {
                        viewModel.errorFromApiResponse.value = null
                        dialog.dismiss()
                    }
                })
        })

        viewModel.loginResponse.observe(viewLifecycleOwner, Observer { userInfo ->
            userInfo?.let {
                UptoddSharedPreferences.getInstance(requireContext())
                    .saveAppExpiryDate(viewModel.appAccessingDate)
                UptoddSharedPreferences.getInstance(requireContext()).saveLoginInfo(userInfo)
                if (viewModel.motherStage == "pre birth") {
                    viewModel.motherStage = "prenatal"
                } else if (viewModel.motherStage == "post birth") {
                    viewModel.motherStage = "postnatal"
                }


                setupHeader()
                UptoddSharedPreferences.getInstance(requireContext()).savePhone(viewModel.phoneNo)
                UptoddSharedPreferences.getInstance(requireContext())
                    .saveStage(viewModel.motherStage)
                UptoddSharedPreferences.getInstance(requireContext())
                    .saveSubStartDate(viewModel.subscriptionStartDate)
                UptoddSharedPreferences.getInstance(requireContext())
                    .saveSubEndDate(viewModel.subsriptionEndDate)

                val start =
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(viewModel.subscriptionStartDate)
                val end =
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(viewModel.subsriptionEndDate)

                val months = AllUtil.getDifferenceMonth(start.time, end.time)
                UptoddSharedPreferences.getInstance(requireContext()).saveCurrentSubPlan(months)

                val difference = AllUtil.getDifferenceDay(start.time, end.time)


                if (difference < 30) {
                    UptoddSharedPreferences.getInstance(requireContext()).saveUserType("nonPremium")

                    viewModel.getNPDetails(requireContext())
                    viewModel.iSNPNew.observe(viewLifecycleOwner, Observer {

                        if (it) {
                            view?.findNavController()
                                ?.navigate(LoginFragmentDirections.actionLoginFragmentToSelectParentFragment())
                        } else {
                            preferences?.edit()?.putBoolean(UserInfo::isNewUser.name, false)
                                ?.apply()
                            startActivity(
                                Intent(activity, TodosListActivity::class.java)
                            )
                            activity?.finish()
                        }
                        Log.d("subscription", " not ended")

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

                    val country = if (viewModel.phoneNo?.startsWith("+91")!!)
                        "india"
                    else
                        "row"

                    AllUtil.registerToken("normal")
                    if (AllUtil.isSubscriptionOverActive(requireContext())) {
                        AllUtil.logoutOnly(requireContext())
                        val upToddDialogs = UpToddDialogs(requireContext())
                        upToddDialogs.showInfoDialog("Your Premium Subscription is ended now",
                            "Close",
                            object : UpToddDialogs.UpToddDialogListener {
                                override fun onDialogButtonClicked(dialog: Dialog) {
                                    dialog.dismiss()
                                }
                            }
                        )

                    } else if (userInfo.isNewUser) {
                        if (viewModel.motherStage == "prenatal") {


                            if ((userInfo.address == null || userInfo.address == "null") && country == "india") {

                                view?.findNavController()
                                    ?.navigate(R.id.action_loginFragment_to_addressFragment)
                            } else {

                                startActivity(
                                    Intent(activity, TodosListActivity::class.java)
                                )
                            }
                        } else {
                            try {
                                val action =
                                    LoginFragmentDirections.actionLoginFragmentToBabyGenderFragment()
                                findNavController().navigate(action)
                            } catch (e: Exception) {

                                Log.i(
                                    "MOVE_LOGIN_TO_GENDER",
                                    "initObservers: ${e.localizedMessage}"
                                )
                            }
                        }
                    } else {
                        if ((userInfo.kidsDob == null || userInfo.kidsDob == "null") && viewModel.motherStage == "postnatal") {

                            view?.findNavController()
                                ?.navigate(R.id.action_loginFragment_to_dobFragment)
                        } else if ((userInfo.address == null || userInfo.address == "null") && country == "india") {

                            view?.findNavController()
                                ?.navigate(R.id.action_loginFragment_to_addressFragment)
                        } else {

                            startActivity(
                                Intent(activity, TodosListActivity::class.java)
                            )
                            activity?.finish()

                        }
                    }
                }
            }
        })
    }
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
//    }

//    private fun onClickEye() {
//        passwordEye=!passwordEye
//        if (passwordEye)
////            binding.mainLoginPassword.transformationMethod = null
//        else
////            binding.mainLoginPassword.transformationMethod = MyPasswordTransformationMethod()
//    }

//    private fun loginWithEmailAndPassword() {
//        val shake: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
//        if(binding.mainLoginMail.text.isEmpty())
//        {
//            binding.textViewPasswordError.text=getString(R.string.enter_email)
//            binding.mainLoginMail.startAnimation(shake)
//            vibrate()
//        }
//        else if(binding.mainLoginPassword.text.isEmpty())
//        {
//            binding.textViewPasswordError.text=getString(R.string.enter_password)
//            binding.mainLoginPassword.startAnimation(shake)
//            vibrate()
//        }
//        else {
//            if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
//                FirebaseInstanceId.getInstance().instanceId
//                    .addOnCompleteListener(OnCompleteListener { task ->
//                        if (!task.isSuccessful) {
//                            Toast.makeText(activity,
//                                getString(R.string.unable_to_fetch_device_token),
//                                Toast.LENGTH_LONG).show()
//                            return@OnCompleteListener
//                        }
//                        val token = task.result?.token
//                        viewModel.errorFromApiResponse=""
//                        viewModel.isValidatingEmailPassword.value = true
//                        viewModel.isEmailPasswordCorrect = false
//                        showLoadingDialog()
//                        viewModel.checkEmailAndPassword(binding.mainLoginMail.text.toString(),
//                            binding.mainLoginPassword.text.toString(), token)
//                        viewModel.isValidatingEmailPassword.observe(viewLifecycleOwner, Observer {
//                            if (!it) {
//                                if (viewModel.isEmailPasswordCorrect) {
//
//                                    if (viewModel.isNewUser) {
//                                        editor?.putBoolean("isNewUser", true)
//                                        editor?.putString("userType", "Normal")
//                                        editor?.putString("uid", viewModel.uid)
//                                        editor?.putString("loginMethod", viewModel.loginMethod)
//                                        editor?.putString("email", viewModel.email)
//                                        editor?.putLong("babyDOB", viewModel.babyDOB)
//                                        editor?.putBoolean("isPaid", true)
//                                        editor?.putLong("loginTime", System.currentTimeMillis())
//                                        editor?.putString("kidsDob", viewModel.kidsDob)
//                                        editor?.putString("babyName", viewModel.babyName)
//                                        editor?.putString("profileImageUrl", viewModel.profileImageUrl)
//                                        editor?.putString("token", viewModel.tokenHeader)
//                                        editor?.putString("parentType", viewModel.parentType)
//                                        editor?.putInt("loginDay",Calendar.DAY_OF_WEEK)
//                                        editor?.putInt("loginDate",SimpleDateFormat("dd").format(Calendar.getInstance().time).toInt())
//                                        editor?.commit()
//
//                                        view?.findNavController()
//                                            ?.navigate(LoginFragmentDirections.actionLoginFragmentToSelectParentFragment())
//                                    } else {
//                                        Log.i("debug",viewModel.uid)
//                                        editor?.putBoolean("loggedIn", true)
//                                        editor?.putBoolean("isNewUser", false)
//                                        editor?.putString("userType", "Normal")
//                                        editor?.putLong("babyDOB", viewModel.babyDOB)
//                                        editor?.putBoolean("isPaid", true)
//                                        editor?.putLong("loginTime", System.currentTimeMillis())
//                                        editor?.putString("uid", viewModel.uid)
//                                        editor?.putString("loginMethod", viewModel.loginMethod)
//                                        editor?.putString("email", viewModel.email)
//                                        editor?.putString("kidsDob", viewModel.kidsDob)
//                                        editor?.putString("babyName", viewModel.babyName)
//                                        editor?.putString("profileImageUrl", viewModel.profileImageUrl)
//                                        editor?.putString("token", viewModel.tokenHeader)
//                                        editor?.putString("parentType", viewModel.parentType)
//                                        editor?.putInt("loginDay",Calendar.DAY_OF_WEEK)
//                                        editor?.putInt("loginDate",SimpleDateFormat("dd").format(Calendar.getInstance().time).toInt())
//                                        editor?.commit()
//                                        Log.d("div",
//                                            "LoginFragment L402 ${
//                                                preferences!!.getInt("loginDate",
//                                                    0)
//                                            }")
//                                        //view?.findNavController()?.navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
//                                        startActivity(Intent(activity,
//                                            TodosListActivity::class.java))
//                                        activity?.finish()
//                                    }
//                                    AllUtil.registerToken("normal")
//                                }
//                                else if(viewModel.errorFromApiResponse!="")
//                                {
//                                    binding.textViewPasswordError.text = viewModel.errorFromApiResponse
//                                    binding.mainLoginMail.startAnimation(shake)
//                                    binding.mainLoginPassword.startAnimation(shake)
//                                    vibrate()
//
//                                    //Dialog shown in initObservers()
//                                }
//                                else {
//                                    binding.textViewPasswordError.text = getString(R.string.something_went_wrong)
//                                    binding.mainLoginMail.startAnimation(shake)
//                                    binding.mainLoginPassword.startAnimation(shake)
//                                    vibrate()
//
//                                    //Dialog shown in initObservers()
//                                }
//                            }
//                        })
//                    })
//            } else {
//                //showInternetNotConnectedDialog()
//                Snackbar.make(binding.layout, getString(R.string.no_internet_connection), Snackbar.LENGTH_LONG).show()
//            }
//        }
//    }

    //L90 to L Facebook signin


    /*fun handleFacebookToken(token: AccessToken)
    {
        Log.d("div", "LoginFragment L90 $token")
        val facebookCredential=FacebookAuthProvider.getCredential(token.token)
        firebaseAuth.signInWithCredential(facebookCredential).addOnCompleteListener(requireActivity(),object : OnCompleteListener<AuthResult> {
            override fun onComplete(task: Task<AuthResult>) {
                if(task.isSuccessful)
                {
                    val user:FirebaseUser?=firebaseAuth.currentUser
                    Toast.makeText(activity, "Log in completed $user",Toast.LENGTH_LONG).show()
                }
                else
                {
                    Toast.makeText(activity,"Log in failed",Toast.LENGTH_LONG).show()
                }
            }

        })

        viewModel.signInWithFacebook(facebookCredential)
        viewModel.loginMethod="facebook"
        viewModel.authenticatedUserLiveData?.observe(this, Observer {
            viewModel.isNewUser = it[0] == 'T'
            viewModel.uid = it.subSequence(1, it.indexOf('?')).toString()
            viewModel.email = it.subSequence(it.indexOf('?') + 1, it.length).toString()
            Log.d("div", "Login Fragment L94 ${viewModel.loginMethod} ${viewModel.isNewUser} ${viewModel.uid} ${viewModel.email}")
            editor?.putBoolean("isNewUser",viewModel.isNewUser)
            editor?.putString("uid",viewModel.uid)
            editor?.putString("loginMethod",viewModel.loginMethod)
            editor?.putString("email",viewModel.email)
            editor?.commit()


            if (viewModel.isNewUser)
                view?.findNavController()
                    ?.navigate(LoginFragmentDirections.actionLoginFragmentToSelectParentFragment())
            else
                view?.findNavController()
                    ?.navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
        })
    }*/

    //L63 to L100 = Google signin
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        Log.d("div", "LoginFragment L127 onActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            googleSignInResult(task)
        }
        Log.d("div", "LoginFragment L148 onActivityResult")
        //callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    fun googleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)!!
            viewModel.email = account.email.toString()
            viewModel.uid = account.id.toString()
            //If data found in database then goto FirebaseGoogleAuth function otherwise go to signUp page
//            Toast.makeText(activity, viewModel.uid, Toast.LENGTH_LONG).show()
            val authCredential: AuthCredential =
                GoogleAuthProvider.getCredential(account.idToken, null)
            signInWithGoogleAuthCredential(authCredential)
        } catch (e: ApiException) {
            Toast.makeText(activity, getString(R.string.log_in_failed), Toast.LENGTH_LONG)
                .show()
            Log.e(
                "googleLogin",
                "Error: ${e.stackTrace} / ${e.cause} / ${e.status} / ${e.localizedMessage}"
            )
        }
    }

    private fun signInWithGoogleAuthCredential(googleAuthCredential: AuthCredential) {
        viewModel.signInWithGoogle(googleAuthCredential)
        viewModel.loginMethod = "google"
        viewModel.authenticatedUserLiveData?.observe(this, Observer {
            viewModel.isNewUser = it.isNewUser
            viewModel.uid = it.uid.toString()
            viewModel.email = it.email.toString()
            Log.d(
                "div",
                "Login Fragment L94 ${viewModel.loginMethod} ${viewModel.isNewUser} ${viewModel.uid} ${viewModel.email}"
            )

            if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener(
                    OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.w(
                                "FCM",
                                "Fetching FCM registration token failed",
                                task.exception
                            )
                            return@OnCompleteListener
                        }

                        // Get new FCM registration token
                        val token = task.result
                        Log.i("FCM", "Token: $token")
                        Log.d("div", "LoginFragment L559 $token")
                        viewModel.isUploadingExplorerData.value = true
                        viewModel.isExplorerDataUploaded = false
                        showUploadingDialog()
                        viewModel.uploadExplorerData(it, token)
                        viewModel.isUploadingExplorerData.observe(
                            viewLifecycleOwner,
                            Observer {
                                Log.d(
                                    "div",
                                    "LoginFragment L514 ${viewModel.isUploadingExplorerData.value}"
                                )
                                if (!it) {
                                    if (viewModel.isExplorerDataUploaded) {

                                        editor?.putString("userType", "Google")
                                        editor?.putString(
                                            "loginMethod",
                                            viewModel.loginMethod
                                        )
                                        editor?.putBoolean("loggedIn", true)
                                        editor?.commit()
                                        //AllUtil.registerToken("google")

                                        //Pre sales notification
                                        val manager: NotificationManager =
                                            requireActivity().application.getSystemService(
                                                Context.NOTIFICATION_SERVICE
                                            ) as NotificationManager
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            val channel = NotificationChannel(
                                                "div",
                                                "PreSales Immediate",
                                                NotificationManager.IMPORTANCE_DEFAULT
                                            )
                                            manager.createNotificationChannel(channel)
                                        }
                                        val builder = NotificationCompat.Builder(
                                            requireContext(),
                                            "div"
                                        )
                                            .setContentTitle("Subscribe Now")
                                            .setContentText("Mom/Dad, my brain is developing fast, I need the right environment. Thank you for joining UpTodd for me.")
                                            .setSmallIcon(R.drawable.app_icon_image)

                                        manager.notify(1, builder.build())

//                                    UptoddNotifications.setPreSalesNotification(requireContext(),
//                                        System.currentTimeMillis()+100000,
//                                        "Subscribe Now",
//                                        "Mom/Dad, my brain is developing fast, I need the right environment. Thank you for joining UpTodd for me.")

                                        startActivity(
                                            Intent(
                                                activity,
                                                UptoddWebsiteActivity::class.java
                                            )
                                        )
                                        requireActivity().finish()
                                    } else
                                        Toast.makeText(
                                            activity,
                                            "Unable to upload data",
                                            Toast.LENGTH_LONG
                                        ).show()
                                }
                            })
                    })

            } else {
                //showInternetNotConnectedDialog()
                Snackbar.make(
                    binding.layout,
                    getString(R.string.no_internet_connection),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(getString(R.string.retry)) {
                        signInWithGoogleAuthCredential(googleAuthCredential)
                    }.show()
            }
            /*if (viewModel.isNewUser) {
                editor?.putBoolean("isNewUser",viewModel.isNewUser)
                editor?.putString("uid",viewModel.uid)
                editor?.putString("loginMethod",viewModel.loginMethod)
                editor?.putString("email",viewModel.email)
                editor?.commit()
                view?.findNavController()?.navigate(LoginFragmentDirections.actionLoginFragmentToSelectParentFragment())
            }
            else {
                editor?.putBoolean("loggedIn",true)
                editor?.commit()
                view?.findNavController()?.navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
                activity?.finish()
            }*/
        })
    }

    private fun vibrate() {
        val v = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v!!.vibrate(
                VibrationEffect.createOneShot(
                    500,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            //deprecated in API 26
            v!!.vibrate(500)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("div", "LoginFragment L454 onStartCalled")
        if (preferences != null && preferences!!.contains("isNewUser") && preferences!!.getBoolean(
                "isNewUser",
                true
            )
        )
            try {
                findNavController().navigate(R.id.action_loginFragment_to_selectParentFragment)
            } catch (e: Exception) {
                activity?.let {
                    Toast.makeText(
                        it,
                        "Oops something Went Wrong Please Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun showInternetNotConnectedDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_loading,
            getString(R.string.no_internet_connection),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                }
            })
    }

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

    private fun showUploadingDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_upload,
            getString(R.string.loading_please_wait),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }
            })
        viewModel.isUploadingExplorerData.observe(viewLifecycleOwner, Observer {
            if (!it) {
                upToddDialogs.dismissDialog()
            }
        })
        val handler = Handler()
        handler.postDelayed({
            upToddDialogs.dismissDialog()
        }, R.string.loadingDuarationInMillis.toLong())

    }

    private fun setupHeader() {
        val b = OkHttpClient.Builder()
        b.addNetworkInterceptor(HttpLoggingInterceptor())
        b.readTimeout(120, TimeUnit.SECONDS)
        b.writeTimeout(120, TimeUnit.SECONDS)
        b.connectTimeout(120, TimeUnit.SECONDS)

        b.addInterceptor { chain: Interceptor.Chain ->
            val original = chain.request()

            //add auth token in header
            var token = AllUtil.getAuthToken()
            val request = original.newBuilder()
                .header("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        AndroidNetworking.initialize(requireContext().applicationContext, b.build())
    }
}


