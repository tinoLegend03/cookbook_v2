package com.project.roomdb_replica_ufficiale.relations.ricettaIngredienteRelation

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.project.roomdb_replica_ufficiale.data.ingrediente.Ingrediente
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta

@Entity(
    tableName = "tab_ricetta_ingrediente",
    primaryKeys = ["idRicetta", "nomeIngrediente"],
    foreignKeys = [
        ForeignKey(
            entity = Ricetta::class,
            parentColumns = ["idRicetta"],
            childColumns = ["idRicetta"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Ingrediente::class,
            parentColumns = ["nomeIngrediente"],
            childColumns = ["nomeIngrediente"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idRicetta"), Index("nomeIngrediente")]
)
data class RicettaIngrediente(
    val idRicetta: Long,
    val nomeIngrediente: String,
    val quantita: String                // es. "200 g", "3 cucchiai"
) {}