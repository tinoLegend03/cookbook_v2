package com.project.roomdb_replica_ufficiale.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
//import com.project.roomdb_replica_ufficiale.data.allergene.Allergene
import com.project.roomdb_replica_ufficiale.data.ingrediente.Ingrediente

/*data class IngredienteConAllergeni (
    @Embedded val ingrediente: Ingrediente,
    @Relation(
        parentColumn = "nomeIngrediente",
        entityColumn = "nomeAllergene",
        associateBy = Junction(IngredienteAllergeneCrossRef::class)
    )
    val allergeni: List<Allergene>
)*/