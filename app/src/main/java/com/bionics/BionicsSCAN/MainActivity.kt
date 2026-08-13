package com.bionics.BionicsSCAN

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bionics.BionicsSCAN.ui.screens.*
import com.bionics.BionicsSCAN.ui.theme.BionicsSCANTheme
import com.bionics.BionicsSCAN.viewmodel.BeltViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private lateinit var viewModel: BeltViewModel
    private lateinit var navController: NavHostController
    
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