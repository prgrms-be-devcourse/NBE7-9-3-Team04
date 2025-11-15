"use client";

import { fetchApi } from "@/lib/client";
//결제 성공
import { loadTossPayments, ANONYMOUS } from "@tosspayments/tosspayments-sdk";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { toast } from "sonner";

const clientKey = process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY!;

//결제창 띄우기
export default function PaymentCheckoutPage() {
  const [payment, setPayment] = useState<any>(null);
  const [customerKey, setCustomerKey] = useState<string | null>(null);
  const router = useRouter();

  // 서버에서 customerKey 불러오기
  useEffect(() => {
    async function initCustomerKey() {
      const res = await fetchApi("/api/v1/subscriptions/me", { method: "GET" });
      setCustomerKey(res.data.customerKey);
    }
    initCustomerKey();
  }, []);


  useEffect(() => {
    async function initTossPayment() {
      if (!customerKey) return; // customerKey를 불러오기 전에는 실행 X
      try {
        const tossPayments = await loadTossPayments(clientKey);
        const payment = tossPayments.payment({ customerKey });
        setPayment(payment);
      } catch (error) {
        console.error("Error initializing TossPayments:", error);
      }
    }

    initTossPayment();
  }, [customerKey]);

  //카드 등록하기 버튼 누르면 결제창 띄우기

  async function requestBillingAuth() {
    try {
      await payment.requestBillingAuth({
        method: "CARD",
        successUrl: window.location.origin + "/billing/success",
        failUrl: window.location.origin + "/billing/fail",
      });
    } catch (err: any) {
      if (err?.code === "USER_CANCEL" || err?.message?.includes("취소")) {
        return;
      }

      toast.error("카드 등록창이 정상적으로 열리지 않았습니다. 다시 시도해주세요.");
    }
  }

  return (
    <>
    <div className="flex flex-col items-center justify-center min-h-[70vh] gap-6">
      <h1 className="text-2xl font-bold text-gray-800">💳 카드 등록하기</h1>
      <p className="text-gray-500 text-center">
        프리미엄 멤버십 결제를 위해 결제 수단 (카드) 을 등록하세요.
      </p>

      <button
        onClick={requestBillingAuth}
        className="bg-blue-600 text-white font-semibold py-3 px-8 rounded-md hover:bg-blue-700 transition"
      >
        카드 등록하기
      </button>

      <button
        onClick={() => router.back()}
        className="text-gray-500 text-sm hover:underline mt-4"
      >
        ← 돌아가기
      </button>
    </div>
    </>
  );
}
