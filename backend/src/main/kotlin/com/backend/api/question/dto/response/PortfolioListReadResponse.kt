package com.backend.api.question.dto.response


data class PortfolioListReadResponse(
    val title: String,
    val count: Long,
    val questions: List<PortfolioReadResponse>
) {
    companion object {
        fun from(
            title: String,
            count: Long,
            questions: List<PortfolioReadResponse>
        ): PortfolioListReadResponse {
            return PortfolioListReadResponse(title, count, questions)
        }
    }
}
