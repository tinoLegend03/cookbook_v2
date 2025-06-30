package com.project.roomdb_replica_ufficiale.data.ricetta

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/** Entità che rappresenta una ricetta */
@Parcelize
@Entity(
    tableName = "tab_ricette",
    indices = [Index(value = ["nomeRicetta"], unique = true)]   // unicità sul nome
)
data class Ricetta (

    /** PK identifica univocamente la ricetta */
    @PrimaryKey(autoGenerate = true)
    val idRicetta: Long = 0L,

    /** Nome ricetta (unico) */
    val nomeRicetta: String,

    /** Durata in minuti */
    val durata: Int,

    /** Difficoltà */
    val livello: String,

    /** Categoria */
    val categoria: String,

    /** Descrizione */
    val descrizione: String,

    /** Data creazione/ultima modifica */
    val ultimaModifica: Long,

    /** Ultima esecuzione */
    val ultimaEsecuzione: Long,

    /** Numero di volte che è stata completata una ricetta */
    val count: Int,

    /** Allergeni */
    val allergeni: List<String>

):Parcelable{

}