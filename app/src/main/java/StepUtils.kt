package com.france.pedometre

import android.content.Context
import java.util.Locale

object StepUtils {
    /**
     * Calcule la distance formatée en kilomètres à partir des pas,
     * en utilisant les mêmes préférences que la MainActivity.
     */
    fun getFormattedDistance(steps: Int, context: Context): String {
        val prefs = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        
        val height = prefs.getInt("height", 175)
        val isMale = prefs.getBoolean("is_male", true)
        val customStride = prefs.getInt("stride", 0)

        val stepLengthKm = if (customStride > 0) {
            customStride.toDouble() / 100000.0
        } else {
            val strideMultiplier = if (isMale) 0.415 else 0.413
            (height * strideMultiplier) / 100000.0
        }

        val distanceKm = steps * stepLengthKm
        return String.format(Locale.getDefault(), "%.2f km", distanceKm)
    }
}