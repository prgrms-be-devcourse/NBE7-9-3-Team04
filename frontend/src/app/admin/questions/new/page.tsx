"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { fetchApi } from "@/lib/client";
import {
  QuestionCategoryType,
  QUESTION_CATEGORY_LIST,
} from "@/types/question";
import { toast } from "sonner";

export default function AdminQuestionAddPage() {
  const router = useRouter();

  // ✅ 폼 데이터 상태
  const [formData, setFormData] = useState({
    title: "",
    category: "" as QuestionCategoryType | "",
    content: "",
    score: 0,
    isApproved: false,
  });

  const [isCheckingAuth, setIsCheckingAuth] = useState(true);
  const [isLoading, setIsLoading] = useState(false);

  // ✅ 관리자 권한 확인
  useEffect(() => {
    const checkAdmin = async () => {
      try {
        const res = await fetchApi("/api/v1/users/check", { method: "GET" });

        if (res.status !== "OK" || res.data.role !== "ADMIN") {
          toast.error("관리자만 접근할 수 있습니다.");
          router.replace("/auth?returnUrl=/admin/questions/new");
          return;
        }

        setIsCheckingAuth(false);
      } catch {
        router.replace("/auth?returnUrl=/admin/questions/new");
      }
    };

    checkAdmin();
  }, [router]);

  // ✅ 로딩 화면
  if (isCheckingAuth) {
    return (
      <div className="fixed inset-0 flex items-center justify-center bg-gray-50">
        <div className="animate-pulse text-gray-400 text-sm">
          관리자 권한 확인 중...
        </div>
      </div>
    );
  }

  // ✅ 폼 제출
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.title || !formData.content || !formData.category) {
      toast.error("모든 필드를 입력해주세요.");
      return;
    }

    const payload = {
      title: formData.title,
      content: formData.content,
      categoryType: formData.category as QuestionCategoryType,
      isApproved: formData.isApproved,
      score: Number(formData.score),
    };

    try {
      setIsLoading(true);
      const apiResponse = await fetchApi("/api/v1/admin/questions", {
        method: "POST",
        body: JSON.stringify(payload),
      });

      if (apiResponse.status === "OK" || apiResponse.status === "CREATED") {
        toast.success("질문이 성공적으로 등록되었습니다!");
        router.push("/admin/questions");
      } else {
        toast.error(apiResponse.message || "등록에 실패했습니다.");
      }
    } catch {
      toast.error("서버 오류가 발생했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-4 py-10 space-y-8">
      {/* 목록으로 돌아가기 */}
      <button
        onClick={() => router.push("/admin/questions")}
        className="text-sm text-gray-500 flex items-center gap-1 hover:text-blue-600"
      >
        ← 목록으로
      </button>

      <div className="bg-white rounded-lg shadow p-8">
        <h1 className="text-3xl font-bold mb-2">질문 등록</h1>
        <p className="text-gray-500 mb-6">새로운 면접 질문을 등록합니다.</p>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* 제목 */}
          <div>
            <label htmlFor="title" className="block text-sm font-semibold mb-2">
              제목
            </label>
            <input
              id="title"
              type="text"
              placeholder="질문 제목을 입력하세요"
              value={formData.title}
              onChange={(e) =>
                setFormData({ ...formData, title: e.target.value })
              }
              className="w-full border border-gray-200 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-200"
              required
            />
          </div>

          {/* 카테고리 */}
          <div>
            <label
              htmlFor="category"
              className="block text-sm font-semibold mb-2"
            >
              카테고리
            </label>
            <select
              id="category"
              value={formData.category}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  category: e.target.value as QuestionCategoryType,
                })
              }
              className="w-full border border-gray-200 rounded-lg px-4 py-2 bg-white focus:ring-2 focus:ring-blue-200"
              required
            >
              <option value="">카테고리 선택</option>
              {QUESTION_CATEGORY_LIST.map((c) => (
                <option key={c.value} value={c.value}>
                  {c.label}
                </option>
              ))}
            </select>
          </div>

          {/* 점수 */}
          <div>
            <label htmlFor="score" className="block text-sm font-semibold mb-2">
              점수
            </label>
            <input
              id="score"
              type="number"
              min="0"
              max="50"
              placeholder="0~50 사이의 점수를 입력하세요"
              value={formData.score}
              onChange={(e) => {
                const value = Number(e.target.value)
                if (value >= 0 && value <= 50) {
                  setFormData({ ...formData, score: value })
                }
              }}
              className="w-full border border-gray-200 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-200"
              required
            />
            <p className="text-xs text-gray-500 mt-1">0~50점 사이의 값만 입력 가능합니다.</p>
          </div>

          {/* 승인 여부 */}
          <div className="flex items-center gap-2">
            <input
              id="isApproved"
              type="checkbox"
              checked={formData.isApproved}
              onChange={(e) =>
                setFormData({ ...formData, isApproved: e.target.checked })
              }
              className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
            />
            <label htmlFor="isApproved" className="text-sm text-gray-700">
              등록 즉시 승인 상태로 설정
            </label>
          </div>

          {/* 내용 */}
          <div>
            <label
              htmlFor="content"
              className="block text-sm font-semibold mb-2"
            >
              내용
            </label>
            <textarea
              id="content"
              placeholder="질문 내용을 자세히 작성해주세요"
              value={formData.content}
              onChange={(e) =>
                setFormData({ ...formData, content: e.target.value })
              }
              rows={10}
              className="w-full border border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-blue-200"
              required
            />
          </div>

          {/* 버튼 */}
          <div className="flex justify-end gap-3">
            <button
              type="button"
              onClick={() => router.push("/admin/questions")}
              className="px-4 py-2 border border-gray-200 rounded-md hover:bg-gray-50"
            >
              취소
            </button>
            <button
              type="submit"
              disabled={isLoading}
              className={`px-4 py-2 rounded-md text-white transition-colors ${isLoading
                  ? "bg-gray-400 cursor-not-allowed"
                  : "bg-blue-600 hover:bg-blue-700"
                }`}
            >
              {isLoading ? "등록 중..." : "질문 등록"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
