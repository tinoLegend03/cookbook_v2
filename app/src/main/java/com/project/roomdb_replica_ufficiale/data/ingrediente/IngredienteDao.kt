package com.project.roomdb_replica_ufficiale.data.ingrediente

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO per l’entità Ingrediente.
 * Espone operazioni CRUD basilari.
 */
@Dao
interface IngredienteDao {

    /** Inserisce un nuovo ingrediente (ignora se esiste già). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun nuovoIngrediente(ingrediente: Ingrediente)

    /** Ritorna tutti gli ingredienti in ordine alfabetico come LiveData. */
    @Query("SELECT * FROM tab_ingredienti ORDER BY nomeIngrediente ASC")
    fun leggiIngredienti(): LiveData<List<Ingrediente>>

    /** Inserimento “silenzioso” usato da altre tabelle ponte. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserisciIngrediente(ingrediente: Ingrediente)
}