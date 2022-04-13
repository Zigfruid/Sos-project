package com.example.sos.core.model

import android.content.Context
import android.content.SharedPreferences
import com.example.sos.R
import com.google.firebase.FirebaseApiNotAvailableException

class Settings(private val context: Context) {
    companion object {
        const val IS_APP_LANGUAGE_SELECTED = "isAppLanguageSelected"
        const val LANGUAGE = "currentLanguage"
        const val POSITION = "position"
        const val FIRST_LAUNCH = "firstLaunch"
        const val CHANGE_LANGUAGE = "changeLanguage"
    }

    private val preferences: SharedPreferences =
        context.getSharedPreferences("LanguageSharedPreferences", Context.MODE_PRIVATE)

    var status: String
        set(value) {
            preferences.edit().putString("status", value).apply()
        }
        get() = preferences.getString("status", context.getString(R.string.background_work))
            ?: context.getString(R.string.background_work)

    fun setFirstLanguageSelected() {
        preferences.edit().putBoolean(IS_APP_LANGUAGE_SELECTED, false).apply()
    }

    fun setLanguage(language: String) {
        preferences.edit().putString(LANGUAGE, language).apply()
    }

    fun getLanguage(): String =
        preferences.getString(LANGUAGE, "") ?: "en"

    fun setPosition(position: Int) {
        preferences.edit().putInt(POSITION, position).apply()
    }

    fun getPosition(): Int = preferences.getInt(POSITION, 3)

    fun setFirstLaunched() {
        preferences.edit().putBoolean(FIRST_LAUNCH, true).apply()
    }

    fun checkLaunchStatic(): Boolean = preferences.getBoolean(FIRST_LAUNCH, false)

    var isChangeLanguage: Boolean
        set(value) = preferences.edit().putBoolean(CHANGE_LANGUAGE, value).apply()
        get() = preferences.getBoolean(CHANGE_LANGUAGE, false) ?: false

    var isServiceRunning: Boolean
        set(value) {
            preferences.edit().putBoolean("running", value).apply()
        }
        get() = preferences.getBoolean("running", true)
}