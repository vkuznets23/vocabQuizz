package com.vocabquizzz.vocab_quizz.repository

import org.springframework.data.jpa.repository.EntityGraph
import com.vocabquizzz.vocab_quizz.entity.UserQuizSettings
import org.springframework.data.jpa.repository.JpaRepository

interface UserQuizSettingsRepository : JpaRepository<UserQuizSettings, Long> {
    // find user's settings by user id
    fun findByUserId(userId: Long): UserQuizSettings?

    // all turned on quiz settings
    @EntityGraph(attributePaths = ["user"]) // to load user data over LAZY settings
    fun findByEnabledTrue(): List<UserQuizSettings>
}