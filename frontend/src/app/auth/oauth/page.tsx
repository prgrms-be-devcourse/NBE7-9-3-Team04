"use client";

import { useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";

export default function OAuthRedirectPage() {
  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    const token = searchParams.get("token");
    const oauthId = searchParams.get("oauthId"); // 백엔드에서 전달 가능
    const email = searchParams.get("email");

    if (token) {
        // JWT를 localStorage에 저장
        localStorage.setItem("accessToken", token);
    }

    // 부모창이 있으면 메시지 전달
    if (window.opener) {
      window.opener.postMessage({ oauthId, email }, "http://localhost:3000");
      window.close();
    } else {
      router.push("/");
    }
  }, [searchParams, router]);

  return (
    <div className="flex justify-center items-center min-h-screen">
      인증 중...
    </div>
  );
}
