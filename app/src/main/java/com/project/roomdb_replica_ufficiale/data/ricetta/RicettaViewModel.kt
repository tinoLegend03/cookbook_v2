package com.project.roomdb_replica_ufficiale.data.ricetta

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.project.roomdb_replica_ufficiale.data.RicettarioDatabase
import com.project.roomdb_replica_ufficiale.data.ingrediente.IngredienteQuantificato
import com.project.roomdb_replica_ufficiale.data.istruzione.Istruzione
import com.project.roomdb_replica_ufficiale.relations.RicettaConIstruzioni
import com.project.roomdb_replica_ufficiale.relations.ricettaIngredienteRelation.RicettaIngrediente
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


//The ViewModel's role is to provide data to the UI and survive configuration changes.
//A ViewModel acts as a communication center between the Repository and the UI.
class RicettaViewModel(application: Application): AndroidViewModel(application) {

    val leggiRicette: LiveData<List<Ricetta>>
    private val repository: RicettaRepository


    init {
        //val ricettaDao = RicettarioDatabase.getDatabase(application).ricettaDao()
        val db = RicettarioDatabase.getDatabase(application)
        val ricettaDao = db.ricettaDao()
        val ingredienteDao = db.ingredienteDao()

        repository = RicettaRepository(ricettaDao, ingredienteDao)
        leggiRicette = repository.leggiRicette
    }

    fun nuovaRicetta(ricetta: Ricetta){
        viewModelScope.launch(Dispatchers.IO) {
            repository.nuovaRicetta(ricetta)
        }
    }

    fun aggiornaRicetta(ricetta: Ricetta){
        viewModelScope.launch(Dispatchers.IO) {
            repository.aggiornaRicetta(ricetta)
        }
    }

    fun eliminaRicetta(ricetta: Ricetta){
        viewModelScope.launch(Dispatchers.IO) {
            repository.eliminaRicetta(ricetta)
        }
    }

    fun getIngredientiConQuantitaPerRicetta(nomeRicetta: String): LiveData<List<IngredienteQuantificato>> {
        return repository.getIngredientiConQuantitaPerRicetta(nomeRicetta)
    }

    fun inserisciRicettaConIstruzioni(ricetta: Ricetta, istruzioni: List<Istruzione>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.inserisciRicettaConIstruzioni(ricetta, istruzioni)
        }
    }

    fun getRicettaConIstruzioni(nomeRicetta: String): LiveData<RicettaConIstruzioni> {
        return repository.getRicettaConIstruzioni(nomeRicetta)
    }

    fun inserisciRicettaCompleta(
        ricetta: Ricetta,
        istruzioni: List<Istruzione>,
        ingredienti: List<RicettaIngrediente>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.inserisciRicettaCompleta(ricetta, istruzioni, ingredienti)
        }
    }

    fun cercaRicetta(searchQuery: String): LiveData<List<Ricetta>>{
        return repository.cercaRicetta(searchQuery).asLiveData()
    }

    val ultime10Ricette: LiveData<List<Ricetta>> = repository.leggiUltime10Ricette()

    fun aggiornaRicettaCompleta(
        ricetta: Ricetta,
        istruzioni: List<Istruzione>,
        ingredientiConQuantita: List<RicettaIngrediente>
    ) = viewModelScope.launch {
        repository.aggiornaRicettaCompleta(ricetta, istruzioni, ingredientiConQuantita)
    }

    fun cercaEFiltraRicette(
        ricetta: String,
        categoria: String?,
        difficolta: String?,
        durataMin: Int?,
        durataMax: Int?
    ): LiveData<List<Ricetta>> {
        return repository.cercaEFiltraRicette(ricetta, categoria, difficolta, durataMin, durataMax).asLiveData()
    }
}