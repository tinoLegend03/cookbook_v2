package com.project.roomdb_replica_ufficiale.data.istruzione

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

//Contiene i metodi per accedere al db
@Dao
interface IstruzioneDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun nuovaIstruzione(istruzione: Istruzione)

    @Query("SELECT * FROM tab_istruzioni ORDER BY id ASC")
    fun leggiIstruzioni(): LiveData<List<Istruzione>>
}