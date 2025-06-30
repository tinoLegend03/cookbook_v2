package com.project.roomdb_replica_ufficiale.relations.ricettaIngredienteRelation

import androidx.room.Embedded
import com.project.roomdb_replica_ufficiale.data.ingrediente.Ingrediente


/**
 * DTO (Data Transfer Object) usato per mostrare
 * un ingrediente con la sua quantità in una ricetta.
 */
data class IngredienteConQuantita (
    @Embedded val ingrediente: Ingrediente,
    val quantita: String
){}