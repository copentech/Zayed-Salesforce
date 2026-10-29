package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CompanyProfile
import com.example.ui.OrderBookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    viewModel: OrderBookingViewModel,
    onBack: () -> Unit
) {
    val companyProfile by viewModel.companyProfile.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val supabaseHealth by viewModel.supabaseHealth.collectAsStateWithLifecycle()
    val isTestingSupabase by viewModel.isTestingSupabase.collectAsStateWithLifecycle()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()

    var showEditProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Settings & Configuration") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Connectivity & Sync
            item { SettingsSectionHeader("Connectivity & Cloud Sync") }
            item {
                SettingsToggleItem(
                    title = "Network Mode",
                    subtitle = if (isOnline) "Online - Real-time synchronization active" else "Offline - Data queued for local outbox",
                    icon = Icons.Default.NetworkCheck,
                    checked = isOnline,
                    onCheckedChange = { viewModel.toggleNetworkMode() }
                )
            }
            item {
                SettingsActionItem(
                    title = "Test Supabase Connection",
                    subtitle = supabaseHealth?.message ?: "Check bridge status to kocvwcwmzaosgofydjha",
                    icon = Icons.Default.CloudSync,
                    loading = isTestingSupabase,
                    onClick = { viewModel.testSupabaseConnection() }
                )
            }
            item {
                SettingsActionItem(
                    title = "Force Sync Queue",
                    subtitle = "$pendingSyncCount items currently pending in local outbox",
                    icon = Icons.Default.Sync,
                    onClick = { viewModel.triggerSync() }
                )
            }

            // 2. Company Profile
            item { SettingsSectionHeader("Company Profile & Branding") }
            item {
                SettingsActionItem(
                    title = "Modify Business Profile",
                    subtitle = "${companyProfile.companyName} • ${companyProfile.ownerName}",
                    icon = Icons.Default.Business,
                    onClick = { showEditProfileDialog = true }
                )
            }

            // 3. Security & Access
            item { SettingsSectionHeader("Security & Master Access") }
            item {
                SettingsInfoItem(
                    title = "Master Admin PIN",
                    subtitle = "Current: ${companyProfile.adminPin} (Used for critical bypass)",
                    icon = Icons.Default.Lock
                )
            }

            // 4. System Preferences
            item { SettingsSectionHeader("System Preferences") }
            item {
                SettingsToggleItem(
                    title = "Low-Bandwidth Mode",
                    subtitle = "Optimizes JSON payload compression for <1Mbps",
                    icon = Icons.Default.SignalCellularConnectedNoInternet0Bar,
                    checked = companyProfile.isLowBandwidthMode,
                    onCheckedChange = { viewModel.updateCompanyProfile(companyProfile.copy(isLowBandwidthMode = it)) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "Zayed SalesForce v1.0.4-stable\nPowered by Zayed Logistics Engine",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            profile = companyProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { updated ->
                viewModel.updateCompanyProfile(updated)
                showEditProfileDialog = false
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun SettingsActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!loading) onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            }
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun SettingsInfoItem(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    profile: CompanyProfile,
    onDismiss: () -> Unit,
    onSave: (CompanyProfile) -> Unit
) {
    var name by remember { mutableStateOf(profile.companyName) }
    var owner by remember { mutableStateOf(profile.ownerName) }
    var email by remember { mutableStateOf(profile.companyEmail) }
    var phone by remember { mutableStateOf(profile.companyPhone) }
    var pin by remember { mutableStateOf(profile.adminPin) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Business Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Company Name") })
                OutlinedTextField(value = owner, onValueChange = { owner = it }, label = { Text("Owner Name") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Contact Email") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Contact Phone") })
                OutlinedTextField(value = pin, onValueChange = { pin = it }, label = { Text("Master Admin PIN") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(profile.copy(
                    companyName = name,
                    ownerName = owner,
                    companyEmail = email,
                    companyPhone = phone,
                    adminPin = pin
                ))
            }) { Text("Save Changes") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
