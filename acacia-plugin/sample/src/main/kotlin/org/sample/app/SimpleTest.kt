package org.sample.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.acacia.generated.bg
import com.acacia.generated.fmw
import com.acacia.generated.fmh
import com.acacia.generated.fms

@Composable
fun SimpleTest() {
    Column(
        modifier = Modifier
            .fmw(1f).fmh(1f)
            .bg(Brush.horizontalGradient(listOf(Color(0xFFF5F5F5))), RectangleShape, 0f)
            .padding(16.dp)
    ) {
        Text(
            text = "Acacia DSL Test",
            modifier = Modifier.fmw(1f),
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.padding(8.dp))
        
        Button(
            onClick = { /* Handle click */ },
            modifier = Modifier.fmw(1f)
        ) {
            Text("Test Button")
        }
        
        Spacer(modifier = Modifier.padding(8.dp))
        
        Card(
            modifier = Modifier.fmw(1f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("This is a test card using Acacia DSL")
                Text("Modifiers: fmw(), fmh(), bg(), fms()")
            }
        }
    }
}
