package com.project.roomdb_replica_ufficiale.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.project.roomdb_replica_ufficiale.data.ingrediente.Ingrediente
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta

/*data class IngredienteConRicette (
    @Embedded val ingrediente: Ingrediente,
    @Relation(
        parentColumn = "nomeIngrediente",
        entityColumn = "nomeRicetta",
        associateBy = Junction(RicettaIngredienteCrossRef::class)
    )
    val ricette: List<Ricetta>
)*/