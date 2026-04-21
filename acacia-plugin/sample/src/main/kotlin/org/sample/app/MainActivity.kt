package org.sample.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        setContent {
            MaterialTheme {
                var showStandard by remember { mutableStateOf(true) }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth().fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(listOf(Color(0xFFF5F5F5))), 
                            shape = RectangleShape
                        )
                ) {
                    // Toggle buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showStandard = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showStandard) Color(0xFF3498DB) else Color.Gray
                            )
                        ) {
                            Text("Standard UI", color = Color.White)
                        }
                        
                        Button(
                            onClick = { showStandard = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!showStandard) Color(0xFF27AE60) else Color.Gray
                            )
                        ) {
                            Text("Acacia UI", color = Color.White)
                        }
                    }
                    
                    // Content area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (showStandard) {
                            DashboardUIStandardCompose()
                        } else {
                            DashboardUIAcaciaComposeShort()
                        }
                    }
                }
            }
        }
    }
}
