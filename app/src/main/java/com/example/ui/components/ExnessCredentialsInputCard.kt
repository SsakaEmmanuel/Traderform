package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Mt5ConnectionState
import com.example.ui.theme.ArtisticBorder
import com.example.ui.theme.ArtisticGreen
import com.example.ui.theme.ArtisticPrimary
import com.example.ui.theme.ArtisticRed
import com.example.ui.theme.ArtisticSurface
import com.example.ui.theme.ArtisticSurfaceElevated
import com.example.ui.theme.ArtisticTextMuted
import com.example.ui.theme.ArtisticTextPrimary
import com.example.ui.theme.ArtisticTextSecondary

@Composable
fun ExnessCredentialsInputCard(
    accountId: String,
    password: String,
    isPasswordVisible: Boolean,
    rememberCredentials: Boolean,
    readOnlyMode: Boolean,
    connectionState: Mt5ConnectionState,
    onAccountIdChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleRemember: (Boolean) -> Unit,
    onToggleReadOnly: (Boolean) -> Unit,
    onConnectClick: () -> Unit,
    onQuickFillDemo: () -> Unit,
    onQuickFillLive: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val isConnecting = connectionState is Mt5ConnectionState.Connecting
    val isConnected = connectionState is Mt5ConnectionState.Connected
    val isError = connectionState is Mt5ConnectionState.Error

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("credentials_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisticSurface),
        border = BorderStroke(1.dp, ArtisticBorder.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Section Header with Quick Fill Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ACCOUNT CREDENTIALS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = ArtisticTextSecondary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ArtisticPrimary.copy(alpha = 0.1f))
                            .clickable(onClick = onQuickFillLive)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("quick_fill_live_button")
                    ) {
                        Text(
                            text = "+ Live Sample",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ArtisticPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0277BD).copy(alpha = 0.1f))
                            .clickable(onClick = onQuickFillDemo)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("quick_fill_demo_button")
                    ) {
                        Text(
                            text = "+ Demo Sample",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0277BD)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Account ID (Login) Field
            Text(
                text = "Account ID (MT5 Login)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = ArtisticTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = accountId,
                onValueChange = onAccountIdChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("account_id_input"),
                placeholder = {
                    Text("e.g. 82940182", fontSize = 13.sp, color = ArtisticTextMuted)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = ArtisticPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (accountId.isNotEmpty()) {
                            IconButton(onClick = { onAccountIdChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = ArtisticTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                clipboardManager.getText()?.text?.let { text ->
                                    onAccountIdChange(text.filter { it.isDigit() })
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = ArtisticPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ArtisticPrimary,
                    unfocusedBorderColor = ArtisticBorder,
                    focusedContainerColor = ArtisticSurfaceElevated,
                    unfocusedContainerColor = ArtisticSurfaceElevated
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Trading Password Field
            Text(
                text = "Trading Password",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = ArtisticTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input"),
                placeholder = {
                    Text("Enter MT5 Master password", fontSize = 13.sp, color = ArtisticTextMuted)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = ArtisticPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = onTogglePasswordVisibility,
                        modifier = Modifier.testTag("password_visibility_toggle")
                    ) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            tint = ArtisticTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ArtisticPrimary,
                    unfocusedBorderColor = ArtisticBorder,
                    focusedContainerColor = ArtisticSurfaceElevated,
                    unfocusedContainerColor = ArtisticSurfaceElevated
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Toggles: Remember credentials + Read-only mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Remember Credentials",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = ArtisticTextPrimary
                    )
                    Text(
                        text = "Save account for quick 1-tap switching",
                        fontSize = 11.sp,
                        color = ArtisticTextMuted
                    )
                }

                Switch(
                    checked = rememberCredentials,
                    onCheckedChange = onToggleRemember,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ArtisticPrimary
                    ),
                    modifier = Modifier.testTag("remember_credentials_switch")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Investor (Read-Only) Mode",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = ArtisticTextPrimary
                    )
                    Text(
                        text = "Audit mode only — disables trading orders",
                        fontSize = 11.sp,
                        color = ArtisticTextMuted
                    )
                }

                Switch(
                    checked = readOnlyMode,
                    onCheckedChange = onToggleReadOnly,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ArtisticPrimary
                    ),
                    modifier = Modifier.testTag("readonly_mode_switch")
                )
            }

            // Connection Progress animation if connecting
            if (isConnecting) {
                val connecting = connectionState as Mt5ConnectionState.Connecting
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ArtisticPrimary.copy(alpha = 0.08f))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = connecting.step,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ArtisticPrimary
                        )
                        Text(
                            text = "${(connecting.progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisticPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { connecting.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = ArtisticPrimary,
                        trackColor = ArtisticBorder.copy(alpha = 0.4f)
                    )
                }
            }

            // Error Message Banner if error
            if (isError) {
                val error = connectionState as Mt5ConnectionState.Error
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ArtisticRed.copy(alpha = 0.1f))
                        .padding(12.dp)
                        .testTag("connection_error_banner")
                ) {
                    Text(
                        text = error.message,
                        fontSize = 12.sp,
                        color = ArtisticRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Connect Button
            Button(
                onClick = onConnectClick,
                enabled = !isConnecting && !isConnected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("connect_mt5_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArtisticPrimary,
                    disabledContainerColor = ArtisticPrimary.copy(alpha = 0.5f)
                )
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "CONNECTING...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONNECT TO EXNESS MT5",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
