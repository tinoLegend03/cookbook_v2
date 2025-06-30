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

class SvolgiRicettaFragment: Fragment() {

    //COSTANTI
    companion object {
        //Add key Strings for use with the Bundle
        const val MBOUND_KEY = "mbound"
        const val CURRENT_STEP = "current_step"

        const val ID_RICETTA = "id_ricetta"

        const val EXTRA_START_TIMER = "extra_start_timer"

    }

    private var _binding: FragmentSvolgiRicettaBinding? = null
    private val binding get() = _binding!!

    private lateinit var mRicettaViewModel: RicettaViewModel
    private lateinit var  ricetta: Ricetta

    private var totalStep: Int = 0
    private var currentStep: Int = 0
    private var valueBar: Int = 0
    var steps = mutableListOf<Istruzione>()
    private var idRecipe: Long = -1

    // Attributi per stato e gestione del timer
    var mService : TimerService? = null
    private var mBound: Boolean = false

    //valori per indipendenza da connessione
    private var pendingTimeLeft: Long = 0L
    private var pendingState: TimerState = TimerState.IDLE

    /* Back-press handling */
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            dialogQuitFragment()
        }
    }


    //CONNESSIONE AL SERVIZIO DEFINITA
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            mService = (service as TimerService.TimerBinder).getService()
            mBound = true

            //Sincronizzo il tempo del servizio a quello visualizzato
            if (mService!!.timeLeft > 0L){
                pendingTimeLeft = mService!!.timeLeft
                pendingState = mService!!.currentState

                if(mService!!.currentState == TimerState.RUNNING) {
                    updateChronometer(pendingTimeLeft)
                }
            } else {
                mService?.timeLeft = pendingTimeLeft
                mService?.currentState = pendingState

                updateChronometer(pendingTimeLeft)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mBound = false
            mService = null
        }

    }

    //RICEZIONE DEL BROADCASTER DEFINITO
    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent) {
            pendingTimeLeft = intent.getLongExtra(TimerService.EXTRA_TIME_LEFT, 0L)


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
                    reset() }
                TimerService.NOTIFY_RESUME -> {
                    val state = intent.getStringExtra(TimerService.EXTRA_STATE) ?: return
                    pendingState = TimerState.valueOf(state)
                    resume() }
                TimerService.ACTION_TICK -> {
                    updateChronometer(pendingTimeLeft)
                }
            }
        }

    }

    //
    override fun onStart(){
        super.onStart()

        val filter = IntentFilter(TimerService.ACTION_TICK).apply{
            addAction(TimerService.NOTIFY_PAUSE)
            addAction(TimerService.NOTIFY_RESET)
            addAction(TimerService.NOTIFY_RESUME)
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
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

        //istanzo la gestione della ricetta da cui prendo gli step
        mRicettaViewModel = ViewModelProvider(this)[RicettaViewModel::class.java]

        //args per accedere al parametro passato
        val args = SvolgiRicettaFragmentArgs.fromBundle(requireArguments())

        if (args.currentRecipe == null) { //accedo dalla notifica, non ho NavArgs
            val preferences = requireActivity().getPreferences(MODE_PRIVATE)

            idRecipe = preferences.getLong(ID_RICETTA, -1) //prendo valore salvato

            pendingTimeLeft = mService?.timeLeft ?: 0
            pendingState = mService?.currentState ?: TimerState.IDLE
        } else {
            idRecipe = args.currentRecipe!!.idRicetta
        }

        //GESTIONE TIMER
        if(savedInstanceState != null){
            mBound = savedInstanceState.getBoolean(MBOUND_KEY)

            if(mBound) {
                val action = TimerService.ACTION_RESUME

                val intent = Intent(ctx, TimerService::class.java).apply {
                    this.action = action
                    putExtra(EXTRA_START_TIMER, pendingTimeLeft)
                }

                ctx.startForegroundService(intent)
                ctx.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
        binding.seekBar.isEnabled = true
        binding.seekBar.setOnClickListener{ }

        if (idRecipe != -1L) {
            mRicettaViewModel.getRicettaConIstruzioni(idRecipe).observe(viewLifecycleOwner) { dati ->
                ricetta = dati.ricetta
                steps = dati.istruzioni.sortedBy { it.numero }.toMutableList()
                binding.nameRecipe.text = ricetta.nomeRicetta

                steps.forEach {
                    binding.stepText.text = getString(R.string.step_message, binding.stepText.text, it.descrizione)
                }
                totalStep = steps.size

                if(savedInstanceState != null) {
                    currentStep = savedInstanceState.getInt(CURRENT_STEP)
                } else {
                    currentStep = 0
                }

                updateButtons()
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

                if(totalStep == 0){
                    Toast.makeText(ctx, "Non presenta step", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_svolgiRicettaFragment_to_listFragment)
                } else {
                    updateText()
                }
            }
        }

        binding.stepForward.setOnClickListener {
            if(currentStep < totalStep - 1){
                currentStep++
                updateText()
                binding.seekBar.progress += valueBar

                if(binding.seekBar.progress == binding.seekBar.max){
                    seekBarFinished(ctx)
                }
            } else if (currentStep == totalStep - 1) {

                if(pendingState != TimerState.IDLE){
                    dialogStopTimer()
                } else {
                    updateCompletamentoRicetta()
                }

            }

            updateButtons()
        }
        binding.stepBack.setOnClickListener {
            if(currentStep > 0){
                currentStep--
                seekBarNotFinishedYed(ctx)
                updateText()
                binding.seekBar.progress -= valueBar
            }
            updateButtons()
        }



        //listener per risultato TIMEPICKER
        setFragmentResultListener(TimePickerFragment.REQUEST_KEY) { requestKey, bundle -> //se arriva la richiesta da TIMEPICKER
            pendingTimeLeft = bundle.getLong(TimePickerFragment.BUNDLE_KEY) //set timeLeft
            pendingState = TimerState.IDLE

            if(mBound) stopService() // Se era connesso, disconnetti il servizio

            updateButtonsTimer(pendingState)
            updateChronometer(pendingTimeLeft)
        }

        //INSERISCO VALORE NEL TIMER
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

        //Listener per il tasto START
        binding.startButton.setOnClickListener {
            if (!mBound) { //non sono connesso
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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

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


    private fun dialogStopTimer() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Vuoi cancellare il timer?")
            setMessage("")
            setPositiveButton("Si") { _, _ ->
                //cancello timer
                stopService()
                updateCompletamentoRicetta()
            }
            setNeutralButton("Cancel") { _, _ ->
            }
            setCancelable(false)
        }.show()
    }
    private fun dialogQuitFragment() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("La ricetta non è completata")
            setMessage("Se esci non completerai la ricetta e si azzererà il timer")
            setPositiveButton("Completata") { _, _ ->
                //cancello timer
                stopService()
                updateCompletamentoRicetta()
            }
            setNegativeButton ("Esci"){ _, _ ->
                stopService()
                findNavController().popBackStack()
            }
            setNeutralButton("Cancel") { _, _ ->
            }
            setCancelable(false)
        }.show()
    }

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
            ricetta.count + 1,
            ricetta.allergeni
        )

        mRicettaViewModel.aggiornaRicetta(updateRicetta)
        findNavController().navigate(R.id.action_svolgiRicettaFragment_to_listFragment)
    }

    private fun seekBarFinished(ctx: Context) {
        val tint = ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.green))
        binding.seekBar.progressTintList = tint
    }
    private fun seekBarNotFinishedYed(ctx: Context) {
        val tint = ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.red))
        binding.seekBar.progressTintList = tint
    }

    private fun finishButton(ctx: Context) {
        // funziona da API 21 in su con MaterialButton
        binding.stepForward.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.green))
    }

    private fun updateButtons() {
    if(currentStep == totalStep-1){
        binding.stepForward.text = "Done"
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
    private fun updateText() {
        binding.stepNumber.text = steps[currentStep].numero.toString()
        binding.stepText.text = steps[currentStep].descrizione
    }

    private fun pause(){
        updateButtonsTimer(pendingState)
        //fermo UI
    }

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

    private fun updateChronometer(timeMs: Long) {
        val totalSec = (timeMs / 1000).toInt()
        val mm = totalSec / 60
        val ss = totalSec % 60
        binding.timer.text = "%02d:%02d".format(mm, ss)   // semplice TextView, non Chronometer
    }

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

    private fun stopService(){
        if(mBound){
            requireActivity().unbindService(connection)
            mBound = false
            mService?.timer?.cancel()
            mService?.stopSelf()
        }
    }

    override fun onPause() {
        super.onPause()

        val preferences = requireActivity().getPreferences(MODE_PRIVATE)
        val editor = preferences.edit()

        editor.putLong(ID_RICETTA, idRecipe)
        editor.apply()
    }

    override fun onStop() {
        super.onStop()
        if (mBound) {
            requireActivity().unbindService(connection)
            mBound = false        // **solo** unbind, NON stopSelf()
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(CURRENT_STEP, currentStep)

        super.onSaveInstanceState(outState)
    }

}

