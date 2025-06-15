package com.project.roomdb_replica_ufficiale.data.ingrediente

import androidx.lifecycle.LiveData

class IngredienteRepository(private val ingredienteDao: IngredienteDao) {

    val leggiIngredienti: LiveData<List<Ingrediente>> = ingredienteDao.leggiIngredienti()

    suspend fun nuovoIngrediente(ingrediente: Ingrediente){
        ingredienteDao.nuovoIngrediente(ingrediente)
    }

}