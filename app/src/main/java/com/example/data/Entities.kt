package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pharmacies")
data class Pharmacy(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String,
    val licenseNumber: String,
    val gstNumber: String,
    val openingTime: String,
    val closingTime: String,
    val rating: Float,
    val reviewsCount: Int,
    val phoneNumber: String,
    val whatsappNumber: String,
    val hasHomeDelivery: Boolean,
    val isOpen: Boolean,
    val distanceMeters: Int,
    val totalMedicinesCount: Int,
    val subscriptionTier: String, // "Free", "Basic", "Premium", "Enterprise"
    val latitude: Double,
    val longitude: Double
)

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pharmacyId: String,
    val name: String,
    val mrp: Double,
    val sellingPrice: Double,
    val discountPercent: Int,
    val stockCount: Int,
    val manufacturer: String,
    val expiryDate: String,
    val batchNumber: String,
    val saltComposition: String,
    val alternativesText: String
)

@Entity(tableName = "sync_logs")
data class SyncLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pharmacyId: String,
    val erpType: String, // "Marg ERP", "RetailGraph", "Tally ERP"
    val timestamp: Long = System.currentTimeMillis(),
    val itemsSyncedCount: Int,
    val status: String, // "Success", "Failed"
    val durationMs: Int
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pharmacyId: String,
    val pharmacyName: String,
    val medicineName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val isHomeDelivery: Boolean,
    val status: String, // "Pending", "Confirmed", "Out for Delivery", "Delivered"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "prescriptions")
data class Prescription(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageUrl: String,
    val uploadTimestamp: Long = System.currentTimeMillis(),
    val extractedText: String = "",
    val detectedMedicines: String = "", // Comma-separated or JSON list of medicines found
    val status: String // "Processing", "Completed", "Failed"
)

@Entity(tableName = "payment_proofs")
data class PaymentProof(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pharmacyId: String,
    val pharmacyName: String,
    val paymentType: String, // "Subscription" or "Commission"
    val amount: Double,
    val utrNumber: String,
    val paymentDate: String,
    val status: String, // "Pending", "Approved", "Rejected"
    val tierRequested: String = "", // e.g. "Basic", "Premium", "Enterprise"
    val timestamp: Long = System.currentTimeMillis()
)
