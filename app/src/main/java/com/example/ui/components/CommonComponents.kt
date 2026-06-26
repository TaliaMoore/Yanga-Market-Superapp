package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryYellow
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp

@Composable
fun YangaStatusBanners(
    errorMessage: String?,
    successMessage: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            errorMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.5.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp))
                        .testTag("error_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error icon",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close error banner",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = successMessage != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            successMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), // light green background
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.5.dp, Color(0xFF16A34A), RoundedCornerShape(12.dp))
                        .testTag("success_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Success icon",
                            tint = Color(0xFF16A34A)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = msg,
                            color = Color(0xFF14532D),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close success banner",
                                tint = Color(0xFF16A34A)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun YangaPlayfulCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = PrimaryPurple,
    borderWidth: Double = 2.0,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .padding(vertical = 6.dp)
            .border(borderWidth.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun YangaHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onIconClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CharcoalBlack
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = CharcoalBlack.copy(alpha = 0.65f),
                fontWeight = FontWeight.Medium
            )
        }
        if (icon != null) {
            IconButton(
                onClick = { onIconClick?.invoke() },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SecondaryYellow)
                    .border(2.dp, PrimaryPurple, RoundedCornerShape(12.dp))
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Header icon action",
                    tint = PrimaryPurple
                )
            }
        }
    }
}

@Composable
fun YangaFunButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = PrimaryPurple,
    contentColor: Color = Color.White,
    borderColor: Color = PrimaryPurple,
    testTagStr: String = "yanga_action_button"
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .height(52.dp)
            .testTag(testTagStr)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            style = LocalTextStyle.current.copy(letterSpacing = 0.5.sp)
        )
    }
}

@Composable
fun YangaBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = SecondaryYellow,
    contentColor: Color = CharcoalBlack
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(containerColor)
            .border(1.5.dp, PrimaryPurple, RoundedCornerShape(30.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Lead Software Architect: Visual Identity and Layout Container representing a nested styled "div" equivalent.
 * Creates visually distinct blocks for sections utilizing neobrutalist borders, padding, and cozy brand colors.
 */
@Composable
fun YangaVisuallyDistinctSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    backgroundColor: Color = Color.White,
    borderColor: Color = PrimaryPurple,
    borderWidth: Double = 2.0,
    headerBadgeText: String? = null,
    headerBadgeColor: Color = SecondaryYellow,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .padding(vertical = 8.dp)
            .border(borderWidth.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (title != null || headerBadgeText != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (title != null) {
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CharcoalBlack
                            )
                        }
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                fontSize = 11.sp,
                                color = CharcoalBlack.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (headerBadgeText != null) {
                        YangaBadge(text = headerBadgeText, containerColor = headerBadgeColor)
                    }
                }
            }
            content()
        }
    }
}

/**
 * Lead Software Architect: Flow Layout system organizing labels, buttons, or parameters across multiple wrapping rows.
 * Simulates adaptive browser layout behavior for screen size class fluid responsiveness.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun YangaFlowButtonsLayout(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    content: @Composable FlowRowScope.() -> Unit
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = content
    )
}

@Composable
fun PurplePeacockLogo(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .testTag("purple_peacock_logo"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.toPx()
            val h = size.toPx()
            val cx = w / 2f
            val cy = h / 2f + (h * 0.1f) // slightly lower to make room for tail feathers

            // 1. Draw Fan/Tail Feathers in a beautiful fan array
            // 7 main plumes radiating from center cy
            val plumeCount = 7
            val startAngle = 180f + 25f
            val endAngle = 360f - 25f
            val angleStep = if (plumeCount > 1) (endAngle - startAngle) / (plumeCount - 1) else 0f
            val maxPlumeLength = w * 0.45f

            for (i in 0 until plumeCount) {
                val angleRad = Math.toRadians((startAngle + i * angleStep).toDouble())
                val endX = cx + (Math.cos(angleRad) * maxPlumeLength).toFloat()
                val endY = cy + (Math.sin(angleRad) * maxPlumeLength).toFloat()

                // Plume Shaft
                drawLine(
                    color = Color(0xFF8B5CF6), // Purple
                    start = Offset(cx, cy),
                    end = Offset(endX, endY),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )

                // Plume Eye (Ocellus)
                // Draw a golden-yellow circle with green center and orange border
                drawCircle(
                    color = Color(0xFFF97316), // Orange
                    radius = w * 0.08f,
                    center = Offset(endX, endY)
                )
                drawCircle(
                    color = Color(0xFFFACC15), // Pale Yellow
                    radius = w * 0.05f,
                    center = Offset(endX, endY)
                )
                drawCircle(
                    color = Color(0xFF10B981), // Emerald Green
                    radius = w * 0.025f,
                    center = Offset(endX, endY)
                )
            }

            // 2. Draw Peacock Main Body
            // Teardrop neck/body
            val bodyPath = Path().apply {
                // start at bottom left of body
                moveTo(cx - w * 0.12f, cy + h * 0.15f)
                // Curve up to neck/head
                quadraticTo(
                    cx - w * 0.15f, cy - h * 0.15f, // control point
                    cx - w * 0.02f, cy - h * 0.25f  // head base
                )
                // Round the head
                cubicTo(
                    cx - w * 0.01f, cy - h * 0.35f,
                    cx + w * 0.12f, cy - h * 0.35f,
                    cx + w * 0.10f, cy - h * 0.23f
                )
                // Curve down to breast
                quadraticTo(
                    cx + w * 0.16f, cy + h * 0.02f,
                    cx + w * 0.12f, cy + h * 0.18f
                )
                // Close bottom body curves
                quadraticTo(
                    cx, cy + h * 0.22f,
                    cx - w * 0.12f, cy + h * 0.15f
                )
            }

            // Fill main body with vibrant gradient purple
            drawPath(
                path = bodyPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF7C3AED), // Vibrant Purple
                        Color(0xFF4C1D95)  // Deep Indigo
                    )
                )
            )

            // 3. Draw Beak (Orange)
            val beakPath = Path().apply {
                moveTo(cx + w * 0.09f, cy - h * 0.27f)
                lineTo(cx + w * 0.18f, cy - h * 0.24f)
                lineTo(cx + w * 0.07f, cy - h * 0.21f)
                close()
            }
            drawPath(
                path = beakPath,
                color = Color(0xFFF97316) // Vibrant Orange
            )

            // 4. Draw Eyes (White with Black Pupil)
            drawCircle(
                color = Color.White,
                radius = w * 0.02f,
                center = Offset(cx + w * 0.04f, cy - h * 0.27f)
            )
            drawCircle(
                color = Color.Black,
                radius = w * 0.009f,
                center = Offset(cx + w * 0.045f, cy - h * 0.27f)
            )

            // 5. Draw Head Crest/Crown Feathers
            val crestY = cy - h * 0.34f
            drawLine(
                color = Color(0xFFFACC15), // Yellow stem
                start = Offset(cx + w * 0.05f, cy - h * 0.30f),
                end = Offset(cx + w * 0.02f, crestY),
                strokeWidth = 2f
            )
            drawCircle(
                color = Color(0xFFEC4899), // Pink dot
                radius = w * 0.02f,
                center = Offset(cx + w * 0.02f, crestY)
            )

            drawLine(
                color = Color(0xFFFACC15),
                start = Offset(cx + w * 0.05f, cy - h * 0.30f),
                end = Offset(cx + w * 0.06f, crestY - 4f),
                strokeWidth = 2f
            )
            drawCircle(
                color = Color(0xFF8B5CF6), // Purple dot
                radius = w * 0.02f,
                center = Offset(cx + w * 0.06f, crestY - 4f)
            )

            drawLine(
                color = Color(0xFFFACC15),
                start = Offset(cx + w * 0.05f, cy - h * 0.30f),
                end = Offset(cx + w * 0.10f, crestY),
                strokeWidth = 2f
            )
            drawCircle(
                color = Color(0xFF3B82F6), // Blue dot
                radius = w * 0.02f,
                center = Offset(cx + w * 0.10f, crestY)
            )
        }
    }
}

