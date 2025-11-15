"use client";


import {useEffect, useState} from "react";
import { useRouter, useSearchParams } from "next/navigation";

import Link from "next/link";
import { fetchApi } from "@/lib/client";
import { UserSignupRequest } from "@/types/user";
import { toast } from "sonner";

export default function SignupPage() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);
  const [isSendingCode, setIsSendingCode] = useState(false);
  const [isCodeSent, setIsCodeSent] = useState(false);
  const [isEmailVerified, setIsEmailVerified] = useState(false);
  const [verificationCode, setVerificationCode] = useState("");
  const [verificationError, setVerificationError] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [formData, setFormData] = useState<UserSignupRequest>({
    email: "",
    password: "",
    name: "",
    nickname: "",
    age: 0,
    github: "",
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: name === "age" ? Number(value) : value,
    }));
  };

  // ✅ 이메일 인증코드 전송
  const handleSendVerificationCode = async () => {
    if (!formData.email) {
      toast.error("이메일을 입력해주세요.");
      return;
    }

    setIsSendingCode(true);
    try {
      const res = await fetchApi(`/api/v1/users/sendEmail?email=${formData.email}`, {
        method: "POST",
      });
      toast.success(res.message || "인증 코드가 이메일로 전송되었습니다.");
      setIsCodeSent(true);
    } catch (err: any) {
      toast.error(err.message || "이메일 전송에 실패했습니다.");
    } finally {
      setIsSendingCode(false);
    }
  };

  // ✅ 인증코드 검증
  const handleVerifyCode = async () => {
    if (!verificationCode) {
      toast.error("인증 코드를 입력해주세요.");
      return;
    }

    try {
      const res = await fetchApi(
        `/api/v1/users/verifyCode?email=${formData.email}&code=${verificationCode}`,
        { method: "POST" }
      );
      toast.success(res.message || "이메일 인증이 완료되었습니다.");
      setIsEmailVerified(true);
    } catch (err: any) {
      toast.error(err.message || "인증 코드가 올바르지 않습니다.");
    }
  };

  // ✅ 입력값 검증
  const validate = (): boolean => {
    if (!isEmailVerified) {
      toast.error("이메일 인증을 완료해주세요.");
      return false;
    }
    if (!formData.email || !formData.password || !formData.name) {
      toast.error("필수 항목을 모두 입력해주세요.");
      return false;
    }
    if (formData.password !== confirmPassword) {
      toast.error("비밀번호가 일치하지 않습니다.");
      return false;
    }
    if (formData.password.length < 6) {
      toast.error("비밀번호는 최소 6자 이상이어야 합니다.");
      return false;
    }
    return true;
  };

  // ✅ 회원가입 요청
  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!validate()) return;

    setIsLoading(true);

    try {
      const apiResponse = await fetchApi(`/api/v1/users/signup`, {
        method: "POST",
        body: JSON.stringify(formData),
      });

      if (apiResponse.status === "OK") {
        toast.success(apiResponse.message);
        router.replace("/auth");
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
    <>
      <div className="min-h-screen flex items-center justify-center px-4 py-12 bg-gray-50">
        <div className="w-full max-w-md space-y-8">
          <div className="text-center">
            <h2 className="text-3xl font-bold text-gray-900">계정 만들기</h2>
            <p className="text-gray-500 mt-2">
              아래 정보를 입력하여 회원가입을 완료하세요
            </p>
          </div>

          <form
            onSubmit={handleSubmit}
            className="bg-white rounded-xl shadow-md border border-gray-200 p-8 space-y-5"
          >
            {/* 이메일 + 인증 버튼 */}
            <div className="space-y-2">
              <label htmlFor="email" className="text-sm font-semibold block text-gray-800">
                이메일 *
              </label>
              <div className="flex gap-2">
                <input
                  id="email"
                  name="email"
                  type="email"
                  placeholder="example@email.com"
                  value={formData.email}
                  onChange={handleChange}
                  required
                  disabled={isEmailVerified}
                  className={`flex-1 px-4 py-3 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition ${
                    isEmailVerified ? "border-green-500 bg-green-50" : "border-gray-300"
                  }`}
                />
                <button
                  type="button"
                  onClick={handleSendVerificationCode}
                  disabled={isSendingCode || isEmailVerified}
                  className={`px-5 py-3 text-sm font-semibold rounded-lg text-white transition whitespace-nowrap ${
                    isEmailVerified
                      ? "bg-green-600 cursor-not-allowed"
                      : "bg-blue-600 hover:bg-blue-700"
                  }`}
                >
                  {isEmailVerified
                    ? "인증완료"
                    : isSendingCode
                    ? "전송 중..."
                    : "인증코드 전송"}
                </button>
              </div>
            </div>

            {/* 인증코드 입력 */}
            {isCodeSent && !isEmailVerified && (
              <div className="space-y-2">
                <label htmlFor="verificationCode" className="text-sm font-semibold block text-gray-800">
                  인증 코드 *
                </label>
                <div className="flex gap-2">
                  <input
                    id="verificationCode"
                    type="text"
                    value={verificationCode}
                    onChange={(e) => setVerificationCode(e.target.value)}
                    placeholder="6자리 인증 코드"
                    maxLength={6}
                    className="flex-1 px-4 py-3 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                  <button
                    type="button"
                    onClick={handleVerifyCode}
                    className="px-5 py-3 text-sm font-semibold rounded-lg text-white bg-gray-700 hover:bg-gray-800 whitespace-nowrap"
                  >
                    인증 확인
                  </button>
                </div>
                {verificationError && (
                  <p className="text-sm text-red-500 flex items-center mt-1">
                    ⚠️ {verificationError}
                  </p>
                )}
              </div>
            )}

            <Input
              label="비밀번호 *"
              name="password"
              type="password"
              placeholder="최소 6자 이상"
              value={formData.password}
              onChange={handleChange}
              required
            />
            <Input
              label="비밀번호 확인 *"
              name="confirmPassword"
              type="password"
              placeholder="비밀번호를 다시 입력하세요"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
            <Input
              label="이름 *"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
            />
            <Input
              label="닉네임 *"
              name="nickname"
              value={formData.nickname}
              onChange={handleChange}
              required
            />
            <Input
              label="나이 *"
              name="age"
              type="number"
              value={formData.age.toString()}
              onChange={handleChange}
              required
            />
            <Input
              label="깃허브 링크"
              name="github"
              placeholder="https://github.com/username"
              value={formData.github}
              onChange={handleChange}
            />

            <button
              type="submit"
              disabled={isLoading}
              className={`w-full py-3 font-semibold rounded-lg bg-blue-600 text-white hover:bg-blue-700 transition ${
                isLoading ? "opacity-50 cursor-not-allowed" : ""
              }`}
            >
              {isLoading ? "가입 중..." : "회원가입"}
            </button>

            <p className="text-sm text-center text-gray-500 mt-3">
              이미 계정이 있으신가요?{" "}
              <Link href="/auth" className="text-blue-600 font-medium hover:underline">
                로그인
              </Link>
            </p>
          </form>
        </div>
      </div>
    </>
  );
}

// ✅ 내부 Input 컴포넌트
function Input({
  label,
  name,
  type = "text",
  value,
  onChange,
  required,
  placeholder,
}: {
  label: string;
  name: string;
  type?: string;
  value: string;
  onChange: React.ChangeEventHandler<HTMLInputElement>;
  required?: boolean;
  placeholder?: string;
}) {
  return (
    <div className="space-y-2">
      <label htmlFor={name} className="text-sm font-semibold block text-gray-800">
        {label}
      </label>
      <input
        id={name}
        name={name}
        type={type}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        required={required}
        className="w-full px-4 py-3 border rounded-lg border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
    </div>
  );
}