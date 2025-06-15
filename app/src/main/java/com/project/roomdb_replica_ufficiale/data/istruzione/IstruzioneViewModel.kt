package com.project.roomdb_replica_ufficiale.data.istruzione

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.project.roomdb_replica_ufficiale.data.RicettarioDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//The ViewModel's role is to provide data to the UI and survive configuration changes.
//A ViewModel acts as a communication center between the Repository and the UI.
class IstruzioneViewModel(application: Application): AndroidViewModel(application) {

    val leggiIstruzioni: LiveData<List<Istruzione>>
    private val repository: IstruzioneRepository

    init {
        val istruzioneDao = RicettarioDatabase.getDatabase(application).istruzioneDao()
        repository = IstruzioneRepository(istruzioneDao)
        leggiIstruzioni = repository.leggiIstruzioni
    }

    fun nuovaIstruzione(istruzione: Istruzione){
        viewModelScope.launch(Dispatchers.IO) {
            repository.nuovaIstruzione(istruzione)
        }
    }
}