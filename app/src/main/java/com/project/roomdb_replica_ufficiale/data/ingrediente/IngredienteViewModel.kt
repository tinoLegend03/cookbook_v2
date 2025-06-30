package com.project.roomdb_replica_ufficiale.data.ingrediente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.project.roomdb_replica_ufficiale.data.RicettarioDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel per la lista degli Ingredienti.
 * Converte le coroutine di repository in chiamate sicure per la UI.
 */
class IngredienteViewModel(application: Application): AndroidViewModel(application) {

    /* Live stream osservato dalla UI */
    val leggiIngredienti: LiveData<List<Ingrediente>>

    private val repository: IngredienteRepository

    init {
        // ottieni DAO dal database Singleton
        val ingredienteDao = RicettarioDatabase.getDatabase(application).ingredienteDao()
        repository = IngredienteRepository(ingredienteDao)
        leggiIngredienti = repository.leggiIngredienti
    }

    /** Inserisce un nuovo ingrediente lanciando una coroutine su IO. */
    fun nuovoIngrediente(ingrediente: Ingrediente){
        viewModelScope.launch(Dispatchers.IO) {
            repository.nuovoIngrediente(ingrediente)
        }
    }

}