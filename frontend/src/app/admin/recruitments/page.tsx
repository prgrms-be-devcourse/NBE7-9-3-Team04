"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
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
  PostResponse,
  PostPageResponse,
  PostStatus,
} from "@/types/post";
import { toast } from "sonner";
import { confirmAlert } from "@/lib/confirm";

function PostRow({
  post,
  onStatusChange,
  onDelete,
}: {
  post: PostResponse;
  onStatusChange: (id: number, status: PostStatus) => void;
  onDelete: (id: number) => void;
}) {
  const { ref, open, setOpen } = useDropdown();

  const getStatusBadge = (status: PostStatus) => {
    const base = "px-2 py-1 text-sm font-semibold rounded";
    return status === "ING" ? (
      <span className={`${base} bg-green-100 text-green-700`}>진행중</span>
    ) : (
      <span className={`${base} bg-gray-100 text-gray-700`}>마감</span>
    );
  };

  return (
    <TableRow key={post.postId}>
      <TableCell className="max-w-xs">
        <Link
          href={`/recruitment/${post.postId}?from=admin`}
          className="font-medium line-clamp-1 hover:text-blue-600 hover:underline"
        >
          {post.title}
        </Link>
      </TableCell>

      {/*작성자 표시 */}
      <TableCell>{post.nickName}</TableCell>

      {/*카테고리 */}
      <TableCell>{post.categoryType === "PROJECT" ? "프로젝트" : "스터디"}</TableCell>

      {/*상태 뱃지 */}
      <TableCell>{getStatusBadge(post.status)}</TableCell>

      {/*작성일 */}
      <TableCell>{new Date(post.createDate).toLocaleDateString().replace(/\.$/, "")}</TableCell>

      {/*Dropdown 메뉴 */}
      <TableCell className="text-right">
        <div ref={ref} className="relative inline-block">
          <button
            onClick={() => setOpen(!open)}
            className="p-2 rounded hover:bg-gray-100 text-gray-600"
            aria-label="더보기"
          >
            ⋮
          </button>

          <DropdownMenuContent open={open} align="end">
            {post.status === "ING" ? (
              <DropdownMenuItem
                onClick={() => {
                  onStatusChange(post.postId, "CLOSED");
                  setOpen(false);
                }}
              >
                마감 처리
              </DropdownMenuItem>
            ) : (
              <DropdownMenuItem
                onClick={() => {
                  onStatusChange(post.postId, "ING");
                  setOpen(false);
                }}
              >
                진행중으로 변경
              </DropdownMenuItem>
            )}
            <DropdownMenuItem
              onClick={() => {
                onDelete(post.postId);
                setOpen(false);
              }}
              className="text-red-600"
            >
              삭제
            </DropdownMenuItem>
          </DropdownMenuContent>
        </div>
      </TableCell>
    </TableRow>
  );
}


export default function AdminPostsPage() {
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState("");
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  //게시글 불러오기
  const fetchPosts = async (pageNum: number) => {
    try {
      setIsLoading(true);
      const res = await fetchApi(`/api/v1/admin/posts?page=${pageNum}`, {
        method: "GET",
      });

      if (res.status === "OK") {
        const data: PostPageResponse = res.data;
        setPosts(data.posts);
        setPage(data.currentPage);
        setTotalPages(data.totalPages);
      } else {
        setErrorMsg(res.message || "게시글을 불러오지 못했습니다.");
      }
    } catch (e: any) {
      setErrorMsg(e.message || "서버 오류가 발생했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchPosts(page);
  }, [page]);

  //상태 변경
  const handleStatusChange = async (postId: number, newStatus: PostStatus) => {
    try {
      const res = await fetchApi(`/api/v1/admin/posts/${postId}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status: newStatus }),
      });

      if (res.status === "OK") {
        toast.success(`게시글이 ${newStatus === "ING" ? "진행중" : "마감"} 처리되었습니다.`);
        setPosts((prev) =>
          prev.map((p) => (p.postId === postId ? { ...p, status: newStatus } : p))
        );
      } else {
        toast.error(res.message || "상태 변경 실패");
      }
    } catch {
      toast.error("서버 오류가 발생했습니다.");
    }
  };

  //삭제
  const handleDelete = async (postId: number) => {
    const ok = await confirmAlert("정말 삭제하시겠습니까?");
    if(!ok) return;

    try {
      const res = await fetchApi(`/api/v1/admin/posts/${postId}`, {
        method: "DELETE",
      });
      if (res.status === "OK") {
        toast.success("게시글이 삭제되었습니다.");
        setPosts((prev) => prev.filter((p) => p.postId !== postId));
      } else {
        toast.error(res.message || "삭제 실패");
      }
    } catch {
      toast.error("서버 오류가 발생했습니다.");
    }
  };


  if (isLoading)
    return (
      <div className="flex justify-center items-center py-20 text-gray-500">
        로딩 중...
      </div>
    );

  if (errorMsg)
    return <div className="text-center text-red-600 py-20">{errorMsg}</div>;

  return (
    <div className="max-w-7xl mx-auto p-8 space-y-6">
      <div>
        <h1 className="text-3xl font-bold mb-2">👥 모집글 관리</h1>
        <p className="text-gray-500">등록된 모집글을 관리합니다.</p>
      </div>

      {/*게시글 테이블 */}
      <div className="overflow-x-auto bg-white border border-gray-200 shadow-sm rounded-lg">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>제목</TableHead>
              <TableHead>작성자</TableHead>
              <TableHead>카테고리</TableHead>
              <TableHead>상태</TableHead>
              <TableHead>작성일</TableHead>
              <TableHead>작업</TableHead>
            </TableRow>
          </TableHeader>

          <TableBody>
            {posts.length > 0 ? (
              posts.map((post) => (
                <PostRow
                  key={post.postId}
                  post={post}
                  onStatusChange={handleStatusChange}
                  onDelete={handleDelete}
                />
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={6} className="text-center py-6 text-gray-500">
                  게시글이 없습니다.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      {/*페이지네이션 */}
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
