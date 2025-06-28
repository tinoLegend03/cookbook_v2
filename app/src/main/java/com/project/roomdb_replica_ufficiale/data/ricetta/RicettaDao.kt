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


@Dao
interface RicettaDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun nuovaRicetta(ricetta: Ricetta)

    @Update
    suspend fun aggiornaRicetta(ricetta: Ricetta)

    @Delete
    suspend fun eliminaRicetta(ricetta: Ricetta)

    @Query("SELECT * FROM tab_ricette ORDER BY nomeRicetta ASC")
    fun leggiRicette(): LiveData<List<Ricetta>>

    @Query("SELECT i.nomeIngrediente, ri.quantita FROM tab_ricetta_ingrediente AS ri INNER JOIN tab_ingredienti AS i ON ri.nomeIngrediente = i.nomeIngrediente WHERE ri.nomeRicetta = :nomeRicetta")
    fun getIngredientiConQuantitaPerRicetta(nomeRicetta: String): LiveData<List<IngredienteQuantificato>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserisciIstruzione(istruzione: Istruzione)

    @Transaction
    @Query("SELECT * FROM tab_ricette WHERE nomeRicetta = :nome")
    fun getRicettaConIstruzioni(nome: String): LiveData<RicettaConIstruzioni>

    @Transaction
    suspend fun inserisciRicettaCompleta(
        ricetta: Ricetta,
        istruzioni: List<Istruzione>,
        //ingredienti: List<RicettaIngrediente>
        ingredientiConQuantita: List<RicettaIngrediente>
    ) {
        nuovaRicetta(ricetta)
        istruzioni.forEach { nuovaIstruzione(it) }
        ingredientiConQuantita.forEach { inserisciRicettaIngrediente(it) }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun nuovaIstruzione(istruzione: Istruzione)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserisciRicettaIngrediente(crossRef: RicettaIngrediente)

    @Query("SELECT * FROM tab_ricette WHERE nomeRicetta LIKE :searchQuery")
    fun cercaRicetta(searchQuery: String): Flow<List<Ricetta>>


    @Query("SELECT * FROM tab_ricette ORDER BY ultimaModifica DESC LIMIT 10")
    fun leggiUltime10Ricette(): LiveData<List<Ricetta>>

    @Transaction
    suspend fun aggiornaRicettaCompleta(
        ricetta: Ricetta,
        nuoveIstruzioni: List<Istruzione>,
        nuoviIngredienti: List<RicettaIngrediente>
    ) {
        // Aggiorna la ricetta
        aggiornaRicetta(ricetta)

        // Elimina istruzioni e ingredienti esistenti
        eliminaIstruzioniPerRicetta(ricetta.nomeRicetta)
        eliminaIngredientiPerRicetta(ricetta.nomeRicetta)


        nuoveIstruzioni.forEach { nuovaIstruzione(it) }

        // Inserisci ogni ingrediente se non già presente
        nuoviIngredienti.forEach {
            // Inserisci l'ingrediente di base, se non esiste
            inserisciIngredienteSeNonEsiste(Ingrediente(it.nomeIngrediente))
            inserisciRicettaIngrediente(it)
        }
    }

    @Query("DELETE FROM tab_istruzioni WHERE ricetta = :nomeRicetta")
    suspend fun eliminaIstruzioniPerRicetta(nomeRicetta: String)

    @Query("DELETE FROM tab_ricetta_ingrediente WHERE nomeRicetta = :nomeRicetta")
    suspend fun eliminaIngredientiPerRicetta(nomeRicetta: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserisciIngredienteSeNonEsiste(ingrediente: Ingrediente)


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

    @Query("SELECT MAX(durata) FROM tab_ricette")
    fun getDurataMassima(): LiveData<Int?>
}

