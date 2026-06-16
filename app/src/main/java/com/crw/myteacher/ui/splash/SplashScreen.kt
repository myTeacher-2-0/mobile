package com.crw.myteacher.ui.splash

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crw.myteacher.ui.theme.BrandBlue
import com.crw.myteacher.ui.theme.LightBlue
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    onAnimationReady: () -> Unit = {}
) {
    val context = LocalContext.current
    var sensorOffsetX by remember { mutableFloatStateOf(0f) }
    var sensorOffsetY by remember { mutableFloatStateOf(0f) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    sensorOffsetX = -it.values[0] * 3f
                    sensorOffsetY = it.values[1] * 3f
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        accelerometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val animatedSensorX by animateFloatAsState(
        targetValue = sensorOffsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "sensorX"
    )
    val animatedSensorY by animateFloatAsState(
        targetValue = sensorOffsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "sensorY"
    )

    val iconScale = remember { Animatable(0f) }
    val iconRotation = remember { Animatable(-30f) }
    val textAlpha = remember { Animatable(0f) }
    val textScale = remember { Animatable(0.8f) }
    val particlesAlpha = remember { Animatable(0f) }
    val particleSpread = remember { Animatable(0f) }

    val pulseAnimation = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val orbitRotation by pulseAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitRotation"
    )

    LaunchedEffect(Unit) {
        onAnimationReady()

        coroutineScope {
            launch {
                iconScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
            launch {
                iconRotation.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 800,
                        easing = FastOutSlowInEasing
                    )
                )
            }
            launch {
                particlesAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(600, delayMillis = 200)
                )
            }
            launch {
                particleSpread.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 1000,
                        delayMillis = 200,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }

        delay(400.milliseconds)
        coroutineScope {
            launch {
                textAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    )
                )
            }
            launch {
                textScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }

        delay(2600.milliseconds)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BrandBlue,
                        LightBlue
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(280.dp)
                .alpha(particlesAlpha.value)
                .rotate(orbitRotation)
        ) {
            drawOrbitingParticles(
                spread = particleSpread.value,
                baseRotation = orbitRotation
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.offset(
                x = animatedSensorX.dp,
                y = animatedSensorY.dp
            )
        ) {
            Icon(
                imageVector = Icons.Filled.School,
                contentDescription = "MyTeacher Logo",
                tint = Color.White,
                modifier = Modifier
                    .size(120.dp)
                    .scale(iconScale.value * pulseScale)
                    .rotate(iconRotation.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "MyTeacher",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .scale(textScale.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Twój osobisty nauczyciel",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .scale(textScale.value)
            )
        }
    }
}

private fun DrawScope.drawOrbitingParticles(
    spread: Float,
    baseRotation: Float
) {
    val center = Offset(size.width / 2, size.height / 2)
    val particleCount = 8
    val maxRadius = size.minDimension / 2 * 0.85f

    for (i in 0 until particleCount) {
        val angle = (360f / particleCount) * i + baseRotation
        val radians = Math.toRadians(angle.toDouble())
        val radius = maxRadius * spread

        val particleRadius = if (i % 2 == 0) 8f else 5f
        val alpha = if (i % 2 == 0) 0.6f else 0.4f

        val x = center.x + (radius * cos(radians)).toFloat()
        val y = center.y + (radius * sin(radians)).toFloat()

        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = particleRadius * spread,
            center = Offset(x, y)
        )
    }

    for (i in 0 until 6) {
        val angle = (360f / 6) * i - baseRotation * 0.5f
        val radians = Math.toRadians(angle.toDouble())
        val radius = maxRadius * 0.5f * spread

        val x = center.x + (radius * cos(radians)).toFloat()
        val y = center.y + (radius * sin(radians)).toFloat()

        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = 4f * spread,
            center = Offset(x, y)
        )
    }
}
