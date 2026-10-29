package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.StaffMember

/**
 * Hardened Enterprise Login Screen
 * Features stealth Admin entry (silently transitions to PIN screen without public disclosure),
 * Forgot Password workflow with 24h temporary password support, and mandatory first-login password change.
 */
@Composable
fun LoginScreen(
    onLogin: (
        identifier: String,
        password: String,
        onAdminPinRequired: (StaffMember, requiresPasswordChange: Boolean) -> Unit,
        onSuccess: (requiresPasswordChange: Boolean) -> Unit,
        onError: (String) -> Unit
    ) -> Unit,
    onRequestPasswordReset: (identifier: String, onComplete: (Boolean) -> Unit) -> Unit,
    onForceChangePassword: (newPassword: String, onSuccess: () -> Unit) -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var emailOrPhone by remember { mutableStateOf("alyasir@zayed-salesforce.com") }
    var password by remember { mutableStateOf("admin") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    val imperialGold = Color(0xFFD4AF37)
    val deepNavy = Color(0xFF0F172A)
    val cardNavy = Color(0xFF1E293B)
    val borderNavy = Color(0xFF334155)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(deepNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Golden Emblem
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(cardNavy)
                    .border(2.dp, imperialGold, CircleShape)
                    .padding(12.dp)
                    .clickable { onOpenAbout() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.zayed_logo),
                    contentDescription = "Zayed SalesForce Emblem",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Zayed SalesForce",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "OWNED BY AL YASIR BIN ZAYED",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = imperialGold,
                letterSpacing = 1.sp
            )

            Text(
                text = "Enterprise Route-to-Market & Regional Dispatch",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Status Banner
            AnimatedVisibility(visible = statusMessage != null, enter = fadeIn(), exit = fadeOut()) {
                statusMessage?.let { msg ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("login_status_banner"),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF064E3B),
                        border = BorderStroke(1.dp, Color(0xFF34D399))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = msg, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Universal Corporate Sign In Card (Admin access stealthily embedded)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardNavy),
                border = BorderStroke(1.dp, borderNavy)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Staff Account Sign In",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = deepNavy,
                            border = BorderStroke(1.dp, imperialGold.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "OFFLINE-READY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = imperialGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Email / Mobile Input
                    OutlinedTextField(
                        value = emailOrPhone,
                        onValueChange = {
                            emailOrPhone = it
                            errorMessage = null
                        },
                        label = { Text("Email or Mobile Number", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("e.g. rep@zayed-salesforce.com", color = Color(0xFF64748B)) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = imperialGold)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_identifier_input")
                    )

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Account Password", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = imperialGold)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password Visibility",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    // Forgot Password link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Forgot Password?",
                            fontSize = 12.sp,
                            color = imperialGold,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable { showForgotPasswordDialog = true }
                                .testTag("btn_forgot_password")
                        )
                    }

                    errorMessage?.let { err ->
                        Text(
                            text = err,
                            color = Color(0xFFF87171),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Sign In Action Button
                    Button(
                        onClick = {
                            if (emailOrPhone.isBlank() || password.isBlank()) {
                                errorMessage = "Please enter both account identifier and password"
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            onLogin(
                                emailOrPhone,
                                password,
                                { adminStaff, reqChange ->
                                    isLoading = false
                                    // Stealth Admin: triggers full PIN verification screen
                                    if (reqChange) {
                                        showChangePasswordDialog = true
                                    }
                                },
                                { reqChange ->
                                    isLoading = false
                                    if (reqChange) {
                                        showChangePasswordDialog = true
                                    }
                                },
                                { error ->
                                    isLoading = false
                                    errorMessage = error
                                }
                            )
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_submit_login"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = imperialGold,
                            contentColor = deepNavy
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = deepNavy, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer note
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onOpenAbout() }
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "System owned by Al Yasir Bin Zayed • Learn More",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Forgot Password Dialog
        if (showForgotPasswordDialog) {
            ForgotPasswordDialog(
                onDismiss = { showForgotPasswordDialog = false },
                onSubmitRequest = { identifier ->
                    onRequestPasswordReset(identifier) { found ->
                        showForgotPasswordDialog = false
                        statusMessage = if (found) {
                            "Password reset request logged for $identifier. Your Administrator can issue a 24-hour temporary login password."
                        } else {
                            "If an account exists for $identifier, a reset request has been flagged for your Administrator."
                        }
                    }
                }
            )
        }

        // Mandatory Password Change Dialog (when using temporary password)
        if (showChangePasswordDialog) {
            ForceChangePasswordDialog(
                onPasswordChanged = { newPass ->
                    onForceChangePassword(newPass) {
                        showChangePasswordDialog = false
                        statusMessage = "Password updated successfully! Welcome to Zayed SalesForce."
                    }
                }
            )
        }
    }
}

/**
 * Forgot Password Request Dialog
 */
@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onSubmitRequest: (identifier: String) -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    val imperialGold = Color(0xFFD4AF37)
    val cardNavy = Color(0xFF1E293B)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardNavy,
            border = BorderStroke(1.dp, imperialGold),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("dialog_forgot_password")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Reset Your Password", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        Text("Request a 24-hour temporary login code", fontSize = 11.sp, color = imperialGold)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFF334155))

                Text(
                    text = "Enter your registered corporate email or mobile number. Your Administrator will issue a secure 24-hour temporary password to regain access.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = identifier,
                    onValueChange = {
                        identifier = it
                        inputError = null
                    },
                    label = { Text("Email or Mobile Number", color = Color(0xFF94A3B8)) },
                    placeholder = { Text("e.g. rep@zayed-salesforce.com", color = Color(0xFF64748B)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_forgot_pass_identifier")
                )

                inputError?.let { err ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(err, color = Color(0xFFEF4444), fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel", color = Color.White)
                    }
                    Button(
                        onClick = {
                            if (identifier.isBlank()) {
                                inputError = "Please enter your email or mobile"
                                return@Button
                            }
                            onSubmitRequest(identifier.trim())
                        },
                        modifier = Modifier.weight(1.5f).testTag("btn_submit_forgot_pass"),
                        colors = ButtonDefaults.buttonColors(containerColor = imperialGold, contentColor = Color(0xFF0F172A))
                    ) {
                        Text("Request Reset", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Mandatory Password Change Dialog (Triggered when user logs in with temporary password)
 */
@Composable
fun ForceChangePasswordDialog(
    onPasswordChanged: (newPassword: String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val imperialGold = Color(0xFFD4AF37)
    val cardNavy = Color(0xFF1E293B)

    Dialog(onDismissRequest = { /* Mandatory: cannot dismiss without setting password */ }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardNavy,
            border = BorderStroke(1.5.dp, imperialGold),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("dialog_force_change_password")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                            .border(1.dp, imperialGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = imperialGold, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Temporary Password Expired", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text("Please create your new permanent password", fontSize = 11.sp, color = imperialGold)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFF334155))

                Text(
                    text = "You logged in with a 24-hour temporary password. For system security, you must set a new permanent password before accessing your account.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; error = null },
                    label = { Text("New Permanent Password", color = Color(0xFF94A3B8)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_new_permanent_password")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; error = null },
                    label = { Text("Confirm New Password", color = Color(0xFF94A3B8)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_confirm_permanent_password")
                )

                error?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(it, color = Color(0xFFEF4444), fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (newPassword.isBlank() || newPassword.length < 4) {
                            error = "Password must be at least 4 characters long"
                            return@Button
                        }
                        if (newPassword != confirmPassword) {
                            error = "Passwords do not match"
                            return@Button
                        }
                        onPasswordChanged(newPassword.trim())
                    },
                    modifier = Modifier.fillMaxWidth().testTag("btn_confirm_new_password"),
                    colors = ButtonDefaults.buttonColors(containerColor = imperialGold, contentColor = Color(0xFF0F172A))
                ) {
                    Text("Save Password & Continue", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
