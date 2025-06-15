package com.project.roomdb_replica_ufficiale.data.allergene

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

//Contiene i metodi per accedere al db
@Dao
interface AllergeneDao {

    /*@Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun nuovoAllergene(allergene: Allergene)

    @Query("SELECT * FROM tab_allergeni ORDER BY nomeAllergene ASC")
    fun leggiAllergeni(): LiveData<List<Allergene>>*/

}