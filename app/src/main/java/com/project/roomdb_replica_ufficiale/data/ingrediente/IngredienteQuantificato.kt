package com.project.roomdb_replica_ufficiale.data.ingrediente

/**
 * DTO semplice usato per visualizzare un ingrediente
 * insieme alla quantità relativa ad una ricetta.
 */
data class IngredienteQuantificato(
    var nomeIngrediente: String,
    var quantita: String
)