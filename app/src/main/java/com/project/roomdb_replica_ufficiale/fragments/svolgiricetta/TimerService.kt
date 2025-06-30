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
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDeepLinkBuilder
import com.project.roomdb_replica_ufficiale.MainActivity
import com.project.roomdb_replica_ufficiale.R
import java.util.Locale

class TimerService : Service() {

    //Binder locale che espone un riferimento al servizio stesso.
    inner class TimerBinder : Binder() {

        //Restituisce l'istanza attuale di TimerService
        fun getService(): TimerService = this@TimerService
    }

    private val binder = TimerBinder()

    //COSTANTI
    companion object {
        const val ACTION_START = "com.project.roomdb_replica_ufficiale.timer.START"
        const val ACTION_PAUSE = "com.project.roomdb_replica_ufficiale.timer.PAUSE"
        const val ACTION_RESUME = "com.project.roomdb_replica_ufficiale.timer.RESUME"
        const val ACTION_AZZERA = "com.project.roomdb_replica_ufficiale.timer.AZZERA"
        const val ACTION_DISCONNETTI = "com.project.roomdb_replica_ufficiale.timer.DISCONNETTI"

        const val ACTION_TICK        = "com.project.roomdb_replica_ufficiale.timer.TICK"   // broadcast periodico
        const val EXTRA_TIME_LEFT = "extra_time_left"     // millis (broadcast)
        const val EXTRA_STATE = "extra_state"         // String (RUNNING/PAUSED)

        const val NOTIFY_PAUSE = "com.project.roomdb_replica_ufficiale.timer.NOTIFY_PAUSE"
        const val NOTIFY_RESUME = "com.project.roomdb_replica_ufficiale.timer.NOTIFY_RESUME"
        const val NOTIFY_RESET = "com.project.roomdb_replica_ufficiale.timer.NOTIFY_RESET"

        const val CHANNEL_ID = "canale_timer"
        const val NOTIFICATION_ID = 100
    }

    //PROPRIETA
    var timeLeft: Long = 0
    var timer: CountDownTimer? = null
    lateinit var notification: Notification
    lateinit var builder: NotificationCompat.Builder
    lateinit var notificationManager: NotificationManager

    enum class TimerState {RUNNING, PAUSED, IDLE}
    var currentState: TimerState = TimerState.IDLE

    /*
    Restituisce il local binder che consente a svolgiRicetta di interagire
    direttamente con il servizio
     */
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        //crea il canale di notifica alla creazione del servizio
        createNotificationChannel()
    }

    /*
    class per ininizializzare effettivamente il servizio in foreground che farà partire la notifica
    cancello qualsiasi informazione precedente nel timer
    reimposto il valore
    poi lo faccio partire
     */
    fun startForeground() {
        timer?.cancel()
        setUpTimer()

        startTimer() //creazione anche della notifica

        //Prima di pubblicare la notifica, controllo che il permesso sia stato accettato
        if (Build.VERSION.SDK_INT > 33) {
            ServiceCompat.startForeground(
                this, NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    /*
    classe che viene chiamata da SvolgiRicerttaFragment.kt per iniziare il servizio.
    Il metodo in base all'intent che riceve decide cosa deve fare.
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action

        /*
        start: prende il valore passato per il timer e inizia il servizio
        disconnetti: termina il servizio
        pause: mette in pausa il timer
        azzera: effettua il rest
        resume: riprende dal valore che il service aveva
         */
        when(action){
            ACTION_START -> {
                timeLeft = intent.getLongExtra(SvolgiRicettaFragment.EXTRA_START_TIMER, 0L)
                startForeground()
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

        return START_NOT_STICKY
    }

    //Creazione del canale di notifica, altrimenti la notifica non puo essere usata
    private fun createNotificationChannel() {
        val name = getString(R.string.timer_value)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Registro il canale nel sistema
        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager

        notificationManager.createNotificationChannel(channel)
    }

    /*
    creazione della notifica, con 3 azioni per mettere in pausa, resettare e far ripartire il timer
    La notifica avrà un NavDeepLink per al click far accedere l'utente direttamente alla schermata
    corretta della ricetta premendo la notifica.
     */
    private fun createNotification(): Notification {
        val ctx = this

        //PendingIntent per i bottoni della notifica
        val pausePI = makePendingIntent(ACTION_PAUSE, 1)
        val resumePI = makePendingIntent(ACTION_RESUME, 2)
        val azzeraPI = makePendingIntent(ACTION_AZZERA, 3)

        //Intent per accedere tramite notifica
        val pendingIntent = NavDeepLinkBuilder(ctx)
            .setGraph(R.navigation.my_nav)
            .addDestination(R.id.svolgiRicettaFragment)
            .setComponentName(MainActivity::class.java)
            .createPendingIntent()

        //builder effettivo della notifica
        builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.icona_notifica)
            .setContentTitle("Timer")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)

        /*
        i bottoni devono apparire nel momento giusto nel modo corretto in base allo stato del timer
         */
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

    //funzione per modificare istante il testo della la notifica
    private fun updateNotification(){
        builder.setContentText(format(timeLeft))
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    //Creazione un intent che compie un azione sul servizio
    private fun makePendingIntent(action: String, reqCode: Int) =
        if(Build.VERSION.SDK_INT >= 31){
            PendingIntent.getService(this, reqCode,
                Intent(this, TimerService::class.java).setAction(action),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE //API31
            )
        } else {
            PendingIntent.getService(
                this, reqCode,
                Intent(this, TimerService::class.java).setAction(action),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

    /*
    funzione per scrivere il valore in maniera corretta nel formato mm:ss
     */
    fun format(time: Long): String {
        val totalSeconds = time / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.ITALY, "%02d:%02d", minutes, seconds)
    }

    /*
    function che inizializza il timer. Timer parte di timeLeft e si aggiornerà ogni 1000ms, ossia 1s
    ad ogni tick timeLeft si aggiorna, e il servizio deve aggiornare sia la notifica che avvertire
    l'UI che deve aggiornarsi perchè è passato un secondo, per farlo fa un Broadcast.

    se il timer è finito, allora parte il suono, e lo stato va in IDLE
     */
    fun setUpTimer(){
        timer = object : CountDownTimer(timeLeft, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                updateNotification()

                Log.d("onTICK", "$timeLeft")
                val intent = setIntentUpdateText(ACTION_TICK)
                sendBroadcast(intent)
            }

            override fun onFinish() {
                playSound()
                timeLeft = 0
                currentState = TimerState.IDLE
                updateNotification()
                endTimerNotification()
                Log.d("TIMER SERVICE", "timer ha finito")
            }


        }
    }

    /*
    funzione per impostare il suono della notifica al momento che termina la notifica
    */
    fun playSound() {
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val r = RingtoneManager.getRingtone(applicationContext, notification)
        r.play()
    }
    private fun endTimerNotification() {
        val ctx = this

        //Intent per accedere tramite notifica
        val pendingIntent = NavDeepLinkBuilder(ctx)
            .setGraph(R.navigation.my_nav)
            .addDestination(R.id.svolgiRicettaFragment)
            .setComponentName(MainActivity::class.java)
            .createPendingIntent()

        //builder effettivo della notifica
        builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.icona_notifica)
            .setContentTitle("Timer Scaduto")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOnlyAlertOnce(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)

        /*
        i bottoni devono apparire nel momento giusto nel modo corretto in base allo stato del timer
         */

        notification = builder.build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /*
    funzione che mette in pausa il timer, e comunicazione al fragment che è in pausa, nel caso
    io sia nella schermata del timer ma ho messo in pausa attraverso dalla notifica
     */
    fun pause(){
        currentState = TimerState.PAUSED
        //ricreazione della notifica dovuto all'aggiornamento dei bottoni
        notification = createNotification()
        notificationManager.notify(NOTIFICATION_ID, notification)
        timer?.cancel()

        Log.d("PAUSE", "pausa")

        val intentPause = setIntentNotification(NOTIFY_PAUSE)
        sendBroadcast(intentPause)
    }
    /*
    funzione che fa riprendere il timer, simile a pause ma per fare il RESUME
    */
    private fun resume() {
        currentState = TimerState.RUNNING
        notification = createNotification()

        val intentResume = setIntentNotification(NOTIFY_RESUME)
        sendBroadcast(intentResume)
    }
    //fa partire il timer
    private fun startTimer() {
        currentState = TimerState.RUNNING
        notification = createNotification()
        timer?.start()
    }

    //resetta a 0 il timer
    fun reset(){
        //timer?.onFinish()
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