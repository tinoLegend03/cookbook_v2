package com.example.ricettario.fragments

import androidx.core.os.bundleOf
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.project.roomdb_replica_ufficiale.R

/*
FragmentDialog che gestisce l'inserimento dei minuti e dei secondi nel timer
comunica con SvolgiRicettaFragment.
 */
class TimePickerFragment : DialogFragment(){

    //costanti
    companion object {
        const val REQUEST_KEY = "TimePickerDurationRequest"
        const val BUNDLE_KEY = "duration_ms"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //ottengo il layout di fragment
        val view = requireActivity().layoutInflater
            .inflate(R.layout.fragment_time_picker, null)

        //ottengo i NumberPicker uno che corrisponde ai minuti, l'altro ai secondi
        val minPicker = view.findViewById<NumberPicker>(R.id.picker_minuti)
        val secPicker = view.findViewById<NumberPicker>(R.id.picker_secondi)

        //i valori dei minuti e dei secondi
        val valori = Array(60) { i -> "%02d".format(i) }  // ["00", "01", ..., "60"]
        minPicker.minValue = 0
        minPicker.maxValue = 59
        minPicker.displayedValues = valori

        secPicker.minValue = 0
        secPicker.maxValue = 59
        secPicker.displayedValues = valori

        /*
        restituisco un dialog che

        premuto l'azione positiva invia le informazioni attraverso parentFragmentManager. Passa il
        valore dei minuti e dei secondi in millisecondi
        premuto l'azione negativa non restituisce nulla
         */
        return AlertDialog.Builder(requireContext())
            .setTitle("Imposta durata")
            .setView(view)                 // il tuo layout personalizzato
            .setPositiveButton("OK") { _, _ ->
                val durataMs =
                    ((minPicker.value * 60) + secPicker.value) * 1_000L

                parentFragmentManager.setFragmentResult(REQUEST_KEY,
                    bundleOf(BUNDLE_KEY to durataMs))
            }
            .setNegativeButton("Annulla", null)
            .create()                     // <<–– questo è il Dialog da restituire
    }

}