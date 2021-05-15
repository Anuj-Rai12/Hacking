package com.uptodd.uptoddapp.ui.nanny

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.ui.todoScreens.TodosListActivity
import java.util.*


class ChangeLanguageNanny : Fragment() {

    private lateinit var englishButton: RadioButton
    private lateinit var hindiButton: RadioButton
    private lateinit var saveButton: Button
    private lateinit var preferences: SharedPreferences
    private lateinit var langEditor: SharedPreferences.Editor
    private var currentLang: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_change_language_nanny, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            requireActivity().resources.getString(R.string.change_language)

        englishButton = view.findViewById(R.id.englishLanguage)
        hindiButton = view.findViewById(R.id.hindiLanguage)
        saveButton = view.findViewById(R.id.setButton)

        preferences = requireContext().getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)

        langEditor = preferences.edit()

        if (preferences.contains("language")) {
            preferences.getString("language", "English")?.let {
                currentLang = it
            }
        }

        if (currentLang == "") {
            // return
        } else if (currentLang == "English") {
            englishButton.isChecked = true
            hindiButton.isChecked = false
        } else if (currentLang == "Hindi") {
            hindiButton.isChecked = true
            englishButton.isChecked = false
        }

        englishButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Log.i("debug", "English Selected")
                hindiButton.isChecked = false
            }
        }

        hindiButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Log.i("debug", "Hindi Selected")
                englishButton.isChecked = false
            }
        }

        saveButton.setOnClickListener {
            if (hindiButton.isChecked) {
                setLanguage("Hindi", "hi")
            } else if (englishButton.isChecked) {
                setLanguage("English", "en")
            }
        }

    }

    private fun setLanguage(language: String, languageCode: String) {
        langEditor.putString("language", language)
        langEditor.commit()

        currentLang = language

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.locale = locale
        requireContext().resources.updateConfiguration(
            config,
            requireContext().resources.displayMetrics
        )

        val intent = Intent(requireActivity(), TodosListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        startActivity(intent)

        Toast.makeText(
            requireContext().applicationContext,
            "Language Changed to $currentLang",
            Toast.LENGTH_SHORT
        ).show()

        requireActivity().finish()
    }

}