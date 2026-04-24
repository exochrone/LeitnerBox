package com.jb.leitnerbox.core.domain.usecase.session

import com.jb.leitnerbox.core.domain.model.Session
import com.jb.leitnerbox.core.domain.repository.SessionRepository

class SaveSessionUseCase(private val repository: SessionRepository) {
    suspend operator fun invoke(session: Session): Long =
        repository.insertSession(session)
}
