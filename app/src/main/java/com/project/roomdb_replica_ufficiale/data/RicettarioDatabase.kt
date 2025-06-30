package com.project.roomdb_replica_ufficiale.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.project.roomdb_replica_ufficiale.Converters
import com.project.roomdb_replica_ufficiale.data.ingrediente.Ingrediente
import com.project.roomdb_replica_ufficiale.data.ingrediente.IngredienteDao
import com.project.roomdb_replica_ufficiale.data.istruzione.Istruzione
import com.project.roomdb_replica_ufficiale.data.istruzione.IstruzioneDao
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta
import com.project.roomdb_replica_ufficiale.data.ricetta.RicettaDao
import com.project.roomdb_replica_ufficiale.relations.ricettaIngredienteRelation.RicettaIngrediente

/**
 * Room database principale dell’app.
 * Contiene le entità e fornisce i DAO necessari.
 * Getsisce la ccreazione e le relative versioni.
 */
@Database(entities = [
    Istruzione::class,
    Ricetta::class,
    Ingrediente::class,
    RicettaIngrediente::class],
    version = 4,
    exportSchema = false
)
// converter per tipi custom
@TypeConverters(Converters::class)      // converter per tipi custom
abstract class RicettarioDatabase: RoomDatabase() {

    /* --------- DAO esposti ------------------------------------------------ */

    abstract fun istruzioneDao(): IstruzioneDao
    abstract fun ricettaDao(): RicettaDao
    abstract fun ingredienteDao(): IngredienteDao

    /* --------- Singleton thread-safe -------------------------------------- */

    companion object{
        @Volatile
        private var INSTANCE: RicettarioDatabase? = null


        /**
         * Ritorna l’istanza Singleton del DB. Se non esiste la crea.
         * "synchronized" garantisce sicurezza in multi-thread.
         */
        fun getDatabase(context: Context): RicettarioDatabase{
            //ritorna subito se già inizializzato
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }

            //altrimenti crea l’istanza
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RicettarioDatabase::class.java,
                    "ricettario_database"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}