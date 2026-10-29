package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room Database entity representing sales representative commission records.
 * Supports both fixed flat fee (e.g. KES 1,500/order) and percentage-based rates (e.g. 3.5%).
 * Synchronized with Supabase BaaS `commissions` table.
 */
@Entity(
    tableName = "commissions",
    indices = [
        Index(value = ["orderId"], unique = true),
        Index(value = ["repId"]),
        Index(value = ["status"])
    ]
)
data class CommissionRecord(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val orderId: String,
    val orderNumber: String,
    val repId: String,
    val repName: String,
    val repEmail: String = "",
    val orderTotalAmount: Double,
    val commissionType: String = TYPE_PERCENTAGE, // "PERCENTAGE" or "FIXED"
    val rateOrFee: Double = 0.035, // Percentage (e.g. 0.035) or Fixed Amount (e.g. 1500.0)
    val calculatedAmount: Double = 0.0,
    val status: String = STATUS_PENDING, // "PENDING", "APPROVED", "PAID"
    val paymentCode: String = "", // Admin payout reference (M-Pesa / Bank transaction ID)
    val paidAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val isSyncedToSupabase: Boolean = false
) {
    val isPaid: Boolean
        get() = status == STATUS_PAID || status == STATUS_APPROVED

    companion object {
        const val TYPE_PERCENTAGE = "PERCENTAGE"
        const val TYPE_FIXED = "FIXED"

        const val STATUS_PENDING = "PENDING"
        const val STATUS_APPROVED = "APPROVED"
        const val STATUS_PAID = "PAID"

        /**
         * Calculates commission amount supporting both percentage and fixed flat fee models.
         */
        fun calculate(orderAmount: Double, type: String, rateOrFee: Double): Double {
            return if (type.equals(TYPE_FIXED, ignoreCase = true)) {
                rateOrFee.coerceAtLeast(0.0)
            } else {
                // Handle percentage input as either decimal (0.035) or whole percent (3.5)
                val rate = if (rateOrFee > 1.0) rateOrFee / 100.0 else rateOrFee
                (orderAmount * rate).coerceAtLeast(0.0)
            }
        }
    }
}
