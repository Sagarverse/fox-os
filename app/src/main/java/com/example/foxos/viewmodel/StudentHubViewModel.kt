package com.example.foxos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foxos.data.AppDatabase
import com.example.foxos.data.Assignment
import com.example.foxos.data.Exam
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class StudentHubViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val examDao = database.examDao()
    private val assignmentDao = database.assignmentDao()

    val exams: StateFlow<List<Exam>> = examDao.getUpcomingExams(System.currentTimeMillis())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assignments: StateFlow<List<Assignment>> = assignmentDao.getPendingAssignments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addExam(subject: String, examDate: Long, notes: String = "") {
        viewModelScope.launch {
            examDao.insertExam(Exam(subject = subject, examDate = examDate, notes = notes))
        }
    }

    fun updateExam(exam: Exam) {
        viewModelScope.launch {
            examDao.updateExam(exam)
        }
    }

    fun deleteExam(exam: Exam) {
        viewModelScope.launch {
            examDao.deleteExam(exam)
        }
    }

    fun addAssignment(title: String, dueDate: Long, isUrgent: Boolean = false, notes: String = "") {
        viewModelScope.launch {
            assignmentDao.insertAssignment(
                Assignment(title = title, dueDate = dueDate, isUrgent = isUrgent, notes = notes)
            )
        }
    }

    fun updateAssignment(assignment: Assignment) {
        viewModelScope.launch {
            assignmentDao.updateAssignment(assignment)
        }
    }

    fun markAssignmentComplete(assignment: Assignment) {
        viewModelScope.launch {
            assignmentDao.updateAssignment(assignment.copy(isCompleted = true))
        }
    }

    fun deleteAssignment(assignment: Assignment) {
        viewModelScope.launch {
            assignmentDao.deleteAssignment(assignment)
        }
    }

    fun getDaysUntil(timestamp: Long): Int {
        val diff = timestamp - System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }

    fun isUrgent(dueDate: Long): Boolean {
        val hoursUntil = TimeUnit.MILLISECONDS.toHours(dueDate - System.currentTimeMillis())
        return hoursUntil <= 24
    }
}
