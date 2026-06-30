package com.france.pedometre

import android.app.*
import android.content.*
import android.hardware.*
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class StepCounterService : LifecycleService(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null

    private lateinit var stepDao: StepDao
    private lateinit var userPrefs: UserPrefs
    
    private var totalStepsAtStartOfToday = -1
    private var stepsAlreadySavedBeforeRestart = 0
    private var lastSavedSteps = -1
    private var serviceCurrentDay: String = ""

    companion object {
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_GOAL_ID = 2
        const val CHANNEL_ID = "steps_channel"
        const val CHANNEL_GOAL_ID = "goal_channel"
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        stepDao = AppDatabase.getDatabase(this).stepDao()
        userPrefs = UserPrefs(this)
        
        createNotificationChannels()

        // Initialiser le jour actuel
        serviceCurrentDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        // Supprimer les données de plus d'un an
        lifecycleScope.launch(Dispatchers.IO) {
            val cutoff = Calendar.getInstance().apply { add(Calendar.YEAR, -1) }
            stepDao.deleteStepsOlderThan(
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cutoff.time)
            )
        }

        if (stepSensor == null) {
            StepRepository.setSensorAvailable(false)
            startForeground(NOTIFICATION_ID, createNotification("Capteur de pas non disponible"))
        } else {
            startForeground(NOTIFICATION_ID, createNotification("Initialisation..."))

            // Charger la DB avant d'enregistrer le capteur pour éviter la race condition :
            // si le capteur reçoit un événement avant que stepsAlreadySavedBeforeRestart soit
            // chargé, finalStepsForToday serait 0 au lieu de la valeur sauvegardée.
            // TYPE_STEP_COUNTER étant cumulatif hardware, aucun pas n'est perdu pendant ce délai.
            lifecycleScope.launch(Dispatchers.IO) {
                val queryDay = serviceCurrentDay
                val savedData = stepDao.getStepsForDate(queryDay)

                withContext(Dispatchers.Main) {
                    if (serviceCurrentDay == queryDay) {
                        if (savedData != null) {
                            stepsAlreadySavedBeforeRestart = savedData.steps
                            lastSavedSteps = savedData.steps
                            StepRepository.updateSteps(savedData.steps)
                            updateNotification(buildNotifText(savedData.steps))
                        } else {
                            StepRepository.updateSteps(0)
                            updateNotification(buildNotifText(0))
                        }
                    }
                    sensorManager?.registerListener(this@StepCounterService, stepSensor!!, SensorManager.SENSOR_DELAY_UI)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        try { onSensorChangedInternal(event) } catch (_: Exception) { }
    }

    private fun onSensorChangedInternal(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalStepsSincePhoneBoot = event.values[0].toInt()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // DÉTECTION DU CHANGEMENT DE JOUR (Minuit)
            if (today != serviceCurrentDay) {
                serviceCurrentDay = today
                totalStepsAtStartOfToday = totalStepsSincePhoneBoot
                stepsAlreadySavedBeforeRestart = 0
                lastSavedSteps = 0
                StepRepository.updateSteps(0)
                updateNotification(buildNotifText(0))
            }

            if (totalStepsAtStartOfToday == -1) {
                totalStepsAtStartOfToday = totalStepsSincePhoneBoot
            }

            val currentSessionSteps = (totalStepsSincePhoneBoot - totalStepsAtStartOfToday).coerceAtLeast(0)
            val finalStepsForToday = currentSessionSteps + stepsAlreadySavedBeforeRestart

            if (finalStepsForToday != lastSavedSteps) {
                lastSavedSteps = finalStepsForToday
                
                StepRepository.updateSteps(finalStepsForToday)
                StepWidget.pushUpdate(this)

                lifecycleScope.launch(Dispatchers.IO) {
                    stepDao.insertOrUpdate(StepData(today, finalStepsForToday))
                    
                    // Vérifier si l'objectif est atteint
                    checkGoalReached(finalStepsForToday, today)
                }

                updateNotification(buildNotifText(finalStepsForToday))
            }
        }
    }

    private fun buildNotifText(steps: Int): String {
        val goal    = userPrefs.getGoal()
        val percent = ((steps.toFloat() / goal) * 100).toInt().coerceAtMost(100)
        val dist    = StepUtils.getFormattedDistance(steps, this)
        return "$steps pas · $percent% · $dist"
    }

    private fun checkGoalReached(steps: Int, today: String) {
        val goal = userPrefs.getGoal()
        val lastReachedDate = userPrefs.getLastGoalReachedDate()

        if (steps >= goal && lastReachedDate != today) {
            userPrefs.setLastGoalReachedDate(today)
            showGoalReachedNotification(goal)
        }
    }

    private fun showGoalReachedNotification(goal: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_GOAL_ID)
            .setSmallIcon(R.drawable.ic_walk_notification)
            .setContentTitle("Objectif Atteint ! \uD83C\uDFC6")
            .setContentText("Félicitations ! Vous avez atteint votre objectif de $goal pas.")
            .setColor("#00E676".toColorInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_GOAL_ID, notification)
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Podomètre Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setShowBadge(false)
            setSound(null, null)
        }
        
        val goalChannel = NotificationChannel(
            CHANNEL_GOAL_ID,
            "Objectifs Atteints",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications de félicitations quand l'objectif de pas est atteint"
            enableLights(true)
            lightColor = android.graphics.Color.GREEN
        }
        
        manager.createNotificationChannel(serviceChannel)
        manager.createNotificationChannel(goalChannel)
    }

    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_walk_notification)
            .setContentTitle("Podomètre Pro")
            .setContentText(content)
            .setColor("#00E676".toColorInt())
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(content: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(content))
    }

    private fun scheduleRestart() {
        val restartIntent = PendingIntent.getService(
            this, 1, Intent(applicationContext, StepCounterService::class.java),
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        (getSystemService(ALARM_SERVICE) as AlarmManager).setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 500,
            restartIntent
        )
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        scheduleRestart()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)
        scheduleRestart()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    
    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }
}
