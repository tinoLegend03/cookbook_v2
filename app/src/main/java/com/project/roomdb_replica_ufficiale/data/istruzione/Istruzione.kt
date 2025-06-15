package com.project.roomdb_replica_ufficiale.data.istruzione

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta

@Entity(
    foreignKeys = [ForeignKey(
        entity = Ricetta::class,
        parentColumns = ["nomeRicetta"],
        childColumns = ["ricetta"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("ricetta")],
    tableName = "tab_istruzioni"
)
data class Istruzione (

    @PrimaryKey(autoGenerate = true)
    val id: Int,

    //Riferisce alla ricetta a cui questa istruzione appartiene (foreign key)
    val ricetta: String,

    //Ordine dello step nella ricetta
    val numero: Int,

    //Testo dell'istruzione
    val descrizione: String
)