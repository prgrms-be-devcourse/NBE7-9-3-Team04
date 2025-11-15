"use client";

import { useState, useEffect } from "react";
import { fetchApi } from "@/lib/client";
import Link from "next/link";
import CategoryTab from "@/components/categoryTab";
import { PostResponse, PostPageResponse, PostStatus, PinStatus } from "@/types/post";


export default function RecruitmentPage() {
  const [pinnedPosts, setPinnedPosts] = useState<PostResponse[]>([]);
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentSlide, setCurrentSlide] = useState(0);
  const [selectedCategory, setSelectedCategory] = useState("전체");
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const postsPerPage = 9;
  const categories = ["전체", "프로젝트", "스터디"];

  const fetchPinnedPosts = async () => {
    try {
      const res = await fetchApi(`/api/v1/posts/pinned`);
      if (res.status === "OK" && res.data) {
        const formatted = res.data.map((p: any) => ({
          ...p,
          categoryType:
            p.categoryType === "PROJECT"
              ? "프로젝트"
              : p.categoryType === "STUDY"
              ? "스터디"
              : p.categoryType,
          createDate: p.createDate?.split("T")[0],
          modifyDate: p.modifyDate?.split("T")[0],
          deadline: p.deadline?.split("T")[0],
        }));
        setPinnedPosts(formatted);
      }
    } catch (err) {
      console.error("프리미엄 게시글 불러오기 실패", err);
    }
  };

  const fetchPosts = async (page = 1) => {
    try {
      setLoading(true);

      const categoryQuery =
        selectedCategory === "전체"
          ? ""
          : selectedCategory === "프로젝트"
          ? "PROJECT"
          : "STUDY";

      const res = (await fetchApi(
        `/api/v1/posts?page=${page}&size=${postsPerPage}&category=${categoryQuery}`
      )) as {
        status: string;
        data: PostPageResponse<PostResponse>;
        message?: string;
      };

      if (res.status === "OK") {
        const formatted = res.data.posts.map((p: any) => ({
          ...p,
          createDate: p.createDate?.split("T")[0],
          modifyDate: p.modifyDate?.split("T")[0],
          deadline: p.deadline?.split("T")[0],
        }));
        setPosts(formatted);
        setCurrentPage(res.data.currentPage);
        setTotalPages(res.data.totalPages === 0 ? 1 : res.data.totalPages);
      } else {
        console.error("게시글 불러오기 실패:", res.message);
      }
    } catch (err) {
      console.error("게시글 불러오기 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPinnedPosts();
  }, []);

  useEffect(() => {
    fetchPosts(1);
  }, [selectedCategory]);

  useEffect(() => {
    const updatePostStatus = async () => {
      const currentDate = new Date();
      const updatedPosts = posts.map((post) => {
        const deadlineDate = new Date(post.deadline);
        if (deadlineDate < currentDate) {
          return {
            ...post,
            status: "CLOSED" as PostStatus,
            pinStatus: "NOT_PINNED" as PinStatus,
          };
        }
        return post;
      });
      return updatedPosts;
    };

    updatePostStatus().then((updatedPosts) => setPosts(updatedPosts));
  }, []);

  useEffect(() => {
    if (pinnedPosts.length === 0) return;
    const timer = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % pinnedPosts.length);
    }, 5000);
    return () => clearInterval(timer);
  }, [pinnedPosts.length]);

  const nextSlide = () =>
    setCurrentSlide((prev) => (prev + 1) % pinnedPosts.length);
  const prevSlide = () =>
    setCurrentSlide((prev) => (prev - 1 + pinnedPosts.length) % pinnedPosts.length);

  return (
    <div className="max-w-screen-xl mx-auto px-6 py-10">
      {/* 헤더 */}
      <div className="mb-10">
        <h1 className="text-3xl font-bold mb-2">👥 팀 프로젝트 & 스터디 모집</h1>
        <p className="text-gray-500">함께 성장할 팀원을 찾아보세요</p>
      </div>

      {/* 프리미엄 모집글 */}
      <div className="mb-10">
        <div className="flex justify-between mb-4">
          <h2 className="text-xl font-semibold">프리미엄 모집글</h2>
          <div className="flex gap-2">
            <button
              onClick={prevSlide}
              className="h-8 w-8 rounded border border-gray-300 hover:bg-gray-100"
            >
              &lt;
            </button>
            <button
              onClick={nextSlide}
              className="h-8 w-8 rounded border border-gray-300 hover:bg-gray-100"
            >
              &gt;
            </button>
          </div>
        </div>

        <div className="relative overflow-hidden rounded-lg">
          <div
            className="flex transition-transform duration-500 ease-in-out"
            style={{ transform: `translateX(-${currentSlide * 100}%)` }}
          >
            {pinnedPosts.map((post) => (
              <div key={post.postId} className="min-w-full flex-shrink-0">
                <div className="flex justify-between p-8 border border-blue-500 bg-blue-100 rounded-lg min-h-[160px]">
                  <div className="flex-1 pr-4 flex flex-col justify-between">
                    <div>
                      <div className="flex gap-3 mb-2">
                        <span className="bg-blue-600 text-white text-[10px] font-semibold rounded-full px-2 py-[2px]">
                          프리미엄
                        </span>
                        <span className="bg-gray-100 text-gray-700 text-[10px] font-medium rounded-full px-2 py-[2px]">
                          {post.categoryType}
                        </span>
                      </div>
                      <h3 className="text-lg font-semibold mb-2 line-clamp-2">
                        {post.title}
                      </h3>
                      <p className="text-sm text-gray-600 line-clamp-2">
                        {post.introduction}
                      </p>
                    </div>
                    <div className="flex items-center gap-1 text-sm text-gray-700 mt-3">
                      🧑‍🤝‍🧑 <span>{post.recruitCount}명</span>
                    </div>
                  </div>

                  <div className="flex flex-col justify-between items-end">
                    <div className="text-sm text-gray-500">
                      ⏰ 마감: {post.deadline}
                    </div>
                    <Link
                      href={`/recruitment/${post.postId}`}
                      className="bg-blue-600 text-white px-4 py-1 rounded hover:bg-blue-700 text-sm"
                    >
                      자세히 보기
                    </Link>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* 슬라이드 dot */}
          <div className="flex justify-center gap-2 mt-3">
            {pinnedPosts.map((_, i) => (
              <button
                key={`slide-${i}`}
                onClick={() => setCurrentSlide(i)}
                className={`h-2 rounded-full transition-all ${
                  i === currentSlide
                    ? "w-8 bg-blue-600"
                    : "w-2 bg-gray-300 hover:bg-gray-400"
                }`}
              />
            ))}
          </div>
        </div>
      </div>

      {/* 카테고리 */}
      <CategoryTab
        categories={categories}
        selected={selectedCategory}
        onSelect={(c) => {
          setSelectedCategory(c);
          setCurrentPage(1);
        }}
      />

      {/* 일반 모집글 */}
      {loading ? (
        <div className="text-center py-12">로딩 중...</div>
      ) : posts.length === 0 ? (
        <div className="text-center py-12 border border-gray-300 rounded-lg">
          <p className="text-gray-500">게시글이 없습니다.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
          {posts.map((post) => (
            <div
              key={post.postId}
              className="bg-white border border-gray-300 rounded-lg p-4 shadow-sm hover:shadow-md transition-shadow"
            >
              <div className="flex justify-between mb-2 text-sm">
                <div className="flex gap-1.5">
                  <span
                    className={`px-2 py-[2px] rounded-full text-[10px] font-medium ${
                      post.categoryType === "PROJECT"
                        ? "bg-indigo-100 text-indigo-700"
                        : "bg-green-100 text-green-700"
                    }`}
                  >
                    {post.categoryType === "PROJECT" ? "프로젝트" : "스터디"}
                  </span>
                  <span
                    className={`px-2 py-[2px] rounded-full text-[10px] font-medium ${
                      post.status === "ING"
                        ? "bg-red-100 text-red-700"
                        : "bg-gray-100 text-gray-500"
                    }`}
                  >
                    {post.status === "ING" ? "모집중" : "마감"}
                  </span>
                </div>
                <span className="text-gray-500 text-xs">
                  마감일 {post.deadline}
                </span>
              </div>

              <h3 className="text-lg font-semibold mb-1 line-clamp-2 min-h-[3rem]">
                {post.title}
              </h3>

              <p className="text-sm text-gray-600 mb-3 line-clamp-2 min-h-[2.5rem]">
                {post.introduction}
              </p>

              <div className="flex justify-between text-sm text-gray-500 mb-3">
                <span>🧑‍🤝‍🧑 {post.recruitCount}명</span>
              </div>

              <Link
                href={`/recruitment/${post.postId}`}
                className="block text-center border border-gray-300 rounded py-2 text-sm font-medium text-gray-700 hover:bg-gray-100"
              >
                자세히 보기
              </Link>
            </div>
          ))}
        </div>
      )}

      {/* 페이지네이션 */}
      <div className="flex justify-center items-center gap-2 mt-6">
        <button
          onClick={() => fetchPosts(1)}
          disabled={currentPage === 1}
          className={`px-3 py-1 rounded bg-gray-200 ${
            currentPage === 1
              ? "opacity-50 cursor-not-allowed"
              : "hover:bg-gray-300"
          }`}
        >
          처음
        </button>
        <button
          onClick={() => fetchPosts(currentPage - 1)}
          disabled={currentPage === 1}
          className={`px-3 py-1 rounded bg-gray-200 ${
            currentPage === 1
              ? "opacity-50 cursor-not-allowed"
              : "hover:bg-gray-300"
          }`}
        >
          &lt;
        </button>

        {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
          <button
            key={page}
            onClick={() => fetchPosts(page)}
            className={`px-3 py-1 rounded ${
              currentPage === page
                ? "bg-blue-600 text-white"
                : "bg-gray-200 hover:bg-gray-300"
            }`}
          >
            {page}
          </button>
        ))}

        <button
          onClick={() => fetchPosts(currentPage + 1)}
          disabled={currentPage === totalPages}
          className={`px-3 py-1 rounded bg-gray-200 ${
            currentPage === totalPages
              ? "opacity-50 cursor-not-allowed"
              : "hover:bg-gray-300"
          }`}
        >
          &gt;
        </button>
        <button
          onClick={() => fetchPosts(totalPages)}
          disabled={currentPage === totalPages}
          className={`px-3 py-1 rounded bg-gray-200 ${
            currentPage === totalPages
              ? "opacity-50 cursor-not-allowed"
              : "hover:bg-gray-300"
          }`}
        >
          마지막
        </button>
      </div>

      {/* 게시글 작성 버튼 */}
      <div className="flex justify-end mt-6">
        <Link
          href="/recruitment/new"
          className="inline-flex items-center justify-center px-4 py-2 rounded bg-blue-600 text-white text-sm hover:bg-blue-700"
        >
          게시글 작성
        </Link>
      </div>
    </div>
  );
}
