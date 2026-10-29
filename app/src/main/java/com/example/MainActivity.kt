package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Customer
import com.example.data.model.OrderWithItems
import com.example.data.model.StaffMember
import com.example.data.model.StaffModule
import com.example.ui.BookingDraftState
import com.example.ui.OrderBookingViewModel
import com.example.ui.components.AboutZayedDialog
import com.example.ui.components.AdminDeletionRequestsDialog
import com.example.ui.components.NetworkSyncBanner
import com.example.ui.components.OrderSummaryDialog
import com.example.ui.components.PermissionManagerDialog
import com.example.ui.components.StaffManagementDialog
import com.example.ui.screens.AppSplashScreen
import com.example.ui.screens.AccessRestrictedScreen
import com.example.ui.screens.AddCustomerModalDialog
import com.example.ui.screens.AdminDispatchScreen
import com.example.ui.screens.AdminPinScreen
import com.example.ui.screens.CustomerDirectoryScreen
import com.example.ui.screens.InventoryCatalogScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NewBookingScreen
import com.example.ui.screens.OrderListScreen
import com.example.ui.screens.SalesRepDashboardScreen
import com.example.ui.screens.ScheduleDeliveryDialog
import com.example.ui.screens.SettingsSyncScreen
import com.example.ui.theme.MyApplicationTheme

enum class MainTab {
    ORDERS,
    NEW_BOOKING,
    CUSTOMERS,
    CATALOG,
    REP_DASHBOARD,
    SETTINGS_SYNC,
    ADMIN_DASHBOARD,
    SUPPLIER_PERFORMANCE,
    APP_SETTINGS
}

class MainActivity : ComponentActivity() {
    private val viewModel: OrderBookingViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                val orders by viewModel.orders.collectAsStateWithLifecycle()
                val customers by viewModel.customers.collectAsStateWithLifecycle()
                val pendingDeletionRequests by viewModel.pendingDeletionRequests.collectAsStateWithLifecycle()
                val products by viewModel.products.collectAsStateWithLifecycle()
                val allStaff by viewModel.allStaff.collectAsStateWithLifecycle()
                val activeStaff by viewModel.activeStaff.collectAsStateWithLifecycle()
                val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
                val companyProfile by viewModel.companyProfile.collectAsStateWithLifecycle()
                val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
                val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
                val lastSyncResult by viewModel.lastSyncResult.collectAsStateWithLifecycle()
                val bookingDraft by viewModel.bookingDraft.collectAsStateWithLifecycle()
                val selectedOrderForSummary by viewModel.selectedOrderForSummary.collectAsStateWithLifecycle()
                val commissions by viewModel.allCommissions.collectAsStateWithLifecycle()
                val supabaseHealth by viewModel.supabaseHealth.collectAsStateWithLifecycle()
                val isTestingSupabase by viewModel.isTestingSupabase.collectAsStateWithLifecycle()
                val activities by viewModel.allActivities.collectAsStateWithLifecycle()

                var isAppReady by remember { mutableStateOf(false) }

                if (!isAppReady) {
                    AppSplashScreen(onLoadingComplete = { isAppReady = true })
                } else {
                    // Request Notification Permission on Android 13+
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = { /* Permission handled by system */ }
                    )

                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    var currentTab by remember { mutableStateOf(MainTab.ORDERS) }
                    var showQuickCustomerDialog by remember { mutableStateOf(false) }
                    var orderToScheduleFromList by remember { mutableStateOf<OrderWithItems?>(null) }

                    // Dialog states
                    var showAboutDialog by remember { mutableStateOf(false) }
                var showStaffManagement by remember { mutableStateOf(false) }
                var showPermissionManager by remember { mutableStateOf(false) }
                var showAdminDeletionRequests by remember { mutableStateOf(false) }
                var adminStaffPendingPin by remember { mutableStateOf<StaffMember?>(null) }

                if (!isLoggedIn) {
                    if (adminStaffPendingPin != null) {
                        // Dedicated Full-Screen Second Security Layer (PIN entry)
                        AdminPinScreen(
                            adminStaff = adminStaffPendingPin!!,
                            onVerifyPin = { pin, onSuccess, onError ->
                                viewModel.verifyAdminPinAndCompleteLogin(
                                    adminStaffPendingPin!!,
                                    pin,
                                    {
                                        adminStaffPendingPin = null
                                        onSuccess()
                                    },
                                    onError
                                )
                            },
                            onBackToLogin = {
                                adminStaffPendingPin = null
                            }
                        )
                    } else {
                        LoginScreen(
                            onLogin = { identifier, password, onAdminPinRequired, onSuccess, onError ->
                                viewModel.login(
                                    identifier,
                                    password,
                                    { adminStaff, reqChange ->
                                        adminStaffPendingPin = adminStaff
                                        onAdminPinRequired(adminStaff, reqChange)
                                    },
                                    { reqChange ->
                                        onSuccess(reqChange)
                                    },
                                    onError
                                )
                            },
                            onRequestPasswordReset = { identifier, onComplete ->
                                viewModel.requestPasswordReset(identifier, onComplete)
                            },
                            onForceChangePassword = { newPass, onSuccess ->
                                viewModel.forceChangePassword(newPass, onSuccess)
                            },
                            onOpenAbout = { showAboutDialog = true }
                        )
                    }

                    if (showAboutDialog) {
                        AboutZayedDialog(
                            onDismiss = { showAboutDialog = false }
                        )
                    }
                } else {
                    // Predictive back handler
                    BackHandler(enabled = currentTab != MainTab.ORDERS || bookingDraft.activeStep > 0) {
                        if (currentTab == MainTab.NEW_BOOKING && bookingDraft.activeStep > 0) {
                            viewModel.setBookingStep(bookingDraft.activeStep - 1)
                        } else {
                            currentTab = MainTab.ORDERS
                        }
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            Column(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                                // Branded Zayed SalesForce Top Navigation Bar
                                TopAppBar(
                                    title = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { showAboutDialog = true }
                                        ) {
                                            // Golden Emblem
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF0F172A))
                                                    .border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(8.dp))
                                                    .padding(4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.zayed_logo),
                                                    contentDescription = "Zayed SalesForce Logo"
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Zayed SalesForce",
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 15.sp,
                                                        maxLines = 1
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = "About",
                                                        modifier = Modifier.size(13.dp),
                                                        tint = Color(0xFFD4AF37)
                                                    )
                                                }
                                                Text(
                                                    text = "Al Yasir Bin Zayed • ${activeStaff.name}",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.outline,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    },
                                    actions = {
                                        // Active Staff & Role Badge
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (activeStaff.isAdmin) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier
                                                .padding(end = 4.dp)
                                                .testTag("staff_profile_badge")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (activeStaff.isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = if (activeStaff.isAdmin) Color(0xFFB45309) else MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = activeStaff.customRoleLabel,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (activeStaff.isAdmin) Color(0xFFB45309) else MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }

                                        // Logout Action
                                        IconButton(
                                            onClick = { viewModel.logout() },
                                            modifier = Modifier.testTag("btn_logout")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                                contentDescription = "Sign Out",
                                                tint = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                )

                                // Real-Time Network Sync Telemetry Banner (<1Mbps bandwidth monitor)
                                NetworkSyncBanner(
                                    isOnline = isOnline,
                                    pendingCount = pendingSyncCount,
                                    isSyncing = isSyncing,
                                    lastSyncResult = lastSyncResult,
                                    onSyncClick = { viewModel.triggerSync() }
                                )
                            }
                        },
                        bottomBar = {
                            NavigationBar(
                                modifier = Modifier.testTag("bottom_nav_bar"),
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                // 1. Orders
                                NavigationBarItem(
                                    selected = currentTab == MainTab.ORDERS,
                                    onClick = { currentTab = MainTab.ORDERS },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (orders.isNotEmpty()) {
                                                    Badge { Text("${orders.size}") }
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Receipt, contentDescription = "Orders")
                                        }
                                    },
                                    label = { Text("Orders", fontSize = 10.sp) },
                                    modifier = Modifier.testTag("nav_tab_orders")
                                )

                                // 2. Rep Dashboard (for Sales Reps)
                                if (!activeStaff.isAdmin) {
                                    NavigationBarItem(
                                        selected = currentTab == MainTab.REP_DASHBOARD,
                                        onClick = { currentTab = MainTab.REP_DASHBOARD },
                                        icon = {
                                            Icon(Icons.Default.TrendingUp, contentDescription = "My Sales & Commission")
                                        },
                                        label = { Text("My Sales", fontSize = 10.sp) },
                                        modifier = Modifier.testTag("nav_tab_rep_dashboard")
                                    )
                                }

                                // 3. Booking Wizard
                                if (activeStaff.canBookOrders && activeStaff.hasModuleAccess(StaffModule.ORDERS)) {
                                    NavigationBarItem(
                                        selected = currentTab == MainTab.NEW_BOOKING,
                                        onClick = { currentTab = MainTab.NEW_BOOKING },
                                        icon = {
                                            BadgedBox(
                                                badge = {
                                                    if (bookingDraft.totalUnits > 0) {
                                                        Badge { Text("${bookingDraft.totalUnits}") }
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.AddShoppingCart, contentDescription = "New Booking")
                                            }
                                        },
                                        label = { Text("Book", fontSize = 10.sp) },
                                        modifier = Modifier.testTag("nav_tab_booking")
                                    )
                                }

                                // 4. Customers Directory
                                if (activeStaff.hasModuleAccess(StaffModule.CUSTOMERS)) {
                                    NavigationBarItem(
                                        selected = currentTab == MainTab.CUSTOMERS,
                                        onClick = { currentTab = MainTab.CUSTOMERS },
                                        icon = {
                                            BadgedBox(
                                                badge = {
                                                    if (activeStaff.isAdmin && pendingDeletionRequests.isNotEmpty()) {
                                                        Badge { Text("${pendingDeletionRequests.size}") }
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.People, contentDescription = "Customers")
                                            }
                                        },
                                        label = { Text("Clients", fontSize = 10.sp) },
                                        modifier = Modifier.testTag("nav_tab_customers")
                                    )
                                }

                                // 5. Catalog
                                if (activeStaff.hasModuleAccess(StaffModule.INVENTORY)) {
                                    NavigationBarItem(
                                        selected = currentTab == MainTab.CATALOG,
                                        onClick = { currentTab = MainTab.CATALOG },
                                        icon = {
                                            Icon(Icons.Default.Inventory, contentDescription = "Catalog")
                                        },
                                        label = { Text("Catalog", fontSize = 10.sp) },
                                        modifier = Modifier.testTag("nav_tab_catalog")
                                    )
                                }

                                // 6. Admin Console / Settings
                                NavigationBarItem(
                                    selected = currentTab == MainTab.SETTINGS_SYNC,
                                    onClick = { currentTab = MainTab.SETTINGS_SYNC },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (pendingSyncCount > 0) {
                                                    Badge { Text("$pendingSyncCount") }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                if (activeStaff.isAdmin) Icons.Default.LocalShipping else Icons.Default.Settings,
                                                contentDescription = "Settings / Admin"
                                            )
                                        }
                                    },
                                    label = { Text(if (activeStaff.isAdmin) "Admin" else "Settings", fontSize = 10.sp) },
                                    modifier = Modifier.testTag("nav_tab_settings")
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentTab) {
                                MainTab.ORDERS -> {
                                    if (!activeStaff.hasModuleAccess(StaffModule.ORDERS)) {
                                        AccessRestrictedScreen(
                                            module = StaffModule.ORDERS,
                                            activeStaff = activeStaff,
                                            onGoToPermitted = { currentTab = MainTab.SETTINGS_SYNC }
                                        )
                                    } else {
                                        OrderListScreen(
                                            orders = orders,
                                            activeRole = if (activeStaff.isAdmin) "ADMIN" else "SALES_REP",
                                            onViewSummary = { viewModel.viewOrderSummary(it) },
                                            onNewBookingClick = { currentTab = MainTab.NEW_BOOKING },
                                            onScheduleDeliveryClick = { orderToScheduleFromList = it }
                                        )
                                    }
                                }

                                MainTab.REP_DASHBOARD -> {
                                    SalesRepDashboardScreen(
                                        activeStaff = activeStaff,
                                        orders = orders,
                                        commissions = commissions,
                                        onNewBookingClick = { currentTab = MainTab.NEW_BOOKING },
                                        onViewOrderSummary = { viewModel.viewOrderSummary(it) },
                                        onSyncCommissions = { viewModel.syncCommissionsToSupabase() }
                                    )
                                }

                                MainTab.NEW_BOOKING -> {
                                    if (!activeStaff.hasModuleAccess(StaffModule.ORDERS)) {
                                        AccessRestrictedScreen(
                                            module = StaffModule.ORDERS,
                                            activeStaff = activeStaff,
                                            onGoToPermitted = { currentTab = MainTab.SETTINGS_SYNC }
                                        )
                                    } else {
                                        NewBookingScreen(
                                            draftState = bookingDraft,
                                            customers = customers,
                                            products = products,
                                            repName = "${activeStaff.name} (${activeStaff.customRoleLabel})",
                                            onSelectCustomer = { viewModel.selectCustomer(it) },
                                            onQuickAddCustomer = { showQuickCustomerDialog = true },
                                            onUpdateProduct = { prod, qty, price -> viewModel.updateProductInCart(prod, qty, price) },
                                            onUpdateRegionalConfig = { km, tier, urgency, tax -> viewModel.updateRegionalConfig(km, tier, urgency, tax) },
                                            onUpdatePaymentAndDelivery = { pay, ord, date -> viewModel.updatePaymentAndDelivery(pay, ord, date) },
                                            onSetStep = { viewModel.setBookingStep(it) },
                                            onSubmitOrder = { onComplete ->
                                                viewModel.submitOrder { completed ->
                                                    onComplete(completed)
                                                    currentTab = MainTab.ORDERS
                                                }
                                            },
                                            onCancel = {
                                                if (bookingDraft.activeStep > 0) {
                                                    viewModel.setBookingStep(bookingDraft.activeStep - 1)
                                                } else {
                                                    currentTab = MainTab.ORDERS
                                                }
                                            }
                                        )
                                    }
                                }

                                MainTab.CUSTOMERS -> {
                                    if (!activeStaff.hasModuleAccess(StaffModule.CUSTOMERS)) {
                                        AccessRestrictedScreen(
                                            module = StaffModule.CUSTOMERS,
                                            activeStaff = activeStaff,
                                            onGoToPermitted = { currentTab = MainTab.SETTINGS_SYNC }
                                        )
                                    } else {
                                        CustomerDirectoryScreen(
                                            customers = customers,
                                            pendingDeletionRequests = pendingDeletionRequests,
                                            isAdmin = activeStaff.isAdmin,
                                            activeStaffName = activeStaff.name,
                                            onAddCustomer = { cName, bName, loc, bEmail, cpName, pos, mob, secMob, cEmail ->
                                                viewModel.createCustomer(cName, bName, loc, bEmail, cpName, pos, mob, secMob, cEmail) {}
                                            },
                                            onBookOrderForCustomer = { customer ->
                                                viewModel.selectCustomer(customer)
                                                currentTab = MainTab.NEW_BOOKING
                                            },
                                            onRequestDeleteCustomer = { customer, reason ->
                                                viewModel.requestCustomerDeletion(customer.id, reason)
                                            },
                                            onArchiveCustomer = { customer ->
                                                viewModel.archiveCustomer(customer.id)
                                            },
                                            onRestoreCustomer = { customer ->
                                                viewModel.restoreCustomer(customer.id)
                                            },
                                            onDirectDeleteCustomer = { customer ->
                                                viewModel.deleteCustomerDirectly(customer.id)
                                            },
                                            onApproveDeleteCustomer = { customer ->
                                                viewModel.approveCustomerDeletion(customer.id)
                                            },
                                            onDismissDeleteRequest = { customer ->
                                                viewModel.dismissCustomerDeletionRequest(customer.id)
                                            }
                                        )
                                    }
                                }

                                MainTab.CATALOG -> {
                                    if (!activeStaff.hasModuleAccess(StaffModule.INVENTORY)) {
                                        AccessRestrictedScreen(
                                            module = StaffModule.INVENTORY,
                                            activeStaff = activeStaff,
                                            onGoToPermitted = { currentTab = MainTab.SETTINGS_SYNC }
                                        )
                                    } else {
                                        InventoryCatalogScreen(
                                            products = products,
                                            onAddProduct = { name, cat, unit, minP, maxP, baseP, stock, sku ->
                                                viewModel.createProduct(name, cat, unit, minP, maxP, baseP, stock, sku) {}
                                            }
                                        )
                                    }
                                }

                                MainTab.SETTINGS_SYNC -> {
                                    if (activeStaff.isAdmin) {
                                        AdminDispatchScreen(
                                            orders = orders,
                                            products = products,
                                            staffList = allStaff,
                                            pendingDeletionCount = pendingDeletionRequests.size,
                                            onScheduleDelivery = { orderId, date -> viewModel.scheduleDelivery(orderId, date) },
                                            onUpdateStatus = { orderId, status -> viewModel.updateOrderStatus(orderId, status) },
                                            onQuickRestock = { prodId, units -> viewModel.quickRestockProduct(prodId, units) },
                                            onApproveDeliveryAndCommission = { orderId, code, amount -> viewModel.approveDeliveryAndCommission(orderId, code, amount) },
                                            onViewSummary = { viewModel.viewOrderSummary(it) },
                                            onOpenStaffManagement = { showStaffManagement = true },
                                            onOpenPermissionManager = { showPermissionManager = true },
                                            onOpenDeletionRequests = { showAdminDeletionRequests = true },
                                            onOpenSupplierPerformance = { currentTab = MainTab.SUPPLIER_PERFORMANCE },
                                            onOpenAppSettings = { currentTab = MainTab.APP_SETTINGS },
                                            onOpenAboutDialog = { showAboutDialog = true }
                                        )
                                    } else {
                                        SettingsSyncScreen(
                                            companyProfile = companyProfile,
                                            activeStaff = activeStaff,
                                            isOnline = isOnline,
                                            pendingSyncCount = pendingSyncCount,
                                            isSyncing = isSyncing,
                                            lastSyncResult = lastSyncResult,
                                            onLogout = { viewModel.logout() },
                                            onOpenAboutDialog = { showAboutDialog = true },
                                            onToggleNetworkMode = { viewModel.toggleNetworkMode() },
                                            onUpdateProfile = { viewModel.updateCompanyProfile(it) },
                                            onTriggerSync = { viewModel.triggerSync() },
                                            supabaseHealth = supabaseHealth,
                                            isTestingSupabase = isTestingSupabase,
                                            onTestSupabase = { viewModel.testSupabaseConnection() },
                                            onOpenAdminDashboard = { currentTab = MainTab.ADMIN_DASHBOARD }
                                        )
                                    }
                                }

                                MainTab.ADMIN_DASHBOARD -> {
                                    com.example.ui.screens.AdminDashboardModuleScreen(
                                        currentStaff = activeStaff,
                                        activities = activities,
                                        staffList = allStaff,
                                        onVerifyPin = { pin, onSuccess, onError ->
                                            viewModel.verifyAdminPinForDashboard(pin, onSuccess, onError)
                                        },
                                        onBack = { currentTab = MainTab.SETTINGS_SYNC }
                                    )
                                }

                                MainTab.SUPPLIER_PERFORMANCE -> {
                                    com.example.ui.screens.SupplierPerformanceScreen(
                                        viewModel = viewModel,
                                        onBack = { currentTab = MainTab.SETTINGS_SYNC }
                                    )
                                }

                                MainTab.APP_SETTINGS -> {
                                    com.example.ui.screens.AppSettingsScreen(
                                        viewModel = viewModel,
                                        onBack = { currentTab = MainTab.SETTINGS_SYNC }
                                    )
                                }
                            }
                        }
                    }

                    // Official Branded Stamp & Client Copy Dialog
                    selectedOrderForSummary?.let { orderWithItems ->
                        OrderSummaryDialog(
                            orderWithItems = orderWithItems,
                            companyProfile = companyProfile,
                            onDismiss = { viewModel.viewOrderSummary(null) }
                        )
                    }

                    // Quick Add Customer Dialog from New Booking
                    if (showQuickCustomerDialog) {
                        AddCustomerModalDialog(
                            repName = activeStaff.name,
                            onDismiss = { showQuickCustomerDialog = false },
                            onSave = { name, bName, loc, bEmail, cName, pos, mob, secMob, cEmail ->
                                viewModel.createCustomer(name, bName, loc, bEmail, cName, pos, mob, secMob, cEmail) {
                                    showQuickCustomerDialog = false
                                }
                            }
                        )
                    }

                    // Admin Schedule Delivery Dialog from Order Card
                    orderToScheduleFromList?.let { item ->
                        ScheduleDeliveryDialog(
                            order = item.order,
                            onDismiss = { orderToScheduleFromList = null },
                            onConfirm = { dateStr, status ->
                                viewModel.scheduleDelivery(item.order.id, dateStr)
                                if (status != item.order.status) {
                                    viewModel.updateOrderStatus(item.order.id, status)
                                }
                                orderToScheduleFromList = null
                            }
                        )
                    }

                    // About Zayed SalesForce Dialog
                    if (showAboutDialog) {
                        AboutZayedDialog(
                            onDismiss = { showAboutDialog = false }
                        )
                    }

                    // Staff Management Console Dialog
                    if (showStaffManagement) {
                        StaffManagementDialog(
                            staffList = allStaff,
                            onSaveStaff = { staff -> viewModel.saveStaffMember(staff) },
                            onDeleteStaff = { id -> viewModel.deleteStaffMember(id) },
                            onIssue24hTempPassword = { staffId, onIssued ->
                                viewModel.issue24hTemporaryPassword(staffId, onIssued)
                            },
                            onOpenPermissionManager = {
                                showStaffManagement = false
                                showPermissionManager = true
                            },
                            onDismiss = { showStaffManagement = false }
                        )
                    }

                    // Role & Module Permission Manager Dialog
                    if (showPermissionManager) {
                        PermissionManagerDialog(
                            staffList = allStaff,
                            onUpdatePermissions = { staffId, canInv, canOrd, canCust ->
                                viewModel.updateStaffModulePermissions(staffId, canInv, canOrd, canCust)
                            },
                            onDismiss = { showPermissionManager = false }
                        )
                    }

                    // Admin Deletion Requests Dialog
                    if (showAdminDeletionRequests) {
                        AdminDeletionRequestsDialog(
                            pendingRequests = pendingDeletionRequests,
                            onApproveDelete = { customer -> viewModel.approveCustomerDeletion(customer.id) },
                            onDismissRequest = { customer -> viewModel.dismissCustomerDeletionRequest(customer.id) },
                            onClose = { showAdminDeletionRequests = false }
                        )
                    }
                    }
                }
            }
        }
    }
}
