"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { fetchApi } from "@/lib/client";
import {
  AdminUserResponse,
  AdminUserStatusUpdateRequest,
  AccountStatus,
  ACCOUNT_STATUS_LABELS,
} from "@/types/user";
import { toast } from "sonner";

export default function AdminUserDetailPage() {
  const { id } = useParams();
  const router = useRouter();

  const [user, setUser] = useState<AdminUserResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isProcessing, setIsProcessing] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [targetStatus, setTargetStatus] = useState<AccountStatus | null>(null);
  const [reason, setReason] = useState("");
  const [suspendEndDate, setSuspendEndDate] = useState("");

  const getStatusBadge = (status: AccountStatus) => {
    const base = "px-2 py-1 text-sm rounded font-medium";
    switch (status) {
      case "ACTIVE":
        return <span className={`${base} bg-green-100 text-green-700`}>활성</span>;
      case "SUSPENDED":
        return <span className={`${base} bg-yellow-100 text-yellow-700`}>일시정지</span>;
      case "BANNED":
        return <span className={`${base} bg-red-100 text-red-700`}>영구정지</span>;
      default:
        return <span className={`${base} bg-gray-100 text-gray-700`}>알 수 없음</span>;
    }
  };

  const fetchUser = async () => {
    try {
      const res = await fetchApi(`/api/v1/admin/users/${id}`, { method: "GET" });
      if (res.status === "OK") setUser(res.data);
      else toast.error(res.message || "사용자 정보를 불러오지 못했습니다.");
    } catch {
      toast.error("서버 오류가 발생했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (id) fetchUser();
  }, [id]);

  // 인자로 상태 직접 전달
  const handleStatusChange = async (status: AccountStatus) => {
    if (!user) return;

    // 정지/영구정지 시 사유 필수
    if (status !== "ACTIVE" && !reason.trim()) {
      return toast.error("정지 사유를 입력해주세요.");
    }

    const body: AdminUserStatusUpdateRequest = {
      status,
      reason: status === "SUSPENDED" || status === "BANNED" ? reason : "",
      suspendEndDate: status === "SUSPENDED" ? suspendEndDate : null,
    };

    setIsProcessing(true);
    try {
      const res = await fetchApi(`/api/v1/admin/users/${user.id}/status`, {
        method: "PATCH",
        body: JSON.stringify(body),
      });

      if (res.status === "OK") {
        toast.success(`"${ACCOUNT_STATUS_LABELS[status]}" 상태로 변경되었습니다.`);
        setShowModal(false);
        setReason("");
        setSuspendEndDate("");

        // 상세 페이지 새로고침
        window.location.href = `/admin/users/${user.id}`;
      } else {
        toast.error(res.message || "상태 변경 실패");
      }
    } catch {
      toast.error("서버 오류가 발생했습니다.");
    } finally {
      setIsProcessing(false);
    }
  };

  if (isLoading)
    return <div className="flex justify-center items-center py-20 text-gray-500">로딩 중...</div>;
  if (!user)
    return <div className="text-center py-20 text-red-600">사용자를 찾을 수 없습니다.</div>;

  return (
    <div className="max-w-3xl mx-auto p-8 space-y-8 bg-white border border-gray-200 shadow-sm rounded-lg">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold">👤 사용자 상세 정보</h1>
        <button
          onClick={() => router.push("/admin")}
          className="px-3 py-2 border border-gray-300 rounded text-sm hover:bg-gray-100"
        >
          ← 목록으로
        </button>
      </div>

      <div className="flex items-center gap-4">
        <div className="w-16 h-16 flex items-center justify-center rounded-full bg-gray-200 text-2xl font-semibold text-gray-600">
          {user.nickname?.[0] || "?"}
        </div>
        <div>
          <p className="text-lg font-semibold">{user.name}</p>
          <p className="text-gray-500">{user.email}</p>
        </div>
      </div>

      <div className="space-y-3 text-sm">
        <p><span className="font-semibold w-32 inline-block">닉네임:</span>{user.nickname}</p>
        <p><span className="font-semibold w-32 inline-block">나이:</span>{user.age}세</p>
        <p>
          <span className="font-semibold w-32 inline-block">GitHub:</span>
          {user.github ? (
            <a href={user.github} target="_blank" className="text-blue-600 hover:underline">{user.github}</a>
          ) : <span className="text-gray-400">없음</span>}
        </p>
        <p><span className="font-semibold w-32 inline-block">역할:</span>{user.role === "ADMIN" ? "관리자" : "일반 사용자"}</p>
        <p><span className="font-semibold w-32 inline-block">계정 상태:</span>{getStatusBadge(user.accountStatus)}</p>
      </div>

      <div className="flex gap-3 pt-4 border-t">
        {user.accountStatus === "ACTIVE" ? (
          <>
            <button
              onClick={() => { setTargetStatus("SUSPENDED"); setShowModal(true); }}
              className="px-4 py-2 rounded border border-yellow-300 text-yellow-700 hover:bg-yellow-50"
            >
              일시정지
            </button>
            <button
              onClick={() => { setTargetStatus("BANNED"); setShowModal(true); }}
              className="px-4 py-2 rounded border border-red-400 bg-red-100 text-red-700 hover:bg-red-200"
            >
              영구정지
            </button>
          </>
        ) : (
          <button
            onClick={() => handleStatusChange("ACTIVE")}
            disabled={isProcessing}
            className="px-4 py-2 rounded bg-green-600 text-white hover:bg-green-700 disabled:opacity-50"
          >
            {isProcessing ? "처리 중..." : "활성화"}
          </button>
        )}
      </div>

      {/* 🔹 정지 사유 입력 모달 */}
      {showModal && (
        <div className="fixed inset-0 flex items-center justify-center bg-black/50 z-50">
          <div className="bg-white p-6 rounded-lg shadow-lg w-96 space-y-4">
            <h2 className="text-lg font-bold mb-2">
              {targetStatus === "SUSPENDED" ? "일시정지 설정" : "영구정지 설정"}
            </h2>

            <div>
              <label className="block text-sm font-medium mb-1">정지 사유</label>
              <textarea
                className="w-full border rounded p-2 text-sm"
                rows={3}
                placeholder="정지 사유를 입력하세요"
                value={reason}
                onChange={(e) => setReason(e.target.value)}
              />
            </div>

            {targetStatus === "SUSPENDED" && (
              <div>
                <label className="block text-sm font-medium mb-1">해제 예정일</label>
                <input
                  type="date"
                  className="w-full border rounded p-2 text-sm"
                  value={suspendEndDate}
                  onChange={(e) => setSuspendEndDate(e.target.value)}
                />
              </div>
            )}

            <div className="flex justify-end gap-2 pt-3">
              <button
                onClick={() => setShowModal(false)}
                className="px-3 py-1.5 border rounded text-gray-600 hover:bg-gray-100"
              >
                취소
              </button>
              <button
                onClick={() => handleStatusChange(targetStatus!)}
                disabled={isProcessing}
                className="px-3 py-1.5 bg-red-600 text-white rounded hover:bg-red-700 disabled:opacity-50"
              >
                {isProcessing ? "처리 중..." : "확인"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
