package com.project.roomdb_replica_ufficiale.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
//import com.project.roomdb_replica_ufficiale.data.allergene.Allergene
import com.project.roomdb_replica_ufficiale.data.ingrediente.Ingrediente

/*data class AllergeneConIngredienti (
    @Embedded val allergene: Allergene,
    @Relation(
        parentColumn = "nomeAllergene",
        entityColumn = "nomeIngrediente",
        associateBy = Junction(IngredienteAllergeneCrossRef::class)
    )
    val ingredienti: List<Ingrediente>
)*/