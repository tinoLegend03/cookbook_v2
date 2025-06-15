package com.project.roomdb_replica_ufficiale.data.ingrediente

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tab_ingredienti")
data class Ingrediente (
    @PrimaryKey(autoGenerate = false)
    val nomeIngrediente: String,


)