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
      title: "사용자 관리",
      href: "/admin",
      icon: "🧑‍💻",
    },
    {
      title: "질문 관리",
      href: "/admin/questions",
      icon: "📖",
    },
    {
      title: "모집글 관리",
      href: "/admin/recruitments",
      icon: "👥",
    },
    {
      title: "QnA 관리",
      href: "/admin/qna",
      icon: "💬",
    },
    {
      title: "결제 관리",
      href: "/admin/premiums",
      icon: "💰",
    }
  ];

  return (
    <>
      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold mb-2">🖥️ 관리자 대시보드</h1>
          <p className="text-gray-500">DevStation 플랫폼 관리</p>
        </div>

        <div className="flex flex-col md:flex-row gap-6">
          {/* 사이드 바 */}
          <aside className="w-full md:w-64 rounded-lg p-4 bg-white ">
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
