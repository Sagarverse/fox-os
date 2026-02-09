package com.example.foxos.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.foxos.model.QuickShortcut
import kotlinx.coroutines.flow.Flow

@Dao
interface QuickShortcutDao {
    @Query("SELECT * FROM quick_shortcuts ORDER BY position ASC")
    fun getShortcuts(): Flow<List<QuickShortcut>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setShortcut(shortcut: QuickShortcut)

    @Query("DELETE FROM quick_shortcuts WHERE position = :position")
    suspend fun clearShortcut(position: Int)
}