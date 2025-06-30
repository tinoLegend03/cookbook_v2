package com.project.roomdb_replica_ufficiale.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.project.roomdb_replica_ufficiale.data.istruzione.Istruzione
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta

/**
 * Relazione 1-N tra Ricetta e Istruzione.
 * Usata per caricare la lista di step di preparazione.
 */
class RicettaConIstruzioni (
    @Embedded val ricetta: Ricetta,         // dati base della ricetta
    @Relation(
        parentColumn = "idRicetta",         // PK Ricetta
        entityColumn = "idRicetta"          // FK in tab_istruzioni
    )
    val istruzioni: List<Istruzione>
)
