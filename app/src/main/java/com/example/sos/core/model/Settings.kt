package com.example.sos.core.model

import android.content.Context
import android.content.SharedPreferences

class Settings(context:Context) {
    companion object {
        const val IS_APP_LANGUAGE_SELECTED = "isAppLanguageSelected"
        const val LANGUAGE = "currentLanguage"
        const val POSITION = "position"
    }
    private val preferences: SharedPreferences  = context.getSharedPreferences("LanguageSharedPreferences", Context.MODE_PRIVATE)


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
    fun getPosition() : Int = preferences.getInt(POSITION, 1)

}