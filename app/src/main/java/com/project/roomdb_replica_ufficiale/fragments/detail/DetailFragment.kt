package com.project.roomdb_replica_ufficiale.fragments.detail

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.project.roomdb_replica_ufficiale.data.ricetta.RicettaViewModel
import com.project.roomdb_replica_ufficiale.databinding.FragmentDetailBinding

/**
 * `DetailFragment` è un Fragment che visualizza i dettagli completi di una singola ricetta.
 * Riceve l'oggetto `Ricetta` tramite gli argomenti di navigazione.
 * Utilizza un `ViewModel` per interagire con il database e recuperare dati correlati
 * come istruzioni e ingredienti.
 */
class DetailFragment : Fragment() {

    // `_binding` è una variabile nullable che conterrà l'istanza di View Binding per questo Fragment.
    // Viene dichiarata come nullable per gestire il ciclo di vita della vista del Fragment,
    // dove il binding deve essere nullificato in `onDestroyView` per evitare memory leak.
    private var _binding: FragmentDetailBinding? = null
    // `binding` è una proprietà di sola lettura che fornisce un accesso non-nullable a `_binding`.
    // L'operatore `!!` (not-null asserted call) viene usato qui, il che significa che si assume
    // che `_binding` non sarà mai nullo quando si accede a `binding`. Questo richiede attenzione
    // per assicurarsi che `_binding` sia sempre inizializzato prima dell'uso.
    private val binding get() = _binding!!
    // `ricettaViewModel` è un'istanza del ViewModel responsabile della logica di business
    // e dell'interazione con il repository dei dati (database Room).
    private lateinit var ricettaViewModel: RicettaViewModel

    /**
     * Questo metodo viene chiamato per creare e restituire la gerarchia di viste associata al Fragment.
     * Qui il layout del Fragment viene gonfiato e le viste vengono inizializzate.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Gonfia il layout per questo Fragment utilizzando View Binding.
        // `FragmentDetailBinding.inflate` crea un'istanza della classe di binding
        // che contiene i riferimenti a tutte le viste nel layout `fragment_detail.xml`.
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        // Inizializza il ViewModel. `ViewModelProvider(this)` crea un ViewModel
        // associato al ciclo di vita di questo Fragment.
        ricettaViewModel = ViewModelProvider(this)[RicettaViewModel::class.java]

        // Recupera gli argomenti passati a questo Fragment in modo type-safe utilizzando Safe Args.
        // `DetailFragmentArgs.fromBundle(requireArguments())` estrae l'oggetto `Ricetta`
        // che è stato passato durante la navigazione.
        val args = DetailFragmentArgs.fromBundle(requireArguments())
        val idRicetta = args.currentRecipe.idRicetta

        // Osserva i dati della ricetta con le istruzioni dal ViewModel.
        // `getRicettaConIstruzioni` restituisce un `LiveData` che emette un oggetto
        // contenente la ricetta e la sua lista di istruzioni.
        // `observe(viewLifecycleOwner)` assicura che l'osservatore sia attivo solo quando
        // la vista del Fragment è nel suo stato attivo, prevenendo memory leak.
        ricettaViewModel.getRicettaConIstruzioni(idRicetta).observe(viewLifecycleOwner) { dati ->
            binding.numeroCompletamenti.text= "${dati.ricetta.count} times"
            binding.nomeTextView.text = dati.ricetta.nomeRicetta
            binding.descrizioneContentTextView.text = dati.ricetta.descrizione
            binding.durataContentTextView.text = "${dati.ricetta.durata} min"
            binding.categoriaContentTextView.text = dati.ricetta.categoria
            binding.difficoltaContentTextView.text = dati.ricetta.livello
            // Ordina le istruzioni per numero e le memorizza in una lista.
            val steps = dati.istruzioni.sortedBy { it.numero }
            binding.stepListLayout.removeAllViews()
            // Itera su ogni step e crea dinamicamente una TextView per visualizzarlo.
            steps.forEach {
                val stepView = TextView(requireContext()).apply {
                    text = "${it.numero}. ${it.descrizione}"
                    textSize = 16f
                    setPadding(0, 8, 0, 8)
                }
                binding.stepListLayout.addView(stepView)
            }

            // Recupera la lista degli allergeni dalla ricetta, o una lista vuota se è null.
            val allergeniList = dati.ricetta.allergeni ?: emptyList()
            binding.allergeniLayout.removeAllViews()

            if (allergeniList.isNotEmpty()) {
                // Itera su ogni allergene e crea dinamicamente una TextView per visualizzarlo.
                allergeniList.forEach {
                    val allergeneView = TextView(requireContext()).apply {
                        text = "- $it"
                        textSize = 16f
                        setPadding(0, 4, 0, 4)
                    }
                    binding.allergeniLayout.addView(allergeneView)
                }
            } else {
                // Se non ci sono allergeni specificati, crea una TextView per indicarlo.
                val noAllergeniView = TextView(requireContext()).apply {
                    text = "Nessun allergene specificato"
                    setTextColor(Color.GRAY)
                }
                binding.allergeniLayout.addView(noAllergeniView)
            }
        }
        // Osserva gli ingredienti con quantità per la ricetta dal ViewModel.
        // `getIngredientiConQuantitaPerRicetta` restituisce un `LiveData` che emette una lista
        // di ingredienti con le loro quantità.
        ricettaViewModel.getIngredientiConQuantitaPerRicetta(idRicetta).observe(viewLifecycleOwner) { ingredienti ->
            binding.ingredientListLayout.removeAllViews()
            // Itera su ogni ingrediente e crea dinamicamente una TextView per visualizzarlo.
            ingredienti.forEach {
                val ingredienteView = TextView(requireContext()).apply {
                    text = "- ${it.nomeIngrediente}: ${it.quantita}"
                    textSize = 16f
                    setPadding(0, 8, 0, 8)
                }
                // Aggiunge la TextView dell'ingrediente al layout.
                binding.ingredientListLayout.addView(ingredienteView)
            }
        }

        return binding.root
    }
    /**
     * Questo metodo viene chiamato quando la vista del Fragment sta per essere distrutta.
     * È importante nullificare l'istanza di View Binding (`_binding = null`) qui
     * per rilasciare i riferimenti alle viste e prevenire memory leak, dato che il Fragment
     * può esistere più a lungo della sua vista.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}