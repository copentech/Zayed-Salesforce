package com.example.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.OrderDatabase
import com.example.data.remote.SupabaseSyncService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = OrderDatabase.getDatabase(applicationContext)
            val syncQueueDao = database.syncQueueDao()
            val customerDao = database.customerDao()
            val orderDao = database.orderDao()
            val commissionDao = database.commissionDao()
            val syncService = SupabaseSyncService(applicationContext)
            val config = syncService.supabaseClient.config.value

            val pendingItems = syncQueueDao.getPendingQueue()
            if (pendingItems.isEmpty()) {
                return@withContext Result.success()
            }

            for (item in pendingItems) {
                val table = when (item.entityType) {
                    "CUSTOMER" -> "customers"
                    "ORDER" -> "orders"
                    "COMMISSION" -> "commissions"
                    "ACTIVITY" -> "staff_activities"
                    "ORDER_STATUS" -> "orders" // Patch status on orders table
                    "STAFF" -> "profiles" // Synchronize staff/profiles
                    "PRODUCT" -> "products"
                    "SUPPLIER" -> "suppliers"
                    "SUPPLIER_PRODUCT" -> "supplier_products"
                    "SUPPLIER_ORDER" -> "supplier_orders"
                    else -> "records"
                }
                
                val syncCall = syncService.syncPayload(
                    supabaseUrl = config.url,
                    supabaseAnonKey = config.anonKey,
                    table = table,
                    payloadJson = item.payloadJson
                )

                if (syncCall.isSuccess) {
                    syncQueueDao.markSynced(item.id)
                    when (item.entityType) {
                        "CUSTOMER" -> customerDao.markAsSynced(item.entityId)
                        "ORDER" -> orderDao.markAsSynced(item.entityId)
                        "ORDER_STATUS" -> orderDao.markAsSynced(item.entityId)
                        "COMMISSION" -> commissionDao.markSynced(item.entityId)
                    }
                } else {
                    syncQueueDao.markFailed(item.id, syncCall.exceptionOrNull()?.message ?: "Sync error")
                }
            }

            syncQueueDao.cleanSyncedItems()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "supabase_background_sync_work"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest = OneTimeWorkRequestBuilder<SupabaseSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncWorkRequest
            )
        }
    }
}
