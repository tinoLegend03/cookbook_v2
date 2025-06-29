package com.project.roomdb_replica_ufficiale.relations.ricettaIngredienteRelation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.project.roomdb_replica_ufficiale.data.ingrediente.Ingrediente
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta

data class RicettaConIngredientiConQuantita (
    @Embedded val ricetta: Ricetta,
    @Relation(
        parentColumn = "idRicetta",
        entityColumn = "nomeIngrediente",
        associateBy = Junction(
            value = RicettaIngrediente::class,
            parentColumn = "idRicetta",
            entityColumn = "nomeIngrediente"
        )
    )
    val ingredienti: List<Ingrediente>
){}