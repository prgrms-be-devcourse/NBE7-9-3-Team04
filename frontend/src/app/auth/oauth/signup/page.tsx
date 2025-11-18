"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

import Link from "next/link";
import { fetchApi } from "@/lib/client";
import { UserOauthSignupRequest } from "@/types/user";
import { toast } from "sonner";

export default function OauthSignupPage() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);

  const [formData, setFormData] = useState<UserOauthSignupRequest>({
    email: "",
    name: "",
    nickname: "",
    age: 0,
    github: "",
    oauthId: "",
  });

  // URL 파라미터에서 값 읽어오기 및 폼 초기화
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);

    setFormData({
      oauthId: params.get("oauthId") || "",
      email: params.get("email") || "",
      name: params.get("name") || "",
      nickname: params.get("nickname") || "",
      github: params.get("githubUrl") || "",
      age: 0,
    });
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === "age" ? Number(value) : value,
    }));
  };

  const validate = (): boolean => {
    if (!formData.email || !formData.name || !formData.nickname) {
      toast.error("필수 항목을 모두 입력해주세요.");
      return false;
    }
    if (!formData.oauthId) {
      toast.error("OAuth 정보가 올바르지 않습니다.");
      return false;
    }
    return true;
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!validate()) return;

    setIsLoading(true);

    try {
      const apiResponse = await fetchApi(`/api/v1/users/oauth/signup`, {
        method: "POST",
        body: JSON.stringify(formData),
      });

      if (apiResponse.status === "OK") {
        toast.success(apiResponse.message);
        router.replace("/auth");
      } else {
        toast.error(apiResponse.message);
      }
    } catch (err: any) {
      toast.error(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-4 py-12 bg-gray-50">
      <div className="w-full max-w-md space-y-8">
        <div className="text-center">
          <h2 className="text-3xl font-bold text-gray-900">SNS 계정 만들기</h2>
          <p className="text-gray-500 mt-2">
            아래 정보를 확인하여 회원가입을 완료하세요
          </p>
        </div>

        <form
          onSubmit={handleSubmit}
          className="bg-white rounded-xl shadow-md border border-gray-200 p-8 space-y-5"
        >
          <Input
            label="이메일 *"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
            disabled
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
            <Link
              href="/auth"
              className="text-blue-600 font-medium hover:underline"
            >
              로그인
            </Link>
          </p>
        </form>
      </div>
    </div>
  );
}

function Input({
  label,
  name,
  type = "text",
  value,
  onChange,
  required,
  disabled,
  placeholder,
}: {
  label: string;
  name: string;
  type?: string;
  value: string;
  onChange: React.ChangeEventHandler<HTMLInputElement>;
  required?: boolean;
  disabled?: boolean;
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
        disabled={disabled}
        className={`w-full px-4 py-3 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
          disabled ? "bg-gray-100 cursor-not-allowed" : "border-gray-300"
        }`}
      />
    </div>
  );
}
