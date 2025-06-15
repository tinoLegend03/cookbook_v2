package com.project.roomdb_replica_ufficiale.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta

/*class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val ricettarioDatabase = RicettarioDatabase.getDatabase(application)
    private val ricettaDao = ricettarioDatabase.ricettaDao()

    // LiveData che osserva le ricette recenti
    val recentlyMadeRecipes: LiveData<List<Ricetta>> = ricettaDao.getRecentlyMade()


    fun getRecipeById(recipeId: Long): LiveData<Ricetta> {
        return ricettaDao.getRecipeById(recipeId)
    }
}*/