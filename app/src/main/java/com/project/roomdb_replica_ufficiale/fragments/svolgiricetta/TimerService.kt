package com.project.roomdb_replica_ufficiale.fragments.svolgiricetta

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.RingtoneManager
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDeepLinkBuilder
import com.project.roomdb_replica_ufficiale.MainActivity
import com.project.roomdb_replica_ufficiale.R
//import com.example.ricettario.fragments.TimerFragment
//import com.example.ricettario.fragments.TimerFragment.Companion.PREF_FILE
//import com.example.ricettario.fragments.TimerFragment.Companion.PREF_KEY_TIME_LEFT
import java.util.Locale

class TimerService : Service() {

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }
    private val binder = TimerBinder()
    private lateinit var mHandler: Handler

    companion object {
        const val ACTION_START = "com.example.ricettario.timer.START"
        const val ACTION_PAUSE = "com.example.ricettario.timer.PAUSE"
        const val ACTION_RESUME = "com.example.ricettario.timer.RESUME"
        const val ACTION_AZZERA = "com.example.ricettario.timer.AZZERA"
        const val ACTION_DISCONNETTI = "com.example.ricettario.timer.DISCONNETTI"

        const val ACTION_TICK        = "com.example.ricettario.timer.TICK"   // broadcast periodico
        const val EXTRA_TIME_LEFT = "extra_time_left"     // millis (broadcast)
        const val EXTRA_STATE = "extra_state"         // String (RUNNING/PAUSED)

        const val NOTIFY_PAUSE = "com.example.ricettario.timer.NOTIFY_PAUSE"
        const val NOTIFY_RESUME = "com.example.ricettario.timer.NOTIFY_RESUME"
        const val NOTIFY_RESET = "com.example.ricettario.timer.NOTIFY_RESET"

        private const val CHANNEL_ID = "canale_timer"
        private const val NOTIFICATION_ID = 100
    }

    var timeLeft: Long = 0
    var timer: CountDownTimer? = null
    lateinit var notification: Notification
    lateinit var builder: NotificationCompat.Builder
    lateinit var notificationManager: NotificationManager

    enum class TimerState {RUNNING, PAUSED, IDLE}
    var currentState: TimerState = TimerState.IDLE

    override fun onBind(intent: Intent?): IBinder {
        Log.d("DEBUG", "onBind chiamato")
        return binder
    }

    override fun onCreate() {
        Log.d("ONCREATE", "creato")
        super.onCreate()
        createNotificationChannel()
    }

    fun startForeground() {
        timer?.cancel()
        setUpTimer()
        timer?.start()

        start()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this, NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action

        when(action){
            ACTION_START -> {
                timeLeft = intent.getLongExtra(SvolgiRicettaFragment.EXTRA_START_TIMER, 0L)
                startForeground()
                start()
            }
            ACTION_DISCONNETTI -> stopSelf()
            ACTION_PAUSE -> pause()
            ACTION_AZZERA -> reset()
            ACTION_RESUME -> {
                startForeground()
                resume()
            }
            else -> Log.d("ACTION DEFAULT", "nothing")
        }

        Log.d("SERVIZIO", "startato")
        //non si puo attivare in background da API 12
        return START_NOT_STICKY
    }



    private fun createNotificationChannel() {
        val name = getString(R.string.timer_value)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system.
        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager

        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val ctx = this

        //PendingIntent per i bottoni della notifica
        val pausePI = makePendingIntent(ACTION_PAUSE, 1)
        val resumePI = makePendingIntent(ACTION_RESUME, 2)
        val azzeraPI = makePendingIntent(ACTION_AZZERA, 3)

        //Intent per accede tramite notifica
        val pendingIntent = NavDeepLinkBuilder(ctx)
            .setGraph(R.navigation.my_nav)
            .addDestination(R.id.svolgiRicettaFragment)
            .setComponentName(MainActivity::class.java)
            .createPendingIntent()


        builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.icona_notifica)
            .setContentTitle("Timer")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setAutoCancel(false)

        when(currentState){
            TimerState.RUNNING ->
                builder.setContentText(format(timeLeft))
                    .addAction(R.drawable.icona_notifica, "Pause", pausePI)
            TimerState.PAUSED ->
                builder.setContentText(format(timeLeft))
                    .addAction(R.drawable.icona_notifica, "Riprendi", resumePI)
                    .addAction(R.drawable.icona_notifica, "Azzera", azzeraPI)
            TimerState.IDLE ->
                builder.setContentText("null")

        }

        return builder.build()
    }

    private fun updateNotification(){
        builder.setContentText(format(timeLeft))
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun makePendingIntent(action: String, reqCode: Int) =
        if(Build.VERSION.SDK_INT >= 31){
            PendingIntent.getService(
                this, reqCode,
                Intent(this, TimerService::class.java).setAction(action),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                this, reqCode,
                Intent(this, TimerService::class.java).setAction(action),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

    fun format(time: Long): String {
        val totalSeconds = time / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    fun setUpTimer(){
        timer = object : CountDownTimer(timeLeft, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                updateNotification()

                Log.d("onTICK", "$timeLeft")
                val intent = setIntentUpdateText(ACTION_TICK)
                sendBroadcast(intent)
                //val preferences = getSharedPreferences(PREF_FILE, MODE_PRIVATE)
                //val editor = preferences.edit()

                //editor.putLong(PREF_KEY_TIME_LEFT, timeLeft)
                //editor.apply()
            }

            override fun onFinish() {
                playSound()
                timeLeft = 0
                currentState = TimerState.IDLE
                updateNotification()
                Log.d("TIMER SERVICE", "timer ha finito")
            }

        }
    }

    fun playSound() {
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val r = RingtoneManager.getRingtone(applicationContext, notification)
        r.play()
    }

    fun pause(){
        currentState = TimerState.PAUSED
        notification = createNotification()
        notificationManager.notify(NOTIFICATION_ID, notification)
        timer?.cancel()

        Log.d("PAUSE", "pausa")

        val intentPause = setIntentNotification(NOTIFY_PAUSE)
        sendBroadcast(intentPause)

    }
    private fun resume() {
        currentState = TimerState.RUNNING
        notification = createNotification()

        val intentResume = setIntentNotification(NOTIFY_RESUME)
        sendBroadcast(intentResume)
    }
    private fun start() {
        currentState = TimerState.RUNNING
        notification = createNotification()
    }


    fun reset(){
        timer?.onFinish()
        notification = createNotification()
        timer?.cancel()
        stopSelf()

        val intentReset = setIntentNotification(NOTIFY_RESET)
        sendBroadcast(intentReset)
    }

    private fun setIntentNotification(action: String) =
        Intent(action)
            .setPackage(packageName)
            .putExtra(EXTRA_STATE, currentState.name)
            .putExtra(EXTRA_TIME_LEFT, timeLeft)

    private fun setIntentUpdateText(action: String) =
        Intent(action)
            .setPackage(packageName)
            .putExtra(EXTRA_TIME_LEFT, timeLeft)

    override fun onDestroy() {
        super.onDestroy()
        Log.d("DESTROY TIMER", "distrutto")

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

}