package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val unit: String, // e.g. "Crate (24 btls)", "Case (12 packs)", "50L Keg"
    val minPrice: Double,
    val maxPrice: Double,
    val basePrice: Double,
    val stockQty: Int = INFINITE_STOCK, // Defaults to infinite inventory if amount is omitted
    val sku: String,
    val iconKey: String = "beverage",
    val supplierId: String? = null // Null for in-house products, non-null for supplier products
) {
    companion object {
        const val INFINITE_STOCK = -1
    }

    val isInfiniteStock: Boolean
        get() = stockQty < 0 || stockQty >= 999999
}

