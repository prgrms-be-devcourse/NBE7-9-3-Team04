package com.backend.api.feedback.event

import com.backend.api.feedback.event.publisher.FeedbackCreateEvent
import com.backend.api.feedback.event.publisher.FeedbackUpdateEvent
import com.backend.api.feedback.service.FeedbackService
import com.backend.global.exception.GlobalExceptionHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class FeedbackEventListener(
    private val feedbackService: FeedbackService,
    private val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
) {


    @Async("feedbackExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onCreateFeedbackEvent(event: FeedbackCreateEvent) {
        logger.info("FeedbackEventLister - onCreateFeedbackEvent: {}", event.answer.id)
        feedbackService.createFeedback(event.answer)
        logger.info("FeedbackEventLister - onCreateFeedbackEvent End: {}", event.answer.id)
    }

    @Async("feedbackExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onUpdateFeedbackEvent(event: FeedbackUpdateEvent) {
        logger.info("FeedbackEventLister - onUpdateFeedbackEvent: {}", event.answer.id)
        feedbackService.updateFeedback(event.answer)
        logger.info("FeedbackEventLister - onUpdateFeedbackEvent End: {}", event.answer.id)
    }
}
