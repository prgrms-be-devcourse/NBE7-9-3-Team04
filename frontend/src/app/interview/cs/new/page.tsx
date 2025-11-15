"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { fetchApi } from "@/lib/client";
import { CreateQuestionRequest, QUESTION_CATEGORY_LIST } from "@/types/question";
import { toast } from "sonner";

export default function CsQuestionCreatePage() {
  const router = useRouter();

  const [formData, setFormData] = useState<CreateQuestionRequest>({
    title: "",
    content: "",
    categoryType: "NETWORK",
  });

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isCheckingLogin, setIsCheckingLogin] = useState(true);

  /**로그인 여부 확인 */
  useEffect(() => {
    const checkLogin = async () => {
      try {
        const res = await fetchApi("/api/v1/users/check", { method: "GET" });

        if (res.status !== "OK") {
          // 로그인 안되어 있으면 로그인 페이지로 이동 (로그인 후 돌아오게 returnUrl 포함)
          router.replace("/auth?returnUrl=/interview/cs/new");
          return;
        }

        setIsCheckingLogin(false);
      } catch {
        router.replace("/auth?returnUrl=/interview/cs/new");
      }
    };

    checkLogin();
  }, [router]);

  /**로그인 확인 중일 때 로딩 UI */
  if (isCheckingLogin) {
    return (
      <div className="fixed inset-0 flex items-center justify-center bg-gray-50">
        <div className="animate-pulse text-gray-400 text-sm">
          로그인 상태 확인 중...
        </div>
      </div>
    );
  }

  /**질문 등록 요청 */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      const res = await fetchApi("/api/v1/questions", {
        method: "POST",
        body: JSON.stringify(formData),
      });

      if (res.status === "CREATED" || res.status === "OK") {
        toast.success("질문이 등록되었습니다!");
        router.replace("/interview/cs");
      } else {
        toast.error(res.message || "질문 등록에 실패했습니다.");
      }
    } catch (error: any) {
      toast.error(error.message || "질문 등록 중 오류가 발생했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-6 py-10">
      {/* 🔙 목록으로 이동 */}
      <Link
        href="/interview/cs"
        className="inline-flex items-center text-sm text-gray-600 hover:text-blue-600 mb-6"
      >
        ← 목록으로
      </Link>

      {/* 안내 문구 */}
      <div className="border border-yellow-200 bg-yellow-50 p-4 rounded-md mb-6">
        <p className="font-semibold mb-1">질문 등록 안내</p>
        <p className="text-sm">
          등록한 질문은 관리자의 검토 후 승인됩니다.
        </p>
      </div>

      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">CS 질문 등록</h1>
        <p className="text-gray-500">새로운 CS 면접 질문을 등록해주세요</p>
      </div>

      {/*등록 폼 */}
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
            placeholder="예: TCP와 UDP의 차이점을 설명하세요"
            value={formData.title}
            onChange={(e) => setFormData({ ...formData, title: e.target.value })}
            required
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
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
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
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
            질문 설명
          </label>
          <textarea
            id="content"
            placeholder="질문에 대한 상세한 설명을 작성해주세요."
            value={formData.content}
            onChange={(e) => setFormData({ ...formData, content: e.target.value })}
            rows={10}
            required
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* 버튼 */}
        <div className="flex justify-end gap-3">
          <Link
            href="/interview/cs"
            className="inline-flex items-center justify-center px-4 py-2 border border-gray-300 rounded-md text-sm text-gray-700 hover:bg-gray-100"
          >
            취소
          </Link>
          <button
            type="submit"
            disabled={isSubmitting}
            className={`inline-flex items-center justify-center px-4 py-2 rounded-md text-sm text-white ${
              isSubmitting ? "bg-gray-400" : "bg-blue-600 hover:bg-blue-700"
            }`}
          >
            {isSubmitting ? "등록 중..." : "등록하기"}
          </button>
        </div>
      </form>
    </div>
  );
}
