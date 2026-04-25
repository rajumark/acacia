// This is what would be generated from foundation-layout PaddingKt.class
// File: build/generated/source/shortify/com/acacia/generated/ShortModifiers.kt

package com.acacia.generated

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * Generated DSL wrapper functions from foundation-layout dependency
 * Only contains padding() function from PaddingKt.class
 */

// Padding function - extracted from PaddingKt.class
fun Modifier.padding(value: Dp): Modifier = this.padding(value)

fun Modifier.padding(horizontal: Dp, vertical: Dp): Modifier = this.padding(horizontal, vertical)

fun Modifier.padding(start: Dp = Dp.Unspecified, top: Dp = Dp.Unspecified, end: Dp = Dp.Unspecified, bottom: Dp = Dp.Unspecified): Modifier = this.padding(start, top, end, bottom)

// Short alias for padding
fun Modifier.p(value: Dp): Modifier = this.padding(value)

fun Modifier.p(horizontal: Dp, vertical: Dp): Modifier = this.padding(horizontal, vertical)

fun Modifier.p(start: Dp = Dp.Unspecified, top: Dp = Dp.Unspecified, end: Dp = Dp.Unspecified, bottom: Dp = Dp.Unspecified): Modifier = this.padding(start, top, end, bottom)
