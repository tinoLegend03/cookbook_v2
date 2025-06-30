package com.project.roomdb_replica_ufficiale.data.istruzione

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.project.roomdb_replica_ufficiale.data.RicettarioDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel che media tra UI e IstruzioneRepository.
 */
class IstruzioneViewModel(application: Application): AndroidViewModel(application) {

    val leggiIstruzioni: LiveData<List<Istruzione>>
    private val repository: IstruzioneRepository

    init {
        // istanzia repository passando il DAO
        val istruzioneDao = RicettarioDatabase.getDatabase(application).istruzioneDao()
        repository = IstruzioneRepository(istruzioneDao)
        leggiIstruzioni = repository.leggiIstruzioni
    }

    /** Inserisce una nuova istruzione in background. */
    fun nuovaIstruzione(istruzione: Istruzione){
        viewModelScope.launch(Dispatchers.IO) {
            repository.nuovaIstruzione(istruzione)
        }
    }
}