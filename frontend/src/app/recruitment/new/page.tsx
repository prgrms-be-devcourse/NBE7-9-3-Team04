"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { PostAddRequest as CreatePost } from "@/types/post";
import { fetchApi } from "@/lib/client";
import { toast } from "sonner";

export default function RecruitmentCreatePage() {
  const router = useRouter();

  const [formData, setFormData] = useState<CreatePost>({
    title: "",
    content: "",
    introduction: "",
    deadline: "",
    status: "ING",
    pinStatus: "NOT_PINNED",
    recruitCount: 1,
    categoryType: "PROJECT", // 기본값 설정
  });

  const [isPremium, setIsPremium] = useState<boolean>(false);

  // ✅ 프리미엄 구독 상태 확인
  useEffect(() => {
    const fetchSubscriptionStatus = async () => {
      try {
        console.log("🔍 API 호출 시작: /api/v1/subscriptions/me");
        const res = await fetchApi("/api/v1/subscriptions/me", {
          method: "GET",
          cache: "no-store",
        });


        if (res.status === "OK" && res.data) {
          const { subscriptionType, isActive } = res.data;
          const premiumStatus =
            subscriptionType === "PREMIUM" && isActive === true;
          setIsPremium(premiumStatus);
        } else {
          setIsPremium(false);
          console.log("⚠️ 기본 상태로 설정: BASIC (비구독자)");
        }
      } catch (error) {
        console.error("❌ 구독 상태 확인 실패:", error);
        setIsPremium(false);
      }
    };

    fetchSubscriptionStatus();
  }, []);

  useEffect(() => {
    console.log("🔍 현재 isPremium 상태:", isPremium);
  }, [isPremium]);
  useEffect(() => {
    if (!isPremium) {
      setFormData((prev) => ({ ...prev, pinStatus: "NOT_PINNED" }));
    }
  }, [isPremium]);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    const deadlineDate = new Date(formData.deadline);
    const currentDate = new Date();

    if (deadlineDate < currentDate) {
      toast.error("마감일은 현재 시간 이후로 설정해야 합니다.");
      return;
    }

    const formattedFormData = {
      ...formData,
      deadline: `${formData.deadline}T00:00:00`,
      categoryType: formData.categoryType === "PROJECT" ? "PROJECT" : "STUDY",
    };

    try {
      const apiResponse = await fetchApi(`/api/v1/posts`, {
        method: "POST",
        body: JSON.stringify(formattedFormData),
      });

      if (apiResponse.status === "OK") {
        toast.success(apiResponse.message || "게시글이 성공적으로 작성되었습니다.");
        router.replace("/recruitment");
      } else {
        toast.error(apiResponse.message || "게시글 작성 중 오류가 발생했습니다.");
      }
    } catch (err: any) {
      toast.error("API 통신 실패: " + err.message);
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-6 py-10">
      <Link
        href="/recruitment"
        className="inline-flex items-center text-sm text-gray-600 hover:text-blue-600 mb-6"
      >
        ← 목록으로
      </Link>

      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">모집글 작성</h1>
        <p className="text-gray-500">
          팀 프로젝트 또는 스터디 모집글을 작성해주세요
        </p>
      </div>

      <form
        onSubmit={handleSubmit}
        className="space-y-6 bg-white border border-gray-200 rounded-lg shadow-sm p-6"
      >
        {/* ✅ 상단 고정 여부 */}
        <div className="mb-6">
          <label
            htmlFor="pinStatus"
            className="block text-sm font-medium text-gray-700 mb-1"
          >
            상단 고정 여부
          </label>
          <select
            id="pinStatus"
            value={formData.pinStatus}
            onChange={(e) =>
              setFormData({
                ...formData,
                pinStatus: e.target.value as "PINNED" | "NOT_PINNED",
              })
            }
            disabled={!isPremium} // 🔒 비구독자는 선택 불가
            required
            className={`w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 ${
              isPremium
                ? "focus:ring-blue-500"
                : "bg-gray-100 text-gray-500 cursor-not-allowed"
            }`}
          >
            <option value="NOT_PINNED">고정 안함</option>
            <option value="PINNED">고정</option>
          </select>
          {!isPremium && (
            <p className="text-xs text-gray-400 mt-1">
              ※ 상단 고정은 PREMIUM 회원만 가능합니다.
            </p>
          )}
        </div>

        {/* 제목 */}
        <div>
          <label
            htmlFor="title"
            className="block text-sm font-medium text-gray-700 mb-1"
          >
            제목
          </label>
          <input
            id="title"
            type="text"
            placeholder="모집글 제목을 입력하세요"
            value={formData.title}
            onChange={(e) =>
              setFormData({ ...formData, title: e.target.value })
            }
            required
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* 한 줄 소개 */}
        <div>
          <label
            htmlFor="description"
            className="block text-sm font-medium text-gray-700 mb-1"
          >
            한 줄 소개
          </label>
          <input
            id="description"
            type="text"
            placeholder="한 줄로 프로젝트를 소개해주세요"
            value={formData.introduction}
            onChange={(e) =>
              setFormData({ ...formData, introduction: e.target.value })
            }
            required
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* 카테고리 + 마감일 */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label
              htmlFor="category"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              카테고리
            </label>
            <select
              id="category"
              value={formData.categoryType}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  categoryType: e.target.value as "PROJECT" | "STUDY",
                })
              }
              required
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="PROJECT">프로젝트</option>
              <option value="STUDY">스터디</option>
            </select>
          </div>

          <div>
            <label
              htmlFor="deadline"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              모집 마감일
            </label>
            <input
              id="deadline"
              type="date"
              value={formData.deadline}
              onChange={(e) =>
                setFormData({ ...formData, deadline: e.target.value })
              }
              required
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        {/* 모집 인원 */}
        <div>
          <label
            htmlFor="maxMembers"
            className="block text-sm font-medium text-gray-700 mb-1"
          >
            모집 인원
          </label>
          <input
            id="maxMembers"
            type="number"
            placeholder="최대 모집 인원"
            value={formData.recruitCount}
            onChange={(e) =>
              setFormData({
                ...formData,
                recruitCount: Number(e.target.value),
              })
            }
            required
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* 상세 내용 */}
        <div>
          <label
            htmlFor="content"
            className="block text-sm font-medium text-gray-700 mb-1"
          >
            상세 내용
          </label>
          <textarea
            id="content"
            placeholder="프로젝트 소개, 필요 기술, 진행 방식 등을 자세히 작성해주세요"
            value={formData.content}
            onChange={(e) =>
              setFormData({ ...formData, content: e.target.value })
            }
            rows={15}
            required
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* 버튼 */}
        <div className="flex justify-end gap-3">
          <Link
            href="/recruitment"
            className="inline-flex items-center justify-center px-4 py-2 border border-gray-300 rounded-md text-sm text-gray-700 hover:bg-gray-100"
          >
            취소
          </Link>
          <button
            type="submit"
            className="inline-flex items-center justify-center px-4 py-2 rounded-md bg-blue-600 text-white text-sm hover:bg-blue-700"
          >
            작성 완료
          </button>
        </div>
      </form>
    </div>
  );
}
