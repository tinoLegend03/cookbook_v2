package com.project.roomdb_replica_ufficiale.data.ingrediente

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entità Room che rappresenta un Ingrediente di base.
 * Usa il nome come chiave primaria.
 */
@Entity(tableName = "tab_ingredienti")
data class Ingrediente (

    /** Nome univoco dell’ingrediente */
    @PrimaryKey(autoGenerate = false)
    val nomeIngrediente: String,

)