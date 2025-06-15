package com.project.roomdb_replica_ufficiale.data.ricetta

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tab_ricette")
data class Ricetta (

    @PrimaryKey(autoGenerate = false)
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