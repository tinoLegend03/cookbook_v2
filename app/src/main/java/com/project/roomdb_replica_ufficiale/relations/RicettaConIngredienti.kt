package com.project.roomdb_replica_ufficiale.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.project.roomdb_replica_ufficiale.data.ingrediente.Ingrediente
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta

/*data class RicettaConIngredienti (
    @Embedded val ricetta: Ricetta,
    @Relation(
        parentColumn = "nomeRicetta",
        entityColumn = "nomeIngrediente",
        associateBy = Junction(RicettaIngredienteCrossRef::class)
    )
    val ingredienti: List<Ingrediente>
)*/