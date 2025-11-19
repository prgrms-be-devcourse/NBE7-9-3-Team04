"use client";

import { useState } from "react";
import { fetchApi } from "@/lib/client";
import { toast } from "sonner";
import Link from "next/link";

export default function FindPasswordPage() {
    const [email, setEmail] = useState("");
    const [name, setName] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    const handleFindPassword = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setIsLoading(true);

        try {
            const res = await fetchApi(
                `/api/v1/users/findPassword?name=${name}&email=${email}`,
                { method: "POST" }
            );

            if (res.status === "OK") {
                toast.success("새 비밀번호가 이메일로 전송되었습니다.");
            } else {
                toast.error("일치하는 사용자를 찾을 수 없습니다.");
            }
        } catch (err: any) {
            toast.error(err.message || "요청 처리 중 오류가 발생했습니다.");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex mt-20 justify-center p-4">
            <div className="w-full max-w-md space-y-8">
                {/* 상단 타이틀 */}
                <div className="text-center">
                    <h2 className="text-3xl font-bold text-gray-900">비밀번호 찾기</h2>
                </div>

                {/* 정보 기입 카드 */}
                <div className="w-full bg-white rounded-lg shadow-lg border border-gray-200">
                    <div className="p-6">
                        <form onSubmit={handleFindPassword} className="space-y-4">
                            <div className="space-y-2">
                                <h3 className="text-xl font-semibold">비밀번호 찾기</h3>
                                <p className="text-sm text-gray-500">
                                    이메일과 이름을 입력해주세요.
                                </p>
                            </div>

                            <div className="space-y-4">
                                <div className="space-y-1">
                                    <label htmlFor="email" className="text-sm font-medium block">
                                        이메일
                                    </label>
                                    <input
                                        id="email"
                                        type="email"
                                        className="w-full p-2 border border-gray-300 rounded focus:border-blue-500"
                                        placeholder="user@example.com"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        required
                                    />
                                </div>

                                <div className="space-y-1">
                                    <label htmlFor="name" className="text-sm font-medium block">
                                        이름
                                    </label>
                                    <input
                                        id="name"
                                        type="text"
                                        className="w-full p-2 border border-gray-300 rounded focus:border-blue-500"
                                        value={name}
                                        onChange={(e) => setName(e.target.value)}
                                        required
                                    />
                                </div>
                            </div>

                            <div className="flex flex-col gap-3 pt-2">
                                <button
                                    type="submit"
                                    className={`w-full py-2 font-medium rounded transition-colors bg-blue-600 text-white hover:bg-blue-700 ${
                                        isLoading ? "opacity-50 cursor-not-allowed" : ""
                                    }`}
                                    disabled={isLoading}
                                >
                                    {isLoading ? "임시 비밀번호 보내는 중..." : "비밀번호 찾기"}
                                </button>

                                <p className="text-sm text-center text-gray-500 mt-3">
                                    로그인 페이지로 돌아가시겠습니까?{" "}
                                    <Link href="/auth" className="text-blue-600 font-medium hover:underline">
                                        로그인
                                    </Link>
                                </p>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
}