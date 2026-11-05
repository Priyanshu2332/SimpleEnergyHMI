package com.example.simpleenergyhmi.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.simpleenergyhmi.data.models.VehicleData
import com.example.simpleenergyhmi.data.viewmodel.VehicleViewModel
import org.koin.androidx.compose.koinViewModel

private val Background = Color(0xFF080D11)
private val Panel = Color(0xFF111B20)
private val CardBackground = Color(0xFF142025)
private val Emerald = Color(0xFF00E5A0)
private val Muted = Color(0xFF82939A)
private val Danger = Color(0xFFFF5263)
private val Warning = Color(0xFFFFB547)
private val BorderColor = Color(0xFF26383B)

@Composable
fun HmiDashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: VehicleViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val vehicle = uiState.vehicleData
    val isStale = uiState.isStale

    val speed = vehicle?.speedKmh?.takeIf {
        it.isFinite() && it >= 0f && !isStale
    }

    val motor = if (isStale) "UNKNOWN"
    else vehicle?.motorStatus?.name ?: "UNKNOWN"

    val killSwitch = if (isStale) "UNKNOWN"
    else vehicle?.killSwitchStatus?.name ?: "UNKNOWN"

    val rideMode = when {
        vehicle == null -> "UNKNOWN"
        isStale -> "UNKNOWN"
        motor == "OFF" && killSwitch == "ACTIVE" -> "MOTOR DISABLED"
        motor == "OFF" -> "PARKED"
        speed == 0f -> "READY"
        speed == 25f -> "RIDING"
        speed == 50f -> "ACCELERATING"
        else -> "RIDING"
    }

    val signalStatus = when {
        uiState.isLoading -> "WAITING"
        uiState.errorMessage != null -> "ERROR"
        isStale -> "STALE"
        vehicle == null -> "WAITING"
        else -> "CONNECTED"
    }

    val isLive = vehicle != null && !isStale

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Background
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val compact = maxHeight < 400.dp
            val horizontalPadding = if (compact) 16.dp else 24.dp
            val verticalPadding = if (compact) 8.dp else 14.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            ) {
                DashboardHeader(
                    isLive = isLive,
                    compact = compact
                )

                Spacer(Modifier.height(if (compact) 6.dp else 12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LeftPanel(
                        rideMode = rideMode,
                        compact = compact,
                        modifier = Modifier
                            .weight(0.24f)
                            .fillMaxHeight()
                    )

                    CenterPanel(
                        speed = speed,
                        compact = compact,
                        modifier = Modifier
                            .weight(0.50f)
                            .fillMaxHeight()
                    )

                    RightPanel(
                        motor = motor,
                        killSwitch = killSwitch,
                        signalStatus = signalStatus,
                        compact = compact,
                        modifier = Modifier
                            .weight(0.26f)
                            .fillMaxHeight()
                    )
                }

                Spacer(Modifier.height(if (compact) 6.dp else 12.dp))

                TelemetryFooter(
                    speed = speed,
                    isStale = isStale,
                    isLoading = uiState.isLoading,
                    errorMessage = uiState.errorMessage,
                    vehicle = vehicle,
                    compact = compact
                )
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    isLive: Boolean,
    compact: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 36.dp else 44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "SIMPLE ENERGY",
            color = Emerald,
            fontSize = if (compact) 18.sp else 22.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        Text(
            text = "RIDE CONTROL SYSTEM",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Panel)
                .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(
                        if (isLive) Emerald else Danger,
                        CircleShape
                    )
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (isLive) "LIVE SIMULATION" else "SIGNAL LOST",
                color = if (isLive) Emerald else Danger,
                fontSize = if (compact) 9.sp else 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LeftPanel(
    rideMode: String,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "VEHICLE",
            color = Muted,
            fontSize = if (compact) 11.sp else 12.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "HMI",
            color = Emerald,
            fontSize = if (compact) 32.sp else 40.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(Modifier.height(if (compact) 12.dp else 20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Panel)
                .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = if (compact) 10.dp else 14.dp)
        ) {
            Text(
                text = "RIDE MODE",
                color = Muted,
                fontSize = if (compact) 9.sp else 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = rideMode,
                color = Emerald,
                fontSize = if (compact) 13.sp else 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CenterPanel(
    speed: Float?,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CURRENT VEHICLE SPEED",
            color = Muted,
            fontSize = if (compact) 10.sp else 12.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = speed?.toInt()?.toString() ?: "--",
            color = Color.White,
            fontSize = if (compact) 72.sp else 92.sp,
            lineHeight = if (compact) 76.sp else 98.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "km/h",
            color = Muted,
            fontSize = if (compact) 13.sp else 15.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(if (compact) 4.dp else 8.dp))

        SpeedGauge(
            speed = speed,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(if (compact) 55.dp else 80.dp)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "0                         60                         120",
            color = Muted,
            fontSize = if (compact) 9.sp else 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SpeedGauge(
    speed: Float?,
    modifier: Modifier = Modifier
) {
    val speedValue = speed ?: 0f
    val progress = (speedValue / 120f).coerceIn(0f, 1f)

    Canvas(modifier = modifier) {
        val strokeWidth = 10.dp.toPx()

        drawArc(
            color = Color(0xFF223035),
            startAngle = 150f,
            sweepAngle = 240f,
            useCenter = false,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            )
        )

        if (speed != null) {
            drawArc(
                color = Emerald,
                startAngle = 150f,
                sweepAngle = 240f * progress,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

@Composable
private fun RightPanel(
    motor: String,
    killSwitch: String,
    signalStatus: String,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        StatusCard(
            title = "MOTOR STATUS",
            value = motor,
            valueColor = when (motor) {
                "ON" -> Emerald
                "OFF" -> Danger
                else -> Warning
            },
            compact = compact
        )

        Spacer(Modifier.height(if (compact) 8.dp else 12.dp))

        StatusCard(
            title = "KILL SWITCH",
            value = killSwitch,
            valueColor = when (killSwitch) {
                "ACTIVE" -> Danger
                "INACTIVE" -> Emerald
                else -> Warning
            },
            compact = compact
        )

        Spacer(Modifier.height(if (compact) 8.dp else 12.dp))

        StatusCard(
            title = "SIGNAL STATUS",
            value = signalStatus,
            valueColor = when (signalStatus) {
                "CONNECTED" -> Emerald
                "STALE", "WAITING" -> Warning
                "ERROR" -> Danger
                else -> Warning
            },
            compact = compact
        )
    }
}

@Composable
private fun StatusCard(
    title: String,
    value: String,
    valueColor: Color,
    compact: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBackground)
            .border(
                1.dp,
                valueColor.copy(alpha = 0.45f),
                RoundedCornerShape(14.dp)
            )
            .padding(
                horizontal = 10.dp,
                vertical = if (compact) 8.dp else 12.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = Muted,
            fontSize = if (compact) 9.sp else 10.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(if (compact) 4.dp else 6.dp))

        Text(
            text = value,
            color = valueColor,
            fontSize = if (compact) 15.sp else 18.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .width(30.dp)
                .height(3.dp)
                .clip(CircleShape)
                .background(valueColor)
        )
    }
}

@Composable
private fun TelemetryFooter(
    speed: Float?,
    isStale: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    vehicle: VehicleData?,
    compact: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 50.dp else 62.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Panel)
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "VEHICLE TELEMETRY",
                color = Muted,
                fontSize = if (compact) 9.sp else 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "REAL-TIME DATA MONITOR",
                color = Color.White,
                fontSize = if (compact) 10.sp else 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "LIVE SPEED",
                color = Muted,
                fontSize = if (compact) 8.sp else 9.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (speed == null) "-- km/h" else "${speed.toInt()} km/h",
                color = Color.White,
                fontSize = if (compact) 13.sp else 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "SYSTEM",
                color = Muted,
                fontSize = if (compact) 8.sp else 9.sp,
                fontWeight = FontWeight.Medium
            )
            val systemStatus = when {
                isLoading -> "INITIALIZING"
                errorMessage != null -> "ERROR"
                isStale -> "STALE DATA"
                vehicle == null -> "NO DATA"
                else -> "OPERATIONAL"
            }
            val statusColor = if (errorMessage != null || isStale) Danger else Emerald

            Text(
                text = systemStatus,
                color = statusColor,
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
