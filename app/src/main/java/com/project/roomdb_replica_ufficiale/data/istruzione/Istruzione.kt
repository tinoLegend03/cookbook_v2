package com.project.roomdb_replica_ufficiale.data.istruzione

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta

/**
 * Entità che rappresenta uno step (istruzione) di una ricetta.
 * Relazione 1-N con Ricetta tramite FK idRicetta.
 */
@Entity(
    foreignKeys = [ForeignKey(
        entity = Ricetta::class,
        parentColumns = ["idRicetta"],
        childColumns = ["idRicetta"],
        onDelete = ForeignKey.CASCADE       // cancella step se la ricetta è rimossa
    )],
    indices = [Index("idRicetta")],
    tableName = "tab_istruzioni"
)
data class Istruzione (

    /** PK autonumerata */
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    /** FK verso tab_ricette */
    val idRicetta: Long,

    /** Numero di ordine dello step */
    val numero: Int,

    /** Testo dell'istruzione */
    val descrizione: String
)