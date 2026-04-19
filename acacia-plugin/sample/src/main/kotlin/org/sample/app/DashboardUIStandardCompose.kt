package org.sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.draggable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.hapticFeedback
import androidx.compose.foundation.indication
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.progressSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DashboardUIStandardCompose() {
    // Single composable using ALL standard Compose modifiers (equivalent to short versions)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(horizontal = 8.dp)
            .padding(vertical = 4.dp)
            .padding(top = 2.dp)
            .padding(bottom = 2.dp)
            .padding(start = 4.dp)
            .padding(end = 4.dp)
            .paddingFrom(Alignment.TopStart, 16.dp)
            .background(Color.White)
            .border(1.dp, Color.Gray)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clipToBounds()
            .alpha(1f)
            .offset(0.dp, 0.dp)
            .absoluteOffset(0.dp, 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth()
                .width(100.dp)
                .height(50.dp)
                .size(200.dp, 100.dp)
                .requiredSize(150.dp, 75.dp)
                .requiredWidth(100.dp)
                .requiredHeight(50.dp)
                .widthIn(50.dp, 200.dp)
                .heightIn(25.dp, 100.dp)
                .defaultMinSize(50.dp, 50.dp)
                .aspectRatio(16f / 9f, true)
                .rotate(0f)
                .scale(1f)
                .graphicsLayer { }
                .zIndex(0f)
                .drawBehind { }
                .drawWithContent { }
                .drawWithCache { }
        ) {
            Text("Standard Compose Modifiers Demo")

            Row(
                modifier = Modifier
                    .weight(1f, true)
                    .fillMaxHeight()
                    .wrapContentHeight()
                    .wrapContentSize()
                    .fillMaxWidth()
                    .wrapContentWidth()
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) {
                            placeable.placeRelative(0, 0)
                        }
                    }
                    .layoutId("row1")
                    .onGloballyPositioned { }
                    .onPlaced { }
                    .onSizeChanged { }
            ) {
                Text(
                    text = "Row",
                    modifier = Modifier
                        .clickable { }
                        .combinedClickable { }
                        .draggable { }
                        .scrollable { }
                        .scrollable { }
                        .clickable { }
                        .clickable { }
                        .selectable { }
                        .selectableGroup()
                        .focusable()
                        .focusProperties { }
                        .onFocusChanged { }
                        .onFocusEvent { }
                        .hapticFeedback()
                        .indication()
                        .pointerInput { }
                        .pointerHoverIcon { }
                        .hoverable { }
                        .pointerInput { }
                )
            }

            Card(
                modifier = Modifier
                    .systemBarsPadding()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .safeDrawingPadding()
                    .safeContentPadding()
                    .safeGesturesPadding()
                    .displayCutoutPadding()
                    .imePadding()
                    .consumeWindowInsets { }
                    .windowInsetsPadding { }
                    .animateContentSize()
                    .nestedScroll { }
                    .testTag("card")
                    .semantics { }
                    .progressSemantics()
                    .clearAndSetSemantics { }
                    .invisibleToUser()
                    .onPreviewKeyEvent { true }
                    .onKeyEvent { true }
            ) {
                Text("System bars & more")
            }
        }
    }
}
