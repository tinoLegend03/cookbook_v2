package com.project.roomdb_replica_ufficiale.data.ingrediente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.project.roomdb_replica_ufficiale.data.RicettarioDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//The ViewModel's role is to provide data to the UI and survive configuration changes.
//A ViewModel acts as a communication center between the Repository and the UI.
class IngredienteViewModel(application: Application): AndroidViewModel(application) {

    val leggiIngredienti: LiveData<List<Ingrediente>>
    private val repository: IngredienteRepository

    init {
        val ingredienteDao = RicettarioDatabase.getDatabase(application).ingredienteDao()
        repository = IngredienteRepository(ingredienteDao)
        leggiIngredienti = repository.leggiIngredienti
    }

    fun nuovoIngrediente(ingrediente: Ingrediente){
        viewModelScope.launch(Dispatchers.IO) {
            repository.nuovoIngrediente(ingrediente)
        }
    }

}