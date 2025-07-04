package com.project.roomdb_replica_ufficiale.fragments.list

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.roomdb_replica_ufficiale.R
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta
import com.project.roomdb_replica_ufficiale.data.ricetta.RicettaViewModel
import com.project.roomdb_replica_ufficiale.databinding.FragmentListBinding

/**
 * Fragment che mostra l’elenco delle ricette con:
 *   - ricerca testuale
 *   - filtri (categoria / difficoltà / durata)
 *   - FAB per aggiungere una nuova ricetta
 */
class ListFragment : Fragment() {

    /* --- ViewModel --- */
    private lateinit var mRecipeViewModel: RicettaViewModel

    /* --- Binding e adapter --- */
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private val adapter = ListAdapter()




    /* ----- Stato attuale della ricerca e dei filtri -----*/
    private var currentQuery = ""
    private var currentCategoria: String? = null
    private var currentDifficolta: String? = null
    private var currentDurataMin: Int? = null
    private var currentDurataMax: Int? = null


    /* LiveData dell’ultima query  */
    private var ricetteLiveData: LiveData<List<Ricetta>>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)


        /* ---------- Settaggio RecyclerView -------------------- */
        val recyclerView = _binding!!.recyclerview
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        /* ---------- ViewModel ----------------------------- */
        mRecipeViewModel = ViewModelProvider(this).get(RicettaViewModel::class.java)


        /* ---------- SeekBar che imposta il range max dinamicamente --- */
        var maxTimeSeek = 0
        // Osserva la durata massima
        mRecipeViewModel.durataMassima.observe(viewLifecycleOwner) { maxDurata ->
            // Se il DB è vuoto assegna un default di 300 minuti
            maxTimeSeek = (maxDurata ?: 300).coerceAtLeast(1)
            val seekBar  = binding.seekBarDurata

            // Imposta il nuovo massimo
            seekBar.max = maxTimeSeek

            // Imposta la posizione iniziale
            currentDurataMin = 0
            currentDurataMax = null                  // null = nessun filtro
            seekBar.progress = maxTimeSeek           // cursore in fondo
            binding.txtDurataSelezionata.text = "${maxTimeSeek} min"
        }

        //Listener della SeekBar
        binding.seekBarDurata.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                    // se utente porta il cursore al valore di max -> “nessun filtro”
                    currentDurataMax =
                        if (value == sb?.max) null else value
                    binding.txtDurataSelezionata.text =
                        if (currentDurataMax == null) "${maxTimeSeek} min" else "$value min"
                }

                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {
                    applySearchAndFilters()      // aggiorna la lista
                }
            }
        )

        /* Carica la lista iniziale */
        applySearchAndFilters()






        /* ---------- Ricerca live -------------------------- */
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText.orEmpty()
                applySearchAndFilters()
                return true
            }
        })


        /* ---------- Spinner Categoria -------- */
        binding.spinnerCategoria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected( parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val sel = parent.getItemAtPosition(pos).toString()
                currentCategoria = if (sel == "All") null else sel
                applySearchAndFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        /* ---------- Spinner Difficoltà -------- */
        binding.spinnerDifficolta.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected( parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val sel = parent.getItemAtPosition(pos).toString()
                currentDifficolta = if (sel == "All") null else sel
                applySearchAndFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        /* ---------- Pulsante FAB per aggiungere una nuova ricetta ------- */
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_addFragment)
        }





        /* ---------- Gestione della toggle Card dei filtri su visibile/nascosta -- */
        binding.toggleFiltersButton.setOnClickListener {
            if (binding.filterCard.visibility == View.GONE) {
                binding.filterCard.visibility = View.VISIBLE
                binding.toggleFiltersButton.setImageResource(R.drawable.ic_close) // ad esempio una X
            } else {
                binding.filterCard.visibility = View.GONE
                binding.toggleFiltersButton.setImageResource(R.drawable.ic_filter_list)
            }
        }

        /*--------- Listener per eliminazione -------*/
        adapter.setOnItemActionListener(object : ListAdapter.OnItemActionListener {
            override fun onDeleteClicked(recipe: Ricetta) {
                showDeleteDialog(recipe)
            }
        })

        return binding.root
    }



    private fun showDeleteDialog(recipe: Ricetta) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            mRecipeViewModel.eliminaRicetta(recipe)
            Toast.makeText(requireContext(), "Successfully removed: ${recipe.nomeRicetta}", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete ${recipe.nomeRicetta}?")
        builder.setMessage("Are you sure you want to delete ${recipe.nomeRicetta}?")
        builder.create().show()
    }


    /* ------- Esegue query filtrata e aggiorna Recycler e il contatore -----*/
    private fun applySearchAndFilters() {
        // Ottieni il nuovo LiveData dai filtri
        val liveData = mRecipeViewModel
            .cercaEFiltraRicette(
                "%${currentQuery}%",
                currentCategoria,
                currentDifficolta,
                currentDurataMin,
                currentDurataMax
            )

        // Rimuovi eventuali observer precedenti per evitare ripetizioni
        ricetteLiveData?.removeObservers(viewLifecycleOwner)
        ricetteLiveData = liveData

        // Osserva e aggiorna sia la lista che il contatore
        liveData.observe(viewLifecycleOwner) { ricette ->

            // aggiorna adapter
            adapter.setData(ricette)

            // aggiorna TextView con plurale corretto
            val txt = resources.getQuantityString(
                R.plurals.numero_ricette,
                ricette.size,
                ricette.size
            )
            binding.resultsNumber.text = txt
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun cercaRicetta(query: String) {
        val searchQuery = "%$query%"
        mRecipeViewModel.cercaRicetta(searchQuery).observe(viewLifecycleOwner) { ricette ->
            adapter.setData(ricette)
        }
    }
}