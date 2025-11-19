package com.backend.api.feedback.event.publisher

import com.backend.domain.answer.entity.Answer
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class FeedbackPublisher(
    private val eventPublisher: ApplicationEventPublisher
) {
    fun publishFeedbackCreate(answer: Answer) {
        eventPublisher.publishEvent(FeedbackCreateEvent(answer))
    }

    fun publishFeedbackUpdate(answer: Answer) {
        eventPublisher.publishEvent(FeedbackUpdateEvent(answer))
    }
}
