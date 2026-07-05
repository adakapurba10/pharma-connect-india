package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PharmacyDao {
    @Query("SELECT * FROM pharmacies ORDER BY distanceMeters ASC")
    fun getAllPharmaciesFlow(): Flow<List<Pharmacy>>

    @Query("SELECT * FROM pharmacies WHERE id = :id LIMIT 1")
    suspend fun getPharmacyById(id: String): Pharmacy?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPharmacy(pharmacy: Pharmacy)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPharmacies(pharmacies: List<Pharmacy>)

    @Update
    suspend fun updatePharmacy(pharmacy: Pharmacy)
}

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines WHERE name LIKE '%' || :query || '%' OR saltComposition LIKE '%' || :query || '%' OR alternativesText LIKE '%' || :query || '%'")
    fun searchMedicinesFlow(query: String): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE pharmacyId = :pharmacyId")
    fun getMedicinesByPharmacyFlow(pharmacyId: String): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE name = :name ORDER BY sellingPrice ASC")
    fun getMedicineOffersFlow(name: String): Flow<List<Medicine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(medicines: List<Medicine>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: Medicine)

    @Query("DELETE FROM medicines WHERE pharmacyId = :pharmacyId")
    suspend fun deleteMedicinesByPharmacy(pharmacyId: String)
}

@Dao
interface SyncLogDao {
    @Query("SELECT * FROM sync_logs ORDER BY timestamp DESC")
    fun getAllSyncLogsFlow(): Flow<List<SyncLog>>

    @Query("SELECT * FROM sync_logs WHERE pharmacyId = :pharmacyId ORDER BY timestamp DESC LIMIT 10")
    fun getSyncLogsByPharmacyFlow(pharmacyId: String): Flow<List<SyncLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncLog(log: SyncLog)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrdersFlow(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE pharmacyId = :pharmacyId ORDER BY timestamp DESC")
    fun getOrdersByPharmacyFlow(pharmacyId: String): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Update
    suspend fun updateOrder(order: Order)
}

@Dao
interface PrescriptionDao {
    @Query("SELECT * FROM prescriptions ORDER BY uploadTimestamp DESC")
    fun getAllPrescriptionsFlow(): Flow<List<Prescription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: Prescription): Long

    @Update
    suspend fun updatePrescription(prescription: Prescription)
}

@Dao
interface PaymentProofDao {
    @Query("SELECT * FROM payment_proofs ORDER BY timestamp DESC")
    fun getAllPaymentProofsFlow(): Flow<List<PaymentProof>>

    @Query("SELECT * FROM payment_proofs WHERE pharmacyId = :pharmacyId ORDER BY timestamp DESC")
    fun getPaymentProofsByPharmacyFlow(pharmacyId: String): Flow<List<PaymentProof>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentProof(paymentProof: PaymentProof)

    @Update
    suspend fun updatePaymentProof(paymentProof: PaymentProof)
}
