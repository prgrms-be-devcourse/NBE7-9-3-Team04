"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { fetchApi } from "@/lib/client";
import { Qna } from "@/types/qna";
import Pagination from "@/components/pagination";

export default function QnAPage() {
  const router = useRouter();

  const [qnaList, setQnaList] = useState<Qna[]>([]);
  const [selectedCategory, setSelectedCategory] = useState("전체");
  const [currentPage, setCurrentPage] = useState(1);
  const [totalItems, setTotalItems] = useState(0);
  const itemsPerPage = 15;
  const [isLoading, setIsLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState("");

  const categories = ["전체", "계정", "결제", "시스템", "모집", "제안", "기타"];

  // ✅ 카테고리별 색상 매핑
  const getCategoryColor = (category: string) => {
    const colorMap: Record<string, string> = {
      계정: "bg-indigo-50 text-indigo-700 border-indigo-100",
      결제: "bg-blue-50 text-blue-700 border-blue-100",
      시스템: "bg-slate-50 text-slate-700 border-slate-100",
      모집: "bg-purple-50 text-purple-700 border-purple-100",
      제안: "bg-emerald-50 text-emerald-700 border-emerald-100",
      기타: "bg-amber-50 text-amber-700 border-amber-100",
    };
    return colorMap[category] || "bg-gray-100 text-gray-700 border-gray-100";
  };

  // ✅ QnA 목록 조회 (404 → 빈 리스트 fallback 처리)
  const fetchQnaList = async () => {
    try {
      setIsLoading(true);
      setErrorMsg(""); // 새 요청 시 이전 에러 초기화

      const map: Record<string, string> = {
        계정: "ACCOUNT",
        결제: "PAYMENT",
        시스템: "SYSTEM",
        모집: "RECRUITMENT",
        제안: "SUGGESTION",
        기타: "OTHER",
      };

      const endpoint =
        selectedCategory === "전체"
          ? `/api/v1/qna?page=${currentPage}`
          : `/api/v1/qna/category/${map[selectedCategory]}?page=${currentPage}`;

      const apiResponse = await fetchApi(endpoint, { method: "GET" });

      if (apiResponse.status === "OK") {
        const data = apiResponse.data;
        const items = data.qna || [];
        const totalCount = data.totalCount || 0;
        setQnaList(items);
        setTotalItems(totalCount);
      } else {
        // ✅ 404 or QNA_NOT_FOUND 같은 경우도 빈 리스트로 처리
        console.warn("QnA 목록 비어 있음:", apiResponse.message);
        setQnaList([]);
        setTotalItems(0);
      }
    } catch (error: any) {
      // ✅ 서버 오류나 네트워크 문제도 fallback 처리
      console.warn("QnA 목록 조회 실패:", error.message);
      setQnaList([]);
      setTotalItems(0);
      // ❌ errorMsg로 표시하지 않음 (사용자에겐 빈 화면으로 보임)
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchQnaList();
  }, [selectedCategory, currentPage]);

  return (
    <div className="min-h-screen px-4 py-10 flex flex-col items-center bg-gray-50">
      <div className="w-full max-w-5xl space-y-10">
        {/* 상단 헤더 */}
        <div className="text-center">
          <h1 className="text-4xl font-bold mb-2 text-gray-900">💬 QnA 게시판</h1>
          <p className="text-gray-500">카테고리를 선택해 질문을 찾아보세요</p>
        </div>

        {/* 상단 버튼 영역 */}
        <div className="flex justify-between items-center">
          <div className="flex flex-wrap gap-2">
            {categories.map((category) => {
              const isSelected = selectedCategory === category;
              const isAll = category === "전체";

              let btnClass = "";
              if (isSelected) {
                if (isAll) {
                  btnClass = "bg-blue-600 text-white border-blue-600 shadow-sm";
                } else {
                  btnClass = getCategoryColor(category) + " font-semibold shadow-sm";
                }
              } else {
                btnClass = "bg-white hover:bg-gray-50 text-gray-700 border-gray-200";
              }

              return (
                <button
                  key={category}
                  onClick={() => {
                    setSelectedCategory(category);
                    setCurrentPage(1);
                  }}
                  className={`px-3 py-1.5 rounded-md text-sm font-medium border transition-all ${btnClass}`}
                >
                  {category}
                </button>
              );
            })}
          </div>

          <button
            onClick={() => router.push("/qna/new")}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
          >
            질문하기
          </button>
        </div>

        {/* 본문 */}
        <div className="space-y-4">
          {isLoading && (
            <div className="flex justify-center items-center py-20">
              <p className="text-gray-500 text-lg">불러오는 중...</p>
            </div>
          )}

          {!isLoading && qnaList.length === 0 && (
            <div className="bg-white rounded-md shadow text-center py-16 text-gray-400">
              등록된 질문이 없습니다.
            </div>
          )}

          {!isLoading &&
            qnaList.length > 0 &&
            qnaList.map((qna) => (
              <div
                key={qna.qnaId}
                onClick={() => router.push(`/qna/${qna.qnaId}`)}
                className={`rounded-md border border-gray-200 shadow p-6 transition-all cursor-pointer ${
                  qna.isAnswered
                    ? "bg-green-50 hover:border-green-300"
                    : "bg-gray-50 hover:border-gray-300"
                }`}
              >
                {/* 상태/카테고리 */}
                <div className="flex items-center gap-2 mb-3">
                  <span
                    className={`px-2 py-1 text-sm rounded-md font-semibold border ${
                      qna.isAnswered
                        ? "bg-green-100 text-green-700 border-green-200"
                        : "bg-yellow-100 text-yellow-700 border-yellow-200"
                    }`}
                  >
                    {qna.isAnswered ? "답변완료" : "대기중"}
                  </span>
                  <span
                    className={`px-2 py-1 text-sm rounded-md border font-medium ${getCategoryColor(
                      qna.categoryName
                    )}`}
                  >
                    {qna.categoryName}
                  </span>
                </div>

                {/* 제목 */}
                <h2
                  className={`text-xl font-bold mb-1 transition-colors ${
                    qna.isAnswered ? "text-green-800" : "text-gray-900"
                  } hover:text-blue-600`}
                >
                  {qna.title}
                </h2>

                {/* 내용 미리보기 */}
                <p className="text-gray-600 text-sm line-clamp-2 mb-3">{qna.content}</p>

                {/* 작성자 */}
                <div className="flex items-center gap-2 text-sm text-gray-500">
                  <div className="w-6 h-6 rounded-md bg-gray-200 flex items-center justify-center text-xs font-semibold">
                    {qna.authorNickname[0]}
                  </div>
                  <span>{qna.authorNickname}</span>
                  <span>•</span>
                  <span>{new Date(qna.createdDate).toLocaleDateString().replace(/\.$/, "")}</span>
                </div>
              </div>
            ))}
        </div>

        {/* ✅ 페이지네이션 */}
        {!isLoading && qnaList.length > 0 && (
          <Pagination
            currentPage={currentPage}
            totalItems={totalItems}
            itemsPerPage={itemsPerPage}
            onPageChange={setCurrentPage}
          />
        )}
      </div>
    </div>
  );
}
