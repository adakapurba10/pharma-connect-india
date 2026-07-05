package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Pharmacy::class,
        Medicine::class,
        SyncLog::class,
        Order::class,
        Prescription::class,
        PaymentProof::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pharmacyDao(): PharmacyDao
    abstract fun medicineDao(): MedicineDao
    abstract fun syncLogDao(): SyncLogDao
    abstract fun orderDao(): OrderDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun paymentProofDao(): PaymentProofDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pharma_connect_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
