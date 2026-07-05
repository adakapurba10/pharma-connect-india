package com.example.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PharmaViewModel(private val repository: PharmaRepository) : ViewModel() {

    enum class AppRole {
        CUSTOMER, PHARMACY, ADMIN
    }

    // Role state
    private val _currentRole = MutableStateFlow(AppRole.CUSTOMER)
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    // Customer search states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Medicine search results: reactively updates on query
    val searchResults: StateFlow<List<Medicine>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.searchMedicines(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All pharmacies
    val pharmacies: StateFlow<List<Pharmacy>> = repository.allPharmacies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All orders
    val orders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All sync logs
    val syncLogs: StateFlow<List<SyncLog>> = repository.allSyncLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Prescriptions
    val prescriptions: StateFlow<List<Prescription>> = repository.allPrescriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Payment Proofs
    val paymentProofs: StateFlow<List<PaymentProof>> = repository.allPaymentProofs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Pharmacy or Medicine detail
    private val _selectedPharmacy = MutableStateFlow<Pharmacy?>(null)
    val selectedPharmacy: StateFlow<Pharmacy?> = _selectedPharmacy.asStateFlow()

    private val _selectedMedicine = MutableStateFlow<Medicine?>(null)
    val selectedMedicine: StateFlow<Medicine?> = _selectedMedicine.asStateFlow()

    // Active pharmacy being configured in Pharmacy Owner dashboard
    private val _activePharmacyId = MutableStateFlow("abc_medical")
    val activePharmacyId: StateFlow<String> = _activePharmacyId.asStateFlow()

    val activePharmacy: StateFlow<Pharmacy?> = _activePharmacyId
        .flatMapLatest { id ->
            repository.allPharmacies.map { list -> list.find { it.id == id } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // AI states
    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // ERP and Connector states
    private val _selectedErp = MutableStateFlow("Marg ERP")
    val selectedErp: StateFlow<String> = _selectedErp.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _isAutoSyncEnabled = MutableStateFlow(false)
    val isAutoSyncEnabled: StateFlow<Boolean> = _isAutoSyncEnabled.asStateFlow()

    // Cart / Order placement state
    private val _cartItem = MutableStateFlow<Medicine?>(null)
    val cartItem: StateFlow<Medicine?> = _cartItem.asStateFlow()

    private val _cartQuantity = MutableStateFlow(1)
    val cartQuantity: StateFlow<Int> = _cartQuantity.asStateFlow()

    private val _orderPlacedSuccessfully = MutableStateFlow(false)
    val orderPlacedSuccessfully: StateFlow<Boolean> = _orderPlacedSuccessfully.asStateFlow()

    init {
        viewModelScope.launch {
            // Guarantee database mock entries are pre-populated
            repository.prepopulateIfEmpty()
        }
        startAutoSyncTimer()
    }

    fun setRole(role: AppRole) {
        _currentRole.value = role
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectPharmacy(pharmacy: Pharmacy?) {
        _selectedPharmacy.value = pharmacy
    }

    fun selectMedicine(medicine: Medicine?) {
        _selectedMedicine.value = medicine
        _aiResponse.value = null // Reset previous AI suggestions
    }

    fun setErp(erp: String) {
        _selectedErp.value = erp
    }

    fun setActivePharmacyId(id: String) {
        _activePharmacyId.value = id
    }

    fun addToCart(medicine: Medicine) {
        _cartItem.value = medicine
        _cartQuantity.value = 1
        _orderPlacedSuccessfully.value = false
    }

    fun incrementCartQuantity() {
        if (_cartQuantity.value < (_cartItem.value?.stockCount ?: 1)) {
            _cartQuantity.value += 1
        }
    }

    fun decrementCartQuantity() {
        if (_cartQuantity.value > 1) {
            _cartQuantity.value -= 1
        }
    }

    fun checkoutOrder(customerName: String, customerPhone: String, address: String, isHomeDelivery: Boolean) {
        val med = _cartItem.value ?: return
        viewModelScope.launch {
            val total = med.sellingPrice * _cartQuantity.value
            val order = Order(
                pharmacyId = med.pharmacyId,
                pharmacyName = getPharmacyNameSync(med.pharmacyId),
                medicineName = med.name,
                quantity = _cartQuantity.value,
                unitPrice = med.sellingPrice,
                totalPrice = total,
                customerName = customerName,
                customerPhone = customerPhone,
                deliveryAddress = if (isHomeDelivery) address else "Store Pickup",
                isHomeDelivery = isHomeDelivery,
                status = "Pending"
            )
            repository.insertOrder(order)
            
            // Subtract stock locally to make it feel real-time!
            val updatedMed = med.copy(stockCount = maxOf(0, med.stockCount - _cartQuantity.value))
            repository.medicineDao.insertMedicine(updatedMed)

            triggerSimulatedNotification(
                type = _activeNotificationChannel.value,
                title = "Payment Successful 🎉",
                message = "₹$total paid securely via ${_activePaymentGateway.value}! Order for ${_cartQuantity.value}x ${med.name} sent to ${order.pharmacyName}."
            )

            _orderPlacedSuccessfully.value = true
            _cartItem.value = null
        }
    }

    private suspend fun getPharmacyNameSync(id: String): String {
        return repository.getPharmacyById(id)?.name ?: "Unknown Pharmacy"
    }

    // Live AI Medicine explanation & alternatives using Gemini API
    fun askAiAboutMedicine(medicine: Medicine) {
        _isAiLoading.value = true
        _aiResponse.value = null
        
        viewModelScope.launch {
            val prompt = """
                Provide a friendly and concise clinical overview of the medicine "${medicine.name}".
                Salt composition: ${medicine.saltComposition}.
                Manufacturer: ${medicine.manufacturer}.
                
                Please cover:
                1. What is this medicine used for?
                2. Explain its salt and active composition simply.
                3. Suggest safe medical alternatives commonly found in India (such as ${medicine.alternativesText}).
                4. Key safety warning / precautions.
                
                Keep it beautifully formatted with structured bullet points, clear headings, and compact paragraphs suitable for a mobile screen.
            """.trimIndent()

            val response = GeminiClient.askGemini(
                prompt = prompt,
                systemPrompt = "You are an expert pharmacological AI assistant for 'Pharma Connect'. Provide accurate, helpful, and concise information in standard English."
            )
            _aiResponse.value = response
            _isAiLoading.value = false
        }
    }

    // Prescription OCR & Drug Extraction Simulation with Gemini API
    fun uploadAndAnalyzePrescription(prescriptionText: String) {
        _isAiLoading.value = true
        _aiResponse.value = null

        viewModelScope.launch {
            // First, insert dummy prescription record
            val pId = repository.insertPrescription(
                Prescription(
                    imageUrl = "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=400",
                    status = "Processing"
                )
            )

            val prompt = """
                The patient uploaded a doctor's handwritten prescription note containing the following text:
                "$prescriptionText"
                
                Please act as a pharmacy OCR assistant:
                1. Detect any medicine names, strengths (e.g. 650mg, 500mg), and dosage instructions (e.g. TDS, BD, twice daily).
                2. Match these against standard medicines like Paracetamol 650, Ibuprofen 400, Amoxicillin 500, Metformin 500, Pantoprazole 40.
                3. Structure the output as a clean, structured analysis with:
                   - Patient Guidelines
                   - Medicines list (Name, Strength, Frequency, Duration)
                   - Suggested actions (e.g. "We found Paracetamol 650 in ABC Medical for Rs 27. Click to order.")
                
                Format it beautifully for mobile display.
            """.trimIndent()

            val response = GeminiClient.askGemini(
                prompt = prompt,
                systemPrompt = "You are a professional medical prescription parser. Extract medicines and structure instructions cleanly."
            )

            _aiResponse.value = response
            _isAiLoading.value = false

            // Identify a matching drug to pre-populate search query
            val matchedDrug = when {
                response.contains("Paracetamol", ignoreCase = true) -> "Paracetamol 650"
                response.contains("Ibuprofen", ignoreCase = true) -> "Ibuprofen 400"
                response.contains("Amoxicillin", ignoreCase = true) -> "Amoxicillin 500"
                response.contains("Metformin", ignoreCase = true) -> "Metformin 500"
                response.contains("Pantoprazole", ignoreCase = true) -> "Pantoprazole 40"
                else -> ""
            }

            repository.updatePrescription(
                Prescription(
                    id = pId.toInt(),
                    imageUrl = "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=400",
                    extractedText = prescriptionText,
                    detectedMedicines = matchedDrug,
                    status = "Completed"
                )
            )

            if (matchedDrug.isNotEmpty()) {
                _searchQuery.value = matchedDrug
            }
        }
    }

    // Simulated ERP Inventory Sync
    fun triggerOneClickSync(erp: String) {
        if (_isSyncing.value) return
        _isSyncing.value = true

        viewModelScope.launch {
            delay(1500) // Simulate database scan & connection overhead

            val currentId = _activePharmacyId.value
            // Generate some random stock variation to show dynamic live updates!
            val updatedMedicines = listOf(
                Medicine(
                    pharmacyId = currentId,
                    name = "Paracetamol 650",
                    mrp = 30.0,
                    sellingPrice = (24..28).random().toDouble(),
                    discountPercent = (5..20).random(),
                    stockCount = (50..200).random(),
                    manufacturer = "GSK Consumer Healthcare",
                    expiryDate = "12/2027",
                    batchNumber = "B-PT" + (8000..9000).random(),
                    saltComposition = "Paracetamol 650mg",
                    alternativesText = "Crocin 650, Calpol 650, Dolo 650"
                ),
                Medicine(
                    pharmacyId = currentId,
                    name = "Ibuprofen 400",
                    mrp = 25.0,
                    sellingPrice = (18..23).random().toDouble(),
                    discountPercent = (10..25).random(),
                    stockCount = (30..150).random(),
                    manufacturer = "Abbott India Ltd",
                    expiryDate = "08/2027",
                    batchNumber = "B-IB" + (300..400).random(),
                    saltComposition = "Ibuprofen 400mg",
                    alternativesText = "Brufen 400, Ibugesic 400"
                ),
                Medicine(
                    pharmacyId = currentId,
                    name = "Atorvastatin 10",
                    mrp = 75.0,
                    sellingPrice = (60..70).random().toDouble(),
                    discountPercent = (10..18).random(),
                    stockCount = (20..90).random(),
                    manufacturer = "Pfizer Ltd",
                    expiryDate = "10/2026",
                    batchNumber = "B-AT" + (900..1000).random(),
                    saltComposition = "Atorvastatin Calcium 10mg",
                    alternativesText = "Lipitor 10, Atorva 10, Tonact 10"
                ),
                Medicine(
                    pharmacyId = currentId,
                    name = "Amoxicillin 500",
                    mrp = 110.0,
                    sellingPrice = (85..100).random().toDouble(),
                    discountPercent = (10..20).random(),
                    stockCount = (10..100).random(),
                    manufacturer = "GlaxoSmithKline",
                    expiryDate = "04/2026",
                    batchNumber = "B-AMX" + (10..99).random(),
                    saltComposition = "Amoxicillin 500mg",
                    alternativesText = "Novamox 500, Mox 500"
                ),
                Medicine(
                    pharmacyId = currentId,
                    name = "Metformin 500",
                    mrp = 40.0,
                    sellingPrice = (28..35).random().toDouble(),
                    discountPercent = (15..30).random(),
                    stockCount = (100..300).random(),
                    manufacturer = "USV Biotech",
                    expiryDate = "03/2028",
                    batchNumber = "B-MT" + (500..600).random(),
                    saltComposition = "Metformin Hydrochloride 500mg",
                    alternativesText = "Glycomet 500, Gluformin 500"
                ),
                Medicine(
                    pharmacyId = currentId,
                    name = "Pantoprazole 40",
                    mrp = 120.0,
                    sellingPrice = (95..110).random().toDouble(),
                    discountPercent = (8..20).random(),
                    stockCount = (40..180).random(),
                    manufacturer = "Alkem Laboratories",
                    expiryDate = "09/2027",
                    batchNumber = "B-PN" + (300..400).random(),
                    saltComposition = "Pantoprazole Sodium 40mg",
                    alternativesText = "Pan 40, Pantocid 40"
                )
            )

            repository.syncPharmacyInventory(
                pharmacyId = currentId,
                erpType = erp,
                updatedMedicines = updatedMedicines
            )

            _isSyncing.value = false
        }
    }

    // Toggle 30 second Auto Sync simulation
    fun setAutoSyncEnabled(enabled: Boolean) {
        _isAutoSyncEnabled.value = enabled
    }

    private fun startAutoSyncTimer() {
        viewModelScope.launch {
            while (true) {
                delay(30000) // 30 seconds interval
                if (_isAutoSyncEnabled.value) {
                    triggerOneClickSync(_selectedErp.value)
                }
            }
        }
    }

    // Update Pharmacy Subscription Plan
    fun selectSubscriptionTier(tier: String) {
        viewModelScope.launch {
            val pharmacy = activePharmacy.value ?: return@launch
            val updated = pharmacy.copy(subscriptionTier = tier)
            repository.updatePharmacy(updated)
        }
    }

    // Update order status in pharmacy owner view
    fun updateOrderStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            val orderList = repository.allOrders.first()
            val order = orderList.find { it.id == orderId } ?: return@launch
            repository.updateOrder(order.copy(status = newStatus))
        }
    }

    // Submit Payment Proof (Subscription or Commission)
    fun submitPaymentProof(
        paymentType: String,
        amount: Double,
        utrNumber: String,
        paymentDate: String,
        tierRequested: String = ""
    ) {
        viewModelScope.launch {
            val pharmacy = activePharmacy.value ?: return@launch
            val proof = PaymentProof(
                pharmacyId = pharmacy.id,
                pharmacyName = pharmacy.name,
                paymentType = paymentType,
                amount = amount,
                utrNumber = utrNumber,
                paymentDate = paymentDate,
                status = "Pending",
                tierRequested = tierRequested
            )
            repository.insertPaymentProof(proof)
        }
    }

    // Approve Payment Proof
    fun approvePaymentProof(paymentId: Int) {
        viewModelScope.launch {
            val list = repository.allPaymentProofs.first()
            val proof = list.find { it.id == paymentId } ?: return@launch
            val updatedProof = proof.copy(status = "Approved")
            repository.updatePaymentProof(updatedProof)

            // If it is a subscription tier request, update that pharmacy's subscription plan!
            if (proof.paymentType == "Subscription" && proof.tierRequested.isNotEmpty()) {
                val targetPharmacy = repository.getPharmacyById(proof.pharmacyId)
                if (targetPharmacy != null) {
                    repository.updatePharmacy(targetPharmacy.copy(subscriptionTier = proof.tierRequested))
                }
            }
        }
    }

    // Reject Payment Proof
    fun rejectPaymentProof(paymentId: Int) {
        viewModelScope.launch {
            val list = repository.allPaymentProofs.first()
            val proof = list.find { it.id == paymentId } ?: return@launch
            val updatedProof = proof.copy(status = "Rejected")
            repository.updatePaymentProof(updatedProof)
        }
    }

    // --- Integration States & Simulated Configurations ---
    // Authentication
    private val _activeAuthMethod = MutableStateFlow("Firebase Auth")
    val activeAuthMethod: StateFlow<String> = _activeAuthMethod.asStateFlow()

    private val _isUserAuthenticated = MutableStateFlow(false)
    val isUserAuthenticated: StateFlow<Boolean> = _isUserAuthenticated.asStateFlow()

    private val _authenticatedUser = MutableStateFlow<String?>(null)
    val authenticatedUser: StateFlow<String?> = _authenticatedUser.asStateFlow()

    // Maps
    private val _activeMapProvider = MutableStateFlow("Google Maps")
    val activeMapProvider: StateFlow<String> = _activeMapProvider.asStateFlow()

    private val _isTrafficLayerEnabled = MutableStateFlow(true)
    val isTrafficLayerEnabled: StateFlow<Boolean> = _isTrafficLayerEnabled.asStateFlow()

    // Payments
    private val _activePaymentGateway = MutableStateFlow("Razorpay")
    val activePaymentGateway: StateFlow<String> = _activePaymentGateway.asStateFlow()

    private val _razorpayApiKey = MutableStateFlow("rzp_live_K90A1sX9P2bC")
    val razorpayApiKey: StateFlow<String> = _razorpayApiKey.asStateFlow()

    private val _phonePeMerchantId = MutableStateFlow("MID-PHONEPE-99210")
    val phonePeMerchantId: StateFlow<String> = _phonePeMerchantId.asStateFlow()

    private val _stripePublishableKey = MutableStateFlow("pk_live_51O2aB9D19E5S92D")
    val stripePublishableKey: StateFlow<String> = _stripePublishableKey.asStateFlow()

    private val _upiVpaId = MutableStateFlow("pay@pharma")
    val upiVpaId: StateFlow<String> = _upiVpaId.asStateFlow()

    // Notifications
    private val _activeNotificationChannel = MutableStateFlow("Firebase Cloud Messaging")
    val activeNotificationChannel: StateFlow<String> = _activeNotificationChannel.asStateFlow()

    // ERP Connector Drivers State
    private val _isMargConnectorActive = MutableStateFlow(true)
    val isMargConnectorActive: StateFlow<Boolean> = _isMargConnectorActive.asStateFlow()

    private val _isTallyXmlConnectorActive = MutableStateFlow(true)
    val isTallyXmlConnectorActive: StateFlow<Boolean> = _isTallyXmlConnectorActive.asStateFlow()

    private val _isRetailGraphSqlConnectorActive = MutableStateFlow(false)
    val isRetailGraphSqlConnectorActive: StateFlow<Boolean> = _isRetailGraphSqlConnectorActive.asStateFlow()

    // Security, Compliance & Privacy Controls
    private val _isAesEncryptionActive = MutableStateFlow(true)
    val isAesEncryptionActive: StateFlow<Boolean> = _isAesEncryptionActive.asStateFlow()

    private val _isDataAnonymizationActive = MutableStateFlow(true)
    val isDataAnonymizationActive: StateFlow<Boolean> = _isDataAnonymizationActive.asStateFlow()

    // Subscription Strategy Configuration
    private val _trialPeriodMonths = MutableStateFlow(3)
    val trialPeriodMonths: StateFlow<Int> = _trialPeriodMonths.asStateFlow()

    private val _monthlySubscriptionPrice = MutableStateFlow(499)
    val monthlySubscriptionPrice: StateFlow<Int> = _monthlySubscriptionPrice.asStateFlow()

    private val _isOnboardingDiscountActive = MutableStateFlow(true)
    val isOnboardingDiscountActive: StateFlow<Boolean> = _isOnboardingDiscountActive.asStateFlow()

    // Real-time floating heads-up alert triggered across components
    private val _activeHeadsUpNotification = MutableStateFlow<IntegrationNotification?>(null)
    val activeHeadsUpNotification: StateFlow<IntegrationNotification?> = _activeHeadsUpNotification.asStateFlow()

    // History of sent notifications
    private val _notificationHistory = MutableStateFlow<List<IntegrationNotification>>(emptyList())
    val notificationHistory: StateFlow<List<IntegrationNotification>> = _notificationHistory.asStateFlow()

    fun setAuthMethod(method: String) {
        _activeAuthMethod.value = method
    }

    fun loginUser(userName: String, method: String) {
        _authenticatedUser.value = userName
        _isUserAuthenticated.value = true
        _activeAuthMethod.value = method
        triggerSimulatedNotification(
            type = _activeNotificationChannel.value,
            title = "Security Alert",
            message = "New login detected via $method for $userName"
        )
    }

    fun logoutUser() {
        _authenticatedUser.value = null
        _isUserAuthenticated.value = false
    }

    fun setMapProvider(provider: String) {
        _activeMapProvider.value = provider
    }

    fun toggleTrafficLayer() {
        _isTrafficLayerEnabled.value = !_isTrafficLayerEnabled.value
    }

    fun setPaymentGateway(gateway: String) {
        _activePaymentGateway.value = gateway
    }

    fun setRazorpayKey(key: String) {
        _razorpayApiKey.value = key
    }

    fun setPhonePeMerchantId(mid: String) {
        _phonePeMerchantId.value = mid
    }

    fun setStripeKey(key: String) {
        _stripePublishableKey.value = key
    }

    fun setUpiVpa(vpa: String) {
        _upiVpaId.value = vpa
    }

    fun setNotificationChannel(channel: String) {
        _activeNotificationChannel.value = channel
    }

    fun triggerSimulatedNotification(type: String, title: String, message: String) {
        val notification = IntegrationNotification(
            id = System.currentTimeMillis().toString(),
            type = type,
            sender = when (type) {
                "Firebase Cloud Messaging" -> "FCM Server (Google)"
                "SMS" -> "Twilio Gateway"
                "WhatsApp" -> "WhatsApp Business"
                "Email" -> "AWS SES Mailer"
                else -> "System"
            },
            title = title,
            message = message
        )
        _activeHeadsUpNotification.value = notification
        _notificationHistory.value = listOf(notification) + _notificationHistory.value

        // Clear after 6 seconds
        viewModelScope.launch {
            delay(6000)
            if (_activeHeadsUpNotification.value?.id == notification.id) {
                _activeHeadsUpNotification.value = null
            }
        }
    }

    fun dismissHeadsUp() {
        _activeHeadsUpNotification.value = null
    }

    // --- ERP Connector Driver Toggles ---
    fun toggleMargConnector() {
        _isMargConnectorActive.value = !_isMargConnectorActive.value
        triggerSimulatedNotification(
            type = _activeNotificationChannel.value,
            title = "Connector Config Changed",
            message = "Marg ERP API Driver is now " + (if (_isMargConnectorActive.value) "ENABLED" else "DISABLED")
        )
    }

    fun toggleTallyXmlConnector() {
        _isTallyXmlConnectorActive.value = !_isTallyXmlConnectorActive.value
        triggerSimulatedNotification(
            type = _activeNotificationChannel.value,
            title = "Connector Config Changed",
            message = "Tally XML SOAP Schema Mapper is now " + (if (_isTallyXmlConnectorActive.value) "ENABLED" else "DISABLED")
        )
    }

    fun toggleRetailGraphSqlConnector() {
        _isRetailGraphSqlConnectorActive.value = !_isRetailGraphSqlConnectorActive.value
        triggerSimulatedNotification(
            type = _activeNotificationChannel.value,
            title = "Connector Config Changed",
            message = "RetailGraph SQL Driver Connector is now " + (if (_isRetailGraphSqlConnectorActive.value) "ENABLED" else "DISABLED")
        )
    }

    // --- Security, Compliance & Privacy Controls ---
    fun toggleAesEncryption() {
        _isAesEncryptionActive.value = !_isAesEncryptionActive.value
        triggerSimulatedNotification(
            type = _activeNotificationChannel.value,
            title = "Security Compliance Update",
            message = "Prescription database AES-256 field-level encryption " + (if (_isAesEncryptionActive.value) "ENABLED" else "DISABLED")
        )
    }

    fun toggleDataAnonymization() {
        _isDataAnonymizationActive.value = !_isDataAnonymizationActive.value
        triggerSimulatedNotification(
            type = _activeNotificationChannel.value,
            title = "Privacy Regulation Update",
            message = "User data HIPAA & GDPR compliant anonymization layers " + (if (_isDataAnonymizationActive.value) "ENABLED" else "DISABLED")
        )
    }

    // --- Subscription Pricing Strategy configuration ---
    fun setSubscriptionStrategy(months: Int, price: Int) {
        _trialPeriodMonths.value = months
        _monthlySubscriptionPrice.value = price
        triggerSimulatedNotification(
            type = _activeNotificationChannel.value,
            title = "SaaS Strategy Plan Updated",
            message = "Onboarding strategy updated: $months Months FREE Trial followed by ₹$price/month!"
        )
    }

    fun toggleOnboardingDiscount() {
        _isOnboardingDiscountActive.value = !_isOnboardingDiscountActive.value
        triggerSimulatedNotification(
            type = _activeNotificationChannel.value,
            title = "Campaign Config Changed",
            message = "Promo onboarding campaign " + (if (_isOnboardingDiscountActive.value) "ACTIVE" else "INACTIVE")
        )
    }
}

data class IntegrationNotification(
    val id: String,
    val type: String, // "Firebase Cloud Messaging", "SMS", "WhatsApp", "Email"
    val sender: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

