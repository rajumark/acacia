package org.sample.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.acacia.generated.*

@Composable
fun DashboardUIAcaciaCompose() {
    // Single composable using ALL short modifiers from acacia-mapping.json
    Box(
        modifier = Modifier
            .fms()
            .p(16.dp)
            .px(8.dp)
            .py(4.dp)
            .pt(2.dp)
            .pb(2.dp)
            .ps(4.dp)
            .pe(4.dp)
            .pf(16.dp, Alignment.TopStart)
            .bg(Color.White)
            .br(1.dp, Color.Gray)
            .sh(4.dp, RoundedCornerShape(8.dp))
            .cp(RoundedCornerShape(8.dp))
            .ctb()
            .al(1f)
            .of(0.dp, 0.dp)
            .aof(0.dp, 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fmw()
                .wcw()
                .w(100.dp)
                .h(50.dp)
                .sz(200.dp, 100.dp)
                .rsz(150.dp, 75.dp)
                .rw(100.dp)
                .rh(50.dp)
                .wi(50.dp, 200.dp)
                .hi(25.dp, 100.dp)
                .dms(50.dp, 50.dp)
                .ar(16f / 9f, true)
                .rt(0f)
                .sc(1f)
                .gl { }
                .zi(0f)
                .db { }
                .dwc { }
                .dwch { }
        ) {
            Text("Acacia Modifiers Demo")

            Row(
                modifier = Modifier
                    .wt(1f, true)
                    .fmh()
                    .wch()
                    .wcs()
                    .fmw()
                    .wcw()
                    .lay { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) {
                            placeable.placeRelative(0, 0)
                        }
                    }
                    .lid("row1")
                    .ogp { }
                    .opl { }
                    .osc { }
            ) {
                Text(
                    text = "Row",
                    modifier = Modifier
                        .clk { }
                        .cclk { }
                        .dg { }
                        .scr { }
                        .swp { }
                        .tg { }
                        .sl { }
                        .sg { }
                        .fc { }
                        .fd { }
                        .ofc { }
                        .ofe { }
                        .hf { }
                        .ind { }
                        .pi { }
                        .phi { }
                        .hv { }
                        .pi { }
                )
            }

            Card(
                modifier = Modifier
                    .sbp()
                    .stp()
                    .nbp()
                    .sdp()
                    .scp()
                    .sgp()
                    .dcp()
                    .imp()
                    .cwi()
                    .wip()
                    .acs()
                    .ns { }
                    .tt("card")
                    .sem { }
                    .psm()
                    .casm { }
                    .itu()
                    .opke { }
                    .oke { }
            ) {
                Text("System bars & more")
            }
        }
    }
}
