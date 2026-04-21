package org.sample.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.acacia.generated.bg
import com.acacia.generated.fmw
import com.acacia.generated.fmh
import com.acacia.generated.fms

@Composable
fun DslTest() {
    Column(
        modifier = Modifier
            .fmw(1f).fmh(1f)
            .bg(Brush.horizontalGradient(listOf(Color(0xFF4CAF50))), RectangleShape, 0f)
            .padding(16.dp)
    ) {
        Text(
            text = "Acacia DSL Working!",
            modifier = Modifier.fmw(1f),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
        
        Text(
            text = "Generated DSL functions: fmw(), fmh(), fms(), bg()",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.padding(8.dp))
        
        Card(
            modifier = Modifier.fmw(1f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Test successful!")
                Text("Plugin builds and generates working DSL")
            }
        }
    }
}
