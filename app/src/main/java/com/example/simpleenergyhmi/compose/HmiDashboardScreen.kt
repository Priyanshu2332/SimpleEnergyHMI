package com.example.simpleenergyhmi.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.simpleenergyhmi.data.models.KillSwitchStatus
import com.example.simpleenergyhmi.data.models.MotorStatus
import com.example.simpleenergyhmi.data.viewmodel.VehicleViewModel
import org.koin.androidx.compose.koinViewModel

private val Background = Color(0xFF080D11)
private val Panel = Color(0xFF111B20)
private val Emerald = Color(0xFF00E5A0)
private val Muted = Color(0xFF82939A)
private val Danger = Color(0xFFFF5263)
private val BorderColor = Color(0xFF26383B)

@Composable
fun HmiDashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: VehicleViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val vehicle = uiState.vehicleData

    val timestamp = vehicle?.timestamp ?: 0L
    val isStale = vehicle != null && System.currentTimeMillis() - timestamp > 5000L

    val speed = vehicle?.speedKmh
        ?.takeIf { it.isFinite() && it >= 0f && !isStale }

    val motor = if (isStale) "UNKNOWN" else
        vehicle?.motorStatus?.name ?: "UNKNOWN"

    val killSwitch = if (isStale) "UNKNOWN" else
        vehicle?.killSwitchStatus?.name ?: "UNKNOWN"

    val animatedSpeed by animateFloatAsState(
        targetValue = speed ?: 0f,
        label = "speed_animation"
    )

    val speedProgress = (animatedSpeed / 120f)
        .coerceIn(0f, 1f)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 16.dp)
        ) {

            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "◆ SIMPLE ENERGY",
                        color = Emerald,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ELECTRIC MOBILITY",
                        color = Muted,
                        fontSize = 10.sp,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = "RIDE CONTROL SYSTEM",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isStale || vehicle == null)
                                    Danger else Emerald,
                                CircleShape
                            )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isStale || vehicle == null)
                            "SIGNAL LOST" else "LIVE SIMULATION",
                        color = if (isStale || vehicle == null)
                            Danger else Emerald,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // MAIN DASHBOARD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // LEFT PANEL
                Column(
                    modifier = Modifier
                        .weight(0.23f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "VEHICLE",
                        color = Muted,
                        fontSize = 13.sp,
                        letterSpacing = 3.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "HMI",
                        color = Emerald,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(Modifier.height(24.dp))

                    StatusChip(
                        title = "RIDE MODE",
                        value = when {
                            speed == null -> "UNKNOWN"
                            motor == "OFF" && killSwitch == "ACTIVE" -> "MOTOR DISABLED"
                            motor == "OFF" -> "PARKED"
                            speed == 0f -> "READY"
                            speed == 25f -> "RIDING"
                            speed == 50f -> "ACCELERATING"
                            else -> "RIDING"
                        }
                    )

                    Spacer(Modifier.height(14.dp))

                    StatusChip(
                        title = "MAX SPEED",
                        value = "120 km/h"
                    )

                    Spacer(Modifier.height(14.dp))

                    StatusChip(
                        title = "DATA SOURCE",
                        value = "SIMULATED"
                    )
                }

                // CENTER SPEEDOMETER
                Column(
                    modifier = Modifier
                        .weight(0.52f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Text(
                        text = "CURRENT VEHICLE SPEED",
                        color = Muted,
                        fontSize = 13.sp,
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = if (speed == null) "--"
                        else animatedSpeed.toInt().toString(),
                        color = Color.White,
                        fontSize = 100.sp,
                        lineHeight = 108.sp,
                        fontWeight = FontWeight.Light
                    )

                    Text(
                        text = "KILOMETRES PER HOUR",
                        color = Muted,
                        fontSize = 12.sp,
                        letterSpacing = 5.sp
                    )

                    Spacer(Modifier.height(10.dp))

                    // SPEED ARC
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(95.dp)
                    ) {
                        val stroke = 13.dp.toPx()

                        drawArc(
                            color = Color(0xFF263439),
                            startAngle = 150f,
                            sweepAngle = 240f,
                            useCenter = false,
                            style = Stroke(
                                width = stroke,
                                cap = StrokeCap.Round
                            )
                        )

                        if (speed != null) {
                            drawArc(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Emerald,
                                        Color(0xFF00A9B7)
                                    )
                                ),
                                startAngle = 150f,
                                sweepAngle = 240f * speedProgress,
                                useCenter = false,
                                style = Stroke(
                                    width = stroke,
                                    cap = StrokeCap.Round
                                )
                            )
                        }
                    }

                    Text(
                        text = "0                         60                         120",
                        color = Muted,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }

                // RIGHT STATUS PANELS
                Column(
                    modifier = Modifier
                        .weight(0.25f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {

                    VehicleStatusCard(
                        title = "MOTOR STATUS",
                        value = motor,
                        active = motor == "ON"
                    )

                    Spacer(Modifier.height(18.dp))

                    VehicleStatusCard(
                        title = "KILL SWITCH",
                        value = killSwitch,
                        active = killSwitch == "ACTIVE"
                    )

                    Spacer(Modifier.height(18.dp))

                    VehicleStatusCard(
                        title = "SIGNAL STATUS",
                        value = when {
                            isStale -> "STALE"
                            vehicle == null -> "WAITING"
                            else -> "CONNECTED"
                        },
                        active = vehicle != null && !isStale
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // BOTTOM TELEMETRY BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Panel)
                    .border(
                        1.dp,
                        BorderColor,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text(
                        text = "VEHICLE TELEMETRY",
                        color = Muted,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "REAL-TIME DATA MONITOR",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LIVE SPEED",
                        color = Muted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = if (speed == null) "-- km/h"
                        else "${animatedSpeed.toInt()} km/h",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SYSTEM",
                        color = Muted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = when {
                            uiState.isLoading -> "INITIALIZING"
                            uiState.errorMessage != null -> "ERROR"
                            isStale -> "STALE DATA"
                            vehicle == null -> "NO DATA"
                            else -> "OPERATIONAL"
                        },
                        color = if (
                            uiState.errorMessage != null || isStale
                        ) Danger else Emerald,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    title: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Panel)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            color = Muted,
            fontSize = 10.sp,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = value,
            color = Emerald,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun VehicleStatusCard(
    title: String,
    value: String,
    active: Boolean
) {
    val statusColor = when {
        value == "UNKNOWN" || value == "STALE" ||
                value == "WAITING" -> Color(0xFFFFB547)
        active -> Emerald
        else -> Danger
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF172328),
                        Color(0xFF101719)
                    )
                )
            )
            .border(
                1.dp,
                statusColor.copy(alpha = 0.55f),
                RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = Muted,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = value,
            color = statusColor,
            fontSize = 23.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .width(42.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
    }
}
