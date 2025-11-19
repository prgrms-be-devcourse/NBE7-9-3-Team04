package com.backend.api.feedback.event.publisher

import com.backend.domain.answer.entity.Answer


data class FeedbackUpdateEvent(val answer: Answer)
