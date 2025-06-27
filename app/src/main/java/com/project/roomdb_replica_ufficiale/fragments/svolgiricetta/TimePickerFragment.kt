package com.example.ricettario.fragments

import androidx.core.os.bundleOf
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.project.roomdb_replica_ufficiale.MainActivity
import com.project.roomdb_replica_ufficiale.R
import com.project.roomdb_replica_ufficiale.databinding.FragmentTimePickerBinding

class TimePickerFragment : DialogFragment(){

    companion object {
        const val REQUEST_KEY = "TimePickerDurationRequest"
        const val BUNDLE_KEY = "duration_ms"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater
            .inflate(R.layout.fragment_time_picker, null)

        val minPicker = view.findViewById<NumberPicker>(R.id.picker_minuti)
        val secPicker = view.findViewById<NumberPicker>(R.id.picker_secondi)

        val valori = Array(60) { i -> "%02d".format(i) }  // ["00", "01", ..., "60"]
        minPicker.minValue = 0
        minPicker.maxValue = 59
        minPicker.displayedValues = valori

        secPicker.minValue = 0
        secPicker.maxValue = 59
        secPicker.displayedValues = valori

        minPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            val valPicker = minPicker.value
            Log.d("PICKER_MIN", "$valPicker")

        }

        return AlertDialog.Builder(requireContext())   // o AlertDialog.Builder
            .setTitle("Imposta durata")
            .setView(view)                 // il tuo layout personalizzato
            .setPositiveButton("OK") { _, _ ->
                val durataMs =
                    ((minPicker.value * 60) + secPicker.value) * 1_000L

                parentFragmentManager.setFragmentResult(
                    REQUEST_KEY,                      // costante tua
                    bundleOf(BUNDLE_KEY to durataMs)  // idem
                )
            }
            .setNegativeButton("Annulla", null)
            .create()                     // <<–– questo è il Dialog da restituire
    }

}