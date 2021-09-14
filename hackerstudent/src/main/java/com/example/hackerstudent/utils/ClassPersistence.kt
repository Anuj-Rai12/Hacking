package com.example.hackerstudent.utils

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.hackerstudent.TAG
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

const val PREFERENCES_USER = "User_INFO"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_USER)

@Singleton
class ClassPersistence @Inject constructor(@ApplicationContext val context: Context) {

    val read = context.dataStore.data.catch { e ->
        if (e is IOException) {
            Log.i(TAG, "READ_EXCEPTION: ${e.localizedMessage}")
            emit(emptyPreferences())
        } else {
            throw e
        }
    }.map { preferences ->
        val email = preferences[data.EMAIL_ADDRESS] ?: ""
        val password = preferences[data.USER_PASSWORD] ?: ""
        val flag = preferences[data.REMEMBER_ME] ?: false
        val ipAddress = preferences[data.Ip_Address] ?: ""
        val firstname = preferences[data.First_Name] ?: ""
        val lastname = preferences[data.Last_Name] ?: ""
        val phone = preferences[data.Phone_Number] ?: ""
        UserStore(
            email = email,
            password = password,
            flag = flag,
            ipAddress = ipAddress,
            firstname = firstname,
            lastname = lastname,
            phone = phone
        )
    }

    suspend fun updateInfo(email: String, password: String, flag: Boolean) {
        context.dataStore.edit { mutablePreferences ->
            mutablePreferences[data.EMAIL_ADDRESS] = email
            mutablePreferences[data.USER_PASSWORD] = password
            mutablePreferences[data.REMEMBER_ME] = flag
        }
    }

    suspend fun storeInitUserDetail(
        ipAddress: String,
        firstname: String,
        lastname: String,
        phone: String,
        email: String,
        password: String
    ) {
        context.dataStore.edit { mutablePreferences ->
            mutablePreferences[data.Ip_Address] = ipAddress
            mutablePreferences[data.First_Name] = firstname
            mutablePreferences[data.Last_Name] = lastname
            mutablePreferences[data.Phone_Number] = phone
            mutablePreferences[data.EMAIL_ADDRESS] = email
            mutablePreferences[data.USER_PASSWORD] = password
        }
    }

    suspend fun updatePassword(password: String) {
        context.dataStore.edit { mutablePreferences ->
            mutablePreferences[data.USER_PASSWORD] = password
        }
    }

    private val data = object {
        val EMAIL_ADDRESS = stringPreferencesKey("EMAIL_ADDRESS")
        val REMEMBER_ME = booleanPreferencesKey("Remember_Me")
        val USER_PASSWORD = stringPreferencesKey("PASSWORD")
        val Ip_Address = stringPreferencesKey("User_Ip_Address")
        val Phone_Number = stringPreferencesKey("User_Phone_Number")
        val First_Name = stringPreferencesKey("First_Name_User")
        val Last_Name = stringPreferencesKey("Last_Name_User")
    }
}

data class UserStore(
    var email: String,
    val password: String,
    var flag: Boolean,
    val ipAddress: String,
    val phone: String,
    var firstname: String,
    val lastname: String
)