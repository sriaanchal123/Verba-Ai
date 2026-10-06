package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        IngestionSessionEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(VerbaConverters::class)
abstract class VerbaDatabase : RoomDatabase() {

    abstract fun verbaDao(): VerbaDao

    companion object {
        @Volatile
        private var INSTANCE: VerbaDatabase? = null

        fun getInstance(context: Context): VerbaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VerbaDatabase::class.java,
                    "verba_ai_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
