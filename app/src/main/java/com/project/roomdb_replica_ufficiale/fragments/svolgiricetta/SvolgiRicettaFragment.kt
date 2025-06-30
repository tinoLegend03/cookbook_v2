package com.project.roomdb_replica_ufficiale.fragments.svolgiricetta

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.ricettario.fragments.TimePickerFragment
import com.project.roomdb_replica_ufficiale.R
import com.project.roomdb_replica_ufficiale.data.istruzione.Istruzione
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta
import com.project.roomdb_replica_ufficiale.data.ricetta.RicettaViewModel
import com.project.roomdb_replica_ufficiale.databinding.FragmentSvolgiRicettaBinding
import com.project.roomdb_replica_ufficiale.fragments.svolgiricetta.TimerService.TimerState

/*
Fragment che gestisce lo svolgimento di una ricetta step by step. Mette a disposizione un timer
sfruttando TimerService.
 */
class SvolgiRicettaFragment: Fragment() {

    //COSTANTI
    companion object {
        //Add key Strings for use with the Bundle
        const val MBOUND_KEY = "mbound"
        const val CURRENT_STEP = "current_step"

        const val ID_RICETTA = "id_ricetta"

        const val EXTRA_START_TIMER = "extra_start_timer"

    }

    //PROPRIETA
    private var _binding: FragmentSvolgiRicettaBinding? = null
    private val binding get() = _binding!!

    //viewModel per ottenimento e aggiornamento ricetta
    private lateinit var mRicettaViewModel: RicettaViewModel

    //proprieta per gestione step by step
    private lateinit var  ricetta: Ricetta //ricetta che verra usata
    private var totalStep: Int = 0
    private var currentStep: Int = 0
    private var valueBar: Int = 0
    var steps = mutableListOf<Istruzione>()
    private var idRecipe: Long = -1

    // Attributi per stato e gestione del timer
    var mService : TimerService? = null
    private var mBound: Boolean = false

    //valori per indipendenza da connessione
    private var pendingTimeLeft: Long = 0L //tempo in millisecondi
    private var pendingState: TimerState = TimerState.IDLE

    /*
    gestione del callback per tornare indietro. Impostato sempre a true
    per gestire l'uscita dalla schermata
     */
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            dialogQuitFragment()
        }
    }


    //Definizione della connessione al Servizio
    private val connection = object : ServiceConnection {
        /*
        imposto la connessione
         */
        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            mService = (service as TimerService.TimerBinder).getService()
            mBound = true

            //Sincronizzo il tempo che devo visualizzare con quello che dtempo del servizio a quello visualizzato
            //devo effettuare una safe call, per evitare di usare !! senza protezioni troppo spesso.
            mService?.let { service ->
                if (service.timeLeft > 0L) {
                    pendingTimeLeft = service.timeLeft
                    pendingState = service.currentState

                    if (service.currentState == TimerState.RUNNING) {
                        updateChronometer(pendingTimeLeft)
                    }
                } else {
                    service.timeLeft = pendingTimeLeft
                    service.currentState = pendingState

                    updateChronometer(pendingTimeLeft)
                }
            }

        }

        //setto quando il servizio è disconnesso
        override fun onServiceDisconnected(name: ComponentName?) {
            mBound = false
            mService = null
        }

    }

    //RICEZIONE DEL BROADCASTER: per le informazioni che vengono ricevute da TimerService
    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent) {
            //informazione che viene sempre fornita dal servizio
            pendingTimeLeft = intent.getLongExtra(TimerService.EXTRA_TIME_LEFT, 0L)

            /*
            in base al tipo di azione:

            metto in pausa timer
            resetto il timer
            riprendo il timer
            aggiorno soltanto il timer perche è passato un secondo
             */
            when(intent.action) {
                TimerService.NOTIFY_PAUSE -> {
                    val state = intent.getStringExtra(TimerService.EXTRA_STATE) ?: return
                    pendingState = TimerState.valueOf(state)

                    updateChronometer(pendingTimeLeft)
                    pause()
                }

                TimerService.NOTIFY_RESET -> {
                    val state = intent.getStringExtra(TimerService.EXTRA_STATE) ?: return
                    pendingState = TimerState.valueOf(state)
                    reset()
                }
                TimerService.NOTIFY_RESUME -> {
                    val state = intent.getStringExtra(TimerService.EXTRA_STATE) ?: return
                    pendingState = TimerState.valueOf(state)
                    resume()
                }
                TimerService.ACTION_TICK -> {
                    updateChronometer(pendingTimeLeft)
                }
            }
        }

    }

    override fun onStart(){
        super.onStart()

        //Creo il IntentFilter con tutti i tipi di azioni che posso ricerevere come broadcast dal servizio TimerService
        val filter = IntentFilter(TimerService.ACTION_TICK).apply{
            addAction(TimerService.NOTIFY_PAUSE)
            addAction(TimerService.NOTIFY_RESET)
            addAction(TimerService.NOTIFY_RESUME)
        }
        /*
        A partire da API 33 (TIRAMISU) bisogna usare il nuovo metodo registerReceiver con il flag di
        esportazione.
        Per le versioni precedenti si utilizza ContextCompat per garantire la compatibilità con i nuovi flag.
         */
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //DA api33 fortemente è
            requireContext().registerReceiver(receiver, filter,
                Context.RECEIVER_NOT_EXPORTED)
        } else {
            ContextCompat.registerReceiver(requireContext(), receiver,
                filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        }


        Intent(requireContext(), TimerService::class.java).also { intent ->
            requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    //Creazione della view
    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val ctx = requireContext() //contesto dell'activity per non scrivere sempre il metodo

        //binding
        _binding = FragmentSvolgiRicettaBinding.inflate(inflater, container, false)
        val view = binding.root

        //imposto il metodo con cui posso scrollare il testo
        binding.stepText.movementMethod = android.text.method.ScrollingMovementMethod()
        //istanzo la gestione della ricetta da cui prendo gli step
        mRicettaViewModel = ViewModelProvider(this)[RicettaViewModel::class.java]

        //args per accedere al parametro passato da list attraverso navigation
        val args = SvolgiRicettaFragmentArgs.fromBundle(requireArguments())

        /*
        quando io entro dalla notifica, io non passo nessun valore args, cio significa che il
        contenuto sara nullo

        prendo allora da preference il valore con cui stavo lavorando
         */
        if (args.currentRecipe == null) { //accedo dalla notifica, non ho NavArgs
            val preferences = requireActivity().getPreferences(MODE_PRIVATE)

            //prendo la chiave primaria: ossia l'id
            idRecipe = preferences.getLong(ID_RICETTA, -1) //prendo valore salvato

            //reimposto le variabili di gestione timer ai valori attuali del servizio
            pendingTimeLeft = mService?.timeLeft ?: 0
            pendingState = mService?.currentState ?: TimerState.IDLE
        } else {
            //se invece non è nullo, posso prendere il valore dell'id
            idRecipe = args.currentRecipe.idRicetta
        }

        //GESTIONE TIMER con salvataggio stato
        if(savedInstanceState != null){
            mBound = savedInstanceState.getBoolean(MBOUND_KEY)

            //se ero connesso, devo riprendere
            if(mBound) {
                val action = TimerService.ACTION_RESUME

                //invio al servizio di riprendere
                val intent = Intent(ctx, TimerService::class.java).apply {
                    this.action = action
                    putExtra(EXTRA_START_TIMER, pendingTimeLeft)
                }
                ctx.startForegroundService(intent)
                ctx.bindService(intent, connection, Context.BIND_AUTO_CREATE) //mi ricollego al servizio
            }
        }
        /*
        impostazioni della seekbar per avere la seekbar decorata
         */
        binding.seekBar.isEnabled = true
        binding.seekBar.setOnTouchListener { _, _ -> true }

        //se idRecipe è diversa da -1, vuol dire che ha trovato il valore
        if (idRecipe != -1L) {
            /*
            Chiede al ViewModel la ricetta con le sue istruzioni
            e si usa il LiveData per ricevere aggiornamenti
            */
            mRicettaViewModel.getRicettaConIstruzioni(idRecipe).observe(viewLifecycleOwner) { dati ->
                //estraggo il i valori della ricectta e degli step e li inserisco nelle proprietà
                ricetta = dati.ricetta
                steps = dati.istruzioni.sortedBy { it.numero }.toMutableList()

                //imposto la textView NameRecipe
                binding.nameRecipe.text = ricetta.nomeRicetta

                steps.forEach {
                    binding.stepText.text = getString(R.string.step_message, binding.stepText.text, it.descrizione)
                }
                totalStep = steps.size //imposto numero di step totali

                /*
                current step puo avere due valori: o quello di default: 0, oppure per il salvataggio dello
                stato ha il valore salvato prima della distruzione del fragment
                 */
                if(savedInstanceState != null) {
                    currentStep = savedInstanceState.getInt(CURRENT_STEP)
                } else {
                    currentStep = 0
                }

                updateButtons()
                /*
                se il numero di step è maggiore di 1 allora imposto lo spostamento singolo della seekbar
                imposto.
                se il progresso della seekbar è uguale al suo valore massimo allora imposto la seekbar
                finita, e anche i bottoni.
                 */
                if (totalStep > 1){
                    valueBar = binding.seekBar.max / (totalStep-1)
                    if(binding.seekBar.progress == binding.seekBar.max){
                        seekBarFinished(ctx)
                        finishButton(ctx)
                    }
                } else {
                    binding.seekBar.progress = binding.seekBar.max
                    seekBarFinished(ctx)
                }

                //se la ricetta non ha ingredienti allora torna indietro
                if(totalStep == 0){
                    findNavController().popBackStack()
                } else {
                    //in altro caso aggiorna il textView con la descrizione dello step corrente.
                    updateText()
                }
            }
        }

        /*
        gestione del bottone di avanzamento ricetta
         */
        binding.stepForward.setOnClickListener {
            //se non sono alla fine degli step
            if(currentStep < totalStep - 1){
                currentStep++
                updateText()
                binding.seekBar.progress += valueBar

                //se incrementando sono alla fine, cambia seekbar
                if(binding.seekBar.progress == binding.seekBar.max){
                    seekBarFinished(ctx)
                }
            } else if (currentStep == totalStep - 1) {
                /*
                altrimenti se sono alla fine, controllo se ho un timer non IDLE chiedo all'utente se
                è sicuro di completare la ricetta, altrimenti completa direttamente la ricetta
                 */
                if(pendingState != TimerState.IDLE){
                    dialogStopTimer()
                } else {
                    updateCompletamentoRicetta()
                }

            }

            //aggiorno i valori dei bottoni
            updateButtons()
        }
        //gestione del bottone per tornare indietro nello step by step
        binding.stepBack.setOnClickListener {
            //se lo step è maggiore non è il primo allora
            if(currentStep > 0){
                currentStep--
                seekBarNotFinishedYed(ctx)
                updateText()
                binding.seekBar.progress -= valueBar
            }
            updateButtons()
        }


        /*
        listener per risultato TIMEPICKER, gestisce la comunicazione tra fragment attraverso
        setFragmentResultListener.

        Posso impostare la comunicazione solo se il servizio è IDLE o PAUSE, se è PAUSE allora è connesso
        di conseguenza disconnetto il servizio per reimpostare il valore.
         */
        setFragmentResultListener(TimePickerFragment.REQUEST_KEY) { requestKey, bundle -> //se arriva la richiesta da TIMEPICKER
            pendingTimeLeft = bundle.getLong(TimePickerFragment.BUNDLE_KEY) //set timeLeft
            pendingState = TimerState.IDLE

            if(mBound) stopService() // Se era connesso, disconnetti il servizio

            //reimposto bottoni e cronomento
            updateButtonsTimer(pendingState)
            updateChronometer(pendingTimeLeft)
        }

        //INSERISCO VALORE NEL TIMER: gestione comunicazione fragment tramite parentFragmentManager
        binding.timer.setOnClickListener {
            //Se sono connesso e il timer non sta andando
            if(mBound && mService!!.currentState != TimerState.RUNNING){
                //PAUSE o IDLE
                val newFragment = TimePickerFragment()
                newFragment.show(parentFragmentManager, TimePickerFragment::class.simpleName)
            } else if (!mBound) {
                //non sono connesso, timer sicuro IDLE
                val newFragment = TimePickerFragment()
                newFragment.show(parentFragmentManager, TimePickerFragment::class.simpleName)
            }
        }

        //Listener per il tasto START del timer
        binding.startButton.setOnClickListener {
            if (!mBound) { // se non sono connesso
                if (pendingTimeLeft > 0L) { //ho gia inserito i valori nel timer
                    //faccio partire il servizio

                    val action = TimerService.ACTION_START

                    val intent = Intent(ctx, TimerService::class.java).apply {
                        this.action = action
                        putExtra(EXTRA_START_TIMER, pendingTimeLeft)
                    }
                    ctx.startForegroundService(intent)
                    requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
                    //aggiorno lo stato

                    pendingState = TimerState.RUNNING
                    updateButtonsTimer(pendingState)

                } else { //timer non connesso e senza valori
                    Toast.makeText(context, "Nessun tempo impostato", Toast.LENGTH_SHORT).show()
                }
            } else { //sono connesso, posso farlo ripartire se in PAUSE
                if(mService!!.timeLeft > 0 && (mService!!.currentState == TimerState.PAUSED)) {
                    //mando l'azione per riprendere il servizio
                    val action = TimerService.ACTION_RESUME
                    val intent = Intent(requireContext(), TimerService::class.java).apply {
                        this.action = action
                        putExtra(EXTRA_START_TIMER, pendingTimeLeft)
                    }
                    ctx.startForegroundService(intent)

                    //aggiorno lo stato
                    pendingState = TimerState.RUNNING
                    updateButtonsTimer(pendingState)
                }
            }
        }

        //Listener del bottone Pause/Reset
        binding.stopButton.setOnClickListener {
            if(mBound) { //se sono connesso
                when (mService!!.currentState) {
                    //da RUNNNING a PAUSE
                    TimerState.RUNNING -> {
                        //mando al servizio di fermarsi
                        val action = TimerService.ACTION_PAUSE
                        val intent = Intent(ctx, TimerService::class.java).apply {
                            this.action = action
                        }
                        ctx.startForegroundService(intent)

                        pendingState = TimerState.PAUSED
                        // il fragment non chiama mService?.pause() qui,
                        // lascia che il servizio gestisca il suo stato e notifichi via connection
                        pause()

                    }

                    else -> { //se era PAUSE o IDLE, allora  resetto in IDLE
                        stopService()
                        reset()
                    }
                }
            } else { //se non sono connesso resetto sempre
                reset()
            }

        }

        //aggiungo il callback al dispatcher per il ritorno indietro
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        //aggiungo il callback anche per l'azione compiuta dal menu dell'activity
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == android.R.id.home) {
                    dialogQuitFragment()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        return view
    }

    /*
    funzione che crea una finestra di dialog per quando si vuole completare la ricetta ma il timer non
    è finito.

    Se vuole cancellare il timer allora ferma il servizio e aggiorna il completamento ricetta
    con cancel non succede nulla e rimane nel fragment
     */
    private fun dialogStopTimer() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Do you want to cancel the timer?")
            setMessage("")
            setPositiveButton("yes") { _, _ ->
                //cancello timer
                stopService()
                updateCompletamentoRicetta()
            }
            setNeutralButton("Cancel") { _, _ ->
            }
            setCancelable(false)
        }.show()
    }
    /*
    funzione che crea una finestra di dialog per quando si vuole uscire dallo svolgimento della ricetta
    senza completarla

    dichiara che l'ha compleata allora blocca il servizio e aggiorna il completamento
    exit: scollega dal servizio e esce torna alla pagina precedente
    cancel: nulla e rimane nel fragment
     */

    private fun dialogQuitFragment() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("The recipe isn't complete")
            setMessage("If you leave, you will not complete the recipe and the timer will reset")
            setPositiveButton("Completed") { _, _ ->
                //cancello timer
                stopService()
                updateCompletamentoRicetta()
            }
            setNegativeButton ("Exit"){ _, _ ->
                stopService()
                findNavController().popBackStack()
            }
            setNeutralButton("Cancel") { _, _ ->
            }
            setCancelable(false)
        }.show()
    }

    /*
    funzione per aggiornare il valore count della ricetta
     */
    private fun updateCompletamentoRicetta() {
        var updateRicetta = Ricetta(
            ricetta.idRicetta,
            ricetta.nomeRicetta,
            ricetta.durata,
            ricetta.livello,
            ricetta.categoria,
            ricetta.descrizione,
            ricetta.ultimaModifica,
            System.currentTimeMillis(),
            ricetta.count + 1,//completata +1 volta
            ricetta.allergeni
        )

        mRicettaViewModel.aggiornaRicetta(updateRicetta)
        findNavController().navigate(R.id.action_svolgiRicettaFragment_to_listFragment)
    }

    //se la seekbar è completa colorala di verde
    private fun seekBarFinished(ctx: Context) {
        val tint = ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.green))
        binding.seekBar.progressTintList = tint
    }
    //se la seekbar non è completata colorala di rosso
    private fun seekBarNotFinishedYed(ctx: Context) {
        val tint = ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.red))
        binding.seekBar.progressTintList = tint
    }

    //mette il bottone stepForward verde
    private fun finishButton(ctx: Context) {
        binding.stepForward.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.green))
        binding.stepForward.text = "Done"
    }

    /*
    funzione che imposta i valori dei bottoni
     */
    private fun updateButtons() {
        if(currentStep == totalStep-1){
            finishButton(requireContext())
        } else {
            binding.stepForward.isEnabled = true
        }
        if(currentStep < 1){
            binding.stepBack.isEnabled = false
        } else {
            binding.stepBack.isEnabled = true
        }
    }
    /*
    funzione che svolge aggiorna lo stepNumber e lo stepText visualizzati in base
    al valore di currentStep
     */
    private fun updateText() {
        binding.stepNumber.text = steps[currentStep].numero.toString()
        binding.stepText.text = steps[currentStep].descrizione
    }

    private fun pause(){
        updateButtonsTimer(pendingState)
        //fermo UI
    }

    //funziona che riporta le informazioni del timer allo stato di partenza
    private fun reset(){
        pendingState = TimerState.IDLE
        pendingTimeLeft = 0L
        updateButtonsTimer(pendingState)
        updateChronometer(pendingTimeLeft)
    }

    private fun resume(){
        updateButtonsTimer(pendingState)
        updateChronometer(pendingTimeLeft)
    }

    //funzione che aggiorna la textview con il formato min:sec
    private fun updateChronometer(timeMs: Long) {
        val totalSec = (timeMs / 1000).toInt()
        val mm = totalSec / 60
        val ss = totalSec % 60
        binding.timer.text = "%02d:%02d".format(mm, ss)   // semplice TextView, non Chronometer
    }

    /*
    imposta i valori del testo dei bottoni timer in base allo stato
     */
    private fun updateButtonsTimer(state: TimerState) {
        binding.startButton.text = when(state) {
            TimerState.IDLE    -> "start"
            TimerState.PAUSED  -> "resume"
            TimerState.RUNNING -> "start"
        }
        binding.stopButton.text = when(state) {
            TimerState.IDLE    -> "stop"
            TimerState.PAUSED  -> "reset"
            TimerState.RUNNING -> "Stop"
        }
    }

    //ferma il servizio
    private fun stopService(){
        if(mBound){
            //scollega
            requireActivity().unbindService(connection)
            mBound = false
            mService?.timer?.cancel()
            mService?.stopSelf() //il servizio si cancella
        }
    }

    override fun onPause() {
        super.onPause()

        //salvo le minime informazioni nello stato persistente per rientrare dalla notifica
        val preferences = requireActivity().getPreferences(MODE_PRIVATE)
        val editor = preferences.edit()

        editor.putLong(ID_RICETTA, idRecipe)
        editor.apply()
    }

    override fun onStop() {
        super.onStop()
        //UI non piu visisible, quindi mi scollego
        if (mBound) {
            requireActivity().unbindService(connection)
            mBound = false
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(CURRENT_STEP, currentStep)

        super.onSaveInstanceState(outState)
    }

}

