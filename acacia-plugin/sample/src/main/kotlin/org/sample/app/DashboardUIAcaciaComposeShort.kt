package org.sample.app

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.acacia.generated.*

/**
 * Demo of short composable wrappers alongside short modifiers.
 * 
 * Generated short composables:
 * - C() = Column()
 * - R() = Row()  
 * - B() = Box()
 * - T() = Text()
 * - Btn() = Button()
 * - Cd() = Card()
 * - IC() = Icon()
 * - Sp() = Spacer()
 * - etc.
 */
@Composable
fun DashboardUIAcaciaComposeShort() {
    // Using short composable wrappers C(), B(), T(), Btn(), Cd(), etc.
    B(
        modifier = Modifier
            .fms()
            .p(16.dp)
            .bg(Color.White)
    ) {
        C(
            modifier = Modifier.fmw(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Short Text composable
            T(
                text = "Dashboard with Short DSL",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Sp() // Short Spacer
            
            // Short Card composable
            Cd(
                modifier = Modifier
                    .fmw()
                    .p(16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                C {
                    T("Card Content")
                    T("More content here")
                }
            }
            
            Sp()
            
            // Short Row composable
            R(
                modifier = Modifier.fmw(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
            ) {
                // Short Button composables
                Btn(onClick = { }) {
                    T("Save")
                }
                Btn(onClick = { }) {
                    T("Cancel")
                }
            }
            
            Sp()
            
            // Short OutlinedTextField
            OTF(
                value = "",
                onValueChange = { },
                label = { T("Enter text") },
                modifier = Modifier.fmw()
            )
            
            Sp()
            
            // Short LazyColumn
            LC {
                items(5) { index ->
                    T("Item $index", modifier = Modifier.p(8.dp))
                }
            }
        }
    }
}

/**
 * Example: Complete transformation
 * 
 * BEFORE (Standard Compose):
 * ```
 * Column(
 *     modifier = Modifier.fillMaxWidth().padding(16.dp),
 *     horizontalAlignment = Alignment.CenterHorizontally
 * ) {
 *     Text("Title")
 *     Spacer(modifier = Modifier.height(8.dp))
 *     Card(modifier = Modifier.fillMaxWidth()) {
 *         Text("Content")
 *     }
 * }
 * ```
 * 
 * AFTER (Acacia Short DSL):
 * ```
 * C(
 *     modifier = Modifier.fmw().p(16.dp),
 *     horizontalAlignment = Alignment.CenterHorizontally
 * ) {
 *     T("Title")
 *     Sp()
 *     Cd(modifier = Modifier.fmw()) {
 *         T("Content")
 *     }
 * }
 * ```
 */
