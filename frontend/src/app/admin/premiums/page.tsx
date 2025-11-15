"use client";

import { useEffect, useMemo, useState } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

import { AdminPayment, AdminPaymentSummary } from "@/types/payment";
import { fetchApi } from "@/lib/client";
import { toast } from "sonner";

export default function AdminPaymentsPage() {
  const [payments, setPayments] = useState<AdminPayment[]>([]);
  const [summary, setSummary] = useState<AdminPaymentSummary | null>(null);
  const [loading, setLoading] = useState(true);


  useEffect(() => {
    const fetchData = async () => {
      try {
        const [paymentsRes, summaryRes] = await Promise.all([ //두개 작업 한번에 실행하고 반환환
          fetchApi(`/api/v1/admin/payments`),
          fetchApi(`/api/v1/admin/payments/summary`),
        ])

        // ApiResponse<T> 구조이므로 .data 접근
        setPayments(paymentsRes.data)
        setSummary(summaryRes.data)
      } catch (error: any) {
        console.error("결제 정보 조회 실패:", error.message)
        toast.error(error.message || "결제 데이터를 불러오지 못했습니다.")
      } finally {
        setLoading(false)
      }
    }

    fetchData()
  }, [])

  const getStatusBadge = (status: string) => {
    const base = "px-2 py-1 text-sm rounded font-medium";
    return status === "DONE" ? (
      <span className={`${base} bg-green-100 text-green-700`}>완료</span>
    ) : (
      <span className={`${base} bg-red-100 text-red-700`}>실패</span>
    );
  };

  if (loading) return <div className="p-8 text-center">불러오는 중...</div>

  return (
    <div className="max-w-7xl mx-auto p-8 space-y-8">

      <div>
        <h1 className="text-3xl font-bold mb-2">💰 결제 관리</h1>
        <p className="text-gray-500">
          프리미엄 멤버십 결제 내역과 통계를 조회합니다
        </p>
      </div>

      {/* Summary Cards */}
      <div className="grid md:grid-cols-3 gap-6">
        <div className="bg-gray-50 p-4 rounded-lg border-gray-900 textcenter">
          <p className="text-sm text-gray-500 mb-1">총 결제 건수</p>
          <p className="text-3xl font-bold">
            {summary?.totalPayments.toLocaleString()}
          </p>
        </div>

        <div className="bg-gray-50 p-4 rounded-lg border-gray-900">
          <p className="text-sm text-gray-500 mb-1">성공한 결제</p>
          <p className="text-3xl font-bold text-green-700">
            {summary?.successPayments.toLocaleString()}
          </p>
        </div>

        <div className="bg-gray-50 p-4 rounded-lg border-gray-900">
          <p className="text-sm text-gray-500 mb-1">총 수익</p>
          <p className="text-3xl font-bold flex items-center gap-2">
            {summary?.totalRevenue.toLocaleString()}원
          </p>
        </div>
      </div>

      {/* Payments Table */}
      <div className="overflow-x-auto bg-white border border-gray-200 shadow-sm rounded-lg">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>사용자</TableHead>
              <TableHead>플랜</TableHead>
              <TableHead>금액</TableHead>
              <TableHead>결제 수단</TableHead>
              <TableHead>상태</TableHead>
              <TableHead>결제일시</TableHead>
            </TableRow>
          </TableHeader>

          <TableBody>
            {payments.map((p) => (
              <TableRow key={p.orderId}>
                <TableCell>
                  <div>
                    <p className="font-medium">{p.userName}</p>
                    <p className="text-gray-500 text-xs">{p.userEmail}</p>
                  </div>
                </TableCell>

                <TableCell>
                  <span className="px-2 py-1 text-sm border border-gray-200 rounded bg-gray-50">
                    {p.orderName}
                  </span>
                </TableCell>

                <TableCell className="font-semibold">
                  {p.amount.toLocaleString()}원
                </TableCell>
                <TableCell>{p.method}</TableCell>
                <TableCell>{getStatusBadge(p.status)}</TableCell>
                <TableCell>{p.approvedAt}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
