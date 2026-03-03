package com.example.foxos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments ORDER BY isCompleted ASC, dueDate ASC")
    fun getAllAssignments(): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE isCompleted = 0 ORDER BY dueDate ASC")
    fun getPendingAssignments(): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE isCompleted = 0 AND dueDate <= :urgentThreshold ORDER BY dueDate ASC")
    fun getUrgentAssignments(urgentThreshold: Long): Flow<List<Assignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: Assignment)

    @Update
    suspend fun updateAssignment(assignment: Assignment)

    @Delete
    suspend fun deleteAssignment(assignment: Assignment)
}
