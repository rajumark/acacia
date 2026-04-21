package org.sample.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun DashboardUIAcaciaComposeShort() {
    Box(
        modifier = Modifier
            .fms(1f)
            .padding(16.dp)
            .bg(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                ),
                RoundedCornerShape(12.dp),
                0f
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fmw(1f)
                .fmh(1f)
        ) {
            Text(
                text = "Acacia DSL Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1565C0)
            )
            
            Spacer(modifier = Modifier.padding(8.dp))
            
            Row(
                modifier = Modifier.fmw(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fmw(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Layout Functions",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("fmw(), fmh(), fms(), bg()")
                    }
                }
                
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fmw(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Background",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("bg(brush, shape, param)")
                    }
                }
            }
            
            Spacer(modifier = Modifier.padding(8.dp))
            
            Card(
                modifier = Modifier.fmw(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Acacia DSL Code Example:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = """
                            Column(
                                modifier = Modifier
                                    .fmw(1f)
                                    .fmh(1f)
                                    .bg(brush, shape, 0f)
                                    .padding(16.dp)
                            )
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
