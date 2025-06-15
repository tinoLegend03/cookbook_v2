package com.project.roomdb_replica_ufficiale.data.ingrediente

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

//Contiene i metodi per accedere al db
@Dao
interface IngredienteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun nuovoIngrediente(ingrediente: Ingrediente)

    @Query("SELECT * FROM tab_ingredienti ORDER BY nomeIngrediente ASC")
    fun leggiIngredienti(): LiveData<List<Ingrediente>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserisciIngrediente(ingrediente: Ingrediente)
}