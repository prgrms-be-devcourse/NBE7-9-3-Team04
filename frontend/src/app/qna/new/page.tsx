"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { fetchApi } from "@/lib/client";
import { CreateQna, QnaCategoryType } from "@/types/qna";
import { toast } from "sonner";

export default function NewQnAPage() {
  const router = useRouter();

  /** ✅ formData를 CreateQna 타입으로 관리 */
  const [formData, setFormData] = useState<CreateQna>({
    title: "",
    content: "",
    categoryType: "" as QnaCategoryType, // 초기값은 빈 문자열로 캐스팅
  });

  const [isCheckingLogin, setIsCheckingLogin] = useState(true);
  const [isLoading, setIsLoading] = useState(false);

  // ✅ UI용 카테고리 매핑 (label ↔ value)
  const CATEGORY_LABELS: Record<QnaCategoryType, string> = {
    ACCOUNT: "계정",
    PAYMENT: "결제",
    SYSTEM: "시스템",
    RECRUITMENT: "모집",
    SUGGESTION: "제안",
    OTHER: "기타",
  };

  // ✅ 로그인 여부 확인
  useEffect(() => {
    const checkLogin = async () => {
      try {
        const res = await fetchApi("/api/v1/users/check", { method: "GET" });
        if (res.status !== "OK") {
          router.replace("/auth?returnUrl=/qna/new");
          return;
        }
        setIsCheckingLogin(false);
      } catch {
        router.replace("/auth?returnUrl=/qna/new");
      }
    };
    checkLogin();
  }, [router]);

  // ✅ 로그인 확인 중일 때
  if (isCheckingLogin) {
    return (
      <div className="fixed inset-0 flex items-center justify-center bg-gray-50">
        <div className="animate-pulse text-gray-400 text-sm">
          로그인 상태 확인 중...
        </div>
      </div>
    );
  }

  /** ✅ QnA 등록 */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.title || !formData.content || !formData.categoryType) {
      toast.error("모든 필드를 입력해주세요.");
      return;
    }

    try {
      setIsLoading(true);

      const res = await fetchApi("/api/v1/qna", {
        method: "POST",
        body: JSON.stringify(formData), // ✅ CreateQna 타입 그대로 전송
      });

      if (res.status === "CREATED" || res.status === "OK") {
        toast.success("질문이 등록되었습니다!");
        router.push("/qna");
      } else {
        toast.error(res.message || "등록에 실패했습니다.");
      }
    } catch (err) {
      toast.error("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-4 py-10 space-y-8">
      {/* 🔙 목록으로 이동 */}
      <button
        onClick={() => router.push("/qna")}
        className="text-sm text-gray-500 flex items-center gap-1 hover:text-blue-600"
      >
        ← 목록으로
      </button>

      {/* ✅ 질문 등록 폼 */}
      <div className="bg-white rounded-lg shadow p-8">
        <h1 className="text-3xl font-bold mb-2">질문하기</h1>
        <p className="text-gray-500 mb-6">궁금한 점을 자유롭게 질문해주세요.</p>

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
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              className="w-full border border-gray-200 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-200"
              required
            />
          </div>

          {/* 카테고리 */}
          <div>
            <label htmlFor="category" className="block text-sm font-semibold mb-2">
              카테고리
            </label>
            <select
              id="category"
              value={formData.categoryType}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  categoryType: e.target.value as QnaCategoryType,
                })
              }
              className="w-full border border-gray-200 rounded-lg px-4 py-2 bg-white focus:ring-2 focus:ring-blue-200"
              required
            >
              <option value="">카테고리 선택</option>
              {Object.entries(CATEGORY_LABELS).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
          </div>

          {/* 내용 */}
          <div>
            <label htmlFor="content" className="block text-sm font-semibold mb-2">
              내용
            </label>
            <textarea
              id="content"
              placeholder="질문 내용을 자세히 작성해주세요"
              value={formData.content}
              onChange={(e) => setFormData({ ...formData, content: e.target.value })}
              rows={10}
              className="w-full border border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-blue-200"
              required
            />
          </div>

          {/* 버튼 */}
          <div className="flex justify-end gap-3">
            <button
              type="button"
              onClick={() => router.push("/qna")}
              className="px-4 py-2 border border-gray-200 rounded-md hover:bg-gray-50"
            >
              취소
            </button>
            <button
              type="submit"
              disabled={isLoading}
              className={`px-4 py-2 rounded-md text-white transition-colors ${
                isLoading
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
