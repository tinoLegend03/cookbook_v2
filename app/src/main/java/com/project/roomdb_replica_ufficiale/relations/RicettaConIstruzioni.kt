package com.project.roomdb_replica_ufficiale.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.project.roomdb_replica_ufficiale.data.istruzione.Istruzione
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta

class RicettaConIstruzioni (
    @Embedded val ricetta: Ricetta,
    @Relation(
        parentColumn = "idRicetta",
        entityColumn = "idRicetta"
    )
    val istruzioni: List<Istruzione>
)

//DA AGGIUNGERE LE QUERY SUL DAO (https://www.youtube.com/watch?v=K8yKH5Ak84E)