package com.project.roomdb_replica_ufficiale.data.allergene

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.project.roomdb_replica_ufficiale.data.RicettarioDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//The ViewModel's role is to provide data to the UI and survive configuration changes.
//A ViewModel acts as a communication center between the Repository and the UI.
class AllergeneViewModel(application: Application): AndroidViewModel(application) {

    /*val leggiAllergeni: LiveData<List<Allergene>>
    private val repository: AllergeneRepository

    init {
        val allergeneDao = RicettarioDatabase.getDatabase(application).allergeneDao()
        repository = AllergeneRepository(allergeneDao)
        leggiAllergeni = repository.leggiAllergeni
    }

    fun nuovoAllergene(allergene: Allergene){
        viewModelScope.launch(Dispatchers.IO) {
            repository.nuovoAllergene(allergene)
        }
    }*/

}