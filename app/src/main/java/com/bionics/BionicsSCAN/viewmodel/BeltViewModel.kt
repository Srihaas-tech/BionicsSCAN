package com.bionics.BionicsSCAN.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bionics.BionicsSCAN.data.Belt
import com.bionics.BionicsSCAN.data.LocalBeltRepository
import com.bionics.BionicsSCAN.service.SheetsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.io.InputStream

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
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    
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
    
    fun loadBelts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            localRepository.getAllBelts().collect { belts ->
                _belts.value = belts
            }
            
            _isLoading.value = false
        }
    }
    
    fun syncWithSpreadsheet() {
        if (sheetsService == null) {
            return
        }
        
        viewModelScope.launch {
            _isSyncing.value = true
            
            sheetsService.getAllBelts()
                .onSuccess { remoteBelts ->
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
                    // Only show error if credentials were initialized but sync failed
                    // Don't show error for invalid credentials (silently fail and use local data)
                    if (exception.message?.contains("Invalid credentials") != true) {
                        _error.value = "Sync failed: ${exception.message}"
                    }
                }
            
            _isSyncing.value = false
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
            
            // Update local first
            val localSuccess = localRepository.checkoutBelt(beltId)
            
            if (localSuccess) {
                // Update spreadsheet if configured
                sheetsService?.checkoutBelt(beltId)
                    ?.onFailure { exception ->
                        _error.value = "Local checkout succeeded but spreadsheet update failed: ${exception.message}"
                    }
                
                // Immediately update UI with latest data
                val updatedBelts = localRepository.getAllBeltsList()
                _belts.value = updatedBelts
                _scannedBarcode.value = null
            } else {
                _error.value = "Failed to checkout belt"
            }
            
            _isLoading.value = false
        }
    }
    
    fun checkinBelt(beltId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Update local first
            val localSuccess = localRepository.checkinBelt(beltId)
            
            if (localSuccess) {
                // Update spreadsheet if configured
                sheetsService?.checkinBelt(beltId)
                    ?.onFailure { exception ->
                        _error.value = "Local checkin succeeded but spreadsheet update failed: ${exception.message}"
                    }
                
                // Immediately update UI with latest data
                val updatedBelts = localRepository.getAllBeltsList()
                _belts.value = updatedBelts
                _scannedBarcode.value = null
            } else {
                _error.value = "Failed to checkin belt"
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
