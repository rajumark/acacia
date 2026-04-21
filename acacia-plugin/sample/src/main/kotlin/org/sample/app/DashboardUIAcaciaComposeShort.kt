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
import com.acacia.generated.p
import com.acacia.generated.B
import com.acacia.generated.C
import com.acacia.generated.R
import com.acacia.generated.Sp

@Composable
fun DashboardUIAcaciaComposeShort() {
    Box(
        modifier = Modifier
            .fms(1f)
            .p(16.dp)
            .bg(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                ),
                RoundedCornerShape(12.dp),
                0f
            )
            .p(16.dp)
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
            
            Sp()
            
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
                        modifier = Modifier.p(16.dp)
                    ) {
                        Text(
                            text = "Layout Functions",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("fmw(), fmh(), fms(), bg(), p(), B(), Sp()")
                    }
                }
                
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fmw(1f)
                ) {
                    Column(
                        modifier = Modifier.p(16.dp)
                    ) {
                        Text(
                            text = "Background",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("bg(brush, shape, param)")
                    }
                }
            }
            
            Sp()
            
            Card(
                modifier = Modifier.fmw(1f)
            ) {
                Column(
                    modifier = Modifier.p(16.dp)
                ) {
                    Text(
                        text = "Acacia DSL Code Example:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Sp()
                    Text(
                        text = """
                            Column(
                                modifier = Modifier
                                    .fmw(1f)
                                    .fmh(1f)
                                    .bg(brush, shape, 0f)
                                    .p(16.dp)
                            )
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
