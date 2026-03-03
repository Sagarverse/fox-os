package com.example.foxos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.foxos.model.AppPair
import com.example.foxos.model.CustomGesture
import com.example.foxos.model.QuickShortcut

@Database(entities = [Task::class, AppPair::class, CustomGesture::class, QuickShortcut::class, Note::class, Exam::class, Assignment::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun appPairDao(): AppPairDao
    abstract fun gestureDao(): GestureDao
    abstract fun quickShortcutDao(): QuickShortcutDao
    abstract fun noteDao(): NoteDao
    abstract fun examDao(): ExamDao
    abstract fun assignmentDao(): AssignmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "foxos_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}