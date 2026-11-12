package com.marsholat.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.marsholat.data.local.dao.PrayerRecordDao
import com.marsholat.data.local.entity.PrayerRecordEntity

@Database(
    entities = [PrayerRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MarSholatDatabase : RoomDatabase() {

    abstract fun prayerRecordDao(): PrayerRecordDao

    companion object {
        @Volatile
        private var INSTANCE: MarSholatDatabase? = null

        fun getDatabase(context: Context): MarSholatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MarSholatDatabase::class.java,
                    "marsholat_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
