package com.project.roomdb_replica_ufficiale.data.ricetta

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.project.roomdb_replica_ufficiale.data.ingrediente.Ingrediente
import com.project.roomdb_replica_ufficiale.data.ingrediente.IngredienteQuantificato
import com.project.roomdb_replica_ufficiale.data.istruzione.Istruzione
import com.project.roomdb_replica_ufficiale.relations.RicettaConIstruzioni
import com.project.roomdb_replica_ufficiale.relations.ricettaIngredienteRelation.RicettaIngrediente
import kotlinx.coroutines.flow.Flow

/**
 * DAO principale per tutte le operazioni su Ricette, Istruzioni e tabelle ponte.
 * Contiene anche metodi @Transaction per operazioni “atomiche”.
 */
@Dao
interface RicettaDao {

    /* ---------- CRUD Ricetta ------------------------------------------------ */

    /** Inserisce una nuova ricetta. Restituisce l’id autogenerato oppure -1 se esiste già. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun nuovaRicetta(ricetta: Ricetta): Long

    /** Aggiorna la ricetta */
    @Update
    suspend fun aggiornaRicetta(ricetta: Ricetta)

    /** Elimina la ricetta
     * L'eliminazione a cascata su istruzioni/ingredienti è garantita dai FK.
     */
    @Delete
    suspend fun eliminaRicetta(ricetta: Ricetta)

    /** Elenco alfabetico di tutte le ricette. */
    @Query("SELECT * FROM tab_ricette ORDER BY nomeRicetta ASC")
    fun leggiRicette(): LiveData<List<Ricetta>>




    /* ---------- JOIN Ingrediente+Quantità ----------------------------------- */

    /**
     * Restituisce gli ingredienti di una ricetta con la relativa quantità
     * usando una INNER JOIN tra tab_ricetta_ingrediente e tab_ingredienti.
     */
    @Query("SELECT i.nomeIngrediente, ri.quantita FROM tab_ricetta_ingrediente AS ri INNER JOIN tab_ingredienti AS i ON ri.nomeIngrediente = i.nomeIngrediente WHERE ri.idRicetta = :idRicetta")
    fun getIngredientiConQuantitaPerRicetta(idRicetta: Long): LiveData<List<IngredienteQuantificato>>


    /* ---------- Istruzioni --------------------------------------------------- */


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserisciIstruzione(istruzione: Istruzione)

    /** Ricava la ricetta e relative istruzioni. */
    @Transaction
    @Query("SELECT * FROM tab_ricette WHERE idRicetta = :id")
    fun getRicettaConIstruzioni(id: Long): LiveData<RicettaConIstruzioni>



    /* ---------- Inserimento completo (Ricetta + istr + ing) ------------------ */

    /**
     * Inserisce la ricetta, le istruzioni e gli ingredienti in un’unica transazione.
     * Se la ricetta esiste già (id = -1) recupera l’id dal nome.
     */
    @Transaction
    suspend fun inserisciRicettaCompleta(
        ricetta: Ricetta,
        istruzioni: List<Istruzione>,
        ingredientiConQuantita: List<RicettaIngrediente>
    ) {
        val id = nuovaRicetta(ricetta)
        val idRicetta = if (id != -1L) id else getIdByNome(ricetta.nomeRicetta)
        // copia l’id corretto prima di inserire
        istruzioni.forEach { nuovaIstruzione(it.copy(idRicetta = idRicetta)) }
        ingredientiConQuantita.forEach { inserisciRicettaIngrediente(it.copy(idRicetta = idRicetta)) }
    }


    /** Ricava l’id a partire dal nome (usato per ricette già presenti). */
    @Query("SELECT idRicetta FROM tab_ricette WHERE nomeRicetta = :nome LIMIT 1")
    suspend fun getIdByNome(nome: String): Long


    /* ---------- Inserimento singolo relazione e istruzione ------------------ */


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun nuovaIstruzione(istruzione: Istruzione)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserisciRicettaIngrediente(crossRef: RicettaIngrediente)


    /* ---------- Ricerca e feed ---------------------------------------------- */

    @Query("SELECT * FROM tab_ricette WHERE nomeRicetta LIKE :searchQuery")
    fun cercaRicetta(searchQuery: String): Flow<List<Ricetta>>

    /** Ultime 10 ricette modificate. */
    @Query("SELECT * FROM tab_ricette ORDER BY ultimaModifica DESC LIMIT 10")
    fun leggiUltime10Ricette(): LiveData<List<Ricetta>>



    /* ---------- Update completo (ricetta + tutte le relazioni) -------------- */

    @Transaction
    suspend fun aggiornaRicettaCompleta(
        ricetta: Ricetta,
        nuoveIstruzioni: List<Istruzione>,
        nuoviIngredienti: List<RicettaIngrediente>
    ) {
        // update base
        aggiornaRicetta(ricetta)

        // Elimina istruzioni e ingredienti esistenti
        eliminaIstruzioniPerRicetta(ricetta.idRicetta)
        eliminaIngredientiPerRicetta(ricetta.idRicetta)

        //reinserisci istruzioni
        nuoveIstruzioni.forEach { nuovaIstruzione(it) }

        // Inserisci ogni ingrediente se non già presente
        nuoviIngredienti.forEach {
            inserisciIngredienteSeNonEsiste(Ingrediente(it.nomeIngrediente))
            inserisciRicettaIngrediente(it)
        }
    }



    /* ---------- Helper per delete -------------------------------------- */


    @Query("DELETE FROM tab_istruzioni WHERE idRicetta = :idRicetta")
    suspend fun eliminaIstruzioniPerRicetta(idRicetta: Long)

    @Query("DELETE FROM tab_ricetta_ingrediente WHERE idRicetta = :idRicetta")
    suspend fun eliminaIngredientiPerRicetta(idRicetta: Long)


    /* ---------- Inserimento Ingrediente se assente -------------------------- */

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserisciIngredienteSeNonEsiste(ingrediente: Ingrediente)




    /* ---------- Filtro avanzato --------------------------------------------- */

    /**
     * Ricerca testuale + filtro su categoria, difficoltà e range di durata.
     * Se si passa null a un parametro si ignora quel filtro.
     */
    @Query("""
    SELECT * FROM tab_ricette 
    WHERE (:categoria IS NULL OR categoria = :categoria)
      AND (:difficolta IS NULL OR livello = :difficolta)
      AND (:durataMin IS NULL OR durata >= :durataMin)
      AND (:durataMax IS NULL OR durata <= :durataMax)
      AND nomeRicetta LIKE :searchQuery
    ORDER BY nomeRicetta ASC
""")
    fun cercaEFiltraRicette(
        searchQuery: String,
        categoria: String?,
        difficolta: String?,
        durataMin: Int?,
        durataMax: Int?
    ): Flow<List<Ricetta>>


    /* ---------- Statistiche ------------------------------------------------- */

    /** Durata massima di tutte le ricette per lo slider. */
    @Query("SELECT MAX(durata) FROM tab_ricette")
    fun getDurataMassima(): LiveData<Int?>



    @Query("SELECT COUNT(*) FROM tab_ricette WHERE nomeRicetta = :nome AND idRicetta != :id")
    suspend fun contaRicetteConNome(nome: String, id: Long): Int
}

