package com.backend.api.question.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "AI 면접 질문 생성 요청")
data class AiQuestionRequest(
    val model: String,
    val messages: List<MessagesRequest>
) {
    companion object {
        fun of(skill: String, url: String, count: Int): AiQuestionRequest {

            val system = MessagesRequest.of(
                "system", "#요청 사항:\n" +
                        "1. ${skill}를 기술 스택으로 가지고 있는 IT 10년 이상의 개발자야. \n" +
                        "2. 노션 URL 내용을 분석하고, 프로젝트별 핵심 정보를 바탕으로 질문을 생성합니다.\n" +
                        "3. 기술 스택, 아키텍처, 역할, 성과 등 가능한 모든 정보를 반영합니다.\n" +
                        "4. 질문은 구체적이고 답변하기 쉽게 작성합니다."
            )
            val user = MessagesRequest.of(
                "user", "#요청 사항:\n" +
                        ("1. 노션 url: ${url}\n" +
                                "2. 면접 질문은 ${count}개만 만들어줘\n" +
                                "3. 각 질문 당 점수가 1 ~ 10점 사이로 주어집니다.\n" +
                                "4. 1~2점은 아주 쉬움(1레벨), 3~4점은 쉬움(2레벨), 5~6점은 평범(3레벨), 7~8점은 어려움(4레벨), 9~10점은 매우 어려움(5레벨)으로 구분됩니다.\n" +
                                "5. 문제의 score를 레벨를 기준으로 골고루 만들어줘\n" +
                                "6. 아래 출력 형식을 반드시 지켜줘야 합니다.\n" +
                                "7. 반드시 질문 형태로 만들어줘\n" +
                                "8. 줄 바꿈 처리를 안한 상태로 json 파일을 전달해줘")
            )
            val assistant = MessagesRequest.of(
                "assistant", """
                               # 출력 형식:
                               [{"title": ${url},"content": "이 프로젝트가 해결하려는 핵심 문제는 무엇인가요?","score": 3},{"title": ${url}},"content": "Spring Boot를 선택한 이유와 다른 대안은 무엇이었나요?","score": 4}]
                        
                        """.trim()
            )

            val requests = listOf(
                system,
                user,
                assistant
            )

            return AiQuestionRequest("gpt-4o-mini-search-preview", requests)
        }
    }
}
