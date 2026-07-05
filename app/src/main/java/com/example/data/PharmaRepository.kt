package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PharmaRepository(private val db: AppDatabase) {
    val pharmacyDao = db.pharmacyDao()
    val medicineDao = db.medicineDao()
    val syncLogDao = db.syncLogDao()
    val orderDao = db.orderDao()
    val prescriptionDao = db.prescriptionDao()
    val paymentProofDao = db.paymentProofDao()

    val allPharmacies: Flow<List<Pharmacy>> = pharmacyDao.getAllPharmaciesFlow()
    val allSyncLogs: Flow<List<SyncLog>> = syncLogDao.getAllSyncLogsFlow()
    val allOrders: Flow<List<Order>> = orderDao.getAllOrdersFlow()
    val allPrescriptions: Flow<List<Prescription>> = prescriptionDao.getAllPrescriptionsFlow()
    val allPaymentProofs: Flow<List<PaymentProof>> = paymentProofDao.getAllPaymentProofsFlow()

    fun searchMedicines(query: String): Flow<List<Medicine>> = medicineDao.searchMedicinesFlow(query)
    fun getMedicinesByPharmacy(pharmacyId: String): Flow<List<Medicine>> = medicineDao.getMedicinesByPharmacyFlow(pharmacyId)
    fun getMedicineOffers(name: String): Flow<List<Medicine>> = medicineDao.getMedicineOffersFlow(name)
    fun getPaymentProofsByPharmacy(pharmacyId: String): Flow<List<PaymentProof>> = paymentProofDao.getPaymentProofsByPharmacyFlow(pharmacyId)

    suspend fun getPharmacyById(id: String): Pharmacy? = pharmacyDao.getPharmacyById(id)
    suspend fun insertOrder(order: Order) = orderDao.insertOrder(order)
    suspend fun updateOrder(order: Order) = orderDao.updateOrder(order)
    suspend fun insertPrescription(prescription: Prescription): Long = prescriptionDao.insertPrescription(prescription)
    suspend fun updatePrescription(prescription: Prescription) = prescriptionDao.updatePrescription(prescription)
    suspend fun insertSyncLog(log: SyncLog) = syncLogDao.insertSyncLog(log)
    suspend fun updatePharmacy(pharmacy: Pharmacy) = pharmacyDao.updatePharmacy(pharmacy)
    suspend fun insertPaymentProof(paymentProof: PaymentProof) = paymentProofDao.insertPaymentProof(paymentProof)
    suspend fun updatePaymentProof(paymentProof: PaymentProof) = paymentProofDao.updatePaymentProof(paymentProof)

    suspend fun syncPharmacyInventory(
        pharmacyId: String,
        erpType: String,
        updatedMedicines: List<Medicine>
    ) = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        
        // Update database
        medicineDao.deleteMedicinesByPharmacy(pharmacyId)
        medicineDao.insertMedicines(updatedMedicines)
        
        // Update Pharmacy medicine count
        val pharmacy = pharmacyDao.getPharmacyById(pharmacyId)
        if (pharmacy != null) {
            pharmacyDao.updatePharmacy(
                pharmacy.copy(
                    totalMedicinesCount = updatedMedicines.size,
                    isOpen = true
                )
            )
        }

        val duration = (System.currentTimeMillis() - startTime).toInt()
        syncLogDao.insertSyncLog(
            SyncLog(
                pharmacyId = pharmacyId,
                erpType = erpType,
                itemsSyncedCount = updatedMedicines.size,
                status = "Success",
                durationMs = duration
            )
        )
    }

    suspend fun prepopulateIfEmpty() = withContext(Dispatchers.IO) {
        val currentPharmacies = pharmacyDao.getAllPharmaciesFlow().first()
        if (currentPharmacies.isEmpty()) {
            val defaultPharmacies = listOf(
                Pharmacy(
                    id = "abc_medical",
                    name = "ABC Medical & General Store",
                    imageUrl = "https://images.unsplash.com/photo-1586015555751-63bb77f4322a?w=400",
                    licenseNumber = "DL-12345/A/2024",
                    gstNumber = "19ABCDE1234F1Z1",
                    openingTime = "08:00 AM",
                    closingTime = "10:00 PM",
                    rating = 4.8f,
                    reviewsCount = 142,
                    phoneNumber = "+91 98765 43210",
                    whatsappNumber = "+91 98765 43210",
                    hasHomeDelivery = true,
                    isOpen = true,
                    distanceMeters = 320,
                    totalMedicinesCount = 12,
                    subscriptionTier = "Premium",
                    latitude = 22.5726,
                    longitude = 88.3639
                ),
                Pharmacy(
                    id = "xyz_pharmacy",
                    name = "XYZ Pharmacy",
                    imageUrl = "https://images.unsplash.com/photo-1607619056574-7b8f304b3b8a?w=400",
                    licenseNumber = "DL-98765/B/2025",
                    gstNumber = "19XYZAB5678C1Z0",
                    openingTime = "09:00 AM",
                    closingTime = "09:30 PM",
                    rating = 4.2f,
                    reviewsCount = 56,
                    phoneNumber = "+91 87654 32109",
                    whatsappNumber = "+91 87654 32109",
                    hasHomeDelivery = true,
                    isOpen = true,
                    distanceMeters = 650,
                    totalMedicinesCount = 8,
                    subscriptionTier = "Basic",
                    latitude = 22.5745,
                    longitude = 88.3612
                ),
                Pharmacy(
                    id = "medlife_care",
                    name = "MedLife Care Hub",
                    imageUrl = "https://images.unsplash.com/photo-1576091160550-2173dba999ef?w=400",
                    licenseNumber = "DL-55443/C/2026",
                    gstNumber = "19MEDLF9988D1Z2",
                    openingTime = "07:00 AM",
                    closingTime = "11:00 PM",
                    rating = 4.5f,
                    reviewsCount = 208,
                    phoneNumber = "+91 76543 21098",
                    whatsappNumber = "+91 76543 21098",
                    hasHomeDelivery = false,
                    isOpen = false,
                    distanceMeters = 1100,
                    totalMedicinesCount = 15,
                    subscriptionTier = "Free",
                    latitude = 22.5780,
                    longitude = 88.3675
                ),
                Pharmacy(
                    id = "apollo_lite",
                    name = "Apollo Pharmacy Lite",
                    imageUrl = "https://images.unsplash.com/photo-1587854692152-cbe660db0969?w=400",
                    licenseNumber = "DL-11223/D/2025",
                    gstNumber = "19APOLO4433E1Z4",
                    openingTime = "24 Hours",
                    closingTime = "24 Hours",
                    rating = 4.7f,
                    reviewsCount = 312,
                    phoneNumber = "+91 65432 10987",
                    whatsappNumber = "+91 65432 10987",
                    hasHomeDelivery = true,
                    isOpen = true,
                    distanceMeters = 1450,
                    totalMedicinesCount = 22,
                    subscriptionTier = "Enterprise",
                    latitude = 22.5695,
                    longitude = 88.3580
                ),
                Pharmacy(
                    id = "peoples_druggist",
                    name = "People's Druggist Store",
                    imageUrl = "https://images.unsplash.com/photo-1550572017-edd951b55104?w=400",
                    licenseNumber = "DL-66778/E/2023",
                    gstNumber = "19PEOPL7766F1Z5",
                    openingTime = "10:00 AM",
                    closingTime = "08:30 PM",
                    rating = 4.0f,
                    reviewsCount = 18,
                    phoneNumber = "+91 54321 09876",
                    whatsappNumber = "",
                    hasHomeDelivery = true,
                    isOpen = true,
                    distanceMeters = 2100,
                    totalMedicinesCount = 5,
                    subscriptionTier = "Free",
                    latitude = 22.5810,
                    longitude = 88.3710
                )
            )
            pharmacyDao.insertPharmacies(defaultPharmacies)

            // Populate medicines for each pharmacy
            val defaultMedicines = mutableListOf<Medicine>()
            
            // ABC Medical Medicines
            defaultMedicines.add(Medicine(pharmacyId = "abc_medical", name = "Crocin 650", mrp = 35.0, sellingPrice = 32.0, discountPercent = 8, stockCount = 250, manufacturer = "GSK Consumer Healthcare", expiryDate = "09/2028", batchNumber = "B-CR772", saltComposition = "Paracetamol 650mg", alternativesText = "Dolo 650, Calpol 650"))
            defaultMedicines.add(Medicine(pharmacyId = "abc_medical", name = "Paracetamol 650", mrp = 30.0, sellingPrice = 27.0, discountPercent = 10, stockCount = 125, manufacturer = "GSK Consumer Healthcare", expiryDate = "12/2027", batchNumber = "B-PT8839", saltComposition = "Paracetamol 650mg", alternativesText = "Crocin 650, Calpol 650, Dolo 650"))
            defaultMedicines.add(Medicine(pharmacyId = "abc_medical", name = "Ibuprofen 400", mrp = 25.0, sellingPrice = 20.0, discountPercent = 20, stockCount = 80, manufacturer = "Abbott India Ltd", expiryDate = "08/2027", batchNumber = "B-IB321", saltComposition = "Ibuprofen 400mg", alternativesText = "Brufen 400, Ibugesic 400"))
            defaultMedicines.add(Medicine(pharmacyId = "abc_medical", name = "Atorvastatin 10", mrp = 75.0, sellingPrice = 65.0, discountPercent = 13, stockCount = 45, manufacturer = "Pfizer Ltd", expiryDate = "10/2026", batchNumber = "B-AT942", saltComposition = "Atorvastatin Calcium 10mg", alternativesText = "Lipitor 10, Atorva 10, Tonact 10"))
            defaultMedicines.add(Medicine(pharmacyId = "abc_medical", name = "Amoxicillin 500", mrp = 110.0, sellingPrice = 95.0, discountPercent = 13, stockCount = 60, manufacturer = "GlaxoSmithKline", expiryDate = "04/2026", batchNumber = "B-AMX11", saltComposition = "Amoxicillin 500mg", alternativesText = "Novamox 500, Mox 500"))
            defaultMedicines.add(Medicine(pharmacyId = "abc_medical", name = "Metformin 500", mrp = 40.0, sellingPrice = 32.0, discountPercent = 20, stockCount = 150, manufacturer = "USV Biotech", expiryDate = "03/2028", batchNumber = "B-MT550", saltComposition = "Metformin Hydrochloride 500mg", alternativesText = "Glycomet 500, Gluformin 500"))
            defaultMedicines.add(Medicine(pharmacyId = "abc_medical", name = "Pantoprazole 40", mrp = 120.0, sellingPrice = 105.0, discountPercent = 12, stockCount = 90, manufacturer = "Alkem Laboratories", expiryDate = "09/2027", batchNumber = "B-PN329", saltComposition = "Pantoprazole Sodium 40mg", alternativesText = "Pan 40, Pantocid 40"))

            // XYZ Pharmacy Medicines
            defaultMedicines.add(Medicine(pharmacyId = "xyz_pharmacy", name = "Crocin Advance", mrp = 20.0, sellingPrice = 18.0, discountPercent = 10, stockCount = 12, manufacturer = "GSK Consumer Healthcare", expiryDate = "04/2028", batchNumber = "B-CR104", saltComposition = "Paracetamol 500mg", alternativesText = "Paracetamol 500"))
            defaultMedicines.add(Medicine(pharmacyId = "xyz_pharmacy", name = "Paracetamol 650", mrp = 30.0, sellingPrice = 29.0, discountPercent = 3, stockCount = 18, manufacturer = "GSK Consumer Healthcare", expiryDate = "11/2027", batchNumber = "B-PT8220", saltComposition = "Paracetamol 650mg", alternativesText = "Crocin 650, Calpol 650, Dolo 650"))
            defaultMedicines.add(Medicine(pharmacyId = "xyz_pharmacy", name = "Ibuprofen 400", mrp = 25.0, sellingPrice = 24.0, discountPercent = 4, stockCount = 15, manufacturer = "Abbott India Ltd", expiryDate = "09/2027", batchNumber = "B-IB335", saltComposition = "Ibuprofen 400mg", alternativesText = "Brufen 400, Ibugesic 400"))
            defaultMedicines.add(Medicine(pharmacyId = "xyz_pharmacy", name = "Atorvastatin 10", mrp = 75.0, sellingPrice = 70.0, discountPercent = 6, stockCount = 20, manufacturer = "Pfizer Ltd", expiryDate = "05/2026", batchNumber = "B-AT911", saltComposition = "Atorvastatin Calcium 10mg", alternativesText = "Lipitor 10, Atorva 10, Tonact 10"))
            defaultMedicines.add(Medicine(pharmacyId = "xyz_pharmacy", name = "Cetirizine 10", mrp = 35.0, sellingPrice = 30.0, discountPercent = 14, stockCount = 200, manufacturer = "Cipla Ltd", expiryDate = "01/2028", batchNumber = "B-CT889", saltComposition = "Cetirizine Dihydrochloride 10mg", alternativesText = "Okacet 10, Alerid 10"))

            // MedLife Care Medicines
            defaultMedicines.add(Medicine(pharmacyId = "medlife_care", name = "Crocin 650", mrp = 35.0, sellingPrice = 30.0, discountPercent = 14, stockCount = 65, manufacturer = "GSK Consumer Healthcare", expiryDate = "06/2028", batchNumber = "B-CR885", saltComposition = "Paracetamol 650mg", alternativesText = "Dolo 650, Calpol 650"))
            defaultMedicines.add(Medicine(pharmacyId = "medlife_care", name = "Paracetamol 650", mrp = 30.0, sellingPrice = 25.0, discountPercent = 16, stockCount = 50, manufacturer = "GSK Consumer Healthcare", expiryDate = "02/2028", batchNumber = "B-PT7411", saltComposition = "Paracetamol 650mg", alternativesText = "Crocin 650, Calpol 650, Dolo 650"))
            defaultMedicines.add(Medicine(pharmacyId = "medlife_care", name = "Metformin 500", mrp = 40.0, sellingPrice = 30.0, discountPercent = 25, stockCount = 80, manufacturer = "USV Biotech", expiryDate = "12/2027", batchNumber = "B-MT502", saltComposition = "Metformin Hydrochloride 500mg", alternativesText = "Glycomet 500, Gluformin 500"))
            defaultMedicines.add(Medicine(pharmacyId = "medlife_care", name = "Pantoprazole 40", mrp = 120.0, sellingPrice = 99.0, discountPercent = 17, stockCount = 35, manufacturer = "Alkem Laboratories", expiryDate = "06/2027", batchNumber = "B-PN310", saltComposition = "Pantoprazole Sodium 40mg", alternativesText = "Pan 40, Pantocid 40"))

            // Apollo Lite Medicines
            defaultMedicines.add(Medicine(pharmacyId = "apollo_lite", name = "Crocin Pain Relief", mrp = 50.0, sellingPrice = 45.0, discountPercent = 10, stockCount = 150, manufacturer = "GSK Consumer Healthcare", expiryDate = "12/2028", batchNumber = "B-CR901", saltComposition = "Paracetamol 650mg + Caffeine 50mg", alternativesText = "Saridon"))
            defaultMedicines.add(Medicine(pharmacyId = "apollo_lite", name = "Paracetamol 650", mrp = 30.0, sellingPrice = 26.0, discountPercent = 13, stockCount = 300, manufacturer = "GSK Consumer Healthcare", expiryDate = "01/2028", batchNumber = "B-APL71", saltComposition = "Paracetamol 650mg", alternativesText = "Crocin 650, Calpol 650, Dolo 650"))
            defaultMedicines.add(Medicine(pharmacyId = "apollo_lite", name = "Ibuprofen 400", mrp = 25.0, sellingPrice = 19.0, discountPercent = 24, stockCount = 120, manufacturer = "Abbott India Ltd", expiryDate = "03/2027", batchNumber = "B-APL72", saltComposition = "Ibuprofen 400mg", alternativesText = "Brufen 400, Ibugesic 400"))
            defaultMedicines.add(Medicine(pharmacyId = "apollo_lite", name = "Atorvastatin 10", mrp = 75.0, sellingPrice = 62.0, discountPercent = 17, stockCount = 140, manufacturer = "Pfizer Ltd", expiryDate = "11/2026", batchNumber = "B-APL73", saltComposition = "Atorvastatin Calcium 10mg", alternativesText = "Lipitor 10, Atorva 10, Tonact 10"))
            defaultMedicines.add(Medicine(pharmacyId = "apollo_lite", name = "Amoxicillin 500", mrp = 110.0, sellingPrice = 90.0, discountPercent = 18, stockCount = 95, manufacturer = "GlaxoSmithKline", expiryDate = "07/2026", batchNumber = "B-APL74", saltComposition = "Amoxicillin 500mg", alternativesText = "Novamox 500, Mox 500"))
            defaultMedicines.add(Medicine(pharmacyId = "apollo_lite", name = "Metformin 500", mrp = 40.0, sellingPrice = 31.0, discountPercent = 22, stockCount = 250, manufacturer = "USV Biotech", expiryDate = "04/2028", batchNumber = "B-APL75", saltComposition = "Metformin Hydrochloride 500mg", alternativesText = "Glycomet 500, Gluformin 500"))
            defaultMedicines.add(Medicine(pharmacyId = "apollo_lite", name = "Pantoprazole 40", mrp = 120.0, sellingPrice = 98.0, discountPercent = 18, stockCount = 160, manufacturer = "Alkem Laboratories", expiryDate = "12/2027", batchNumber = "B-APL76", saltComposition = "Pantoprazole Sodium 40mg", alternativesText = "Pan 40, Pantocid 40"))
            defaultMedicines.add(Medicine(pharmacyId = "apollo_lite", name = "Azithromycin 500", mrp = 150.0, sellingPrice = 125.0, discountPercent = 16, stockCount = 75, manufacturer = "Alembic Pharma", expiryDate = "05/2027", batchNumber = "B-APL77", saltComposition = "Azithromycin 500mg", alternativesText = "Azee 500, Azithral 500"))

            // People's Druggist Medicines
            defaultMedicines.add(Medicine(pharmacyId = "peoples_druggist", name = "Paracetamol 650", mrp = 30.0, sellingPrice = 30.0, discountPercent = 0, stockCount = 0, manufacturer = "GSK Consumer Healthcare", expiryDate = "06/2027", batchNumber = "B-PD01", saltComposition = "Paracetamol 650mg", alternativesText = "Crocin 650, Calpol 650, Dolo 650"))
            defaultMedicines.add(Medicine(pharmacyId = "peoples_druggist", name = "Cetirizine 10", mrp = 35.0, sellingPrice = 32.0, discountPercent = 8, stockCount = 40, manufacturer = "Cipla Ltd", expiryDate = "10/2027", batchNumber = "B-PD02", saltComposition = "Cetirizine Dihydrochloride 10mg", alternativesText = "Okacet 10, Alerid 10"))

            medicineDao.insertMedicines(defaultMedicines)

            // Insert initial orders
            orderDao.insertOrder(
                Order(
                    pharmacyId = "abc_medical",
                    pharmacyName = "ABC Medical & General Store",
                    medicineName = "Paracetamol 650",
                    quantity = 2,
                    unitPrice = 27.0,
                    totalPrice = 54.0,
                    customerName = "Arjun Sharma",
                    customerPhone = "+91 98888 77777",
                    deliveryAddress = "22B, Park Street, Kolkata",
                    isHomeDelivery = true,
                    status = "Delivered"
                )
            )
            orderDao.insertOrder(
                Order(
                    pharmacyId = "apollo_lite",
                    pharmacyName = "Apollo Pharmacy Lite",
                    medicineName = "Atorvastatin 10",
                    quantity = 3,
                    unitPrice = 62.0,
                    totalPrice = 186.0,
                    customerName = "Priya Roy",
                    customerPhone = "+91 91111 22222",
                    deliveryAddress = "Block C, Salt Lake, Sector V",
                    isHomeDelivery = true,
                    status = "Confirmed"
                )
            )

            // Insert initial sync logs
            syncLogDao.insertSyncLog(
                SyncLog(
                    pharmacyId = "abc_medical",
                    erpType = "Marg ERP",
                    itemsSyncedCount = 12,
                    status = "Success",
                    durationMs = 450
                )
            )
            syncLogDao.insertSyncLog(
                SyncLog(
                    pharmacyId = "xyz_pharmacy",
                    erpType = "Tally ERP",
                    itemsSyncedCount = 8,
                    status = "Success",
                    durationMs = 380
                )
            )
        }
    }
}
