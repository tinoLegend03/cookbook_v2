package com.project.roomdb_replica_ufficiale.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.project.roomdb_replica_ufficiale.Converters
import com.project.roomdb_replica_ufficiale.data.allergene.AllergeneDao
import com.project.roomdb_replica_ufficiale.data.ingrediente.Ingrediente
import com.project.roomdb_replica_ufficiale.data.ingrediente.IngredienteDao
import com.project.roomdb_replica_ufficiale.data.istruzione.Istruzione
import com.project.roomdb_replica_ufficiale.data.istruzione.IstruzioneDao
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta
import com.project.roomdb_replica_ufficiale.data.ricetta.RicettaDao
import com.project.roomdb_replica_ufficiale.relations.ricettaIngredienteRelation.RicettaIngrediente

@Database(entities = [Istruzione::class, Ricetta::class, Ingrediente::class, RicettaIngrediente::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class RicettarioDatabase: RoomDatabase() {

    abstract fun istruzioneDao(): IstruzioneDao
    abstract fun ricettaDao(): RicettaDao
    abstract fun ingredienteDao(): IngredienteDao
    abstract fun allergeneDao(): AllergeneDao

    companion object{
        @Volatile
        private var INSTANCE: RicettarioDatabase? = null

        fun getDatabase(context: Context): RicettarioDatabase{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RicettarioDatabase::class.java,
                    "ricettario_database"
                ).fallbackToDestructiveMigration(false).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}