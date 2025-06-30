package com.project.roomdb_replica_ufficiale.data.istruzione

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO per gestire le istruzioni di preparazione.
 */
@Dao
interface IstruzioneDao {

    /** Inserisce una nuova istruzione (ignora se già presente) */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun nuovaIstruzione(istruzione: Istruzione)

    /** Restituisce l’elenco completo ordinato per id */
    @Query("SELECT * FROM tab_istruzioni ORDER BY id ASC")
    fun leggiIstruzioni(): LiveData<List<Istruzione>>
}