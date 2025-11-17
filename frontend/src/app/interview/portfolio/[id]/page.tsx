"use client";

import { useState, useEffect } from "react";
import { useRouter, useParams } from "next/navigation";
import { fetchApi } from "@/lib/client";
import { Question, Feedback } from "@/types/aiquestion";
import { toast } from "sonner";

export default function PortfolioQuestionDetailPage() {
  const router = useRouter();
  const params = useParams();
  const groupId = params?.id;

  const [questions, setQuestions] = useState<Question[]>([]);
  const [title, setTitle] = useState("");
  const [currentIndex, setCurrentIndex] = useState(0);
  const [answers, setAnswers] = useState<Record<number, string>>({});
  const [feedback, setFeedback] = useState<Record<number, Feedback>>({});
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [feedbackLoading, setFeedbackLoading] = useState(false);
  const [showFeedback, setShowFeedback] = useState(false);
  //const [alertMsg, setAlertMsg] = useState<string | null>(null);

  const current = questions[currentIndex];
  const progress = ((currentIndex + 1) / questions.length) * 100;
  const hasFeedback = !!feedback[current?.id];
  const isLastQuestion = currentIndex === questions.length - 1;

  // -------------------------------
  // 피드백 재조회 함수
  // -------------------------------
  const fetchFeedbackWithRetry = async (
    questionId: number,
    attempt = 1,
    maxAttempts = 10
  ) => {
    try {
      const fbRes = await fetchApi(`/api/v1/feedback/${questionId}`, {
        method: "GET",
      });
      if (fbRes?.data) {
        setFeedback((prev) => ({
          ...prev,
          [questionId]: {
            score: fbRes.data.score ?? 0,
            comment: fbRes.data.comment ?? fbRes.data.content ?? "",
          },
        }));
        setAnswers((prev) => ({ ...prev, [questionId]: input }));
        setShowFeedback(true);
        setFeedbackLoading(false);
        return true; // 성공
      }
      throw new Error("피드백 데이터 없음");
    } catch {
      if (attempt < maxAttempts) {
        setTimeout(
          () => fetchFeedbackWithRetry(questionId, attempt + 1, maxAttempts),
          3000
        );
      } else {
        toast.error("❌ 피드백 조회 실패");
        setShowFeedback(true);
        setFeedbackLoading(false);
      }
    }
  };

  // -------------------------------
  // 질문 목록 및 기존 답변/피드백 조회
  // -------------------------------
  useEffect(() => {
    const fetchQuestions = async () => {
      if (!groupId) return;
      try {
        const res = await fetchApi(`/api/v1/ai/questions/${groupId}`, {
          method: "GET",
        });
        if (res?.data) {
          const mapped = res.data.questions
            .map((q: any) => ({ id: q.id, content: q.content }))
            .sort((a: Question, b: Question) => a.id - b.id);

          setQuestions(mapped);
          setTitle(res.data.title);

          const tempAnswers: Record<number, string> = {};
          const tempFeedback: Record<number, Feedback> = {};

          for (const q of mapped) {
            try {
              const ansRes = await fetchApi(
                `/api/v1/questions/${q.id}/answers/mine`,
                { method: "GET" }
              );
              if (ansRes?.data?.content)
                tempAnswers[q.id] = ansRes.data.content;

              const fbRes = await fetchApi(`/api/v1/feedback/${q.id}`, {
                method: "GET",
              });
              if (fbRes?.data) {
                tempFeedback[q.id] = {
                  score: fbRes.data.score ?? 0,
                  comment: fbRes.data.comment ?? fbRes.data.content ?? "",
                };
              }
            } catch {
              // 조회 실패 무시
            }
          }

          setAnswers(tempAnswers);
          setFeedback(tempFeedback);
        }
      } catch (err) {
        console.error("❌ 질문 조회 실패:", err);
        toast.error("질문 조회 중 오류가 발생했습니다.");
      }
    };

    fetchQuestions();
  }, [groupId]);

  // -------------------------------
  // 답변 제출 및 피드백 조회
  // -------------------------------
  const handleSubmit = async () => {
    if (!input.trim()) {
      toast.error("답변을 입력해주세요!");
      return;
    }

    setLoading(true);
    setFeedbackLoading(true);
    try {
      // 답변 생성
      const answerRes = await fetchApi(
        `/api/v1/questions/${current.id}/answers`,
        {
          method: "POST",
          body: JSON.stringify({ content: input, isPublic: true }),
        }
      );

      if (!answerRes?.data) throw new Error("답변 생성 실패");

      // 피드백 조회 재시도
      fetchFeedbackWithRetry(current.id);
    } catch (err) {
      console.error("❌ 답변 생성 실패:", err);
      toast.error("답변 생성 중 오류가 발생했습니다.");
      setLoading(false);
      setFeedbackLoading(false);
    } finally {
      setLoading(false);
    }
  };

  // -------------------------------
  // 다음 질문
  // -------------------------------
  const handleNext = () => {
    if (!isLastQuestion) {
      setCurrentIndex((prev) => prev + 1);
      setInput("");
      setShowFeedback(false);
    }
  };

  if (!questions.length)
    return <p className="text-center mt-10">질문이 없습니다.</p>;

  return (
    <div className="max-w-3xl mx-auto px-6 py-10">
      {/* 제목 및 진행상황 */}
      <div className="flex items-center justify-between mb-6">
        <button
          onClick={() => router.replace("/interview/portfolio")}
          className="text-gray-600 hover:text-gray-800"
        >
          ← 목록으로
        </button>
        <span className="px-3 py-1 text-sm bg-blue-100 rounded-md text-gray-600">
          {currentIndex + 1} / {questions.length}
        </span>
      </div>

      <h2 className="text-2xl font-bold mb-4">{title}</h2>
      <div className="w-full bg-gray-200 rounded-full h-2 mb-6">
        <div
          className="bg-blue-600 h-2 rounded-full transition-all duration-500"
          style={{ width: `${progress}%` }}
        />
      </div>

      {/* 질문 카드 */}
      <div className="border border-blue-200 bg-blue-50 rounded-lg p-5 mb-6">
        <div className="flex items-start gap-3">
          <div className="text-blue-600 text-lg mt-1">✨</div>
          <div>
            <h3 className="text-lg font-semibold mb-1">{current.content}</h3>
            <p className="text-sm text-gray-500">
              AI가 생성한 예상 면접 질문입니다.
            </p>
          </div>
        </div>
      </div>

      {/* 답변 입력 */}
      <div className="space-y-4">
        <textarea
          className="w-full border border-gray-200 rounded-md p-3 focus:outline-none focus:ring-2 focus:ring-blue-300 resize-none"
          rows={6}
          placeholder="이 질문에 대한 답변을 작성하세요"
          value={hasFeedback ? answers[current.id] ?? "" : input}
          onChange={(e) => setInput(e.target.value)}
          disabled={loading || hasFeedback}
        />
        {!hasFeedback && (
          <div className="flex justify-end">
            <button
              onClick={handleSubmit}
              disabled={loading}
              className="bg-blue-600 text-white px-5 py-2 rounded-md hover:bg-blue-700 disabled:opacity-60"
            >
              답변 제출
            </button>
          </div>
        )}
      </div>

      {/* 피드백 */}
      {(hasFeedback || feedbackLoading) && (
        <div className="space-y-6 mt-6">
          <div>
            <h4 className="font-semibold mb-2">💡 AI 피드백</h4>

            <div className="p-4 border border-blue-200 bg-blue-50 rounded-md min-h-[100px]">
              {feedbackLoading ? (
                // ✅ AI 피드백 생성 애니메이션
                <div className="flex flex-col items-center justify-center py-10 text-center animate-fade-in">
                  <div className="animate-spin text-blue-500 text-5xl mb-3">
                    ⏳
                  </div>
                  <p className="text-gray-700 font-medium animate-pulse">
                    AI가 당신의 답변을 분석 중이에요...
                  </p>
                  <p className="text-sm text-gray-500">
                    잠시만 기다려주세요 🤖
                  </p>
                </div>
              ) : (
                <div className="flex flex-col gap-3">
                  <p className="text-gray-800 text-sm whitespace-pre-wrap leading-relaxed">
                    {feedback[current.id]?.comment}
                  </p>
                  <div className="flex justify-end">
                    <span className="text-sm bg-blue-500 text-white px-3 py-1 rounded-md shadow-sm">
                      AI 점수: {feedback[current.id]?.score ?? "-"}점
                    </span>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* 버튼 영역 */}
          <div className="flex justify-end mt-2">
            {isLastQuestion ? (
              <button
                onClick={() => router.replace("/interview/portfolio")}
                className="bg-green-500 text-white px-5 py-2 rounded-md hover:bg-green-600"
              >
                목록으로 돌아가기
              </button>
            ) : (
              <button
                onClick={handleNext}
                className="bg-blue-500 text-white px-5 py-2 rounded-md hover:bg-blue-600"
              >
                다음 질문
              </button>
            )}
          </div>
        </div>
      )}

      {/* 경고 메시지
      {alertMsg && (
        <div className="mt-4 text-red-500 font-medium text-center">
          {alertMsg}
        </div> */}
      {/* )} */}
    </div>
  );
}
