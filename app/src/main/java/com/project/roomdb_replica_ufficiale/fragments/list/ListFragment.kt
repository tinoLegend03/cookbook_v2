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
        mRecipeViewModel.leggiRicette.observe(viewLifecycleOwner, Observer { ricetta ->
            adapter.setData(ricetta)
        })

        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_addFragment)
        }


        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    cercaRicetta(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    cercaRicetta(newText)
                } else {
                    // Se il campo è vuoto, mostra tutte le ricette
                    mRecipeViewModel.leggiRicette.observe(viewLifecycleOwner) { ricette ->
                        adapter.setData(ricette)
                    }
                }
                return true
            }
        })

        binding.searchView.queryHint = "Cerca ricetta..."


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