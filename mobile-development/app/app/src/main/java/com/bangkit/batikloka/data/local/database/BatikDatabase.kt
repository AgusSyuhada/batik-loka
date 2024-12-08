package com.bangkit.batikloka.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bangkit.batikloka.data.local.dao.ScanHistoryDao
import com.bangkit.batikloka.data.local.entity.ScanHistoryEntity
import com.bangkit.batikloka.utils.DateConverter

@Database(entities = [ScanHistoryEntity::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class BatikDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: BatikDatabase? = null

        fun getDatabase(context: Context): BatikDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BatikDatabase::class.java,
                    "batik_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}