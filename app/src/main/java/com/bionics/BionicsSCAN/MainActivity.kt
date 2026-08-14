package com.bionics.BionicsSCAN

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bionics.BionicsSCAN.scanner.HardwareBarcodeScanner
import com.bionics.BionicsSCAN.ui.screens.*
import com.bionics.BionicsSCAN.ui.theme.BionicsSCANTheme
import com.bionics.BionicsSCAN.viewmodel.BeltViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private lateinit var viewModel: BeltViewModel
    private lateinit var navController: NavHostController
    private lateinit var hardwareScanner: HardwareBarcodeScanner
    
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Handle permission result if needed
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val app = application as BionicsScanApplication
        val repository = app.inventoryRepository
        val syncScheduler = app.syncScheduler
        
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BeltViewModel(repository, syncScheduler) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[BeltViewModel::class.java]
        
        hardwareScanner = HardwareBarcodeScanner { barcode ->
            routeHardwareScan(barcode)
        }
        
        setContent {
            BionicsSCANTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    navController = rememberNavController()
                    
                    // Request camera permission
                    LaunchedEffect(Unit) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.CAMERA
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                    
                    AppNavigation(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }
    }

    /**
     * Global capture for Bluetooth/USB HID barcode scanners (e.g. Zebra DS3678).
     * Scans arrive as key events; this forwards them to the hardware scanner
     * handler, which routes B9/B15/GR/SP scans to their respective window.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (hardwareScanner.handleKeyEvent(event)) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    /**
     * Routes a hardware-scanned barcode to the window for its inventory type.
     *
     * The inventory type tab is selected from the barcode prefix, then we land
     * on the main list for that category. If the scanned item already exists in
     * inventory, we go straight to its detail screen on top of that list.
     * Camera scanning is unaffected.
     */
    private fun routeHardwareScan(barcode: String) {
        val inventoryType = HardwareBarcodeScanner.inventoryTypeForBarcode(barcode) ?: return

        viewModel.setInventoryType(inventoryType)
        viewModel.setSearchQuery("")

        lifecycleScope.launch {
            val belt = viewModel.getBeltByBarcode(barcode)

            navController.navigate("belt_list") {
                popUpTo(navController.graph.id) { inclusive = false }
                launchSingleTop = true
            }

            if (belt != null) {
                navController.navigate("belt_detail/${belt.id}")
            }
        }
    }
}

@Composable
fun AppNavigation(
    viewModel: BeltViewModel,
    navController: NavHostController
) {
    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    NavHost(
        navController = navController,
        startDestination = "belt_list"
    ) {
        composable("belt_list") {
            BeltListScreen(
                viewModel = viewModel,
                onScanClick = {
                    navController.navigate("scan")
                },
                onBeltClick = { belt ->
                    navController.navigate("belt_detail/${belt.id}")
                },
                onLabelsClick = {
                    navController.navigate("barcode_labels")
                },
                onLowStockClick = {
                    navController.navigate("stock_filter/LOW_STOCK")
                },
                onOutOfStockClick = {
                    navController.navigate("stock_filter/OUT_OF_STOCK")
                }
            )
        }
        
        composable("stock_filter/{filterType}") { backStackEntry ->
            val filterType = backStackEntry.arguments?.getString("filterType") ?: "LOW_STOCK"
            StockFilterScreen(
                title = if (filterType == "LOW_STOCK") "Low Stock Items" else "Out of Stock Items",
                viewModel = viewModel,
                filterType = filterType,
                onBackClick = { navController.popBackStack() },
                onBeltClick = { belt ->
                    navController.navigate("belt_detail/${belt.id}")
                }
            )
        }
        
        composable("scan") {
            ScanScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onBarcodeScanned = { barcode ->
                    scannedBarcode = barcode
                    scope.launch {
                        val belt = viewModel.getBeltByBarcode(barcode)
                        if (belt != null) {
                            navController.navigate("belt_detail/${belt.id}")
                        }
                    }
                }
            )
        }
        
        composable("belt_detail/{beltId}") { backStackEntry ->
            val beltId = backStackEntry.arguments?.getString("beltId") ?: return@composable
            val belts by viewModel.belts.collectAsState()
            val belt = belts.find { it.id == beltId }
            
            if (belt != null) {
                BeltDetailScreen(
                    belt = belt,
                    viewModel = viewModel,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        composable("barcode_labels") {
            BarcodeLabelsScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
