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

@Composable
fun SimpleTestStandard() {
    Column(
        modifier = Modifier
            .fillMaxWidth().fillMaxHeight()
            .background(
                brush = Brush.horizontalGradient(listOf(Color(0xFFF5F5F5))), 
                shape = RectangleShape
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Standard Compose Test",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.padding(8.dp))
        
        Button(
            onClick = { /* Handle click */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test Button")
        }
        
        Spacer(modifier = Modifier.padding(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("This is a test card using standard Compose")
                Text("Modifiers: fillMaxWidth(), fillMaxHeight(), background(), padding()")
            }
        }
    }
}
