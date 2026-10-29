package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CommissionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface CommissionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommission(commission: CommissionRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(commissions: List<CommissionRecord>)

    @Update
    suspend fun updateCommission(commission: CommissionRecord)

    @Query("SELECT * FROM commissions ORDER BY createdAt DESC")
    fun getAllCommissions(): Flow<List<CommissionRecord>>

    @Query("SELECT * FROM commissions WHERE repId = :repId ORDER BY createdAt DESC")
    fun getCommissionsByRep(repId: String): Flow<List<CommissionRecord>>

    @Query("SELECT * FROM commissions WHERE orderId = :orderId LIMIT 1")
    suspend fun getCommissionByOrderId(orderId: String): CommissionRecord?

    @Query("""
        UPDATE commissions 
        SET status = 'PAID', 
            paymentCode = :paymentCode, 
            paidAt = :paidAt,
            isSyncedToSupabase = 0
        WHERE orderId = :orderId
    """)
    suspend fun approveAndPayCommission(orderId: String, paymentCode: String, paidAt: Long)

    @Query("UPDATE commissions SET isSyncedToSupabase = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("SELECT * FROM commissions WHERE isSyncedToSupabase = 0")
    suspend fun getUnsyncedCommissions(): List<CommissionRecord>

    @Query("""
        SELECT COALESCE(SUM(calculatedAmount), 0.0) 
        FROM commissions 
        WHERE repId = :repId AND (status = 'PAID' OR status = 'APPROVED')
    """)
    fun getTotalCommissionEarned(repId: String): Flow<Double?>

    @Query("""
        SELECT COALESCE(SUM(calculatedAmount), 0.0) 
        FROM commissions 
        WHERE repId = :repId AND status = 'PENDING'
    """)
    fun getTotalCommissionPending(repId: String): Flow<Double?>

    @Query("DELETE FROM commissions WHERE orderId = :orderId")
    suspend fun deleteByOrderId(orderId: String)
}
