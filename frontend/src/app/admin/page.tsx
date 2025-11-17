"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { fetchApi } from "@/lib/client";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  DropdownMenuContent,
  DropdownMenuItem,
  useDropdown,
} from "@/components/ui/dropdown-menu";
import {
  AdminUserResponse,
  UserPageResponse,
  AdminUserStatusUpdateRequest,
  AccountStatus,
  ACCOUNT_STATUS_LABELS,
} from "@/types/user";
import { toast } from "sonner";

/*계정 상태 뱃지 */
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
      return null;
  }
};

/*행 컴포넌트 */
function UserRow({
  user,
  onStatusChange,
}: {
  user: AdminUserResponse;
  onStatusChange: (id: number, newStatus: AccountStatus) => void;
}) {
  const router = useRouter();
  const { ref, open, setOpen } = useDropdown();

  //상태별 전환 옵션 (DEACTIVATED 제거)
  const nextStatusOptions: AccountStatus[] = (() => {
    switch (user.accountStatus) {
      case "ACTIVE":
        return ["SUSPENDED", "BANNED"];
      case "SUSPENDED":
      case "BANNED":
        return ["ACTIVE"];
      default:
        return [];
    }
  })();

  return (
    <TableRow
      key={user.id}
      onClick={() => router.push(`/admin/users/${user.id}`)}
      className="hover:bg-gray-50 cursor-pointer transition"
    >
      <TableCell>
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 flex items-center justify-center rounded-full bg-gray-200 font-bold text-gray-700">
            {user.nickname?.[0] || "?"}
          </div>
          <div>
            <p className="font-medium">{user.name}</p>
            <p className="text-sm text-gray-500">{user.email}</p>
          </div>
        </div>
      </TableCell>

      <TableCell>{user.nickname}</TableCell>
      <TableCell>{user.age}</TableCell>

      <TableCell>
        <a
          href={user.github}
          target="_blank"
          className="text-blue-600 hover:underline text-sm"
          onClick={(e) => e.stopPropagation()} //링크 클릭 시 라우팅 방지
        >
          GitHub
        </a>
      </TableCell>

      <TableCell>{user.role === "ADMIN" ? "관리자" : "사용자"}</TableCell>
      <TableCell>{getStatusBadge(user.accountStatus)}</TableCell>
    </TableRow>
  );
}

/*메인 페이지 */
export default function AdminUsersPage() {
  const [users, setUsers] = useState<AdminUserResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState("");
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  //사용자 목록 조회
  const fetchUsers = async (pageNum: number) => {
    try {
      setIsLoading(true);
      const res = await fetchApi(`/api/v1/admin/users?page=${pageNum}`, {
        method: "GET",
      });

      if (res.status === "OK") {
        const data: UserPageResponse = res.data;
        setUsers(data.users);
        setPage(data.currentPage);
        setTotalPages(data.totalPages);
      } else {
        setErrorMsg(res.message || "사용자 목록을 불러오지 못했습니다.");
      }
    } catch (e: any) {
      setErrorMsg(e.message || "서버 오류가 발생했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers(page);
  }, [page]);

  //상태 변경
  const handleStatusChange = async (userId: number, newStatus: AccountStatus) => {
    try {
      const body: AdminUserStatusUpdateRequest = { status: newStatus, reason: "", suspendEndDate: null };
      const res = await fetchApi(`/api/v1/admin/users/${userId}/status`, {
        method: "PATCH",
        body: JSON.stringify(body),
      });

      if (res.status === "OK") {
        toast.success(`사용자 상태가 "${ACCOUNT_STATUS_LABELS[newStatus]}"로 변경되었습니다.`);
        setUsers((prev) =>
          prev.map((u) =>
            u.id === userId ? { ...u, accountStatus: newStatus } : u
          )
        );
      } else {
        toast.error(res.message || "상태 변경 실패");
      }
    } catch {
      toast.error("서버 오류가 발생했습니다.");
    }
  };

  //로딩/에러 처리
  if (isLoading)
    return (
      <div className="flex justify-center items-center py-20 text-gray-500">
        로딩 중...
      </div>
    );

  if (errorMsg)
    return <div className="text-center text-red-600 py-20">{errorMsg}</div>;

  //메인 렌더링
  return (
    <div className="max-w-7xl mx-auto p-8 space-y-6">
      <div>
        <h1 className="text-3xl font-bold mb-2">🧑‍💻 사용자 관리</h1>
        <p className="text-gray-500">플랫폼 사용자의 상태를 관리합니다.</p>
      </div>

      {/* 사용자 테이블 */}
      <div className="overflow-visible bg-white border border-gray-200 shadow-sm rounded-lg">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>사용자</TableHead>
              <TableHead>닉네임</TableHead>
              <TableHead>나이</TableHead>
              <TableHead>GitHub</TableHead>
              <TableHead>역할</TableHead>
              <TableHead>계정 상태</TableHead>
            </TableRow>
          </TableHeader>

          <TableBody>
            {users.length > 0 ? (
              users.map((user) => (
                <UserRow
                  key={user.id}
                  user={user}
                  onStatusChange={handleStatusChange}
                />
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={7} className="text-center py-6 text-gray-500">
                  사용자가 없습니다.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      {/* 페이지네이션 */}
      <div className="flex justify-center items-center gap-2 mt-6">
        <button
          onClick={() => setPage((p) => Math.max(1, p - 1))}
          disabled={page === 1}
          className="px-3 py-1 border border-gray-200 rounded disabled:opacity-50"
        >
          이전
        </button>

        <span className="text-sm text-gray-600">
          {page} / {totalPages}
        </span>

        <button
          onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
          disabled={page === totalPages}
          className="px-3 py-1 border border-gray-200 rounded disabled:opacity-50"
        >
          다음
        </button>
      </div>
    </div>
  );
}
