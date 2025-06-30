package com.project.roomdb_replica_ufficiale.data.ingrediente

import androidx.lifecycle.LiveData

/**
 * Repository che incapsula l’accesso a IngredienteDao.
 * Espone LiveData per la UI e funzioni sospese per I/O.
 */
class IngredienteRepository(private val ingredienteDao: IngredienteDao) {

    /** Stream in tempo reale di tutti gli ingredienti. */
    val leggiIngredienti: LiveData<List<Ingrediente>> = ingredienteDao.leggiIngredienti()

    /** Aggiunge un nuovo ingrediente*/
    suspend fun nuovoIngrediente(ingrediente: Ingrediente){
        ingredienteDao.nuovoIngrediente(ingrediente)
    }

}