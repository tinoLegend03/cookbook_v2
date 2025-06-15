package com.project.roomdb_replica_ufficiale.relations.ricettaIngredienteRelation

import androidx.room.Embedded
import com.project.roomdb_replica_ufficiale.data.ingrediente.Ingrediente

data class IngredienteConQuantita (
    @Embedded val ingrediente: Ingrediente,
    val quantita: String
){}