package com.example.foxos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.foxos.model.CustomGesture
import kotlinx.coroutines.flow.Flow

@Dao
interface GestureDao {
    @Query("SELECT * FROM custom_gestures")
    fun getAllGestures(): Flow<List<CustomGesture>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGesture(gesture: CustomGesture)

    @Delete
    suspend fun deleteGesture(gesture: CustomGesture)
}