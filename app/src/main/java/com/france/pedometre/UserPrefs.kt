package com.france.pedometre

import android.content.Context
import androidx.core.content.edit

class UserPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)

    fun getStride(): Int = prefs.getInt("stride", 0)
    fun setStride(value: Int) = prefs.edit { putInt("stride", value) }

    fun getGoal(): Int = prefs.getInt("goal", 10000)
    fun setGoal(value: Int) = prefs.edit { putInt("goal", value) }

    fun getHeight(): Int = prefs.getInt("height", 175)
    fun setHeight(value: Int) = prefs.edit { putInt("height", value) }

    fun getWeight(): Int = prefs.getInt("weight", 70)
    fun setWeight(value: Int) = prefs.edit { putInt("weight", value) }

    fun getIsMale(): Boolean = prefs.getBoolean("is_male", true)
    fun setIsMale(value: Boolean) = prefs.edit { putBoolean("is_male", value) }

    fun getLastGoalReachedDate(): String = prefs.getString("last_goal_reached_date", "") ?: ""
    fun setLastGoalReachedDate(date: String) = prefs.edit { putString("last_goal_reached_date", date) }
}