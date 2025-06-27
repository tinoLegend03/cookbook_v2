package com.project.roomdb_replica_ufficiale.fragments.list

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.SearchEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.roomdb_replica_ufficiale.R
import com.project.roomdb_replica_ufficiale.adapter.RecentRecipesAdapter
import com.project.roomdb_replica_ufficiale.data.istruzione.IstruzioneViewModel
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta
import com.project.roomdb_replica_ufficiale.data.ricetta.RicettaViewModel
import com.project.roomdb_replica_ufficiale.databinding.FragmentListBinding


class ListFragment : Fragment() {

    private lateinit var mRecipeViewModel: RicettaViewModel

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private val adapter = ListAdapter()
    private var listaRicette: List<Ricetta> = emptyList()
    // Stato attuale della ricerca e dei filtri
    private var currentQuery = ""
    private var currentCategoria: String? = null
    private var currentDifficolta: String? = null
    private var currentDurataMin: Int? = null
    private var currentDurataMax: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)

        //setHasOptionsMenu(true)

        //Recyclerview
        //val adapter = ListAdapter()
        val recyclerView = _binding!!.recyclerview
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        //UserViewModel
        mRecipeViewModel = ViewModelProvider(this).get(RicettaViewModel::class.java)
        /*mRecipeViewModel.leggiRicette.observe(viewLifecycleOwner, Observer { ricetta ->
            //adapter.setData(ricetta)
            listaRicette = ricetta
            aggiornaListaFiltrata()
        })*/
        applySearchAndFilters()

        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_addFragment)
        }




        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText.orEmpty()
                applySearchAndFilters()
                return true
            }
        })

        /*binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    //cercaRicetta(query)
                    aggiornaListaFiltrata()
                    return true
                }
                return true
            }

            /*override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    cercaRicetta(newText)
                } else {
                    // Se il campo è vuoto, mostra tutte le ricette
                    mRecipeViewModel.leggiRicette.observe(viewLifecycleOwner) { ricette ->
                        adapter.setData(ricette)
                    }
                }
                return true
            }*/

            override fun onQueryTextChange(newText: String?): Boolean {
                aggiornaListaFiltrata()
                return true
            }
        })*/

        // categoria
        binding.spinnerCategoria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected( parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val sel = parent.getItemAtPosition(pos).toString()
                currentCategoria = if (sel == "Tutte") null else sel
                applySearchAndFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // difficoltà
        binding.spinnerDifficolta.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected( parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val sel = parent.getItemAtPosition(pos).toString()
                currentDifficolta = if (sel == "Tutte") null else sel
                applySearchAndFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // DURATA  – SeekBar che imposta SOLO il valore massimo
        binding.seekBarDurata.apply {
            progress = 180                          // posizione iniziale = “nessun limite”
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?, value: Int, fromUser: Boolean
                ) {
                    // se vuoi mostrare il numero in un TextView:
                    // binding.txtDurataSelezionata.text = "$value min"
                    currentDurataMin = 0           // manteniamo min fisso
                    currentDurataMax = if (value == max) null else value
                    binding.txtDurataSelezionata.text =
                        if (value == max) "∞ min" else "$value min"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) { }
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    applySearchAndFilters()        // aggiorna la lista SOLO quando l’utente rilascia
                }
            })
        }



        /*
        val filtroListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                aggiornaListaFiltrata()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerDurata.onItemSelectedListener = filtroListener
        binding.spinnerCategoria.onItemSelectedListener = filtroListener
        binding.spinnerDifficolta.onItemSelectedListener = filtroListener

        binding.searchView.queryHint = "Cerca ricetta..."

        val durataOptions = listOf("Tutte", "< 30 min", "30 - 60 min", "> 60 min")
        val categoriaOptions = listOf("Tutte", "Antipasto", "Primo", "Secondo", "Dolce") // modifica secondo il tuo schema
        val difficoltaOptions = listOf("Tutte", "Facile", "Media", "Difficile")

        val durataAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, durataOptions)
        binding.spinnerDurata.adapter = durataAdapter

        val categoriaAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoriaOptions)
        binding.spinnerCategoria.adapter = categoriaAdapter

        val difficoltaAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, difficoltaOptions)
        binding.spinnerDifficolta.adapter = difficoltaAdapter*/


        //binding.searchView.onActionViewExpanded()

        /*binding.searchView.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.searchView.findFocus(), InputMethodManager.SHOW_IMPLICIT)
        */

        // Listener per eliminazione
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

    private fun applySearchAndFilters() {
        mRecipeViewModel
            .cercaEFiltraRicette(
                "%${currentQuery}%",
                currentCategoria,
                currentDifficolta,
                currentDurataMin,
                currentDurataMax
            )
            .observe(viewLifecycleOwner) { ricette ->
                adapter.setData(ricette)
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

    /*private fun aggiornaListaFiltrata() {
        val query = binding.searchView.query?.toString()?.trim() ?: ""
        val durataFiltro = binding.spinnerDurata.selectedItem.toString()
        val categoriaFiltro = binding.spinnerCategoria.selectedItem.toString()
        val difficoltaFiltro = binding.spinnerDifficolta.selectedItem.toString()

        val filtrate = listaRicette.filter { ricetta ->
            val corrispondeNome = query.isEmpty() || ricetta.nomeRicetta.contains(query, ignoreCase = true)

            val corrispondeDurata = when (durataFiltro) {
                "< 30 min" -> ricetta.durata < 30
                "30 - 60 min" -> ricetta.durata in 30..60
                "> 60 min" -> ricetta.durata > 60
                else -> true
            }

            val corrispondeCategoria = categoriaFiltro == "Tutte" || ricetta.categoria == categoriaFiltro
            val corrispondeDifficolta = difficoltaFiltro == "Tutte" || ricetta.livello == difficoltaFiltro

            corrispondeNome && corrispondeDurata && corrispondeCategoria && corrispondeDifficolta
        }

        adapter.setData(filtrate)
    }*/




    /*override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)

        val search = menu?.findItem(R.id.menu_search)
        val searchView = search?.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)

    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        /*if(query != null){
            cercaRicetta(query)
        }*/
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if(query != null){
            cercaRicetta(query)
        }
        return true
    }

    private fun cercaRicetta(query: String){
        val searchQuery = "%$query%"

        mRecipeViewModel.cercaRicetta(searchQuery).observe(this, { list ->
            list.let{
                adapter.setData(it)
            }
        })
    }*/
}