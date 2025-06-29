package com.project.roomdb_replica_ufficiale.data.ricetta

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "tab_ricette",
    indices = [Index(value = ["nomeRicetta"], unique = true)]   // unicità sul nome
)
data class Ricetta (

    // ---------- nuova PK ----------
    @PrimaryKey(autoGenerate = true)
    val idRicetta: Long = 0L,

    //Nome ricetta
    val nomeRicetta: String,

    //Durata in minuti
    val durata: Int,

    //Difficoltà
    val livello: String,

    //Categoria
    val categoria: String,

    //Descrizione
    val descrizione: String,

    //Data creazione/ultima modifica
    val ultimaModifica: Long,

    //Immagine
    //TODO

    //Ultima esecuzione
    val ultimaEsecuzione: Long,

    //Count
    val count: Int,

    //Allergeni
    val allergeni: List<String>

):Parcelable{

}