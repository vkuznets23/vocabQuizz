package com.vocabquizzz.vocab_quizz.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.repository.UserQuizSettingsRepository
import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.entity.UserQuizSettings
import java.time.Instant
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class UserQuizSettingsService(
    private val repository: UserQuizSettingsRepository,
) {
    fun getSettings(user: User): UserQuizSettings {
        return repository.findByUserId(user.id) 
            ?: throw EntityNotFoundException("Settings missing for user ${user.id}")
    }

    fun findEnabled(): List<UserQuizSettings> =
        repository.findByEnabledTrue()

    fun setEnabled(user: User, enabled: Boolean) {
        val settings = getSettings(user)
    
        if (settings.enabled == enabled) return

        settings.enabled = enabled
        settings.updatedAt = Instant.now()
        repository.save(settings)
    }

    fun setInterval(user: User, hours: Int) {
        require(hours in 1..3) { "Interval hours must be between 1 and 3" }

        val settings = getSettings(user)
        if (settings.intervalHours == hours) return

        settings.intervalHours = hours
        settings.updatedAt = Instant.now()
        repository.save(settings)
    }

    fun setQuestionsPerLesson(user: User, n: Int) {
        require(n in 5..10) { "Questions per lesson must be between 5 and 10" }

        val settings = getSettings(user)
        if (settings.questionsPerLesson == n) return

        settings.questionsPerLesson = n
        settings.updatedAt = Instant.now()
        repository.save(settings)
    }

    fun setWindowPreset(user: User, preset: String) {
        val parts = preset.split("-")
        require(parts.size == 2) { "Invalid window preset: $preset" }

        val startHour = parts[0].toIntOrNull()
            ?: throw IllegalArgumentException("Invalid start hour: $parts[0]")

        val endHour = parts[1].toIntOrNull()
            ?: throw IllegalArgumentException("Invalid end hour: $parts[1]")

        require(startHour in 0..23 && endHour in 0..23) {
            "Hours must be between 0 and 23"
        }

        require(startHour < endHour) {
            "Start hour must be before end hour"
        }

        val settings = getSettings(user)
        if (settings.windowStart == LocalTime.of(startHour, 0) && settings.windowEnd == LocalTime.of(endHour, 0)) return

        settings.windowStart = LocalTime.of(startHour, 0)
        settings.windowEnd = LocalTime.of(endHour, 0)
        settings.updatedAt = Instant.now()
        repository.save(settings)
    }

    fun isEligibleNow(settings: UserQuizSettings): Boolean {
        val now = ZonedDateTime.now(ZoneId.of(settings.timezone)).toLocalTime()
        if (now !in settings.windowStart..settings.windowEnd) return false

        val lastSent = settings.lastQuizSentAt ?: return true
        return Duration.between(lastSent, Instant.now()) >= Duration.ofHours(settings.intervalHours.toLong())
    }

    fun markQuizSent(settings: UserQuizSettings) {
        settings.lastQuizSentAt = Instant.now()
        settings.updatedAt = Instant.now()
        repository.save(settings)
    }

}