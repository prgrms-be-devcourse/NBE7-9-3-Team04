"use client";

import { useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { fetchApi } from "@/lib/client";
import { toast } from "sonner";

export default function LoginPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const returnUrl = searchParams.get("returnUrl");

  const handleLogin = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      const apiResponse = await fetchApi(`/api/v1/users/login`, {
        method: "POST",
        body: JSON.stringify({ email, password }),
      });

      if (apiResponse.status === "OK") {
        toast.success(apiResponse.message);

        // 로그인 성공 시 상단바 업데이트 이벤트 발생
        window.dispatchEvent(new Event("loginSuccess"));

        const user = apiResponse.data;

        // 관리자라면 자동으로 /admin으로 이동
        if (user.role === "ADMIN") {
          router.push("/admin");
        } else {
          router.push(returnUrl || "/");
        }

        router.refresh();
      } else {
        toast.success(apiResponse.message);
      }
    } catch (err: any) {
      toast.error(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex mt-20 justify-center p-4">
      <div className="w-full max-w-md space-y-8">
        {/* 상단 타이틀 */}
        <div className="text-center">
          <h2 className="text-3xl font-bold text-gray-900">로그인</h2>
          <p className="text-gray-500 mt-2">DevStation에 오신 것을 환영합니다</p>
        </div>

        {/* 로그인 카드 */}
        <div className="w-full bg-white rounded-lg shadow-lg border border-gray-200">
          <div className="p-6">
            <form onSubmit={handleLogin} className="space-y-4">
              <div className="space-y-2">
                <h3 className="text-xl font-semibold">로그인</h3>
                <p className="text-sm text-gray-500">
                  이메일과 비밀번호를 입력해주세요.
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
                  <label
                    htmlFor="password"
                    className="text-sm font-medium block"
                  >
                    비밀번호
                  </label>
                  <input
                    id="password"
                    type="password"
                    className="w-full p-2 border border-gray-300 rounded focus:border-blue-500"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
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
                  {isLoading ? "로그인 중..." : "로그인"}
                </button>

                <div className="text-sm text-center text-gray-500">
                  계정이 없으신가요?{" "}
                  <Link
                    href="/auth/signup"
                    className="text-blue-600 hover:underline"
                  >
                    회원가입
                  </Link>
                </div>
                  <div>
                    <Link
                        href="/auth/findpw"
                        className="text-blue-600 hover:underline"
                    >
                        비밀번호 찾기
                    </Link>
                  </div>
                {/* 두 줄 띄움 */}
                <div className="h-2"></div>

                {/* SNS 로그인 영역 */}
                <div className="text-center text-gray-500 mb-2">
                  SNS 계정으로 간편하게 시작하기
                </div>

                <div className="flex justify-center">
                  <button
                    type="button"
                    onClick={() =>
                      window.location.href = "http://localhost:8080/oauth2/authorization/github?mode=login"
                    }
                    className="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded hover:bg-gray-100 hover:cursor-pointer"
                  >
                    {/* GitHub 아이콘 (SVG) */}
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="20"
                      height="20"
                      fill="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path d="M12 0C5.371 0 0 5.371 0 12c0 5.302 3.438 9.8 8.205 11.387.6.111.82-.261.82-.58 0-.287-.011-1.243-.017-2.253-3.338.726-4.042-1.611-4.042-1.611-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.085 1.84 1.238 1.84 1.238 1.07 1.834 2.807 1.304 3.492.997.108-.775.418-1.305.76-1.605-2.665-.304-5.466-1.332-5.466-5.931 0-1.31.469-2.381 1.236-3.221-.124-.303-.536-1.524.117-3.176 0 0 1.008-.322 3.3 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.289-1.552 3.294-1.23 3.294-1.23.655 1.652.243 2.873.12 3.176.77.84 1.235 1.911 1.235 3.221 0 4.61-2.804 5.624-5.475 5.921.43.372.815 1.102.815 2.222 0 1.606-.015 2.901-.015 3.293 0 .321.216.694.825.576C20.565 21.796 24 17.3 24 12c0-6.629-5.371-12-12-12z"/>
                    </svg>
                    GitHub로 시작하기
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
