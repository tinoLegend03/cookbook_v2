package com.project.roomdb_replica_ufficiale.relations.ricettaIngredienteRelation

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.project.roomdb_replica_ufficiale.data.ingrediente.Ingrediente
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta

/**
 * Tabella ponte Ricetta - Ingrediente (relazione N-M)
 * con campo extra “quantita”.
 */
@Entity(
    tableName = "tab_ricetta_ingrediente",
    primaryKeys = ["idRicetta", "nomeIngrediente"],       // PK composta
    foreignKeys = [
        ForeignKey(
            entity = Ricetta::class,
            parentColumns = ["idRicetta"],
            childColumns = ["idRicetta"],
            onDelete = ForeignKey.CASCADE          // elimina relazioni se ricetta sparisce
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
    val idRicetta: Long,            // FK Ricetta
    val nomeIngrediente: String,    // FK Ingrediente
    val quantita: String
) {}