package com.france.pedometre

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews

class StepWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) updateWidget(context, appWidgetManager, id)
    }

    companion object {
        fun pushUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, StepWidget::class.java))
            for (id in ids) updateWidget(context, manager, id)
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val steps   = StepRepository.currentSteps.value
            val goal    = UserPrefs(context).getGoal()
            val percent = ((steps.toFloat() / goal) * 100).toInt().coerceAtMost(100)

            val views = RemoteViews(context.packageName, R.layout.widget_step_counter)
            views.setTextViewText(R.id.widget_steps, if (steps >= 10_000) "${steps / 1000}k" else "$steps")
            views.setTextViewText(R.id.widget_percent, "$percent% de l'objectif")
            manager.updateAppWidget(widgetId, views)
        }
    }
}
