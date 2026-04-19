package org.sample.app

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.acacia.generated.*
import com.acacia.generated.Cl  // Cl = Color
import com.acacia.generated.D   // D = Dp
import com.acacia.generated.Al  // Al = Alignment
import com.acacia.generated.Arr // Arr = Arrangement

/**
 * Demo of short composable wrappers + short modifiers + short type aliases.
 * 
 * Generated short composables:
 * - C() = Column(), R() = Row(), B() = Box()
 * - T() = Text(), Btn() = Button(), Cd() = Card()
 * - etc.
 * 
 * Generated type aliases:
 * - Cl = Color, D = Dp, Sh = Shape
 * - Arr = Arrangement, Al = Alignment
 * - M = Modifier, etc.
 */
@Composable
fun DashboardUIAcaciaComposeShort() {
    // Using short composable wrappers C(), B(), T(), Btn(), Cd(), etc.
    B(
        modifier = M  // M = Modifier
            .fms()
            .p(16.D)  // D = Dp
            .bg(Cl.White)  // Cl = Color
    ) {
        C(
            modifier = M.fmw(),
            horizontalAlignment = Al.CenterHorizontally  // Al = Alignment
        ) {
            // Short Text composable
            T(
                text = "Dashboard with Short DSL",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Sp() // Short Spacer
            
            // Short Card composable
            Cd(
                modifier = M
                    .fmw()
                    .p(16.D),
                shape = RoundedCornerShape(8.D)
            ) {
                C {
                    T("Card Content")
                    T("More content here")
                }
            }
            
            Sp()
            
            // Short Row composable
            R(
                modifier = M.fmw(),
                horizontalArrangement = Arr.SpaceEvenly  // Arr = Arrangement
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
                modifier = M.fmw()
            )
            
            Sp()
            
            // Short LazyColumn
            LC {
                items(5) { index ->
                    T("Item $index", modifier = M.p(8.D))
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
 *     modifier = M.fmw().p(16.D),
 *     horizontalAlignment = Al.CenterHorizontally
 * ) {
 *     T("Title")
 *     Sp()
 *     Cd(modifier = M.fmw()) {
 *         T("Content")
 *     }
 *     Btn(onClick = { }) { T("Save") }
 * }
 * 
 * // Cl = Color, D = Dp, M = Modifier, Al = Alignment
 * ```
 */
