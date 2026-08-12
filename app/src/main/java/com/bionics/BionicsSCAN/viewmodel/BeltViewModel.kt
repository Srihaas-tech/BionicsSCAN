package com.bionics.BionicsSCAN.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.bionics.BionicsSCAN.data.Belt
import com.bionics.BionicsSCAN.data.InventoryRepository
import com.bionics.BionicsSCAN.data.InventoryType
import com.bionics.BionicsSCAN.sync.InventorySyncScheduler
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SyncStatus {
    ONLINE,
    SYNCING,
    OFFLINE,
    PENDING_CHANGES,
    SYNC_ERROR
}

class BeltViewModel(
    private val repository: InventoryRepository,
    private val syncScheduler: InventorySyncScheduler
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _syncStatus = MutableStateFlow(SyncStatus.OFFLINE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    private val _selectedInventoryType = MutableStateFlow(InventoryType.BELT_9MM)
    val selectedInventoryType: StateFlow<InventoryType> = _selectedInventoryType.asStateFlow()
    
    private val _scannedBarcode = MutableStateFlow<String?>(null)
    val scannedBarcode: StateFlow<String?> = _scannedBarcode.asStateFlow()
    
    val pendingCount: StateFlow<Int> = repository.getPendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val belts: StateFlow<List<Belt>> = combine(
        _selectedInventoryType,
        repository.getAllBelts()
    ) { type, allBelts ->
        allBelts
            .filter { it.inventoryType == type }
            .sortedBy { it.length }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var pollingJob: Job? = null
    
    init {
        viewModelScope.launch {
            if (repository.isEmpty()) {
                refreshFromBackend()
            } else {
                _syncStatus.value = SyncStatus.ONLINE
            }
        }
        
        viewModelScope.launch {
            pendingCount.collect { count ->
                if (count > 0 && _syncStatus.value != SyncStatus.SYNCING) {
                    _syncStatus.value = SyncStatus.PENDING_CHANGES
                } else if (count == 0 && (_syncStatus.value == SyncStatus.PENDING_CHANGES || _syncStatus.value == SyncStatus.OFFLINE)) {
                    _syncStatus.value = SyncStatus.ONLINE
                }
            }
        }
        
        syncScheduler.schedulePeriodicSync()
        startLivePolling()
    }

    private fun startLivePolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(15_000) // Poll every 15 seconds for "live" feel
                if (pendingCount.value == 0 && _syncStatus.value != SyncStatus.SYNCING) {
                    Log.d("Sync", "Starting background poll...")
                    repository.syncInventory()
                        .onSuccess { 
                            _syncStatus.value = SyncStatus.ONLINE 
                        }
                        .onFailure { e -> 
                            Log.e("Sync", "Background poll failed", e)
                        }
                }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
    
    fun setInventoryType(inventoryType: InventoryType) {
        _selectedInventoryType.value = inventoryType
    }
    
    fun refreshFromBackend() {
        viewModelScope.launch {
            _isLoading.value = true
            _syncStatus.value = SyncStatus.SYNCING
            Log.d("Sync", "Manual refresh started")
            
            repository.syncInventory()
                .onSuccess {
                    _syncStatus.value = SyncStatus.ONLINE
                    _error.value = null
                    Log.d("Sync", "Manual refresh success")
                }
                .onFailure { exception ->
                    _syncStatus.value = SyncStatus.SYNC_ERROR
                    _error.value = "Sync failed: ${exception.message}"
                    Log.e("Sync", "Manual refresh failed", exception)
                }
            
            _isLoading.value = false
        }
    }

    fun loadBelts() = refreshFromBackend()
    
    fun onBarcodeScanned(barcode: String) {
        _scannedBarcode.value = barcode
    }
    
    suspend fun getBeltByBarcode(barcode: String): Belt? {
        return repository.getBeltByBarcode(barcode)
    }
    
    fun checkoutBelt(beltId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.checkoutBelt(beltId)
                .onSuccess {
                    syncScheduler.scheduleImmediateSync()
                }
                .onFailure { exception ->
                    _error.value = "Checkout failed: ${exception.message}"
                }
            
            _isLoading.value = false
        }
    }
    
    fun checkinBelt(beltId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.checkinBelt(beltId)
                .onSuccess {
                    syncScheduler.scheduleImmediateSync()
                }
                .onFailure { exception ->
                    _error.value = "Checkin failed: ${exception.message}"
                }
            
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
