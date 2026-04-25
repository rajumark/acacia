package org.sample.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import com.acacia.dsl.generated.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        setContent {
            MaterialTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .padding(all = 12.dp)
                        .padding()
                        .padding(paddingValues = PaddingValues(16.dp))
                ) {}
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .ac_padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
                        .ac_padding(horizontal = 8.dp, vertical = 8.dp)
                        .ac_padding(all = 12.dp)
                        .ac_padding()
                        .ac_padding(paddingValues = PaddingValues(16.dp))
                ) {}
            }
        }
    }
}
