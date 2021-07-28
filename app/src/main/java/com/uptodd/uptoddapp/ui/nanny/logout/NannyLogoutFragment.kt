package com.uptodd.uptoddapp.ui.nanny.logout

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.LoginActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.databinding.DialogExtendSubscriptionBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.workManager.cancelAllWorkManagers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject


class NannyLogoutFragment : Fragment() {


    var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    var uid = ""
    var token = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        ChangeLanguage(requireContext()).setLanguage()

        Log.d("div", "NannyLogoutFragment L31")
        super.onCreate(savedInstanceState)
        Log.d("div", "NannyLogoutFragment L33")
        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.logout)
        preferences = activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        editor = preferences!!.edit()
        if (preferences!!.contains("uid") &&
            preferences!!.getString("uid", "")!!.isNotEmpty()
        ) {
            uid = preferences!!.getString("uid", "")!!
        }

        if (preferences!!.contains("token")
            && preferences!!.getString("token", "")!!.isNotEmpty()
        ) {
            token = preferences!!.getString("token", "").toString()
        }

        UptoddSharedPreferences.getInstance(requireContext()).clearLoginInfo()

        onClickLogout()

    }

    private fun onClickLogout() {
        Log.d("div", "NannyLogoutFragment L41")
        val dialogBinding = DataBindingUtil.inflate<DialogExtendSubscriptionBinding>(
            layoutInflater, R.layout.dialog_extend_subscription, null, false
        )
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogBinding.textView.text = getString(R.string.are_you_sure_logout)
        dialogBinding.buttonYes.setOnClickListener {
            editor?.putBoolean("loggedIn", false)
            editor?.commit()

            AndroidNetworking.get("https://www.uptodd.com/api/nannylogout/{userId}")
                .addHeaders("Authorization", "Bearer $token")
                .addPathParameter("userId", uid)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        Log.i("debug", "$response")
                        editor?.remove("LaunchTime")
                    }

                    override fun onError(anError: ANError?) {
                        Log.i("debug", "${anError?.message}")
                    }
                })

            cancelAllWorkManagers(requireActivity().application, requireContext())


            AllUtil.unregisterToken()

            CoroutineScope(Dispatchers.IO).launch {
                UptoddDatabase.getInstance(requireContext()).clearAllTables()
            }

            UptoddSharedPreferences.getInstance(requireContext()).clearAllPreferences()

            startActivity(
                Intent(
                    activity, LoginActivity::
                    class.java
                )
            )
            activity?.finish()
        }
        dialogBinding.buttonNo.setOnClickListener {
            dialog.dismiss()
            activity?.onBackPressed()
        }
        dialog.setCancelable(false)
        dialog.show()

    }

}
