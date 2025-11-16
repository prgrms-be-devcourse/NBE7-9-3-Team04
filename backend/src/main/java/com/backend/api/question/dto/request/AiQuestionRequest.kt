package com.backend.api.question.dto.request;

import com.backend.api.question.dto.response.AiQuestionResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Schema(description = "AI 면접 질문 생성 요청")
public record AiQuestionRequest(
        @Schema(description = "AI 모델")
        String model,
        @Schema(description = "프롬프트")
        List<MessagesRequest> messages
) {
    public static AiQuestionRequest of(String skill,String url, int count){
        List<MessagesRequest> requests = Stream.of(
                MessagesRequest.of("system","#요청 사항:\n" +
                        "1. %s를 기술 스택으로 가지고 있는 IT 10년 이상의 개발자야. \n" +
                        "2. 노션 URL 내용을 분석하고, 프로젝트별 핵심 정보를 바탕으로 질문을 생성합니다.\n" +
                        "3. 기술 스택, 아키텍처, 역할, 성과 등 가능한 모든 정보를 반영합니다.\n" +
                        "4. 질문은 구체적이고 답변하기 쉽게 작성합니다.".formatted(skill)),
                MessagesRequest.of("user","#요청 사항:\n" +
                        ("1. 노션 url: %s\n" +
                                "2. 면접 질문은 %d개만 만들어줘\n" +
                                "3. 각 질문 당 점수가 1 ~ 10점 사이로 주어집니다.\n" +
                                "4. 1~2점은 아주 쉬움(1레벨), 3~4점은 쉬움(2레벨), 5~6점은 평범(3레벨), 7~8점은 어려움(4레벨), 9~10점은 매우 어려움(5레벨)으로 구분됩니다.\n" +
                                "5. 문제의 score를 레벨를 기준으로 골고루 만들어줘\n" +
                                "6. 아래 출력 형식을 반드시 지켜줘야 합니다.\n" +
                                "7. 반드시 질문 형태로 만들어줘\n" +
                                "8. 줄 바꿈 처리를 안한 상태로 json 파일을 전달해줘").formatted(url,count)),
                MessagesRequest.of("assistant","""
                               # 출력 형식:
                               [{"title": %s,"content": "이 프로젝트가 해결하려는 핵심 문제는 무엇인가요?","score": 3},{"title": %s,"content": "Spring Boot를 선택한 이유와 다른 대안은 무엇이었나요?","score": 4}]
                        """.formatted(url,url))
        ).collect(Collectors.toList());
        return new AiQuestionRequest("gpt-4o-mini-search-preview",requests);
    }
}
