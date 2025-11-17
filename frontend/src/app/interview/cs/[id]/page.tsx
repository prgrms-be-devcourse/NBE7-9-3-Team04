"use client";

import Link from "next/link";
import { useRouter, useParams } from "next/navigation";
import { useState, useEffect } from "react";
import { fetchApi } from "@/lib/client";
import {
  AnswerCreateRequest,
  AnswerCreateResponse,
  MyAnswerReadResponse,
} from "@/types/answer";
import { QuestionResponse } from "@/types/question";
import { FeedbackReadResponse } from "@/types/feedback";
import { toast } from "sonner";

export default function QuestionDetailPage() {
  const router = useRouter();
  const params = useParams();
  const questionId = params.id as string;

  const [question, setQuestion] = useState<QuestionResponse | null>(null);
  const [answer, setAnswer] = useState("");
  const [submitted, setSubmitted] = useState(false);
  const [isJustSubmitted, setIsJustSubmitted] = useState(false);
  const [submittedAnswer, setSubmittedAnswer] =
    useState<AnswerCreateResponse | null>(null);
  const [feedback, setFeedback] = useState<FeedbackReadResponse | null>(null);
  const [feedbackMessage, setFeedbackMessage] = useState<string>(""); // 피드백 상태 메시지
  const [loading, setLoading] = useState(true);
  const [isPublic, setIsPublic] = useState(true);

  // 질문 + 내 답변 + 피드백 불러오기
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);

        // 질문 조회
        const res = (await fetchApi(`/api/v1/questions/${questionId}`)) as {
          status: string;
          data: QuestionResponse;
          message?: string;
        };
        if (res.status === "OK") setQuestion(res.data);
        else toast.error(`질문 조회 실패: ${res.message}`);

        // 내 답변 조회
        const myAnswerRes = (await fetchApi(
          `/api/v1/questions/${questionId}/answers/mine`
        )) as {
          status: string;
          data: MyAnswerReadResponse;
          message?: string;
        };
        if (myAnswerRes.status === "OK" && myAnswerRes.data) {
          setSubmitted(true);
          setSubmittedAnswer(myAnswerRes.data);
          setAnswer(myAnswerRes.data.content);
          setIsPublic(myAnswerRes.data.isPublic);
        }

        // 피드백 조회
        const feedbackRes = (await fetchApi(
          `/api/v1/feedback/${questionId}`
        )) as {
          status: string;
          data: FeedbackReadResponse;
          message?: string;
        };

        if (feedbackRes.status === "OK") {
          setFeedback(feedbackRes.data);
        } else {
          setFeedback(null);
        }
      } catch (err) {
        console.warn("피드백 조회 실패:", err);
        setFeedback(null);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [questionId]);

  // ✅ 수정된 폴링 로직
  const startFeedbackPolling = async () => {
    setFeedbackMessage("AI가 피드백을 생성중입니다...");

    const delays = [2000, 2000, 2000, 4000, 5000, 5000, 5000]; // 요청 간격
    let attempt = 0;
    let success = false;

    const poll = async () => {
      if (attempt >= delays.length || success) return;
      attempt++;

      try {
        const res = (await fetchApi(`/api/v1/feedback/${questionId}`)) as {
          status: string;
          data: FeedbackReadResponse;
          message?: string;
        };

        if (res.status === "OK") {
          setFeedback(res.data);
          setFeedbackMessage(""); // 성공 시 메시지 제거
          success = true;
          return;
        } else {
          console.warn(
            `피드백 응답 오류 (${attempt}/${delays.length}): ${res.message}`
          );
        }
      } catch (err) {
        console.warn(`피드백 요청 실패 (${attempt}/${delays.length}):`, err);
      }

      // 실패 시 다음 요청 예약
      if (attempt < delays.length) {
        setTimeout(poll, delays[attempt]);
      } else {
        // 7회 모두 실패
        setFeedbackMessage(
          "피드백 확인에 실패했습니다. 새로고침하여 다시 확인해주세요."
        );
      }
    };

    // 첫 번째 요청
    setTimeout(poll, delays[0]);
  };

  // 답변 제출
  const handleSubmit = async () => {
    if (!answer.trim()) {
      toast.error("답변을 입력해주세요.");
      return;
    }

    try {
      if (submittedAnswer) {
        // 기존 답변 PATCH
        const res = (await fetchApi(
          `/api/v1/questions/${questionId}/answers/${submittedAnswer.id}`,
          {
            method: "PATCH",
            body: JSON.stringify({
              content: answer,
              isPublic: isPublic,
            }),
          }
        )) as { status: string; data: AnswerCreateResponse; message?: string };

        if (res.status === "OK") {
          setSubmittedAnswer(res.data);
          setSubmitted(true);
          setIsJustSubmitted(true);
          toast.success("답변이 수정되었습니다!");
          startFeedbackPolling();
        } else {
          toast.error(`답변 수정 실패: ${res.message}`);
        }
      } else {
        // 새 답변 POST
        const payload: AnswerCreateRequest = {
          content: answer,
          isPublic: isPublic,
        };

        const res = (await fetchApi(`/api/v1/questions/${questionId}/answers`, {
          method: "POST",
          body: JSON.stringify(payload),
        })) as { status: string; data: AnswerCreateResponse; message?: string };

        if (res.status === "CREATED") {
          setSubmittedAnswer(res.data);
          setSubmitted(true);
          setIsJustSubmitted(true);
          toast.success("답변이 제출되었습니다!");
          startFeedbackPolling();
        } else {
          toast.error(`답변 제출 실패: ${res.message}`);
        }
      }
    } catch (err) {
      console.error(err);
      toast.error("답변 제출 중 오류가 발생했습니다.");
    }
  };

  // 공개/비공개 전환
  const handleToggleVisibility = async () => {
    if (!submittedAnswer) return;

    try {
      const updatedIsPublic = !isPublic;

      const res = (await fetchApi(
        `/api/v1/questions/${questionId}/answers/${submittedAnswer.id}`,
        {
          method: "PATCH",
          body: JSON.stringify({ isPublic: updatedIsPublic }),
        }
      )) as { status: string; data: AnswerCreateResponse; message?: string };

      if (res.status === "OK") {
        setIsPublic(updatedIsPublic);
        setSubmittedAnswer(res.data);
      } else {
        toast.error(`공개/비공개 전환 실패: ${res.message}`);
      }
    } catch (err) {
      console.error(err);
      toast.error("공개/비공개 전환 중 오류가 발생했습니다.");
    }
  };

  // 다시 풀기 버튼
  const handleRetry = () => {
    setSubmitted(false);
    setAnswer(submittedAnswer?.content || "");
    setIsJustSubmitted(false);
    setFeedback(null);
    setFeedbackMessage("");
  };

  if (loading || !question) {
    return <div className="text-center py-12">로딩 중...</div>;
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="mb-6">
        <Link
          href="/interview/cs"
          className="inline-flex items-center text-sm text-gray-600 hover:text-blue-600 mb-6"
        >
          ← 목록으로
        </Link>
      </div>

      {/* 질문 박스 */}
      <div className="border border-gray-200 bg-white rounded-lg shadow-sm mb-6 p-6">
        <div className="flex items-center gap-2 mb-3">
          <span className="px-2 py-0.5 rounded-full border text-xs text-gray-700">
            {question.categoryType}
          </span>
        </div>
        <h1 className="text-2xl font-bold mb-3">{question.title}</h1>
        <p className="whitespace-pre-wrap text-gray-700 text-sm leading-relaxed">
          {question.content}
        </p>
      </div>

      {/* 답변 상태별 렌더링 */}
      {!submitted ? (
        <div className="border border-gray-200 bg-white rounded-lg shadow-sm p-6">
          <h2 className="text-xl font-semibold mb-1">답변 작성</h2>
          <p className="text-gray-500 text-sm mb-4">
            문제에 대한 답변을 작성해주세요.
          </p>

          <textarea
            value={answer}
            onChange={(e) => setAnswer(e.target.value)}
            rows={12}
            placeholder="답변을 입력하세요"
            className="w-full border border-gray-300 rounded-md p-3 text-sm font-mono focus:ring-2 focus:ring-blue-500 focus:outline-none"
          />

          <div className="flex items-center mt-2">
            <input
              type="checkbox"
              id="privateCheck"
              checked={!isPublic}
              onChange={(e) => setIsPublic(!e.target.checked ? true : false)}
              className="mr-2"
            />
            <label htmlFor="privateCheck" className="text-sm text-gray-700">
              비공개로 작성
            </label>
          </div>

          <div className="flex justify-end gap-3 mt-4">
            <button
              onClick={handleSubmit}
              disabled={loading}
              className={`px-4 py-2 rounded-md text-sm text-white ${
                loading
                  ? "bg-gray-400 cursor-not-allowed"
                  : "bg-blue-600 hover:bg-blue-700 cursor-pointer"
              }`}
            >
              {loading ? "제출 중..." : "답변 제출"}
            </button>
          </div>
        </div>
      ) : (
        <div className="border border-green-200 bg-green-50 rounded-lg shadow-sm p-6">
          {isJustSubmitted && (
            <h2 className="text-xl font-semibold text-green-700 mb-2">
              ✅ 답변이 제출되었습니다!
            </h2>
          )}

          <div className="mb-4">
            <h3 className="font-semibold mb-2">내 답변:</h3>
            <div className="p-4 bg-white border border-gray-300 rounded-md whitespace-pre-wrap text-sm text-gray-800">
              {submittedAnswer?.content || answer}
            </div>

            <div className="mt-3 flex items-center justify-between">
              <span
                className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium ${
                  isPublic
                    ? "bg-green-100 text-green-800 border border-green-200"
                    : "bg-red-100 text-red-800 border border-red-200"
                }`}
              >
                {isPublic ? "🔓 공개 상태" : "🔒 비공개 상태"}
              </span>

              <button
                onClick={handleToggleVisibility}
                className="px-3 py-1 text-xs font-medium rounded-md bg-blue-600 text-white hover:bg-blue-700 transition-colors"
              >
                {isPublic ? "비공개로 전환" : "공개로 전환"}
              </button>
            </div>
          </div>

          <div className="mb-4 mt-10">
            <h3 className="font-semibold mb-2">AI 피드백:</h3>

            <div className="p-4 bg-sky-100 border border-gray-300 rounded-md text-sm text-gray-800 min-h-[100px]">
              {feedback ? (
                // ✅ 실제 피드백은 왼쪽 정렬
                <div className="text-left">
                  <p className="mb-3">{feedback.content}</p>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-gray-700">
                        AI 점수
                      </span>
                      <span className="inline-flex items-center justify-center px-3 py-1 rounded-full bg-blue-500 text-white font-bold">
                        {feedback.score} / 100
                      </span>
                    </div>
                  </div>
                </div>
              ) : feedbackMessage ? (
                // ✅ 로딩 중 상태는 중앙 정렬
                <div className="flex flex-col items-center justify-center text-center h-full gap-3 py-6">
                  <div className="animate-spin text-blue-500 text-3xl">⏳</div>
                  <p className="text-gray-700 font-medium animate-pulse">
                    {feedbackMessage}
                  </p>
                  <p className="text-xs text-gray-500">
                    AI가 당신의 답변을 분석 중이에요... 잠시만 기다려주세요 🤖
                  </p>
                </div>
              ) : (
                <p className="text-gray-500 italic text-left">
                  아직 피드백이 존재하지 않습니다.
                </p>
              )}
            </div>
          </div>

          <div className="flex gap-3">
            <button
              type="button"
              onClick={() => router.push(`/interview/cs/${questionId}/answers`)}
              className="px-4 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 cursor-pointer"
            >
              다른 사람들의 답변 보기
            </button>

            <button
              type="button"
              onClick={handleRetry}
              className="px-4 py-2 bg-gray-200 text-gray-800 text-sm rounded-md hover:bg-gray-300 cursor-pointer"
            >
              다시 풀기
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
