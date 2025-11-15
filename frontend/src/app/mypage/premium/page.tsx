"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { fetchApi } from "@/lib/client";
import { Subscription } from "@/types/subscription";
import { toast } from "sonner";
import { confirmAlert } from "@/lib/confirm";

export default function MyPremiumPage() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);
  const [subscription, setSubscription] = useState<Subscription | null>(null);

  useEffect(() => {
    async function loadSubscription() {
      try {
        const apiResponse = await fetchApi(`/api/v1/subscriptions/me`, {
          method: "GET",
        });
        setSubscription(apiResponse.data);
      } catch (err) {
        console.error("구독 정보를 불러오는 중 오류 발생:", err);
      } finally {
        setIsLoading(false);
      }
    }
    loadSubscription();
  }, []);

  const handleCancel = async () => {
    if (!subscription?.customerKey) {
      toast.error("구독 정보를 찾을 수 없습니다.");
      return;
    }


    const confirmCancel = await confirmAlert("구독을 정말 취소하시겠습니까?");
    if (!confirmCancel) return;

    try {
      const apiResponse = await fetchApi(
        `/api/v1/subscriptions/cancel/${subscription.customerKey}`,
        {
          method: "DELETE",
        }
      );
      toast.success(apiResponse.message);
      window.location.reload();
    } catch (err) {
      toast.error("구독 취소에 실패했습니다.");
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh] text-gray-500">
        ⏳ 로딩 중...
      </div>
    );
  }

  const isPremium = subscription?.isActive === true;

  //취소 예약 상태
  const isCanceledScheduled =
    isPremium && !subscription?.billingKey && subscription?.nextBillingDate;

  return (
    <div className="max-w-screen-lg mx-auto px-6 py-10">
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">💳 유료 서비스 관리</h1>
        <p className="text-gray-500 mb-6">프리미엄 멤버십을 관리하세요.</p>
      </div>

      <div className="space-y-6">
        {isPremium ? (
          <>
            {/* 프리미엄 회원 */}
            <div className="p-6 bg-gradient-to-br from-blue-50 to-blue-100 border border-blue-200 rounded-lg space-y-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-blue-600 text-white rounded-lg text-lg">
                    👑
                  </div>
                  <div>
                    <h3 className="text-lg font-bold">프리미엄 멤버십</h3>
                    <span className="inline-block px-2 py-0.5 text-xs font-semibold text-white bg-blue-600 rounded-full mt-1">
                      활성화
                    </span>
                  </div>
                </div>
                <div className="text-right">
                  <p className="text-2xl font-bold text-blue-600">
                    {subscription.price.toLocaleString()}원
                  </p>
                  <p className="text-sm text-gray-500">/ 월</p>
                </div>
              </div>

              <div className="pt-4 border-t border-blue-200 text-sm space-y-2">
                <p>
                  📅 <span className="text-gray-600">구독 시작일:</span>{" "}
                  <span className="font-semibold">
                    {subscription.startDate?.split("T")[0] || "-"}
                  </span>
                </p>
                <p>
                  📅{" "}
                  <span className="text-gray-600">
                    {isCanceledScheduled ? "만료일:" : "다음 결제일:"}
                  </span>{" "}
                  <span className="font-semibold">
                    {subscription.nextBillingDate || "-"}
                  </span>
                </p>
                {isCanceledScheduled && (
                  <p className="text-orange-600 font-medium mt-2">
                    ⚠️ 현재 구독이 취소된 상태이며,{" "}
                    {subscription.nextBillingDate} 까지 서비스 이용이
                    가능합니다.
                    <br />
                    ⚠️ 다시 구독을 원하실 경우, 만료일 이후 재결제를 진행해
                    주세요.
                  </p>
                )}
              </div>
            </div>

            <div>
              <h4 className="font-semibold flex items-center gap-2 mb-2">
                ✅ 이용 중인 혜택
              </h4>
              <ul className="space-y-1 pl-4 text-sm text-gray-700">
                <li>• 모집 게시글 상단 고정</li>
                <li>• 포트폴리오 첨삭 서비스</li>
                <li>• 면접 질문 횟수 증가</li>
              </ul>
            </div>

            {!isCanceledScheduled && (
              <div className="pt-4 border-t space-y-3">
                <button
                  onClick={handleCancel}
                  className="w-full border border-red-300 text-red-600 py-2 rounded-md hover:bg-red-50 transition"
                >
                  구독 취소
                </button>
              </div>
            )}
          </>
        ) : (
          <>
            {/* BASIC 사용자 */}
            <div className="p-8 border-2 border-blue-500 rounded-lg bg-blue-50 space-y-6">
              <div>
                <h3 className="text-2xl font-bold mb-2 flex items-center gap-2 text-blue-700">
                  👑 프리미엄 멤버십
                </h3>
                <p className="text-4xl font-bold text-blue-600">
                  9,900원{" "}
                  <span className="text-lg font-normal text-gray-500">
                    / 월
                  </span>
                </p>
              </div>

              <div>
                <h4 className="font-semibold mb-2 text-gray-800">혜택</h4>
                <ul className="space-y-1 text-gray-700 text-sm">
                  <li>✅ 모집 게시글 상단 고정</li>
                  <li>✅ 포트폴리오 첨삭 서비스</li>
                  <li>✅ 면접 질문 횟수 증가</li>
                </ul>
              </div>
            </div>

            <div className="p-4 bg-gray-100 border border-gray-300 rounded-lg flex items-start gap-3">
              <span className="text-gray-600 text-lg mt-0.5">⚠️</span>
              <div className="space-y-1">
                <p className="text-sm font-medium">
                  결제 전 카드 등록이 필요합니다
                </p>
                <p className="text-xs text-gray-500">
                  안전한 결제를 위해 먼저 카드를 등록해주세요.
                </p>
              </div>
            </div>

            <button
              onClick={() => router.push("/mypage/premium/checkout")}
              className="w-full bg-blue-600 text-white text-lg font-semibold py-3 rounded-md hover:bg-blue-700 transition"
            >
              카드 등록하고 구독하기
            </button>
          </>
        )}
      </div>
    </div>
  );
}
