package com.backend.api.feedback.dto.request


data class AiFeedbackRequest(
    val systemMessage: String,
    val userMessage: String,
    val assistantMessage: String
) {
    companion object {
        fun of(questionContent: String, answerContent: String): AiFeedbackRequest {
            return AiFeedbackRequest(
                "#요청 사항:" +
                        "1. IT 실무 10년 이상의 개발자야.\n" +
                        "2. 현재는 면접 담당자로 질문과 답변에 대한 점수와 피드백을 남겨주는 역할이야." +
                        "3. 피드백은 구체적이고 기술적으로 답변해줘.\n" +
                        "4. 점수 측정 이유도 피드백에 남겨줘.",
                "#요청 사항:\n" +
                        "1. 면접 질문: ${questionContent}\n" +
                        "2. 면접 질문에 대한 답변: ${answerContent}\n" +
                        "3. 면접 질문에 대한 답변에  점수는 1 ~ 100점 사이로 주어집니다.\n" +
                        "4. 점수 측정 이유도 피드백에 남겨줘." +
                        "5. 아래 출력 형식을 반드시 지켜줘야 합니다.\n" +
                        "6. 줄 바꿈 처리를 안한 상태로 json 파일을 전달해줘",
                """
                # 출력 형식:
                [{"content": "질문과 답변이 어울리지 않네여","score": 1},{"content": "질문에 대한 답변에 수치적으로 잘 답한 모습이 좋았습니다.","score": 100}]
         
         """.trimIndent()
            )
        }
    }
}
