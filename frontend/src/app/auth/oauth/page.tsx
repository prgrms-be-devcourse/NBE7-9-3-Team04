"use client";

import { useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";

export default function OAuthRedirectPage() {
  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    const oauthId = searchParams.get("oauthId"); // 백엔드에서 전달 가능
    const email = searchParams.get("email");
    const githubUrl = searchParams.get("githubUrl");

    // 부모창이 있으면 메시지 전달
    if (window.opener) {
      window.opener.postMessage({ oauthId, email, githubUrl }, "http://localhost:3000");
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
