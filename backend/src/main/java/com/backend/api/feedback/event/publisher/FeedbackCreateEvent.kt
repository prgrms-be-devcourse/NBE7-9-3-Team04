package com.backend.api.feedback.event.publisher

import com.backend.domain.answer.entity.Answer

data class FeedbackCreateEvent(val answer: Answer)
