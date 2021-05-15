package com.uptodd.uptoddapp.ui.login.loginpage

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.uptodd.uptoddapp.database.logindetails.Explorers


internal class AuthRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun firebaseSignInWithGoogle(googleAuthCredential: AuthCredential?): MutableLiveData<Explorers> {
        val authenticatedUserMutableLiveData: MutableLiveData<Explorers> = MutableLiveData()
        firebaseAuth.signInWithCredential(googleAuthCredential!!)
            .addOnCompleteListener { authTask: Task<AuthResult> ->
                if (authTask.isSuccessful) {
                    val isNewUser =
                        authTask.result!!.additionalUserInfo!!.isNewUser
                    val firebaseUser = firebaseAuth.currentUser
                    if (firebaseUser != null) {
                        var explorers=Explorers()
                        explorers.uid=firebaseUser.uid
                        explorers.email=firebaseUser.email
                        explorers.isNewUser=isNewUser
                        explorers.loginMethod="google"
                        explorers.name=firebaseUser.displayName
                        explorers.phone=firebaseUser.phoneNumber
                        explorers.profileImageUrl=firebaseUser.photoUrl.toString()
                        Log.d("div","AuthRepository L35 $explorers")
                        authenticatedUserMutableLiveData.value = explorers
                    }
                } else {
                    Log.d("div","AuthRepository Google Failed")
                }
            }
        return authenticatedUserMutableLiveData
    }

    fun googleSignOut(googleSignInClient:GoogleSignInClient)
    {
        googleSignInClient.signOut()
    }

    fun firebaseSignInWithFacebook(facebookAuthCredential: AuthCredential): MutableLiveData<String>
    {
        val authenticatedUserMutableLiveData: MutableLiveData<String> = MutableLiveData()
        firebaseAuth.signInWithCredential(facebookAuthCredential).addOnCompleteListener{
            if(it.isSuccessful) {
                Log.d("div", "AuthRepository L52 Success")
                val firebaseUser = firebaseAuth.currentUser
                val isNewUser =
                    it.result!!.additionalUserInfo!!.isNewUser
                if (firebaseUser != null) {
                    val uid = firebaseUser.uid
                    val email = firebaseUser.email
                    var user: String
                    user = if (isNewUser)
                        "T$uid?$email"
                    else
                        "F$uid?$email"                           //Note the pattern of data
                    authenticatedUserMutableLiveData.value = user
                }
            }else {
                    Log.d("div", "AuthRepository Facebook Failed")
                }
            }
            return authenticatedUserMutableLiveData
    }


}