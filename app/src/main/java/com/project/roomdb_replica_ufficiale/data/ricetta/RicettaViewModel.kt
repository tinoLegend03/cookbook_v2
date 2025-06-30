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


/**
 * ViewModel: espone dati LiveData alla UI e lancia coroutine
 * su Dispatchers.IO per operazioni di I/O.
 */
class RicettaViewModel(application: Application): AndroidViewModel(application) {

    /* ---------- DAO & Repository ---------------------------------------- */
    val leggiRicette: LiveData<List<Ricetta>>
    private val repository: RicettaRepository


    init {

        val db = RicettarioDatabase.getDatabase(application)
        val ricettaDao = db.ricettaDao()
        val ingredienteDao = db.ingredienteDao()

        repository = RicettaRepository(ricettaDao, ingredienteDao)
        leggiRicette = repository.leggiRicette
    }

    /* ---------- CRUD BASE ------------------------------------------------ */

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


    /* ---------- Ingredienti & Istruzioni -------------------------------- */

    fun getIngredientiConQuantitaPerRicetta(idRicetta: Long): LiveData<List<IngredienteQuantificato>> {
        return repository.getIngredientiConQuantitaPerRicetta(idRicetta)
    }

    fun inserisciRicettaConIstruzioni(ricetta: Ricetta, istruzioni: List<Istruzione>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.inserisciRicettaConIstruzioni(ricetta, istruzioni)
        }
    }

    fun getRicettaConIstruzioni(idRicetta: Long): LiveData<RicettaConIstruzioni> {
        return repository.getRicettaConIstruzioni(idRicetta)
    }


    /* ---------- Inserimento / update completi --------------------------- */

    fun inserisciRicettaCompleta(
        ricetta: Ricetta,
        istruzioni: List<Istruzione>,
        ingredienti: List<RicettaIngrediente>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.inserisciRicettaCompleta(ricetta, istruzioni, ingredienti)
        }
    }


    fun aggiornaRicettaCompleta(
        ricetta: Ricetta,
        istruzioni: List<Istruzione>,
        ingredientiConQuantita: List<RicettaIngrediente>
    ) = viewModelScope.launch {
        repository.aggiornaRicettaCompleta(ricetta, istruzioni, ingredientiConQuantita)
    }


    /* ---------- Ricerca semplice ---------------------------------------- */

    fun cercaRicetta(searchQuery: String): LiveData<List<Ricetta>>{
        return repository.cercaRicetta(searchQuery).asLiveData()
    }

    /* ---------- Feed home ----------------------------------------------- */

    val ultime10Ricette: LiveData<List<Ricetta>> = repository.leggiUltime10Ricette()


    /* ---------- Ricerca + filtri avanzati ------------------------------- */

    fun cercaEFiltraRicette(
        ricetta: String,
        categoria: String?,
        difficolta: String?,
        durataMin: Int?,
        durataMax: Int?
    ): LiveData<List<Ricetta>> {
        return repository.cercaEFiltraRicette(ricetta, categoria, difficolta, durataMin, durataMax).asLiveData()
    }


    /* ---------- Statistiche --------------------------------------------- */

    val durataMassima: LiveData<Int?> = repository.getDurataMassima()


    suspend fun esisteNomeDuplicato(nome: String, idAttuale: Long): Boolean {
        return repository.esisteNomeDuplicato(nome, idAttuale)
    }
}