package com.project.roomdb_replica_ufficiale.data.istruzione

import androidx.lifecycle.LiveData

class IstruzioneRepository(private val istruzioneDao: IstruzioneDao) {

    val leggiIstruzioni: LiveData<List<Istruzione>> = istruzioneDao.leggiIstruzioni()

    suspend fun nuovaIstruzione(istruzione: Istruzione){
        istruzioneDao.nuovaIstruzione(istruzione)
    }

}