package com.example.foxos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.foxos.model.AppPair
import kotlinx.coroutines.flow.Flow

@Dao
interface AppPairDao {
    @Query("SELECT * FROM app_pairs")
    fun getAllAppPairs(): Flow<List<AppPair>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppPair(appPair: AppPair)

    @Delete
    suspend fun deleteAppPair(appPair: AppPair)
}