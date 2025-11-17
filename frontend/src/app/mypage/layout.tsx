"use client";

import React from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";

export default function ProfileLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();

  const profileNavItems = [
    {
      title: "기본 정보",
      href: "/mypage",
      icon: "📊",
    },
    {
      title: "내 활동",
      href: "/mypage/activity",
      icon: "📝",
    },
    {
      title: "문제 풀이 기록",
      href: "/mypage/solved",
      icon: "💡",
    },
    {
      title: "이력서 관리",
      href: "/mypage/resume",
      icon: "📄",
    },
    {
      title: "개인정보",
      href: "/mypage/settings",
      icon: "⚙️",
    },
    {
      title: "유료 서비스",
      href: "/mypage/premium",
      icon: "💳",
    },
  ];

  return (
    <>
      <div className="max-w-screen-xl mx-auto px-6 py-10">
        <div className="mb-10">
          <h1 className="text-3xl font-bold mb-2">🧑‍💻 마이페이지</h1>
          <p className="text-gray-500">내 활동과 정보를 관리하세요</p>
        </div>

        <div className="flex flex-col md:flex-row gap-6">
          {/* 사이드 바 */}
          <aside className="w-full md:w-64 border border-gray-200 rounded-lg p-4 bg-white shadow-sm">
            <nav className="flex flex-col gap-2">
              {profileNavItems.map((item) => {
                const isActive = pathname === item.href;
                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    className={`flex items-center gap-3 px-4 py-2 rounded-md font-medium transition-colors ${
                      isActive
                        ? "bg-blue-600 text-white"
                        : "hover:bg-gray-100 text-gray-700"
                    }`}
                  >
                    <span className="text-lg">{item.icon}</span>
                    <span>{item.title}</span>
                  </Link>
                );
              })}
            </nav>
          </aside>

          <main className="flex-1 bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
            {children}
          </main>
        </div>
      </div>
    </>
  );
}
