package com.example.arabic_unity.ui

import android.content.Context
import android.content.SharedPreferences
import com.example.arabic_unity.R

object PreferenceUtil {
    private lateinit var sharedPref: SharedPreferences
    fun getInstance(context: Context): PreferenceUtil{
        sharedPref = context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        return this
    }

    fun putString(key: String, value: String): PreferenceUtil{
        sharedPref.edit().putString(key, value).apply()
        return this
    }

    fun getString(key: String): String{
        return sharedPref.getString(key, "")?: ""
    }
}