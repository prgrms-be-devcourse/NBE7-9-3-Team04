"use client";

import { useRouter } from "next/navigation";
import { useState, useEffect } from "react";
import { fetchApi } from "@/lib/client";
import { UserMyPageResponse } from "@/types/user";
import { toast } from "sonner";

export default function MySettingsPage() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);
  const [showPasswordModal, setShowPasswordModal] = useState(true);
  const [passwordInput, setPasswordInput] = useState("");
  const [passwordError, setPasswordError] = useState("");
  const [canEdit, setCanEdit] = useState(false);
  const [isSnsUser, setIsSnsUser] = useState(false);
  const [formData, setFormData] = useState<UserMyPageResponse>({
    userId: 0,
    email: "",
    password: "",
    name: "",
    nickname: "",
    age: 0,
    github: "",
    oauthId: null,
  });

  useEffect(() => {
    setIsLoading(false);
  }, []);

  const handleVerifyPassword = async () => {
    try {
      const userInfo = await fetchApi(`/api/v1/users/me`, { method: "GET" });

      if (userInfo.data.oauthId !== null) {
        toast.error("SNS 회원은 비밀번호 인증을 사용할 수 없습니다.");
        return;
      }

      if (passwordInput.trim().length === 0) {
        setPasswordError("비밀번호를 입력해주세요.");
        return;
      }

      const res = await fetchApi(`/api/v1/users/verifyPassword`, {
        method: "POST",
        body: JSON.stringify({ password: passwordInput }),
      });

      if (res.data === true) {
        setCanEdit(true);
        setShowPasswordModal(false);
        setPasswordError("");
        setPasswordInput("");
        setFormData(userInfo.data);
      }
    } catch {
      setPasswordError("비밀번호가 일치하지 않습니다. 다시 입력해주세요.");
      setCanEdit(false);
    }
  };

  const handleSNSLoginPopup = async (provider: string) => {
    try {
      const userInfo = await fetchApi(`/api/v1/users/me`, { method: "GET" });

      if (userInfo.data.oauthId === null) {
        toast.error("기존 회원은 SNS 인증을 사용할 수 없습니다.");
        return;
      }

      const width = 600;
      const height = 700;
      const left = window.screen.width / 2 - width / 2;
      const top = window.screen.height / 2 - height / 2;

      const popup = window.open(
        `http://localhost:8080/oauth2/authorization/${provider}?mode=profile`,
        "SNS Login",
        `width=${width},height=${height},top=${top},left=${left}`
      );

      const receiveMessage = async (event: MessageEvent) => {
        if (event.origin !== "http://localhost:3000") return;

        const { oauthId, email, githubUrl } = event.data || {};
        if (!oauthId || !email || !githubUrl) return;

        const res = await fetchApi(`/api/v1/users/verifyOauthId`, {
          method: "POST",
          body: JSON.stringify({ oauthId: oauthId }),
        });

        if (res.data === true) {
          setCanEdit(true);
          setIsSnsUser(true);
          setShowPasswordModal(false);
          setPasswordError("");
          setPasswordInput("");
          setFormData({
            ...userInfo.data,
            email: email, 
            github: githubUrl
          });
          toast.success("SNS 인증이 확인되었습니다.");
        } else {
          toast.error("현재 로그인된 계정과 SNS 계정이 일치하지 않습니다.");
        }

        window.removeEventListener("message", receiveMessage);
        popup?.close();
      };

      window.addEventListener("message", receiveMessage);
    } catch (err) {
      console.error(err);
      toast.error("사용자 정보를 가져오는 중 오류가 발생했습니다.");
    }
  };

  const handleSave = async () => {
    try {
      const res = await fetchApi(`/api/v1/users/me`, {
        method: "PUT",
        body: JSON.stringify(formData),
      });
      toast.success(res.message || "개인정보가 수정되었습니다.");
      window.dispatchEvent(new Event("profileUpdated"));
    } catch (err: any) {
      console.error(err);
      toast.error(err.message || "정보 수정 중 오류가 발생했습니다.");
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh] text-gray-500">
        로딩 중...
      </div>
    );
  }

  return (
    <div className="relative">
      {/* 비밀번호 입력 모달 */}
      {showPasswordModal && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
          <div className="bg-white p-6 rounded-lg shadow-lg w-96">
            <h2 className="text-xl font-semibold mb-2">비밀번호 확인</h2>
            <p className="text-sm text-gray-600 mb-4">
              개인정보 수정을 위해 비밀번호를 입력해주세요.
            </p>
            <input
              type="password"
              placeholder="비밀번호 입력"
              className="w-full border border-gray-300 rounded-md p-2 mb-2 focus:outline-blue-500"
              value={passwordInput}
              onChange={(e) => {
                setPasswordInput(e.target.value);
                setPasswordError("");
              }}
              onKeyDown={(e) => e.key === "Enter" && handleVerifyPassword()}
            />
            {passwordError && (
              <p className="text-sm text-red-500 mb-3">{passwordError}</p>
            )}

            {/* SNS 가입 유저 안내 및 소셜 로그인 버튼 */}
            <p className="text-sm text-gray-600 mb-2 mt-2">
              SNS 로그인 유저는 SNS 계정으로 인증해주세요.
            </p>
            <div className="flex justify-center mb-4">
              <button
                type="button"
                onClick={() => handleSNSLoginPopup("github")}
                className="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded hover:bg-gray-100 hover:cursor-pointer"
              >
                GitHub로 인증하기
              </button>
            </div>

            <div className="flex justify-end gap-2">
              <button
                className="px-3 py-1 rounded-md hover:bg-gray-100"
                onClick={() => router.replace("/mypage")}
              >
                취소
              </button>
              <button
                className="px-3 py-1 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                onClick={handleVerifyPassword}
              >
                확인
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 개인정보 수정 폼 */}
      {canEdit && (
        <div className="max-w-screen-lg mx-auto px-6 py-10">
          <div className="mb-8">
            <h1 className="text-3xl font-bold mb-2">⚙️ 개인정보 수정</h1>
            <p className="text-gray-500 mb-6">
              회원 정보를 수정하고 저장하세요.
            </p>
          </div>

          <form
            className="space-y-5"
            onSubmit={(e) => {
              e.preventDefault();
              handleSave();
            }}
          >
            <div>
              <label className="block text-sm font-medium mb-1">이메일</label>
              <input
                type="email"
                className="w-full border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-400 cursor-not-allowed"
                value={formData.email}
                disabled
              />
            </div>

            {!isSnsUser && (
              <div>
                <label className="block text-sm font-medium mb-1">
                  비밀번호
                </label>
                <input
                  type="password"
                  className="w-full border border-gray-300 rounded-md p-2"
                  placeholder="새 비밀번호 입력 (선택)"
                  value={formData.password || ""}
                  onChange={(e) =>
                    setFormData({ ...formData, password: e.target.value })
                  }
                />
              </div>
            )}

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium mb-1">이름</label>
                <input
                  type="text"
                  className="w-full border border-gray-300 rounded-md p-2"
                  value={formData.name}
                  onChange={(e) =>
                    setFormData({ ...formData, name: e.target.value })
                  }
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">닉네임</label>
                <input
                  type="text"
                  className="w-full border border-gray-300 rounded-md p-2"
                  value={formData.nickname}
                  onChange={(e) =>
                    setFormData({ ...formData, nickname: e.target.value })
                  }
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">나이</label>
              <input
                type="number"
                className="w-full border border-gray-300 rounded-md p-2"
                value={formData.age}
                onChange={(e) =>
                  setFormData({ ...formData, age: Number(e.target.value) })
                }
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">
                GitHub URL
              </label>
              <input
                type="url"
                className="w-full border border-gray-300 rounded-md p-2"
                placeholder="https://github.com/username"
                value={formData.github || ""}
                onChange={(e) =>
                  setFormData({ ...formData, github: e.target.value })
                }
              />
            </div>

            <div className="flex gap-3 mt-6">
              <button
                type="submit"
                className="flex-1 bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700"
              >
                저장
              </button>
              <button
                type="button"
                onClick={() => {
                  setCanEdit(false);
                  setShowPasswordModal(true);
                }}
                className="flex-1 border border-gray-300 py-2 rounded-md hover:bg-gray-100"
              >
                취소
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
