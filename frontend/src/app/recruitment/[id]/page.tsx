"use client";

import Link from "next/link";
import { useState, useEffect } from "react";
import { fetchApi } from "@/lib/client";
import { useParams } from "next/navigation";
import { PostResponse } from "@/types/post";
import {
  CommentResponse,
  CommentRequest,
  CommentPageResponse,
} from "@/types/comment";
import { marked } from "marked"; // Markdown 변환 라이브러리 추가
import { toast } from "sonner";
import { confirmAlert } from "@/lib/confirm";

type CommentWithEdit = CommentResponse & {
  isEditing: boolean;
  editContent: string;
};

export default function RecruitmentDetailPage() {
  const params = useParams();
  const postId = params.id as string;

  const [post, setPost] = useState<PostResponse | null>(null);
  const [comments, setComments] = useState<CommentWithEdit[]>([]);
  const [newComment, setNewComment] = useState<string>("");
  const [totalCount, setTotalCount] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [totalPages, setTotalPages] = useState<number>(1);
  const [pageSize, setPageSize] = useState<number>(30);

  // 게시글 불러오기
  const fetchPost = async () => {
    try {
      const res = (await fetchApi(`/api/v1/posts/${postId}`)) as {
        status: string;
        data: PostResponse;
        message?: string;
      };

      if (res.status === "OK" && res.data) {
        const formatted: PostResponse = {
          ...res.data,
          createDate: res.data.createDate.split("T")[0],
          modifyDate: res.data.modifyDate.split("T")[0],
          deadline: res.data.deadline.split("T")[0],
          content: await marked.parse(res.data.content), // Markdown을 HTML로 변환
        };
        setPost(formatted);
      } else {
        console.error("게시글 불러오기 실패", res.message);
      }
    } catch (err) {
      console.error("게시글 불러오기 실패:", err);
    }
  };

  useEffect(() => {
    fetchPost();
  }, [postId]);

  // 댓글 불러오기
  const fetchComments = async (page = 1) => {
    try {
      const res = (await fetchApi(
        `/api/v1/posts/${postId}/comments?page=${page}`
      )) as {
        status: string;
        data: CommentPageResponse;
        message?: string;
      };

      if (res.status === "OK" && Array.isArray(res.data.comments)) {
        const formatted: CommentWithEdit[] = res.data.comments.map((c) => ({
          ...c,
          isEditing: false,
          editContent: c.content,
        }));
        setComments(formatted);
        setTotalCount(res.data.totalCount);
        setCurrentPage(res.data.currentPage);
        setTotalPages(res.data.totalPages);
        setPageSize(res.data.pageSize);
      } else {
        setComments([]);
        setTotalCount(0);
      }
    } catch (err) {
      console.error("댓글 불러오기 실패:", err);
      setComments([]);
      setTotalCount(0);
    }
  };

  useEffect(() => {
    if (postId) fetchComments();
  }, [postId]);

  // 댓글 작성
  const handleSubmitComment = async () => {
    if (!newComment.trim()) return;

    const body: CommentRequest = { content: newComment };

    try {
      const res = (await fetchApi(`/api/v1/posts/${postId}/comments`, {
        method: "POST",
        body: JSON.stringify(body),
      })) as { status: string; data: CommentResponse; message?: string };

      if (res.status === "CREATED" && res.data) {
        setNewComment("");
        const newTotalCount = totalCount + 1;
        setTotalCount(newTotalCount);
        const lastPage = Math.ceil(newTotalCount / pageSize);
        fetchComments(lastPage);
      } else {
        toast.error(res.message || "댓글 작성 실패");
      }
    } catch (err: any) {
      toast.error(err.message);
    }
  };

  // 댓글 삭제
  const handleDeleteComment = async (commentId: number) => {
    const ok = await confirmAlert("정말로 댓글을 삭제하시겠습니까?");
    if (!ok) return;

    try {
      const res = (await fetchApi(
        `/api/v1/posts/${postId}/comments/${commentId}`,
        { method: "DELETE" }
      )) as { status: string; message?: string };

      if (res.status === "OK") {
        setComments((prev) => prev.filter((c) => c.id !== commentId));
        setTotalCount((prev) => prev - 1);
        toast.success("댓글이 삭제되었습니다.");
      } else {
        toast.error(res.message || "댓글 삭제 실패");
      }
    } catch (err: any) {
      toast.error(err.message);
    }
  };

  // 댓글 수정
  const handleEditStart = (commentId: number) => {
    setComments((prev) =>
      prev.map((c) => ({
        ...c,
        isEditing: c.id === commentId,
      }))
    );
  };

  const handleEditChange = (commentId: number, value: string) => {
    setComments((prev) =>
      prev.map((c) => (c.id === commentId ? { ...c, editContent: value } : c))
    );
  };

  const handleEditCancel = (commentId: number) => {
    setComments((prev) =>
      prev.map((c) =>
        c.id === commentId
          ? { ...c, isEditing: false, editContent: c.content }
          : c
      )
    );
  };

  const handleEditSave = async (commentId: number) => {
    const comment = comments.find((c) => c.id === commentId);
    if (!comment) return;

    try {
      const res = (await fetchApi(
        `/api/v1/posts/${postId}/comments/${commentId}`,
        {
          method: "PATCH",
          body: JSON.stringify({ content: comment.editContent }),
        }
      )) as { status: string; data?: CommentResponse; message?: string };

      if (res.status === "OK") {
        setComments((prev) =>
          prev.map((c) =>
            c.id === commentId
              ? { ...c, content: comment.editContent, isEditing: false }
              : c
          )
        );
        toast.success("댓글이 수정되었습니다.");
      } else {
        toast.error(res.message || "댓글 수정 실패");
      }
    } catch (err: any) {
      toast.error(err.message);
    }
  };

  const handlePageChange = (page: number) => {
    if (page < 1 || page > totalPages) return;
    fetchComments(page);
  };

  return (
    <div className="max-w-4xl mx-auto px-6 py-10">
      <Link
        href="/recruitment"
        className="inline-flex items-center text-sm text-gray-600 hover:text-blue-600 mb-6"
      >
        ← 목록으로
      </Link>

      {post && (
        <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-6">
          <div className="mb-6">
            <div className="flex items-start justify-between mb-3">
              <div className="flex items-center gap-2">
                <span
                  className={`px-2 py-0.5 text-xs font-medium rounded-full ${
                    post.categoryType === "PROJECT"
                      ? "bg-indigo-50 text-indigo-700"
                      : "bg-green-50 text-green-700"
                  }`}
                >
                  {post.categoryType === "PROJECT" ? "프로젝트" : "스터디"}
                </span>

                {/* 🔥 모집중 / 마감 배지 (같은 라인으로 이동) */}
                <span
                  className={`px-2 py-0.5 text-xs font-medium rounded-full ${
                    post.status === "ING"
                      ? "bg-red-100 text-red-700"
                      : "bg-gray-100 text-gray-500"
                  }`}
                >
                  {post.status === "ING" ? "모집중" : "마감"}
                </span>
              </div>

              <div className="flex gap-2">
                {post.isMine && (
                  <>
                    <button
                      onClick={() =>
                        (window.location.href = `/recruitment/edit/${post.postId}`)
                      }
                      className="px-3 py-1 rounded-md bg-emerald-600 text-white text-sm hover:bg-emerald-700 cursor-pointer"
                    >
                      수정
                    </button>
                    <button
                      onClick={async () => {
                        const ok = await confirmAlert(
                          "정말로 게시글을 삭제하시겠습니까?"
                        );
                        if (!ok) return;

                        try {
                          const res = (await fetchApi(
                            `/api/v1/posts/${post.postId}`,
                            { method: "DELETE" }
                          )) as { status: string; message?: string };

                          if (res.status === "OK") {
                            toast.success("게시글이 삭제되었습니다.");
                            window.location.href = "/recruitment";
                          } else {
                            toast.error(res.message || "게시글 삭제 실패");
                          }
                        } catch (err: any) {
                          toast.error(err.message);
                        }
                      }}
                      className="px-3 py-1 rounded-md bg-zinc-500 text-white text-sm hover:bg-zinc-600 cursor-pointer"
                    >
                      삭제
                    </button>

                    <button
                      onClick={async () => {
                        const ok = await confirmAlert("해당 모집글을 마감 처리 하겠습니까?");
                        if (!ok) return;

                        try {
                          const res = (await fetchApi(
                            `/api/v1/posts/${post.postId}/close`,
                            { method: "POST" }
                          )) as { status: string; message?: string };

                          if (res.status === "OK") {
                            toast.success("모집글이 마감되었습니다.");
                            window.location.reload();
                          } else {
                            toast.error(res.message || "모집글 마감 실패");
                          }
                        } catch (err: any) {
                          toast.error(err.message);
                        }
                      }}
                      className="px-3 py-1 rounded-md bg-red-600 text-white text-sm hover:bg-red-700 cursor-pointer"
                    >
                      마감
                    </button>
                  </>
                )}
              </div>
            </div>

            <h1 className="text-3xl font-bold mb-2">{post.title}</h1>
            <p className="text-gray-600 mb-4">{post.introduction}</p>

            <div className="flex flex-wrap gap-4 text-sm text-gray-500">
              <span>작성일: {post.createDate}</span>
              <span>마감: {post.deadline}</span>
              <span>모집 인원: {post.recruitCount}</span>
              <span>작성자: {post.nickName}</span>
            </div>
          </div>

          <hr className="my-5 border-gray-300" />

          <div
            className="prose prose-sm max-w-none whitespace-pre-wrap text-gray-800 mb-10"
            dangerouslySetInnerHTML={{ __html: post.content }}
          />

          <hr className="my-8 border-gray-300" />

          {/* 댓글 영역 */}
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-xl font-semibold">댓글 {totalCount}개</h3>
            <div className="flex items-center gap-2">
              <span className="text-lg font-semibold">
                {currentPage} / {totalPages === 0 ? 1 : totalPages}
              </span>
              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 1}
                className="px-3 py-1 rounded-full bg-gray-200 hover:bg-gray-300 cursor-pointer disabled:cursor-default disabled:bg-gray-200 disabled:opacity-50"
              >
                &lt;
              </button>
              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage === totalPages}
                className="px-3 py-1 rounded-full bg-gray-200 hover:bg-gray-300 cursor-pointer disabled:cursor-default disabled:bg-gray-200 disabled:opacity-50"
              >
                &gt;
              </button>
            </div>
          </div>

          <div className="space-y-4 mb-6">
            {comments.map((comment) => (
              <div key={comment.id} className="flex gap-3 items-start">
                <div className="w-10 h-10 flex items-center justify-center rounded-full bg-gray-200 text-gray-700 font-semibold">
                  {comment.authorNickName[0]}
                </div>

                <div className="flex-1">
                  <div className="flex items-center justify-between mb-1">
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-sm">
                        {comment.authorNickName}
                      </span>
                      <span className="text-xs text-gray-400">
                        {comment.createDate.split("T")[0]}
                      </span>
                    </div>

                    {!comment.isEditing ? (
                      <div className="flex gap-2 text-xs">
                        {comment.isMine && (
                          <>
                            <button
                              onClick={() => handleEditStart(comment.id)}
                              className="hover:underline cursor-pointer"
                            >
                              수정
                            </button>
                            <button
                              onClick={() => handleDeleteComment(comment.id)}
                              className="hover:underline cursor-pointer text-red-500"
                            >
                              삭제
                            </button>
                          </>
                        )}
                      </div>
                    ) : (
                      <div className="flex gap-2 text-xs">
                        <button
                          onClick={() => handleEditSave(comment.id)}
                          className="hover:underline cursor-pointer text-blue-600"
                        >
                          저장
                        </button>
                        <button
                          onClick={() => handleEditCancel(comment.id)}
                          className="hover:underline cursor-pointer text-gray-500"
                        >
                          취소
                        </button>
                      </div>
                    )}
                  </div>

                  {!comment.isEditing ? (
                    <p className="text-sm text-gray-700">{comment.content}</p>
                  ) : (
                    <textarea
                      value={comment.editContent}
                      onChange={(e) =>
                        handleEditChange(comment.id, e.target.value)
                      }
                      rows={3}
                      className="w-full border border-gray-300 rounded-md px-2 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  )}
                </div>
              </div>
            ))}
          </div>

          <div className="mb-6">
            <textarea
              placeholder="댓글을 작성해주세요"
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              rows={4}
              className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <div className="flex justify-end mt-3">
              <button
                onClick={handleSubmitComment}
                className="px-4 py-2 rounded-md bg-blue-600 text-white text-sm hover:bg-blue-700 cursor-pointer"
              >
                댓글 작성
              </button>
            </div>
          </div>

          <div className="flex gap-2 justify-center">
            {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
              <button
                key={page}
                onClick={() => handlePageChange(page)}
                className={`px-3 py-1 rounded ${
                  currentPage === page
                    ? "bg-blue-600 text-white"
                    : "bg-gray-200 hover:bg-gray-300 cursor-pointer"
                }`}
              >
                {page}
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
