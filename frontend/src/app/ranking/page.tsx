"use client";

import { useEffect, useState } from "react";
import { fetchApi } from "@/lib/client";
import { RankingResponse, RankingSummaryResponse } from "@/types/ranking";
import {
  tierStyles,
  tierBorderStyles,
  tierAvatarStyles,
} from "@/components/ui/tierStyle";

export default function RankingPage() {
  const [sortBy, setSortBy] = useState<"score" | "problems">("score");
  const [myRanking, setMyRanking] = useState<RankingResponse | null>(null);
  const [rankings, setRankings] = useState<RankingResponse[]>([]);

  useEffect(() => {
    async function loadRankingData() {
      try {
        const apiResponse = await fetchApi("/api/v1/rankings", {
          method: "GET",
        });
        const data = apiResponse.data as RankingSummaryResponse;
        setMyRanking(data.myRanking);
        setRankings(data.topRankings);
      } catch (error) {
        console.error("랭킹 데이터 불러오기 실패:", error);
      }
    }
    loadRankingData();
  }, []);

  if (!myRanking) {
    return (
      <div className="flex justify-center items-center h-screen text-gray-500">
        랭킹 정보를 불러오는 중입니다...
      </div>
    );
  }

  const TIER_SCORE_RANGES: Record<string, string> = {
    UNRATED: "0 ~ 299점",
    BRONZE: "300 ~ 599점",
    SILVER: "600 ~ 899점",
    GOLD: "900 ~ 1199점",
    PLATINUM: "1200 ~ 1499점",
    DIAMOND: "1500 ~ 1799점",
    RUBY: "1800 ~ 2099점",
    MASTER: "2100점 이상",
  };

  const tierOf = (tier: string) => tierStyles[tier] || tierStyles.UNRATED;

  const getRankEmoji = (rank: number) => {
    if (rank === 1) return "🥇";
    if (rank === 2) return "🥈";
    if (rank === 3) return "🥉";
    return `${rank}`;
  };

  const sortedRankings = [...rankings]
    .sort((a, b) => {
      if (sortBy === "score") {
        if (b.totalScore !== a.totalScore) return b.totalScore - a.totalScore;
        return a.nickName.localeCompare(b.nickName, "ko");
      } else {
        if (b.solvedCount !== a.solvedCount)
          return b.solvedCount - a.solvedCount;
        return a.nickName.localeCompare(b.nickName, "ko");
      }
    })
    .map((user, i) => ({ ...user, rankValue: i + 1 }));

  return (
    <div className="max-w-screen-xl mx-auto px-6 py-10">
      {/* 제목 */}
      <div className="mb-10">
        <h1 className="text-3xl font-bold mb-2">🏆 랭킹 & 티어</h1>
        <p className="text-gray-500">문제를 풀고 티어를 올려보세요!</p>
      </div>

      {/* 내 랭킹 - 티어별 화려한 스타일 적용 */}
      <div
        className={`relative rounded-2xl shadow-2xl mb-10 overflow-hidden transition-all duration-300 hover:scale-[1.02] ${
          tierBorderStyles[myRanking.currentTier] || tierBorderStyles.UNRATED
        }`}
      >
        {/* 배경 그라데이션 효과 */}
        <div
          className={`absolute inset-0 opacity-10 ${
            tierOf(myRanking.currentTier).gradient
          } ${tierOf(myRanking.currentTier).animation}`}
        ></div>

        {/* 내부 내용 */}
        <div className="bg-white rounded-xl p-6 relative z-10">
          <div className="flex justify-between items-start">
            <div className="flex items-center gap-6">
              {/* 프로필 아바타 - 티어별 테두리 */}
              <div
                className={`relative w-24 h-24 bg-gradient-to-br from-gray-100 to-gray-300 rounded-full flex items-center justify-center text-3xl font-bold transition-all duration-300 ${
                  tierAvatarStyles[myRanking.currentTier] ||
                  tierAvatarStyles.UNRATED
                }`}
              >
                <span className="relative z-10">{myRanking.nickName[0]}</span>
                {/* 티어 아이콘 배지 */}
                <div className="absolute -bottom-1 -right-1 text-3xl">
                  {tierOf(myRanking.currentTier).icon}
                </div>
              </div>

              <div>
                <h2 className="text-2xl font-bold">{myRanking.nickName}</h2>
                <p className="text-gray-500">
                  현재 랭킹: {myRanking.rankValue}위
                </p>

                {/* 티어 배지 - 더 화려하게 */}
                <div
                  className={`mt-2 inline-flex items-center gap-2 px-4 py-2 rounded-full text-base font-bold shadow-lg transition-all duration-300 hover:scale-110 ${
                    tierOf(myRanking.currentTier).gradient
                  } text-gray-600" ${tierOf(myRanking.currentTier).shadow}`}
                >
                  <span className="text-xl">
                    {tierOf(myRanking.currentTier).icon}
                  </span>
                  <span>{myRanking.currentTier}</span>
                </div>
              </div>
            </div>
          </div>

          {/* 통계 - 카드 스타일 개선 */}
          <div className="grid md:grid-cols-3 gap-4 mt-6 mb-6">
            <StatBox
              label="해결한 문제"
              value={myRanking.solvedCount}
              tier={myRanking.currentTier}
            />
            <StatBox
              label="총 점수"
              value={myRanking.totalScore}
              tier={myRanking.currentTier}
              highlight
            />
            <StatBox
              label="제출한 질문"
              value={myRanking.questionCount}
              tier={myRanking.currentTier}
            />
          </div>

          {/* 다음 티어 진행률 - 더 화려한 프로그레스바 */}
          {myRanking.nextTier && myRanking.scoreToNextTier > 0 ? (
            <>
              <div className="flex justify-between text-sm text-gray-600 mb-2">
                <span className="font-semibold">다음 티어까지</span>
                <span className="font-bold text-gray-600">
                  {myRanking.scoreToNextTier}점 남음
                </span>
              </div>

              {/* 그라데이션 프로그레스바 */}
              <div className="relative w-full bg-gray-200 rounded-full h-4 overflow-hidden shadow-inner">
                <div
                  className={`h-4 transition-all duration-500 ease-out ${
                    tierOf(myRanking.currentTier).gradient
                  } ${tierOf(myRanking.currentTier).shadow} relative`}
                  style={{
                    width: `${
                      100 -
                      (myRanking.scoreToNextTier /
                        (myRanking.totalScore + myRanking.scoreToNextTier)) *
                        100
                    }%`,
                  }}
                >
                  {/* 반짝이는 효과 */}
                  <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/30 to-transparent animate-shimmer"></div>
                </div>
              </div>

              <div className="flex justify-between items-center text-sm mt-2">
                <div className="flex items-center gap-1">
                  <span className="text-xl">
                    {tierOf(myRanking.currentTier).icon}
                  </span>
                  <span className="font-semibold">{myRanking.currentTier}</span>
                </div>
                <div className="flex items-center gap-1">
                  <span className="text-xl">
                    {tierOf(myRanking.nextTier).icon}
                  </span>
                  <span className="font-semibold">{myRanking.nextTier}</span>
                </div>
              </div>
            </>
          ) : (
            <div className="flex flex-col items-center justify-center py-4 bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg">
              <span className="text-3xl mb-2">🎉</span>
              <p className="text-blue-600 font-bold text-lg">
                최고 티어 달성 완료!
              </p>
            </div>
          )}
        </div>
      </div>

      {/* 티어 안내문 - 카드 스타일 개선 */}
      <div className="bg-white rounded-xl p-6 shadow-lg border-2 border-gray-200 mb-10 hover:shadow-xl transition-shadow duration-300">
        <div className="flex items-center gap-2 mb-4">
          <span className="text-2xl">🏆</span>
          <h3 className="text-xl font-bold">티어 시스템</h3>
        </div>
        <p className="text-sm text-gray-600 mb-6">
          점수에 따라 티어가 결정되며, 300점 단위로 상승합니다.
        </p>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {Object.entries(tierStyles).map(([tier, style]) => (
            <div
              key={tier}
              className="group relative text-center border-2 border-gray-200 rounded-xl p-4 bg-gradient-to-br from-white to-gray-50 hover:scale-105 transition-all duration-300 cursor-pointer overflow-hidden"
            >
              {/* 호버 시 배경 효과 */}
              <div
                className={`absolute inset-0 opacity-0 group-hover:opacity-20 transition-opacity duration-300 ${style.gradient}`}
              ></div>

              <div className="relative z-10">
                <div className="text-4xl mb-2">{style.icon}</div>
                <div
                  className={`inline-block px-3 py-1 rounded-full text-sm font-bold ${style.gradient} text-gray-800 shadow-md`}
                >
                  {tier}
                </div>
                <p className="text-xs text-gray-500 mt-2 font-medium">
                  {TIER_SCORE_RANGES[tier]}
                </p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 전체 랭킹 */}
      <div className="bg-white rounded-xl p-6 shadow-lg border-2 border-gray-200">
        <div className="flex justify-between items-center mb-6">
          <h3 className="text-xl font-bold">전체 랭킹</h3>

          <div className="flex border-2 border-gray-300 rounded-lg overflow-hidden shadow-sm">
            <button
              onClick={() => setSortBy("score")}
              className={`px-5 py-2 text-sm font-semibold transition-all duration-200 ${
                sortBy === "score"
                  ? "bg-blue-500 text-white shadow-md"
                  : "bg-white text-gray-700 hover:bg-gray-100"
              }`}
            >
              점수순
            </button>
            <button
              onClick={() => setSortBy("problems")}
              className={`px-5 py-2 text-sm font-semibold transition-all duration-200 ${
                sortBy === "problems"
                  ? "bg-blue-500 text-white shadow-md"
                  : "bg-white text-gray-700 hover:bg-gray-100"
              }`}
            >
              문제수순
            </button>
          </div>
        </div>

        <div className="space-y-3">
          {sortedRankings.map((user, index) => (
            <div
              key={user.userId}
              className="flex items-center gap-4 p-4 rounded-xl border-2 transition-all duration-300 hover:scale-[1.02] border-gray-200 bg-gray-50 hover:bg-gray-100 hover:border-gray-300"
            >
              <div className="w-10 flex justify-center text-2xl font-bold">
                {getRankEmoji(user.rankValue)}
              </div>

              <div className="relative w-12 h-12">
                <div
                  className={`w-12 h-12 rounded-full flex items-center justify-center font-bold text-lg shadow-md transition-all duration-300 ${
                    index < 3
                      ? "bg-white text-gray-800"
                      : "bg-gray-200 text-gray-700"
                  }`}
                >
                  {user.nickName[0]}
                </div>
                <div className="absolute -bottom-1 -right-1 text-xl">
                  {tierOf(user.currentTier).icon}
                </div>
              </div>

              <div className="flex-1">
                <p className="font-bold text-lg text-gray-800">
                  {user.nickName}
                </p>
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <span>문제 {user.solvedCount}개</span>
                  <span> / </span>
                  <span className="font-semibold">{user.totalScore}점</span>
                  <span> / </span>
                  <span>질문 {user.questionCount}개</span>
                </div>
              </div>

              <div
                className={`inline-flex items-center gap-2 px-3 py-2 rounded-full text-sm font-bold shadow-lg transition-transform hover:scale-110 ${
                  tierOf(user.currentTier).gradient
                } text-black`}
              >
                <span className="text-lg">{tierOf(user.currentTier).icon}</span>
                <span>{user.currentTier}</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

// StatBox 컴포넌트
function StatBox({
  label,
  value,
  tier,
  highlight = false,
}: {
  label: string;
  value: number;
  tier?: string;
  highlight?: boolean;
}) {
  const tierStyle = tier ? tierStyles[tier] : tierStyles.UNRATED;

  return (
    <div
      className={`rounded-xl p-5 text-center border-2 transition-all duration-300 hover:scale-105 ${
        highlight
          ? `${tierStyle.gradient} border-transparent text-black ${tierStyle.shadow}`
          : "bg-gradient-to-br from-gray-50 to-gray-100 border-gray-200 hover:border-gray-300"
      }`}
    >
      <p
        className={`text-sm font-semibold mb-1 ${
          highlight ? "text-black/90" : "text-gray-600"
        }`}
      >
        {label}
      </p>
      <p
        className={`text-4xl font-bold ${
          highlight ? "drop-shadow-md" : "text-gray-800"
        }`}
      >
        {value}
      </p>
    </div>
  );
}
