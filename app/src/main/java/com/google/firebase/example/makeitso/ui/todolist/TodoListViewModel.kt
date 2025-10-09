package com.google.firebase.example.makeitso.ui.todolist

import com.google.firebase.example.makeitso.MainViewModel
import com.google.firebase.example.makeitso.data.model.TodoItem
import com.google.firebase.example.makeitso.data.repository.AuthRepository
import com.google.firebase.example.makeitso.data.repository.TodoItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val todoItemRepository: TodoItemRepository
) : MainViewModel() {
    private val _isLoadingUser = MutableStateFlow(true)
    val isLoadingUser: StateFlow<Boolean>
        get() = _isLoadingUser.asStateFlow()
    private val _navigateToSignIn = MutableStateFlow(false)
    val navigateToSignIn: StateFlow<Boolean>
        get() = _navigateToSignIn.asStateFlow()
    fun resetNavigation() {
        _navigateToSignIn.value = false
    }

    val todoItems = todoItemRepository.getTodoItems(authRepository.currentUserIdFlow)

    fun loadCurrentUser() {
        launchCatching {
            if (authRepository.currentUser == null) {
                _navigateToSignIn.value = true
            } else {
                _isLoadingUser.value = false
            }
        }
    }

    fun updateItem(item: TodoItem) {
        launchCatching {
            todoItemRepository.update(item)
        }
    }
}