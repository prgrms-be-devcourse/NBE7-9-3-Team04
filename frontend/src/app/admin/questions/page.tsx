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
} from "@/components/ui/dropdown-menu";
import { QuestionResponse, QUESTION_CATEGORY_LIST } from "@/types/question";
import { toast } from "sonner";
import { confirmAlert } from "@/lib/confirm";

export default function AdminQuestionsPage() {
  const router = useRouter();
  const [questions, setQuestions] = useState<QuestionResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState("");
  const [selectedQuestion, setSelectedQuestion] = useState<QuestionResponse | null>(null);
  const [openMenuId, setOpenMenuId] = useState<number | null>(null);

  //영어 카테고리를 한글로 변환하는 유틸 함수
  const getCategoryLabel = (value: string) => {
    const category = QUESTION_CATEGORY_LIST.find((c) => c.value === value);
    return category ? category.label : value;
  };

  //질문 목록 불러오기
  const fetchQuestions = async () => {
    try {
      setIsLoading(true);
      const apiResponse = await fetchApi("/api/v1/admin/questions?page=1", {
        method: "GET",
      });

      if (apiResponse.status === "OK") {
        setQuestions(apiResponse.data.questions || []);
      } else {
        setErrorMsg(apiResponse.message || "질문 목록을 불러오지 못했습니다.");
      }
    } catch (error: any) {
      setErrorMsg(error.message || "서버 오류가 발생했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchQuestions();
  }, []);

  //승인 / 미승인 처리
  const handleApprove = async (questionId: number, isApproved: boolean) => {
    try {
      const res = await fetchApi(`/api/v1/admin/questions/${questionId}/approve`, {
        method: "PATCH",
        body: JSON.stringify({ isApproved }),
      });

      if (res.status === "OK") {
        toast.success(isApproved ? "질문이 승인되었습니다." : "질문이 미승인 처리되었습니다.");
        setQuestions((prev) =>
          prev.map((q) =>
            q.questionId === questionId ? { ...q, isApproved } : q
          )
        );
        setSelectedQuestion(null);
      } else {
        toast.error(res.message || "처리 중 오류가 발생했습니다.");
      }
    } catch {
      toast.error("서버 통신 중 오류가 발생했습니다.");
    }
  };

  //삭제
  const handleDelete = async (questionId: number) => {
    const confirmed = await confirmAlert("정말 삭제하시겠습니까?");
    if (!confirmed) return; 

    try {
      const res = await fetchApi(`/api/v1/admin/questions/${questionId}`, {
        method: "DELETE",
      });
      if (res.status === "OK") {
        toast.success("질문이 삭제되었습니다.");
        setQuestions((prev) => prev.filter((q) => q.questionId !== questionId));
        setSelectedQuestion(null);
      } else {
        toast.error(res.message || "삭제 실패");
      }
    } catch {
      toast.error("서버 오류가 발생했습니다.");
    }
  };

  //상태 뱃지
  const getStatusBadge = (isApproved: boolean) => {
    return isApproved ? (
      <span className="px-2 py-1 text-sm font-semibold rounded-md bg-green-100 text-green-700">
        승인됨
      </span>
    ) : (
      <span className="px-2 py-1 text-sm font-semibold rounded-md bg-yellow-100 text-yellow-700">
        미승인
      </span>
    );
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
      {/*상단 헤더 */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold mb-2">📖 질문 관리</h1>
          <p className="text-gray-500">
            관리자가 등록된 질문을 승인/삭제할 수 있습니다.
          </p>
        </div>

        <button
          onClick={() => router.push("/admin/questions/new")}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition"
        >
          질문 등록하기
        </button>
      </div>

      {/*테이블 */}
      <div className="overflow-x-auto bg-white border border-gray-200 shadow-sm rounded-lg">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>질문 제목</TableHead>
              <TableHead>작성자</TableHead>
              <TableHead>카테고리</TableHead>
              <TableHead>점수</TableHead>
              <TableHead>상태</TableHead>
              <TableHead>등록일</TableHead>
              <TableHead>작업</TableHead>
            </TableRow>
          </TableHeader>

          <TableBody>
            {questions.map((q) => (
              <TableRow key={q.questionId}>
                <TableCell
                  className="max-w-xs cursor-pointer hover:text-blue-600 hover:underline"
                  onClick={() => setSelectedQuestion(q)}
                >
                  {q.title}
                </TableCell>
                <TableCell>{q.authorNickname}</TableCell>
                {/*카테고리 한글 변환 */}
                <TableCell>{getCategoryLabel(q.categoryType)}</TableCell>
                <TableCell>{q.score}</TableCell>
                <TableCell>{getStatusBadge(q.isApproved)}</TableCell>
                <TableCell>
                  {new Date(q.createdDate).toLocaleDateString().replace(/\.$/, "")}
                </TableCell>

                <TableCell className="text-right">
                  <div className="relative inline-block">
                    <button
                      onClick={() =>
                        setOpenMenuId(openMenuId === q.questionId ? null : q.questionId)
                      }
                      className="p-2 rounded hover:bg-gray-100 text-gray-600"
                    >
                      ⋮
                    </button>

                    {openMenuId === q.questionId && (
                      <DropdownMenuContent open align="end">
                        {q.isApproved ? (
                          <DropdownMenuItem
                            onClick={() => {
                              handleApprove(q.questionId, false);
                              setOpenMenuId(null);
                            }}
                          >
                            미승인
                          </DropdownMenuItem>
                        ) : (
                          <DropdownMenuItem
                            onClick={() => {
                              handleApprove(q.questionId, true);
                              setOpenMenuId(null);
                            }}
                          >
                            승인
                          </DropdownMenuItem>
                        )}
                        <DropdownMenuItem
                          onClick={() => {
                            handleDelete(q.questionId);
                            setOpenMenuId(null);
                          }}
                          className="text-red-600"
                        >
                          삭제
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    )}
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {/*질문 상세 모달 */}
      {selectedQuestion && (
        <div className="fixed inset-0 bg-black/40 flex justify-center items-center z-50">
          <div className="bg-white rounded-lg shadow-lg w-full max-w-2xl p-6 relative">
            <button
              className="absolute top-3 right-4 text-gray-400 hover:text-gray-600"
              onClick={() => setSelectedQuestion(null)}
            >
              ✕
            </button>

            <h2 className="text-2xl font-bold mb-4">{selectedQuestion.title}</h2>

            <div className="text-gray-800 whitespace-pre-wrap border border-gray-200 p-4 rounded-lg bg-gray-50 mb-4">
              {selectedQuestion.content}
            </div>

            {/* ✅ 점수 수정 영역 추가 */}
            <div className="flex justify-between items-center border-t pt-4 mt-4">
              <div className="flex gap-3 items-center">
                {getStatusBadge(selectedQuestion.isApproved)}
                <span className="text-sm text-gray-500">
                  {getCategoryLabel(selectedQuestion.categoryType)}
                </span>
              </div>

              <div className="flex items-center gap-2 text-sm text-gray-700">
                <label htmlFor="scoreInput" className="text-gray-600 font-medium">
                  점수:
                </label>
                <input
                  id="scoreInput"
                  type="number"
                  min={0}
                  max={50}
                  value={selectedQuestion.score}
                  onChange={(e) =>
                    setSelectedQuestion({
                      ...selectedQuestion,
                      score: Number(e.target.value),
                    })
                  }
                  className="w-15 px-2 py-1 border border-gray-200 rounded-md text-center"
                />
                <button
                  className="px-2 py-1 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                  onClick={async () => {
                    try {
                      const res = await fetchApi(
                        `/api/v1/admin/questions/${selectedQuestion.questionId}/score`,
                        {
                          method: "PATCH",
                          body: JSON.stringify({ score: selectedQuestion.score }),
                        }
                      );

                      if (res.status === "OK") {
                        toast.success("점수가 수정되었습니다.");
                        // 목록에서도 즉시 반영
                        setQuestions((prev) =>
                          prev.map((q) =>
                            q.questionId === selectedQuestion.questionId
                              ? { ...q, score: selectedQuestion.score }
                              : q
                          )
                        );
                      } else {
                        toast.error(res.message || "점수 수정 실패");
                      }
                    } catch (err) {
                      toast.error("서버 오류로 점수 수정에 실패했습니다.");
                    }
                  }}
                >
                  수정
                </button>
              </div>
            </div>

            {/* 하단 승인/삭제 버튼 */}
            <div className="flex justify-end gap-3 mt-6">
              {selectedQuestion.isApproved ? (
                <>
                  <button
                    className="px-4 py-2 bg-yellow-500 text-white rounded-md hover:bg-yellow-600"
                    onClick={() =>
                      handleApprove(selectedQuestion.questionId, false)
                    }
                  >
                    미승인
                  </button>
                  <button
                    className="px-4 py-2 bg-red-500 text-white rounded-md hover:bg-red-600"
                    onClick={() => handleDelete(selectedQuestion.questionId)}
                  >
                    삭제
                  </button>
                </>
              ) : (
                <>
                  <button
                    className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700"
                    onClick={() =>
                      handleApprove(selectedQuestion.questionId, true)
                    }
                  >
                    승인
                  </button>
                  <button
                    className="px-4 py-2 bg-red-500 text-white rounded-md hover:bg-red-600"
                    onClick={() => handleDelete(selectedQuestion.questionId)}
                  >
                    삭제
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}