package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.StaffMember
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff_members ORDER BY isAdmin DESC, name ASC")
    fun getAllStaff(): Flow<List<StaffMember>>

    @Query("SELECT * FROM staff_members WHERE id = :id LIMIT 1")
    suspend fun getStaffById(id: String): StaffMember?

    @Query("SELECT * FROM staff_members WHERE LOWER(email) = LOWER(:query) OR phone = :query LIMIT 1")
    suspend fun findStaffByEmailOrPhone(query: String): StaffMember?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffMember)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(staffList: List<StaffMember>)

    @Update
    suspend fun updateStaff(staff: StaffMember)

    @Query("DELETE FROM staff_members WHERE id = :id")
    suspend fun deleteStaffById(id: String)

    @Query("SELECT * FROM staff_members WHERE passwordResetRequested = 1 ORDER BY name ASC")
    fun getPasswordResetRequests(): Flow<List<StaffMember>>

    @Query("UPDATE staff_members SET passwordResetRequested = 1 WHERE LOWER(email) = LOWER(:identifier) OR phone = :identifier")
    suspend fun flagPasswordResetRequest(identifier: String): Int

    @Query("UPDATE staff_members SET tempPassword = :tempPass, tempPasswordExpiresAt = :expiresAt, isTemporaryPassword = 1, passwordResetRequested = 0 WHERE id = :staffId")
    suspend fun issueTemporaryPassword(staffId: String, tempPass: String, expiresAt: Long)

    @Query("UPDATE staff_members SET password = :newPassword, isTemporaryPassword = 0, tempPassword = '', tempPasswordExpiresAt = 0 WHERE id = :staffId")
    suspend fun updatePasswordAndClearTemp(staffId: String, newPassword: String)

    @Query("UPDATE staff_members SET adminPin = :newPin WHERE id = :staffId")
    suspend fun updateStaffSecurityPin(staffId: String, newPin: String)

    @Query("UPDATE staff_members SET accountStatus = :status WHERE id = :staffId")
    suspend fun updateAccountStatus(staffId: String, status: String)

    @Query("UPDATE staff_members SET commissionType = :type, commissionRate = :rate, fixedCommissionAmount = :fixedAmount WHERE id = :staffId")
    suspend fun updateCommissionSettings(staffId: String, type: String, rate: Double, fixedAmount: Double)

    @Query("SELECT COUNT(*) FROM staff_members")
    suspend fun getStaffCount(): Int
}
