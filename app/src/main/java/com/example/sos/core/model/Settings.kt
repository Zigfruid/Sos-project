package com.example.sos.core.model

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.FirebaseApiNotAvailableException

class Settings(context:Context) {
    companion object {
        const val IS_APP_LANGUAGE_SELECTED = "isAppLanguageSelected"
        const val LANGUAGE = "currentLanguage"
        const val POSITION = "position"
        const val FIRST_LAUNCH = "firstLaunch"
    }
    private val preferences: SharedPreferences = context.getSharedPreferences("LanguageSharedPreferences", Context.MODE_PRIVATE)


    fun setFirstLanguageSelected() {
        preferences.edit().putBoolean(IS_APP_LANGUAGE_SELECTED, false).apply()
    }

    fun setLanguage(language: String) {
        preferences.edit().putString(LANGUAGE, language).apply()
    }

    fun getLanguage() : String =
            preferences.getString(LANGUAGE, "") ?: "en"

    fun setPosition(position:Int){
        preferences.edit().putInt(POSITION, position).apply()
    }
    fun getPosition() : Int = preferences.getInt(POSITION, 3)

    fun setFirstLaunched(){
        preferences.edit().putBoolean(FIRST_LAUNCH, true).apply()
    }
    fun checkLaunchStatic() : Boolean = preferences.getBoolean(FIRST_LAUNCH,false)



}