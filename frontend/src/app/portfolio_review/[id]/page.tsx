"use client";

import { marked } from "marked"; // 마크다운 파싱 라이브러리 추가
import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { fetchApi } from "@/lib/client";
import { toast } from "sonner";

export default function FeedbackDetailPage() {
  const router = useRouter();
  const { id } = useParams();
  const [feedback, setFeedback] = useState<any | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchFeedback = async () => {
      setIsLoading(true);
      try {
        const response = await fetchApi(`/api/v1/portfolio-review/${id}`);
        console.log("✅ Feedback data:", response.data);

        // 마크다운을 HTML로 변환하여 상태에 설정
        const parsedContent = marked.parse(response.data.feedbackContent);
        setFeedback({
          ...response.data,
          feedbackContent: parsedContent,
          createdAt: response.data.createDate || "날짜 정보 없음",
        });
      } catch (error) {
        console.error("❌ Failed to fetch feedback:", error);
        toast.error("피드백을 불러오는 데 실패했습니다.");
        router.push("/portfolio_review");
      } finally {
        setIsLoading(false);
      }
    };

    fetchFeedback();
  }, [id, router]);

  const formatDate = (dateStr: string) => {
    if (!dateStr) return "날짜 정보 없음";

    // 소수점 이하의 초 제거
    const cleanedDateStr = dateStr.split(".")[0];
    console.log("✅ cleanedDateStr:", cleanedDateStr); // cleanedDateStr 디버깅 로그 추가

    const date = new Date(cleanedDateStr);
    if (isNaN(date.getTime())) return "유효하지 않은 날짜";

    const formattedDate = date.toLocaleString("ko-KR", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
    });

    console.log("✅ Formatted date:", formattedDate);
    return formattedDate;
  };

  if (isLoading) {
    return (
      <div className="max-w-4xl mx-auto px-6 py-20 text-center text-gray-500">
        ⏳ 첨삭 결과를 불러오는 중입니다...
      </div>
    );
  }

  if (!feedback) {
    return (
      <div className="max-w-4xl mx-auto px-6 py-20 text-center">
        <p className="text-gray-600 mb-6">존재하지 않는 첨삭 결과입니다.</p>
        <button
          onClick={() => router.push("/portfolio_review")}
          className="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2 rounded-md font-semibold text-sm transition"
        >
          목록으로 돌아가기
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-6 py-10">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-2xl font-bold mb-1">AI 첨삭 결과</h1>
          <p className="text-gray-500 text-sm">
            생성일: {formatDate(feedback.createdAt)}
          </p>
        </div>

        <button
          onClick={() => router.push("/portfolio_review")}
          className="bg-gray-200 hover:bg-gray-300 text-gray-800 px-4 py-2 rounded-md text-sm font-medium transition"
        >
          목록으로 돌아가기
        </button>
      </div>

      <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-6">
        <div
          className="prose prose-gray max-w-none whitespace-pre-wrap leading-relaxed"
          dangerouslySetInnerHTML={{ __html: feedback.feedbackContent }}
        />
      </div>
    </div>
  );
}
