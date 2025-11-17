"use client";

import Link from "next/link";
import { useRouter, useParams } from "next/navigation";
import { useState, useEffect } from "react";
import { PostUpdateRequest, PostResponse } from "@/types/post";
import { fetchApi } from "@/lib/client";
import { toast } from "sonner";

export default function RecruitmentEditPage() {
    const router = useRouter();
    const params = useParams();
    const postId = params?.id;

    const [formData, setFormData] = useState<PostUpdateRequest | null>(null);
    const [loading, setLoading] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        if (!postId) {
            console.error("postId is undefined or invalid", params);
            toast.error("잘못된 접근입니다. 게시글 ID가 없습니다.");
            router.replace("/recruitment");
            return;
        }

        let isMounted = true;

        const fetchPostData = async () => {
            setLoading(true);

            try {
                const res = (await fetchApi(`/api/v1/posts/${postId}`)) as {
                    status: string;
                    data: PostResponse;
                    message?: string;
                };

                if (!isMounted) return;

                if (res.status === "OK" && res.data) {
                    if (!res.data.isMine) {
                        toast.error("수정 권한이 없습니다. 본인이 작성한 글만 수정할 수 있습니다.");
                        router.replace(`/recruitment/${postId}`);
                        return;
                    }

                    setFormData({
                        title: res.data.title,
                        content: res.data.content,
                        introduction: res.data.introduction,
                        deadline: res.data.deadline.split("T")[0],
                        status: res.data.status,
                        pinStatus: res.data.pinStatus,
                        recruitCount: res.data.recruitCount,
                        categoryType: res.data.categoryType,
                    });
                } else {
                    toast.error(res.message || "수정할 게시글 정보를 불러오는 데 실패했습니다.");
                    router.replace(`/recruitment/${postId}`);
                }
            } catch (err) {
                if (!isMounted) return;
                console.error("게시글 로딩 중 오류 발생:", err);
                toast.error("게시글 로딩 중 오류 발생.");
                router.replace(`/recruitment/${postId}`);
            } finally {
                if (isMounted) {
                    setLoading(false);
                }
            }
        };

        fetchPostData();

        return () => {
            isMounted = false;
        };
    }, [postId, router]);


    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (isSubmitting || !formData) return;

        const formattedFormData = {
            ...formData,
            deadline: `${formData.deadline}T00:00:00`,
        };

        if (typeof formattedFormData.recruitCount !== 'number' || formattedFormData.recruitCount < 1) {
            toast.error("모집 인원은 1명 이상이어야 합니다.");
            return;
        }

        setIsSubmitting(true);

        try {
            const apiResponse = await fetchApi(`/api/v1/posts/${postId}`, {
                method: "PUT", 
                body: JSON.stringify(formattedFormData),
            });

            if (apiResponse.status === "OK") {
                toast.success(apiResponse.message || "게시글이 성공적으로 수정되었습니다.");
                router.replace(`/recruitment/${postId}`);
            } else {
                toast.error(apiResponse.message || "게시글 수정 중 오류가 발생했습니다.");
            }
        } catch (err: any) {
            toast.error("API 통신 실패: " + err.message);
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        const { id, value } = e.target;
        let newValue: string | number = value;

        if (id === 'recruitCount') {
            newValue = Number(value);
        }

        setFormData((prev) => ({
            ...prev!,
            [id]: newValue as any,
        }));
    };
    
    
    if (loading) {
        return (
            <div className="max-w-4xl mx-auto px-6 py-10">
                <p>게시글 데이터를 불러오는 중입니다...</p>
            </div>
        );
    }
    
    if (!formData) return null;


    return (
        <>
            <div className="max-w-4xl mx-auto px-6 py-10">
                <Link
                    href={`/recruitment/${postId}`}
                    className="inline-flex items-center text-sm text-gray-600 hover:text-blue-600 mb-6"
                >
                    ← 상세 보기로
                </Link>

                <div className="mb-8">
                    <h1 className="text-3xl font-bold mb-2">모집글 수정</h1>
                    <p className="text-gray-500">
                        게시글 내용을 수정하고 저장해주세요
                    </p>
                </div>

                <form
                    onSubmit={handleSubmit}
                    className="space-y-6 bg-white border border-gray-200 rounded-lg shadow-sm p-6"
                >
                    {/* 제목 */}
                    <div>
                        <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-1">제목</label>
                        <input
                            id="title"
                            type="text"
                            placeholder="모집글 제목을 입력하세요"
                            value={formData.title}
                            onChange={handleChange}
                            required
                            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>

                    {/* 한 줄 소개 */}
                    <div>
                        <label htmlFor="introduction" className="block text-sm font-medium text-gray-700 mb-1">한 줄 소개</label>
                        <input
                            id="introduction" 
                            type="text"
                            placeholder="한 줄로 프로젝트를 소개해주세요"
                            value={formData.introduction}
                            onChange={handleChange}
                            required
                            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        {/* 카테고리 */}
                        <div>
                            <label htmlFor="categoryType" className="block text-sm font-medium text-gray-700 mb-1">카테고리</label>
                            <select
                                id="categoryType" 
                                value={formData.categoryType}
                                onChange={handleChange}
                                required
                                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                <option value="PROJECT">프로젝트</option>
                                <option value="STUDY">스터디</option>
                            </select>
                        </div>

                        {/* 모집 마감일 */}
                        <div>
                            <label htmlFor="deadline" className="block text-sm font-medium text-gray-700 mb-1">모집 마감일</label>
                            <input
                                id="deadline"
                                type="date"
                                value={formData.deadline}
                                onChange={handleChange}
                                required
                                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                    </div>

                    {/* 모집 인원 */}
                    <div>
                        <label htmlFor="recruitCount" className="block text-sm font-medium text-gray-700 mb-1">모집 인원</label>
                        <input
                            id="recruitCount" 
                            type="number"
                            placeholder="최대 모집 인원"
                            value={formData.recruitCount}
                            onChange={handleChange}
                            min={1} 
                            required
                            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>

                    {/* 상세 내용 */}
                    <div>
                        <label htmlFor="content" className="block text-sm font-medium text-gray-700 mb-1">상세 내용</label>
                        <textarea
                            id="content"
                            placeholder="프로젝트 소개, 필요 기술, 진행 방식 등을 자세히 작성해주세요"
                            value={formData.content}
                            onChange={handleChange}
                            rows={15}
                            required
                            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>

                    {/* 저장 버튼 */}
                    <div className="flex justify-end gap-3">
                        <Link
                            href={`/recruitment/${postId}`}
                            className="inline-flex items-center justify-center px-4 py-2 border border-gray-300 rounded-md text-sm text-gray-700 hover:bg-gray-100"
                        >
                            취소
                        </Link>
                        <button
                            type="submit"
                            disabled={isSubmitting}
                            className="inline-flex items-center justify-center px-4 py-2 rounded-md bg-blue-600 text-white text-sm hover:bg-blue-700 disabled:opacity-70 disabled:cursor-not-allowed"
                        >
                            {isSubmitting ? '수정 중...' : '수정 완료'}
                        </button>
                    </div>
                </form>
            </div>
        </>
    );
}
