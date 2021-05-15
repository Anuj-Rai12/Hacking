package com.uptodd.uptoddapp.ui.login.facebooklogin

import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.uptodd.uptoddapp.LoginActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.UptoddWebsiteActivity
import com.uptodd.uptoddapp.database.logindetails.Explorers
import com.uptodd.uptoddapp.databinding.ActivityFacebookLoginBinding
import com.uptodd.uptoddapp.databinding.DialogExtendSubscriptionBinding
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import com.uptodd.uptoddapp.workManager.cancelAllWorkManagers


class FacebookLoginActivity : AppCompatActivity() {

    lateinit var callbackManager: CallbackManager
    lateinit var firebaseAuth: FirebaseAuth

    lateinit var viewModel: FacebookLoginViewModel

    private lateinit var binding: ActivityFacebookLoginBinding

    var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

    private lateinit var layout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_facebook_login)

        val loginButton: LoginButton = findViewById(R.id.button_facebookLogin)
        layout = findViewById(R.id.layout)

        preferences = getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        editor = preferences!!.edit()

        viewModel = ViewModelProvider(this).get(FacebookLoginViewModel::class.java)

        if (preferences!!.contains("loggedIn") && preferences!!.getBoolean("loggedIn", false)
            && preferences!!.contains("userType") && preferences!!.getString(
                "userType",
                ""
            ) == "Facebook"
        )
            changeFunctionalityToHome()
        else
            changeFunctionalityToFacebookLogin()

        binding.buttonWebsite.setOnClickListener { onClickWebsite() }

        firebaseAuth = FirebaseAuth.getInstance()
        FacebookSdk.sdkInitialize(FacebookSdk.getApplicationContext())
        //AppEventsLogger.activateApp(activity);
        callbackManager = CallbackManager.Factory.create()
        /*loginButton.setOnClickListener{
            Log.d("div", "FacebookLoginActivity L148 button clicked")
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
        }*/
        loginButton.setReadPermissions("public_profile", "email")
        LoginManager.getInstance().registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {
                    Log.d("div", "FacebookLoginActivity L75 Success $loginResult")
                    GraphRequest.newMeRequest(loginResult!!.accessToken) { json, response -> // Application code
                        if (response.error != null) {
                            Log.d("div", "FacebookLoginActivity L78 Error")
                        } else {
                            Log.d("div", "FacebookLoginActivity L80 Email success")
                            val fbUserEmail = json.optString("email")
                            editor?.putString("email", fbUserEmail)
                            editor?.commit()
                            Log.d("div", "Email: $fbUserEmail")
                        }
                        Log.d("div", "FacebookLoginActivity$response")
                    }.executeAsync()
                    val parameters = Bundle()
                    parameters.putString("fields", "email")

                    binding.buttonFacebookLogin.visibility = View.INVISIBLE
                    handleFacebookToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    Log.d("div", "FacebookLoginActivity L67 Cancel")
                }

                override fun onError(exception: FacebookException?) {
                    Log.d("div", "FacebookLoginActivity L67 exception")
                }
            })

        binding.imageButtonLogout.setOnClickListener { onClickLogout() }

        // back button listener
        binding.goBackFacebook.setOnClickListener {
            this.finish()
        }

    }


    private fun changeFunctionalityToFacebookLogin() {
        binding.imageButtonLogout.visibility = View.INVISIBLE
        binding.buttonWebsite.visibility = View.INVISIBLE
        binding.textViewContactUs.visibility = View.INVISIBLE
        binding.buttonFacebookLogin.visibility = View.VISIBLE
    }

    private fun changeFunctionalityToHome() {
        binding.imageButtonLogout.visibility = View.INVISIBLE
        binding.buttonWebsite.visibility = View.INVISIBLE
        binding.textViewContactUs.visibility = View.INVISIBLE
        binding.buttonFacebookLogin.visibility = View.INVISIBLE
    }

    private fun onClickLogout() {
        val dialogBinding = DataBindingUtil.inflate<DialogExtendSubscriptionBinding>(
            layoutInflater, R.layout.dialog_extend_subscription, null, false
        )
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogBinding.textView.text = getString(R.string.are_you_sure_logout)
        dialogBinding.buttonYes.setOnClickListener {
            editor?.putBoolean("loggedIn", false)
            editor?.commit()

            cancelAllWorkManagers(this.application, this)

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        dialogBinding.buttonNo.setOnClickListener { dialog.dismiss() }
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun onClickWebsite() {
        startActivity(Intent(this, UptoddWebsiteActivity::class.java))
    }

    fun handleFacebookToken(token: AccessToken) {
        Log.d("div", "FacebookLoginActivity L107 $token")
        val facebookCredential = FacebookAuthProvider.getCredential(token.token)
        firebaseAuth.signInWithCredential(facebookCredential).addOnCompleteListener(this, object :
            OnCompleteListener<AuthResult> {
            override fun onComplete(task: Task<AuthResult>) {
                Log.d("div", "FacebookLoginActivity L106 Firebase onComplete $facebookCredential")
                if (task.isSuccessful) {
                    Log.d("div", "FacebookLoginActivity L109 onSuccess")
                    val firebaseUser = firebaseAuth.currentUser
                    val isNewUser = task.result!!.additionalUserInfo!!.isNewUser

                    if (firebaseUser != null) {
                        var explorers = Explorers()
                        explorers.uid = firebaseUser.uid
                        explorers.email = firebaseUser.email
                        explorers.isNewUser = isNewUser
                        explorers.loginMethod = "facebook"
                        explorers.name = firebaseUser.displayName
                        explorers.phone = firebaseUser.phoneNumber
                        explorers.profileImageUrl = firebaseUser.photoUrl.toString()

                        Log.d("div", "FacebookLoginActivity L117 $explorers")

                        if (AppNetworkStatus.getInstance(applicationContext).isOnline) {
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

                                    viewModel.isUploadingExplorerData.value = true
                                    viewModel.isExplorerDataUploaded = false
                                    showUploadingDialog()
                                    viewModel.uploadExplorerData(explorers, token)
                                    Log.d("div", "FacebookLoginActivity L203 $token")
                                    viewModel.isUploadingExplorerData.observe(this@FacebookLoginActivity,
                                        Observer {
                                            Log.d(
                                                "div",
                                                "FacebookLoginActivity L132 ${viewModel.isUploadingExplorerData.value}"
                                            )
                                            if (!it && viewModel.isExplorerDataUploaded) {
                                                if (viewModel.isExplorerDataUploaded) {
                                                    editor?.putString("userType", "Facebook")
                                                    editor?.putString("loginMethod", "facebook")
                                                    editor?.putBoolean("loggedIn", true)
                                                    editor?.commit()
                                                    AllUtil.registerToken("facebook")
                                                    changeFunctionalityToHome()
                                                    LoginManager.getInstance().logOut()

                                                    val manager: NotificationManager =
                                                        application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                        val channel = NotificationChannel(
                                                            "div",
                                                            "PreSales Immediate",
                                                            NotificationManager.IMPORTANCE_DEFAULT
                                                        )
                                                        manager.createNotificationChannel(
                                                            channel
                                                        )
                                                    }
                                                    val builder = NotificationCompat.Builder(
                                                        this@FacebookLoginActivity,
                                                        "div"
                                                    )
                                                        .setContentTitle("Subscribe Now")
                                                        .setContentText("Mom/Dad, my brain is developing fast, I need the right environment. Thank you for joining UpTodd for me.")
                                                        .setSmallIcon(R.drawable.app_icon_image)

                                                    manager.notify(1, builder.build())


                                                    val intent =
                                                        Intent(
                                                            this@FacebookLoginActivity,
                                                            UptoddWebsiteActivity::class.java
                                                        )
                                                    intent.flags =
                                                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                    startActivity(intent)
                                                    finish()
                                                } else {
                                                    LoginManager.getInstance().logOut()
                                                    Toast.makeText(
                                                        this@FacebookLoginActivity,
                                                        getString(R.string.unable_to_upload_data_try_again_after_sometime),
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                        })
                                })

                        } else {
                            //showInternetNotConnectedDialog()
                            LoginManager.getInstance().logOut()
                            Snackbar.make(
                                layout,
                                getString(R.string.no_internet_connection),
                                Snackbar.LENGTH_LONG
                            )
                                .setAction(getString(R.string.retry)) {
                                    handleFacebookToken(token)
                                }.show()
                        }

                        /*if (isNewUser) {
                            editor?.putBoolean("loggedIn", false)
                            editor?.putBoolean("isNewUser", true)
                            editor?.putString("uid", uid)
                            editor?.putString("email", email)
                            editor?.commit()
                            startActivity(Intent(this@FacebookLoginActivity, LoginActivity::class.java))
                            finish()
                        } else {
                            editor?.putBoolean("loggedIn", true)
                            editor?.putBoolean("isNewUser", false)
                            editor?.putString("uid", uid)
                            editor?.putString("email", email)
                            editor?.commit()
                            startActivity(Intent(this@FacebookLoginActivity, MainActivity::class.java))
                            finish()
                        }*/
                    } else
                        Toast.makeText(
                            this@FacebookLoginActivity,
                            getString(R.string.log_in_failed),
                            Toast.LENGTH_LONG
                        ).show()

                } else {
                    Log.d("div", "FacebookLoginActivity L71 onFailed")
                    Toast.makeText(
                        this@FacebookLoginActivity,
                        getString(R.string.log_in_failed),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("div", "FacebookLoginActivity L193 onActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("div", "FacebookLoginActivity L195 onActivityResult")
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun showInternetNotConnectedDialog() {
        val upToddDialogs = UpToddDialogs(this)
        upToddDialogs.showDialog(R.drawable.gif_loading,
            getString(R.string.no_internet_connection),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                }
            })
    }

    private fun showUploadingDialog() {
        val upToddDialogs = UpToddDialogs(this)
        upToddDialogs.showDialog(R.drawable.gif_upload,
            getString(R.string.loading_please_wait),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                    onBackPressed()
                }
            })
        viewModel.isUploadingExplorerData.observe(this, Observer {
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