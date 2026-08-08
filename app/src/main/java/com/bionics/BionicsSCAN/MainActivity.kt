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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bionics.BionicsSCAN.data.LocalBeltRepository
import com.bionics.BionicsSCAN.service.SheetsService
import com.bionics.BionicsSCAN.ui.screens.BarcodeLabelsScreen
import com.bionics.BionicsSCAN.ui.screens.BeltDetailScreen
import com.bionics.BionicsSCAN.ui.screens.BeltListScreen
import com.bionics.BionicsSCAN.ui.screens.ScanScreen
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
        
        val repository = LocalBeltRepository()
        
        // Try to load Google Sheets credentials
        val sheetsService = try {
            val credentialsStream = assets.open("credentials.json")
            SheetsService(credentialsStream)
        } catch (e: Exception) {
            null // Credentials not found, will use local-only mode
        }
        
        viewModel = BeltViewModel(repository, sheetsService)
        
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
                    val belt = viewModel.getBeltByBarcode(barcode)
                    if (belt != null) {
                        navController.navigate("belt_detail/${belt.id}")
                    }
                }
            )
        }
        
        composable("belt_detail/{beltId}") { backStackEntry ->
            val beltId = backStackEntry.arguments?.getString("beltId") ?: return@composable
            val belt = viewModel.belts.value.find { it.id == beltId }
            
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