package com.project.roomdb_replica_ufficiale.data.ricetta


import androidx.lifecycle.LiveData
import com.project.roomdb_replica_ufficiale.data.ingrediente.Ingrediente
import com.project.roomdb_replica_ufficiale.data.ingrediente.IngredienteDao
import com.project.roomdb_replica_ufficiale.data.ingrediente.IngredienteQuantificato
import com.project.roomdb_replica_ufficiale.data.istruzione.Istruzione
import com.project.roomdb_replica_ufficiale.relations.RicettaConIstruzioni
import com.project.roomdb_replica_ufficiale.relations.ricettaIngredienteRelation.RicettaIngrediente
import kotlinx.coroutines.flow.Flow


/**
 * Repository: astrae l’accesso ai vari DAO.
 */
class RicettaRepository(private val ricettaDao: RicettaDao, private val ingredienteDao: IngredienteDao) {

    /* Live stream di tutte le ricette */
    val leggiRicette: LiveData<List<Ricetta>> = ricettaDao.leggiRicette()


    /* ---------- CRUD Ricetta base ----------------------------------------- */

    suspend fun nuovaRicetta(ricetta: Ricetta){
        ricettaDao.nuovaRicetta(ricetta)
    }

    suspend fun aggiornaRicetta(ricetta: Ricetta){
        ricettaDao.aggiornaRicetta(ricetta)
    }

    suspend fun eliminaRicetta(ricetta: Ricetta){
        ricettaDao.eliminaRicetta(ricetta)
    }


    /* ---------- Join / relazioni ------------------------------------------ */

    fun getIngredientiConQuantitaPerRicetta(idRicetta: Long): LiveData<List<IngredienteQuantificato>> {
        return ricettaDao.getIngredientiConQuantitaPerRicetta(idRicetta)
    }


    /* ---------- Inserimento ricetta + istruzioni --------------- */

    suspend fun inserisciRicettaConIstruzioni(ricetta: Ricetta, istruzioni: List<Istruzione>) {
        ricettaDao.nuovaRicetta(ricetta)
        istruzioni.forEach {
            ricettaDao.inserisciIstruzione(it)
        }
    }

    fun getRicettaConIstruzioni(idRicetta: Long): LiveData<RicettaConIstruzioni> {
        return ricettaDao.getRicettaConIstruzioni(idRicetta)
    }

    /* ---------- Inserimento completo ricetta + istr + ing ----------------- */

    suspend fun inserisciRicettaCompleta(
        ricetta: Ricetta,
        istruzioni: List<Istruzione>,
        ingredienti: List<RicettaIngrediente>
    ) {
        ingredienti.forEach {
            ingredienteDao.inserisciIngrediente(Ingrediente(it.nomeIngrediente))
        }

        ricettaDao.inserisciRicettaCompleta(ricetta, istruzioni, ingredienti)
    }


    /* ---------- Ricerca -------------------------------------------- */

    fun cercaRicetta(searchQuery: String): Flow<List<Ricetta>>{
        return ricettaDao.cercaRicetta(searchQuery)
    }

    fun leggiUltime10Ricette(): LiveData<List<Ricetta>> {
        return ricettaDao.leggiUltime10Ricette()
    }


    /* ---------- Update completo ------------------------------------------- */

    suspend fun aggiornaRicettaCompleta(
        ricetta: Ricetta,
        istruzioni: List<Istruzione>,
        ingredientiConQuantita: List<RicettaIngrediente>
    ) {
        ricettaDao.aggiornaRicettaCompleta(ricetta, istruzioni, ingredientiConQuantita)
    }


    /* ---------- Ricerca + filtri avanzati --------------------------------- */

    fun cercaEFiltraRicette(
        ricetta: String,
        categoria: String?,
        difficolta: String?,
        durataMin: Int?,
        durataMax: Int?
    ): Flow<List<Ricetta>> {
        return ricettaDao.cercaEFiltraRicette(ricetta, categoria, difficolta, durataMin, durataMax)
    }

    /* ---------- Statistica ------------------------------------------------- */

    fun getDurataMassima() = ricettaDao.getDurataMassima()



    suspend fun esisteNomeDuplicato(nome: String, idAttuale: Long): Boolean {
        return ricettaDao.contaRicetteConNome(nome, idAttuale) > 0
    }
}