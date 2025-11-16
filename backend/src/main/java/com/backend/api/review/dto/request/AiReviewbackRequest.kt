package com.backend.api.review.dto.request;

import com.backend.api.question.dto.request.MessagesRequest;
import com.backend.domain.resume.entity.Resume;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record AiReviewbackRequest(
        String model,
        List<MessagesRequest> messages
) {


    public static AiReviewbackRequest of(Resume resume) {

        String systemContent = """
                # 요청 사항:
                1. 당신은 IT 10년차 이상의 시니어 개발자 겸 채용 담당자입니다.
                2. 제공된 이력서 정보와 포트폴리오 URL 내용을 분석해야 합니다.
                3. 분석 내용을 바탕으로 지원자의 강점, 개선 제안, 예상 면접 질문을 생성합니다.
                4. 피드백은 구체적이고 전문적이며, 건설적인 방향으로 제시해야 합니다.
                """;

        String userContent = """
                # 이력서 정보:
                - 이력서 내용: %s
                - 기술 스택: %s
                - 대외 활동: %s
                - 자격증: %s
                - 경력 사항: %s
                - 포트폴리오 URL: %s
                
                # 요청 사항:
                1. 위 '이력서 정보', 특히 '포트폴리오 URL'의 내용을 심층적으로 분석해주세요.
                2. 분석한 내용을 바탕으로 아래 '출력 형식'을 **반드시** 지켜서 피드백을 생성해주세요.
                3. 모든 제목(##, ###)과 항목(✅, 📌, 1.) 사이에는 **반드시 줄바꿈 문자(\\n\\n)를 두 번 포함**하여 문단을 명확히 구분해주세요.
                4. 강점은 1~2개, 개선 제안은 3~4개, 추천 질문은 3개로 구성해주세요.
                5. '출력 형식'의 마크다운 구조(##, ###, ✅, 📌)를 정확히 지켜주세요.
                """.formatted(
                resume.getContent(),
                resume.getSkill(),
                resume.getActivity(),
                resume.getCertification(),
                resume.getCareer(),
                resume.getPortfolioUrl()
        );

        String assistantContent = """
                # 출력 형식:
                ### 강점\n\n✅ 강점 내용\n\n### 개선 제안\n\n📌 **프로젝트 성과 추가**\n- 각 프로젝트의 정량적 성과를 추가하면 좋습니다.\n- 예: "사용자 수 증가율", "성능 개선 비율" 등\n\n📌 **기술적 깊이 강화**\n- 기술 선택 이유와 트레이드오프에 대한 설명 추가\n- 어려웠던 기술적 챌린지와 해결 방법 상세히 기술\n\n📌 **시각적 요소 보완**\n- 프로젝트 스크린샷이나 데모 영상 추가\n- 아키텍처 다이어그램으로 시스템 구조 시각화\n\n📌 **협업 경험 강조**\n- 팀 프로젝트에서의 역할과 기여도 명시\n- 코드 리뷰, 문서화 등 협업 프로세스 경험 추가\n\n### 추천 질문 대비\n\n이 포트폴리오를 바탕으로 예상되는 면접 질문:\n1. "이 프로젝트에서 가장 어려웠던 기술적 문제는 무엇이었나요?"\n2. "왜 이 기술 스택을 선택하셨나요?"\n3. "프로젝트의 성능을 어떻게 최적화하셨나요?"
                """;


        List<MessagesRequest> requests = Stream.of(
                MessagesRequest.of("system", systemContent),
                MessagesRequest.of("user", userContent),
                MessagesRequest.of("assistant", assistantContent)
        ).collect(Collectors.toList());


        return new AiReviewbackRequest("gpt-4o-mini-search-preview", requests);
    }
}
