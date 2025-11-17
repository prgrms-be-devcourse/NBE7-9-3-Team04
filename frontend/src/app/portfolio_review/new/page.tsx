"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { fetchApi } from "@/lib/client";
import { marked } from "marked";
import { toast } from "sonner";

export default function NewFeedbackPage() {
  const router = useRouter();

  const [progress, setProgress] = useState(0);
  const [feedbackContent, setFeedbackContent] = useState("");
  const [isAnalysisComplete, setIsAnalysisComplete] = useState(false);
  const [createDate, setCreateDate] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isPremium, setIsPremium] = useState<boolean | null>(null); // null = 아직 로딩 전

  // 진행 상태 애니메이션
  useEffect(() => {
    if (isPremium !== true) return;
    
    const interval = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 100) {
          clearInterval(interval);
          return 100;
        }
        return prev + 10;
      });
    }, 300);
    return () => clearInterval(interval);
  }, []);

  // ✅ 프리미엄 여부 확인 함수
  const checkPremiumStatus = async () => {
    try {
      console.log("🔍 API 호출 시작: /api/v1/subscriptions/me");
      const response = await fetchApi("/api/v1/subscriptions/me", { method: "GET" });
      console.log("✅ API 응답 전체:", response);

      if (response?.status === "OK" && response.data) {
        const { subscriptionType, isActive } = response.data;
        console.log("🔥 subscriptionType:", subscriptionType);
        console.log("🔥 isActive:", isActive);

        const premium = subscriptionType === "PREMIUM" && isActive === true;
        setIsPremium(premium);
        console.log("⭐ Premium 여부 상태 업데이트:", premium);
      } else {
        setIsPremium(false);
        console.log("⚠️ Premium 상태 기본값으로 설정: false");
      }
    } catch (error) {
      console.error("❌ API 호출 오류:", error);
      setIsPremium(false);
    }
  };

  // ✅ 처음 마운트 시 프리미엄 확인
  useEffect(() => {
    checkPremiumStatus();
  }, []);

  // ✅ Premium 상태 확정 후 동작 분기
  useEffect(() => {
    if (isPremium === null) return; // 아직 로딩 중이면 아무 것도 안 함

    if (isPremium === false) {
      toast.error("포트폴리오 첨삭은 PREMIUM 등급 사용자만 이용 가능합니다.");
      router.push("/mypage/premium");
      return;
    }

    // Premium일 때만 AI 첨삭 시작
    if (isPremium === true) {
      createFeedback();
    }
  }, [isPremium]);

  // ✅ AI 피드백 생성 함수
  const createFeedback = async () => {
    const MAX_RETRIES = 3; // 최대 재시도 횟수
    let attempt = 0;

    while (attempt < MAX_RETRIES) {
      try {
        console.log(`🔄 리뷰 생성 시도 ${attempt + 1}`);
        const response = await fetchApi("/api/v1/portfolio-review", { method: "POST" });
        console.log("✅ 생성된 리뷰 데이터:", response.data);

        const { feedbackContent, createDate } = response.data;
        const parsedContent = await marked.parse(feedbackContent);
        setFeedbackContent(parsedContent);
        setCreateDate(createDate);
        setIsAnalysisComplete(true);
        toast.success("✅ AI 포트폴리오 분석이 완료되었습니다!");
        return; // 성공 시 함수 종료
      } catch (error) {
        console.error(`❌ 리뷰 생성 실패 (시도 ${attempt + 1}):`, error);
        attempt++;
        if (attempt >= MAX_RETRIES) {
          toast.error("❌ AI 포트폴리오 분석 생성에 실패했습니다. 나중에 다시 시도해주세요.");
        }
      }
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-6 py-20 relative">
      {/* 뒤로가기 버튼 */}
      {isAnalysisComplete && (
        <div className="absolute top-4 left-4">
          <a
            href="/portfolio_review"
            className="text-blue-500 hover:underline flex items-center gap-2"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              strokeWidth="1.5"
              stroke="currentColor"
              className="w-5 h-5"
            >
              <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
            </svg>
            첨삭 목록으로
          </a>
        </div>
      )}

      {/* 로딩/분석 중 화면 */}
      {!isAnalysisComplete && (
        <div className="text-center">
          <h1 className="text-3xl font-bold mb-4">AI 포트폴리오 분석 중...</h1>
          <p className="text-gray-600 mb-10">
            AI가 당신의 포트폴리오를 정밀 분석하고 있습니다. 잠시만 기다려주세요.
          </p>

          <div className="w-full max-w-md mx-auto bg-gray-200 rounded-full h-3 mb-6">
            <div
              className="bg-blue-600 h-3 rounded-full transition-all duration-300"
              style={{ width: `${progress}%` }}
            />
          </div>

          <p className="text-gray-500 mb-16">{progress}% 완료</p>

          <div className="flex flex-col gap-2 items-center">
            <div className="animate-spin rounded-full h-10 w-10 border-4 border-blue-500 border-t-transparent" />
          </div>
        </div>
      )}

      {/* 분석 결과 화면 */}
      {isAnalysisComplete && feedbackContent && (
        <div className="mt-10">
          <h2 className="text-3xl font-bold mb-6 text-center">AI 첨삭 결과</h2>
          <div className="bg-white shadow-md rounded-lg p-6">
            <h3 className="text-2xl font-semibold mb-4">포트폴리오 분석 결과</h3>
            <p className="text-sm text-gray-500 mb-4">
              생성일: {new Date(createDate).toLocaleString()}
            </p>
            <div
              className="prose prose-blue max-w-none"
              dangerouslySetInnerHTML={{ __html: feedbackContent }}
            />
          </div>
        </div>
      )}
    </div>
  );
}
