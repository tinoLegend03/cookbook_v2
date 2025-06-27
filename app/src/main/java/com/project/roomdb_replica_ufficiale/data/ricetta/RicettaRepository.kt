package com.project.roomdb_replica_ufficiale.data.ricetta

import androidx.core.app.Person
import androidx.lifecycle.LiveData
import com.project.roomdb_replica_ufficiale.data.ingrediente.Ingrediente
import com.project.roomdb_replica_ufficiale.data.ingrediente.IngredienteDao
import com.project.roomdb_replica_ufficiale.data.ingrediente.IngredienteQuantificato
import com.project.roomdb_replica_ufficiale.data.istruzione.Istruzione
import com.project.roomdb_replica_ufficiale.relations.RicettaConIstruzioni
import com.project.roomdb_replica_ufficiale.relations.ricettaIngredienteRelation.RicettaIngrediente
import kotlinx.coroutines.flow.Flow

class RicettaRepository(private val ricettaDao: RicettaDao, private val ingredienteDao: IngredienteDao) {

    val leggiRicette: LiveData<List<Ricetta>> = ricettaDao.leggiRicette()


    suspend fun nuovaRicetta(ricetta: Ricetta){
        ricettaDao.nuovaRicetta(ricetta)
    }

    suspend fun aggiornaRicetta(ricetta: Ricetta){
        ricettaDao.aggiornaRicetta(ricetta)
    }

    suspend fun eliminaRicetta(ricetta: Ricetta){
        ricettaDao.eliminaRicetta(ricetta)
    }

    fun getIngredientiConQuantitaPerRicetta(nomeRicetta: String): LiveData<List<IngredienteQuantificato>> {
        return ricettaDao.getIngredientiConQuantitaPerRicetta(nomeRicetta)
    }

    suspend fun inserisciRicettaConIstruzioni(ricetta: Ricetta, istruzioni: List<Istruzione>) {
        ricettaDao.nuovaRicetta(ricetta)
        istruzioni.forEach {
            ricettaDao.inserisciIstruzione(it)
        }
    }

    fun getRicettaConIstruzioni(nomeRicetta: String): LiveData<RicettaConIstruzioni> {
        return ricettaDao.getRicettaConIstruzioni(nomeRicetta)
    }

    suspend fun inserisciRicettaCompleta(
        ricetta: Ricetta,
        istruzioni: List<Istruzione>,
        ingredienti: List<RicettaIngrediente>
    ) {
        //ricettaDao.inserisciRicettaCompleta(ricetta, istruzioni, ingredienti)
        //ricettaDao.nuovaRicetta(ricetta)

        ///////////
        //istruzioni.forEach { ricettaDao.nuovaIstruzione(it) }

        ingredienti.forEach {
            ingredienteDao.inserisciIngrediente(Ingrediente(it.nomeIngrediente)) // CORRETTO QUI
            //ricettaDao.inserisciRicettaIngrediente(it)
        }

        ricettaDao.inserisciRicettaCompleta(ricetta, istruzioni, ingredienti)
    }

    fun cercaRicetta(searchQuery: String): Flow<List<Ricetta>>{
        return ricettaDao.cercaRicetta(searchQuery)
    }

    fun leggiUltime10Ricette(): LiveData<List<Ricetta>> {
        return ricettaDao.leggiUltime10Ricette()
    }

    suspend fun aggiornaRicettaCompleta(
        ricetta: Ricetta,
        istruzioni: List<Istruzione>,
        ingredientiConQuantita: List<RicettaIngrediente>
    ) {
        ricettaDao.aggiornaRicettaCompleta(ricetta, istruzioni, ingredientiConQuantita)
    }

    fun cercaEFiltraRicette(
        ricetta: String,
        categoria: String?,
        difficolta: String?,
        durataMin: Int?,
        durataMax: Int?
    ): Flow<List<Ricetta>> {
        return ricettaDao.cercaEFiltraRicette(ricetta, categoria, difficolta, durataMin, durataMax)
    }
}