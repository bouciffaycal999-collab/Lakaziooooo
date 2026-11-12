package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProductEntity::class,
        InvoiceEntity::class,
        CustomerEntity::class,
        SupplierEntity::class,
        SupplierMovementEntity::class,
        CaisseMovementEntity::class,
        DailyCaisseCountEntity::class,
        StaffUserEntity::class,
        NotificationEntity::class,
        AppKvEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun posDao(): PosDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kader_ceramic_pos.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
