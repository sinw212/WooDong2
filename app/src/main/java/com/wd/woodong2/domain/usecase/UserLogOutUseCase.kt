package com.wd.woodong2.domain.usecase

import com.wd.woodong2.domain.repository.UserRepository

class UserLogOutUseCase(
    private val repository: UserRepository
) {
    operator fun invoke() {
        return repository.logout()
    }
}