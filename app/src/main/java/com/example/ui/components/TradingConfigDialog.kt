package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.IndicatorConfig
import com.example.ui.theme.ArtisticBg
import com.example.ui.theme.ArtisticBorder
import com.example.ui.theme.ArtisticCard
import com.example.ui.theme.ArtisticGreen
import com.example.ui.theme.ArtisticPrimary
import com.example.ui.theme.ArtisticRed
import com.example.ui.theme.ArtisticSurface
import com.example.ui.theme.ArtisticSurfaceElevated
import com.example.ui.theme.ArtisticTextMuted
import com.example.ui.theme.ArtisticTextPrimary

@Composable
fun TradingConfigDialog(
    indicatorConfig: IndicatorConfig,
    defaultTpPips: Double,
    defaultSlPips: Double,
    onToggleMaMatrix: (Boolean) -> Unit,
    onToggleRsi: (Boolean) -> Unit,
    onToggleMacd: (Boolean) -> Unit,
    onUpdateDefaultTp: (Double) -> Unit,
    onUpdateDefaultSl: (Double) -> Unit,
    onResetDefaults: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, ArtisticBorder, RoundedCornerShape(16.dp))
                .testTag("trading_config_dialog"),
            color = ArtisticSurfaceElevated,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ArtisticPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = ArtisticPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Chart & Risk Configuration",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisticTextPrimary
                            )
                            Text(
                                text = "Indicator toggles & manual overrides",
                                fontSize = 11.sp,
                                color = ArtisticTextMuted
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_config_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ArtisticTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = ArtisticBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 1: INDICATOR TOGGLES
                Text(
                    text = "TECHNICAL INDICATORS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ArtisticPrimary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 1. 4-MA Matrix
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ArtisticSurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "4-MA Matrix",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ArtisticTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ArtisticPrimary.copy(alpha = 0.15f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text("9, 21, 50, 200", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ArtisticPrimary)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IndicatorColorTag("MA9", Color(0xFFFFB300))
                                IndicatorColorTag("MA21", Color(0xFF00B0FF))
                                IndicatorColorTag("MA50", Color(0xFF9C27B0))
                                IndicatorColorTag("MA200", Color(0xFFE91E63))
                            }
                        }
                        Switch(
                            checked = indicatorConfig.showMaMatrix,
                            onCheckedChange = onToggleMaMatrix,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ArtisticPrimary),
                            modifier = Modifier.testTag("toggle_ma_matrix")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. RSI (14)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ArtisticSurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "RSI Oscillator (14)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = ArtisticTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Relative Strength with 70/30 overbought & oversold levels",
                                fontSize = 11.sp,
                                color = ArtisticTextMuted
                            )
                        }
                        Switch(
                            checked = indicatorConfig.showRsi,
                            onCheckedChange = onToggleRsi,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF7C4DFF)),
                            modifier = Modifier.testTag("toggle_rsi")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. MACD (12, 26, 9)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ArtisticSurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "MACD (12, 26, 9)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = ArtisticTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Moving Average Convergence Divergence & Histogram",
                                fontSize = 11.sp,
                                color = ArtisticTextMuted
                            )
                        }
                        Switch(
                            checked = indicatorConfig.showMacd,
                            onCheckedChange = onToggleMacd,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2979FF)),
                            modifier = Modifier.testTag("toggle_macd")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = ArtisticBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 2: DEFAULT TAKE-PROFIT AND STOP-LOSS OVERRIDES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DEFAULT RISK & EXIT OVERRIDES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ArtisticPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Initial baseline for new orders (Normal: 10p TP / 15p SL)",
                            fontSize = 10.sp,
                            color = ArtisticTextMuted
                        )
                    }
                    IconButton(
                        onClick = onResetDefaults,
                        modifier = Modifier.size(28.dp).testTag("reset_defaults_icon_button")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset", tint = ArtisticPrimary, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Take Profit Override Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ArtisticSurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Default Take-Profit Target",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ArtisticTextPrimary
                            )
                            Text(
                                text = "+${String.format("%.1f", defaultTpPips)} Pips",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = ArtisticGreen,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Steppers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onUpdateDefaultTp(defaultTpPips - 1.0) },
                                modifier = Modifier.weight(1f).height(36.dp).testTag("tp_override_minus_1"),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("-1p", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { onUpdateDefaultTp(defaultTpPips - 5.0) },
                                modifier = Modifier.weight(1f).height(36.dp).testTag("tp_override_minus_5"),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("-5p", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { onUpdateDefaultTp(defaultTpPips + 1.0) },
                                modifier = Modifier.weight(1f).height(36.dp).testTag("tp_override_plus_1"),
                                colors = ButtonDefaults.buttonColors(containerColor = ArtisticGreen),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("+1p", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                            Button(
                                onClick = { onUpdateDefaultTp(defaultTpPips + 5.0) },
                                modifier = Modifier.weight(1f).height(36.dp).testTag("tp_override_plus_5"),
                                colors = ButtonDefaults.buttonColors(containerColor = ArtisticGreen),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("+5p", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(5.0, 8.0, 10.0, 15.0, 20.0).forEach { p ->
                                val isSelected = defaultTpPips == p
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) ArtisticGreen.copy(alpha = 0.2f) else ArtisticCard)
                                        .border(1.dp, if (isSelected) ArtisticGreen else ArtisticBorder, RoundedCornerShape(6.dp))
                                        .clickable { onUpdateDefaultTp(p) }
                                        .padding(vertical = 4.dp)
                                        .testTag("tp_preset_${p.toInt()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${p.toInt()}p${if (p == 10.0) "*" else ""}",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) ArtisticGreen else ArtisticTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stop Loss Override Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ArtisticSurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Default Stop-Loss Limit",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ArtisticTextPrimary
                            )
                            Text(
                                text = "-${String.format("%.1f", defaultSlPips)} Pips",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = ArtisticRed,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Steppers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onUpdateDefaultSl(defaultSlPips - 5.0) },
                                modifier = Modifier.weight(1f).height(36.dp).testTag("sl_override_minus_5"),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("-5p", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { onUpdateDefaultSl(defaultSlPips - 1.0) },
                                modifier = Modifier.weight(1f).height(36.dp).testTag("sl_override_minus_1"),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("-1p", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { onUpdateDefaultSl(defaultSlPips + 1.0) },
                                modifier = Modifier.weight(1f).height(36.dp).testTag("sl_override_plus_1"),
                                colors = ButtonDefaults.buttonColors(containerColor = ArtisticRed),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("+1p", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                            Button(
                                onClick = { onUpdateDefaultSl(defaultSlPips + 5.0) },
                                modifier = Modifier.weight(1f).height(36.dp).testTag("sl_override_plus_5"),
                                colors = ButtonDefaults.buttonColors(containerColor = ArtisticRed),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("+5p", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(8.0, 10.0, 12.0, 15.0, 20.0).forEach { p ->
                                val isSelected = defaultSlPips == p
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) ArtisticRed.copy(alpha = 0.2f) else ArtisticCard)
                                        .border(1.dp, if (isSelected) ArtisticRed else ArtisticBorder, RoundedCornerShape(6.dp))
                                        .clickable { onUpdateDefaultSl(p) }
                                        .padding(vertical = 4.dp)
                                        .testTag("sl_preset_${p.toInt()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${p.toInt()}p${if (p == 15.0) "*" else ""}",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) ArtisticRed else ArtisticTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onResetDefaults,
                        modifier = Modifier.weight(1f).height(44.dp).testTag("reset_to_normal_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Reset to Normal (10/15)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(44.dp).testTag("apply_config_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ArtisticPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Apply & Close", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun IndicatorColorTag(label: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(ArtisticCard)
            .border(0.5.dp, ArtisticBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(3.dp))
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = ArtisticTextPrimary)
    }
}
