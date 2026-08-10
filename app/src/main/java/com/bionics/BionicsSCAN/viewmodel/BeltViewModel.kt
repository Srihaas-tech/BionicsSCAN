package com.bionics.BionicsSCAN.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bionics.BionicsSCAN.data.Belt
import com.bionics.BionicsSCAN.data.InventoryType
import com.bionics.BionicsSCAN.data.LocalBeltRepository
import com.bionics.BionicsSCAN.service.SheetsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.io.InputStream

enum class SyncStatus {
    ONLINE,
    SYNCING,
    OFFLINE
}

class BeltViewModel(
    private val localRepository: LocalBeltRepository,
    private val sheetsService: SheetsService?
) : ViewModel() {
    
    private val _belts = MutableStateFlow<List<Belt>>(emptyList())
    val belts: StateFlow<List<Belt>> = _belts.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _scannedBarcode = MutableStateFlow<String?>(null)
    val scannedBarcode: StateFlow<String?> = _scannedBarcode.asStateFlow()
    
    private val _syncStatus = MutableStateFlow(SyncStatus.OFFLINE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    private val _selectedInventoryType = MutableStateFlow(InventoryType.BELT_9MM)
    val selectedInventoryType: StateFlow<InventoryType> = _selectedInventoryType.asStateFlow()
    
    private val syncInterval = 2000L // 2 seconds in milliseconds
    
    init {
        loadBelts()
        startPeriodicSync()
    }
    
    private fun startPeriodicSync() {
        viewModelScope.launch {
            while (true) {
                delay(syncInterval)
                syncWithSpreadsheet()
            }
        }
    }
    
    fun setInventoryType(inventoryType: InventoryType) {
        _selectedInventoryType.value = inventoryType
        loadBelts()
    }
    
    fun loadBelts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Try online first if service is available
            if (sheetsService != null) {
                _syncStatus.value = SyncStatus.SYNCING
                sheetsService.getAllBeltsByType(_selectedInventoryType.value)
                    .onSuccess { remoteBelts ->
                        _syncStatus.value = SyncStatus.ONLINE
                        // Update local repository with remote data
                        remoteBelts.forEach { remoteBelt ->
                            localRepository.updateBeltQuantity(
                                remoteBelt.id,
                                remoteBelt.quantity
                            )
                        }
                        _belts.value = remoteBelts
                        _isLoading.value = false
                        return@launch
                    }
                    .onFailure {
                        _syncStatus.value = SyncStatus.OFFLINE
                        // Fallback to local if online fails
                    }
            }
            
            // Local fallback
            localRepository.getAllBelts().collect { belts ->
                _belts.value = belts.filter { it.inventoryType == _selectedInventoryType.value }
            }
            
            _isLoading.value = false
        }
    }
    
    fun syncWithSpreadsheet() {
        if (sheetsService == null) {
            _syncStatus.value = SyncStatus.OFFLINE
            return
        }
        
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.SYNCING
            
            sheetsService.getAllBeltsByType(_selectedInventoryType.value)
                .onSuccess { remoteBelts ->
                    _syncStatus.value = SyncStatus.ONLINE
                    // Update local repository with remote data
                    remoteBelts.forEach { remoteBelt ->
                        localRepository.updateBeltQuantity(
                            remoteBelt.id,
                            remoteBelt.quantity
                        )
                    }
                    _belts.value = remoteBelts
                    _error.value = null
                }
                .onFailure { exception ->
                    _syncStatus.value = SyncStatus.OFFLINE
                    // Silent fallback to local data on network issues
                    // Only show error if it's something other than a network/connection issue
                    val errorMsg = exception.message ?: ""
                    if (!errorMsg.contains("timed out") && 
                        !errorMsg.contains("UnknownHostException") && 
                        !errorMsg.contains("connection") &&
                        !errorMsg.contains("Invalid credentials")) {
                        _error.value = "Sync failed: ${exception.message}"
                    }
                    
                    // On failure, ensure we are showing latest local data
                    localRepository.getAllBelts().collect { localBelts ->
                        _belts.value = localBelts.filter { it.inventoryType == _selectedInventoryType.value }
                    }
                }
        }
    }
    
    fun onBarcodeScanned(barcode: String) {
        _scannedBarcode.value = barcode
    }
    
    fun getBeltByBarcode(barcode: String): Belt? {
        return localRepository.getBeltByBarcode(barcode)
    }
    
    fun checkoutBelt(beltId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Try online update first if primary
            var onlineSuccess = false
            if (sheetsService != null) {
                sheetsService.checkoutBelt(beltId, _selectedInventoryType.value)
                    .onSuccess {
                        onlineSuccess = true
                    }
                    .onFailure { exception ->
                        val errorMsg = exception.message ?: ""
                        if (errorMsg.contains("timed out") || errorMsg.contains("connection") || errorMsg.contains("UnknownHostException")) {
                            // Network error, fall back to local only and notify
                            _error.value = "Offline mode: Saved locally. Syncing later."
                        } else {
                            _error.value = "Checkout failed: ${exception.message}"
                            _isLoading.value = false
                            return@launch
                        }
                    }
            }
            
            // Update local repository (either as primary or as fallback/cache)
            val localSuccess = localRepository.checkoutBelt(beltId)
            
            if (localSuccess) {
                // Immediately update UI with latest data
                val updatedBelts = localRepository.getAllBeltsList()
                    .filter { it.inventoryType == _selectedInventoryType.value }
                _belts.value = updatedBelts
                _scannedBarcode.value = null
            } else if (!onlineSuccess) {
                _error.value = "Failed to checkout item"
            }
            
            _isLoading.value = false
        }
    }
    
    fun checkinBelt(beltId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Try online update first if primary
            var onlineSuccess = false
            if (sheetsService != null) {
                sheetsService.checkinBelt(beltId, _selectedInventoryType.value)
                    .onSuccess {
                        onlineSuccess = true
                    }
                    .onFailure { exception ->
                        val errorMsg = exception.message ?: ""
                        if (errorMsg.contains("timed out") || errorMsg.contains("connection") || errorMsg.contains("UnknownHostException")) {
                            // Network error, fall back to local only and notify
                            _error.value = "Offline mode: Saved locally. Syncing later."
                        } else {
                            _error.value = "Checkin failed: ${exception.message}"
                            _isLoading.value = false
                            return@launch
                        }
                    }
            }
            
            // Update local repository (either as primary or as fallback/cache)
            val localSuccess = localRepository.checkinBelt(beltId)
            
            if (localSuccess) {
                // Immediately update UI with latest data
                val updatedBelts = localRepository.getAllBeltsList()
                    .filter { it.inventoryType == _selectedInventoryType.value }
                _belts.value = updatedBelts
                _scannedBarcode.value = null
            } else if (!onlineSuccess) {
                _error.value = "Failed to checkin item"
            }
            
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearScannedBarcode() {
        _scannedBarcode.value = null
    }
}
