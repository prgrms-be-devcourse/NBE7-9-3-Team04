"use client";

import { useEffect, useState } from "react";
import { useRouter, useParams } from "next/navigation";
import Link from "next/link";
import { fetchApi } from "@/lib/client";
import { CreateQuestionRequest, QUESTION_CATEGORY_LIST, QuestionResponse } from "@/types/question";
import { toast } from "sonner";

export default function CsQuestionEditPage() {
  const router = useRouter();
  const params = useParams();
  const questionId = Number(params.id);

  const [formData, setFormData] = useState<CreateQuestionRequest>({
    title: "",
    content: "",
    categoryType: "NETWORK",
  });

  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [userId, setUserId] = useState<number | null>(null);


  useEffect(() => {
    const fetchUser = async () => {
      const res = await fetchApi("/api/v1/users/check", { method: "GET" });
      if (res.status === "OK") setUserId(res.data.id);
      else router.replace("/auth");
    };
    fetchUser();
  }, [router]);


  useEffect(() => {
    if (!userId || !questionId) return;

    const fetchQuestion = async () => {
      try {
        const res = await fetchApi(`/api/v1/users/${userId}/questions/${questionId}`, {
          method: "GET",
        });

        if (res.status === "OK") {
          const data: QuestionResponse = res.data;
          setFormData({
            title: data.title,
            content: data.content,
            categoryType: data.categoryType,
          });
        } else {
          toast.error(res.message || "질문 불러오기 실패");
          router.replace("/interview/cs");
        }
      } catch (err) {
        console.error("질문 불러오기 실패:", err);
        toast.error("승인되지 않은 질문이거나 접근 권한이 없습니다.");
        router.replace("/interview/cs");
      } finally {
        setIsLoading(false);
      }
    };

    fetchQuestion();
  }, [userId, questionId, router]);

  //수정 요청
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      const res = await fetchApi(`/api/v1/questions/${questionId}`, {
        method: "PUT",
        body: JSON.stringify(formData),
      });

      if (res.status === "OK") {
        toast.success("질문이 수정되었습니다!");
        router.replace("/mypage/activity");
      } else {
        toast.error(res.message || "질문 수정 실패");
      }
    } catch (err) {
      console.error(err);
      toast.error("수정 중 오류가 발생했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading)
    return (
      <div className="fixed inset-0 flex items-center justify-center bg-gray-50">
        <div className="animate-pulse text-gray-400 text-sm">질문 불러오는 중...</div>
      </div>
    );

  return (
    <div className="max-w-4xl mx-auto px-6 py-10">

      <Link
        href="/interview/cs"
        className="inline-flex items-center text-sm text-gray-600 hover:text-blue-600 mb-6"
      >
        ← 목록으로
      </Link>

      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">질문 수정</h1>
        <p className="text-gray-500">등록한 질문 내용을 수정할 수 있습니다.</p>
      </div>

      <form
        onSubmit={handleSubmit}
        className="space-y-6 bg-white border border-gray-200 rounded-lg shadow-sm p-6"
      >
        {/* 제목 */}
        <div>
          <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-1">
            질문 제목
          </label>
          <input
            id="title"
            type="text"
            value={formData.title}
            onChange={(e) => setFormData({ ...formData, title: e.target.value })}
            required
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* 카테고리 */}
        <div>
          <label htmlFor="category" className="block text-sm font-medium text-gray-700 mb-1">
            카테고리
          </label>
          <select
            id="category"
            value={formData.categoryType}
            onChange={(e) =>
              setFormData({
                ...formData,
                categoryType: e.target.value as CreateQuestionRequest["categoryType"],
              })
            }
            required
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
          >
            {QUESTION_CATEGORY_LIST.map((c) => (
              <option key={c.value} value={c.value}>
                {c.label}
              </option>
            ))}
          </select>
        </div>

        {/* 내용 */}
        <div>
          <label htmlFor="content" className="block text-sm font-medium text-gray-700 mb-1">
            질문 내용
          </label>
          <textarea
            id="content"
            value={formData.content}
            onChange={(e) => setFormData({ ...formData, content: e.target.value })}
            rows={10}
            required
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div className="flex justify-end gap-3">
          <Link
            href="/mypage/activity"
            className="px-4 py-2 border border-gray-300 rounded-md text-sm text-gray-700 hover:bg-gray-100"
          >
            취소
          </Link>
          <button
            type="submit"
            disabled={isSubmitting}
            className={`px-4 py-2 rounded-md text-sm text-white ${
              isSubmitting ? "bg-gray-400" : "bg-blue-600 hover:bg-blue-700"
            }`}
          >
            {isSubmitting ? "수정 중..." : "수정하기"}
          </button>
        </div>
      </form>
    </div>
  );
}
