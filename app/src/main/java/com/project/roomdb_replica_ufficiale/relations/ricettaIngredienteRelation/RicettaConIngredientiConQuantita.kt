package com.project.roomdb_replica_ufficiale.relations.ricettaIngredienteRelation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.project.roomdb_replica_ufficiale.data.ingrediente.Ingrediente
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta

/**
 * Oggetto di relazione N-M tra Ricetta e Ingrediente
 * che include anche la quantità dal record ponte.
 */
data class RicettaConIngredientiConQuantita (
    @Embedded val ricetta: Ricetta,

    // Relazione Ricetta - (RicettaIngrediente) - Ingrediente
    @Relation(
        parentColumn = "idRicetta",             // PK di Ricetta
        entityColumn = "nomeIngrediente",       // PK di Ingrediente
        associateBy = Junction(                 // tabella ponte
            value = RicettaIngrediente::class,
            parentColumn = "idRicetta",
            entityColumn = "nomeIngrediente"
        )
    )
    val ingredienti: List<Ingrediente>           // lista ingredienti della ricetta
){}