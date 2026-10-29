package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class StaffModule(val key: String, val title: String, val description: String) {
    INVENTORY("inventory", "Inventory", "Catalog, stock levels, packaging & pricing tiers"),
    ORDERS("orders", "Orders", "Sales pipeline, new booking wizard & customer invoices"),
    CUSTOMERS("customers", "Customers", "Customer directory, client on-boarding & contact info")
}

@Entity(tableName = "staff_members")
data class StaffMember(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val phone: String,
    val password: String = "pass123",
    val adminPin: String = "2026", // 4 to 8 digit second security layer for admin accounts
    val customRoleLabel: String, // e.g. "Senior Sales Executive", "Logistics Officer", "Warehouse Lead", "Administrator"
    val canBookOrders: Boolean = true,
    val canManageInventory: Boolean = false,
    val canScheduleDelivery: Boolean = false,
    val isAdmin: Boolean = false,
    val isActive: Boolean = true,
    // Granular Module-Level Access Permissions
    val canAccessInventory: Boolean = true,
    val canAccessOrders: Boolean = true,
    val canAccessCustomers: Boolean = true,

    // Commission Configuration (Fixed Amount & Percentage %)
    val commissionType: String = "PERCENTAGE", // "PERCENTAGE" or "FIXED"
    val commissionRate: Double = 0.035, // 3.5%
    val fixedCommissionAmount: Double = 1500.0, // e.g. KES 1,500 flat per delivered order

    // Account Lifecycle & Governance
    val accountStatus: String = "ACTIVE", // "ACTIVE", "SUSPENDED", "TERMINATED"

    // Granular Privileges (Checkboxes)
    val canOnboardCustomers: Boolean = false,
    val canPlaceOrders: Boolean = false,
    val canApproveSuppliers: Boolean = false,
    val canManageStaff: Boolean = false,
    val canViewLogs: Boolean = false,

    // 24-Hour Temporary Password & Password Reset Flow
    val passwordResetRequested: Boolean = false,
    val tempPassword: String = "",
    val tempPasswordExpiresAt: Long = 0L, // Valid for 24 hours from issuance
    val isTemporaryPassword: Boolean = false
) {
    fun hasModuleAccess(module: StaffModule): Boolean {
        if (isAdmin) return true
        return when (module) {
            StaffModule.INVENTORY -> canAccessInventory
            StaffModule.ORDERS -> canAccessOrders
            StaffModule.CUSTOMERS -> canAccessCustomers
        }
    }

    fun isSuspended(): Boolean = accountStatus.equals("SUSPENDED", ignoreCase = true)
    fun isTerminated(): Boolean = accountStatus.equals("TERMINATED", ignoreCase = true)

    fun isTempPasswordValid(now: Long = System.currentTimeMillis()): Boolean {
        return isTemporaryPassword && tempPassword.isNotBlank() && now <= tempPasswordExpiresAt
    }

    fun calculateCommission(orderSubtotal: Double): Double {
        return if (commissionType.equals("FIXED", ignoreCase = true)) {
            fixedCommissionAmount
        } else {
            orderSubtotal * commissionRate
        }
    }
}

