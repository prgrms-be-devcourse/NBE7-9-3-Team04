"use client";
import { fetchApi } from "@/lib/client";  
import { ResumeCreateRequest } from "@/types/resume";
import { useState, useEffect } from "react";
import { toast } from "sonner";

export default function MyResumePage() {
  const [isLoading, setIsLoading] = useState(true);

  const [resumeData, setResumeData] = useState<ResumeCreateRequest | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  //   const [activities, setActivities] = useState<any[]>([]);
  //   const [certifications, setCertifications] = useState<any[]>([]);
  //   const [experiences, setExperiences] = useState<any[]>([]);
  // ✅ 이력서 조회
  const fetchResume = async () => {
    try{
      const res = await fetchApi("/api/v1/users/resumes", { method: "GET" });
    
      if (!res || !res.data) {
        setIsEditing(false);
        return;
      }
      setIsEditing(true);
      setResumeData(res.data);
    } catch{
      return;
    }
    
     
  };

  // ✅ 이력서 등록 (POST)
  const createResume = async () => {
    if (!resumeData) return;
    try {
      const res = await fetchApi("/api/v1/users/resumes", {
        method: "POST",
        body: JSON.stringify(resumeData),
      });
      toast.success("이력서가 성공적으로 등록되었습니다.");
      setResumeData(res.data);
      setIsEditing(true);
    } catch (error) {
      console.error("이력서 등록 실패:", error);
      toast.error("이력서 등록 중 오류가 발생했습니다.");
    }
  };

  // ✅ 이력서 수정 (PUT)
  const updateResume = async () => {
    try {
      const res = await fetchApi(`/api/v1/users/resumes`, {
        method: "PUT",
        body: JSON.stringify(resumeData),
      });
      toast.success("이력서가 성공적으로 수정되었습니다.");
    } catch (error) {
      console.error("이력서 수정 실패:", error);
      toast.error("이력서 수정 중 오류가 발생했습니다.");
    }
  };



  const handleSave = () => {
    if (isEditing) updateResume();
    else createResume();
  };
  useEffect(() => {
    fetchResume();
  }, []);
  // ✅ 임시 유저 가정
  useEffect(() => {
    const timer = setTimeout(() => setIsLoading(false), 200);
    return () => clearTimeout(timer);
  }, []);

  return (
    <>
      <div className="max-w-screen-lg mx-auto px-6 py-10">
        <div className="mb-8">
          <h1 className="text-3xl font-bold mb-2">💼 이력서 관리</h1>
          <p className="text-gray-500 mb-6">
          {isEditing ? "이력서를 수정하세요." : "새로운 이력서를 작성하세요."}
          </p>
        </div>

        <div className="mb-6">
          <label className="block font-semibold mb-1">이력서 내용</label>
          <textarea
            className="w-full border border-gray-300 rounded-md p-3 focus:outline-blue-500"
            placeholder="자기소개 및 경력 요약을 작성하세요"
            rows={6}
            value={resumeData?.content || ""}
            onChange={(e) =>
              setResumeData({ ...resumeData, content: e.target.value } as ResumeCreateRequest)
            }
          />
        </div>

        <div className="mb-6">
          <label className="block font-semibold mb-1">기술 스택</label>
          <textarea
            className="w-full border border-gray-300 rounded-md p-3 focus:outline-blue-500"
            placeholder="예: React, Next.js, TypeScript, Node.js, PostgreSQL"
            rows={3}
            value={resumeData?.skill || ""}
            onChange={(e) =>
              setResumeData({ ...resumeData, skill: e.target.value } as ResumeCreateRequest)
            }
          />
        </div>

        <div className="mb-6">
          <label className="block font-semibold mb-1">대외 활동</label>
          <textarea
            className="w-full border border-gray-300 rounded-md p-3 focus:outline-blue-500"
            placeholder="예: 해커톤 대회 1등"
            rows={3}
            value={resumeData?.activity || ""}
            onChange={(e) =>
              setResumeData({ ...resumeData, activity: e.target.value } as ResumeCreateRequest)
            }
          />
        </div>

        <div className="mb-6">
          <label className="block font-semibold mb-1">자격증</label>
          <textarea
            className="w-full border border-gray-300 rounded-md p-3 focus:outline-blue-500"
            placeholder="예: 정보처리기사"
            rows={3}
            value={resumeData?.certification || ""}
            onChange={(e) =>
              setResumeData({ ...resumeData, certification: e.target.value } as ResumeCreateRequest)
            }
          />
        </div>

        <div className="mb-6">
          <label className="block font-semibold mb-1">경력 사항</label>
          <textarea
            className="w-full border border-gray-300 rounded-md p-3 focus:outline-blue-500"
            placeholder="예: 데브 회사 2년차"
            rows={3}
            value={resumeData?.career || ""}
            onChange={(e) =>
              setResumeData({ ...resumeData, career: e.target.value } as ResumeCreateRequest)
            }
          />
        </div>

        <div className="mb-6">
          <label className="block font-semibold mb-1">포트폴리오 URL</label>
          <div className="flex items-center gap-2">
            <span className="text-xl">💻</span>
            <input
              type="text"
              className="flex-1 border border-gray-300 rounded-md p-2"
              placeholder="https://github.com/username 또는 포트폴리오 사이트"
              value={resumeData?.portfolioUrl || ""}
              onChange={(e) =>
                setResumeData({ ...resumeData, portfolioUrl: e.target.value } as ResumeCreateRequest)
              }
            />
          </div>
        </div>

        <button 
          onClick={handleSave}
          className="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 font-semibold">
            {isEditing ? "이력서 수정" : "이력서 등록"}
          
        </button>
      </div>
    </>
  );
}

/* 이후 활동 폼 추가 시 필요요
function Section({
    title,
    type,
    list,
    handleAdd,
    handleUpdate,
    handleRemove,
  }: {
    title: string;
    type: string;
    list: any[];
    handleAdd: Function;
    handleUpdate: Function;
    handleRemove: Function;
  }) {
    const fieldMap: Record<string, any> = {
      activity: [
        { label: "날짜", field: "date", type: "date" },
        { label: "활동명 (대회 이름, 수상 내역)", field: "name", type: "text", placeholder: "예: 해커톤 대회 1등" },
        { label: "활동 정보", field: "description", type: "textarea", placeholder: "활동 내용이나 수상 내역을 작성하세요" },
      ],
      certification: [
        { label: "취득 날짜", field: "date", type: "date" },
        { label: "자격증명", field: "name", type: "text", placeholder: "예: 정보처리기사" },
      ],
      experience: [
        { label: "회사명", field: "company", type: "text", placeholder: "예: ABC 주식회사" },
        { label: "직무", field: "position", type: "text", placeholder: "예: 프론트엔드 개발자" },
        { label: "재직 시작일", field: "startDate", type: "date" },
        { label: "재직 종료일", field: "endDate", type: "date" },
      ],
    };
  
    const fields = fieldMap[type];
  
    return (
      <div className="mb-8">
        <div className="flex justify-between items-center mb-3">
          <label className="font-semibold">{title}</label>
          <button
            onClick={() => handleAdd(type)}
            className="px-3 py-1 text-sm bg-gray-100 rounded-md hover:bg-gray-200"
          >
            추가
          </button>
        </div>
  
        {list.length === 0 ? (
          <p className="text-sm text-gray-500">{title}을(를) 추가해주세요</p>
        ) : (
          <div className="space-y-4">
            {list.map((item) => (
              <div
                key={item.id}
                className="border border-gray-200 rounded-lg p-4 bg-gray-50 space-y-3"
              >
                <div className="flex justify-end">
                  <button
                    onClick={() => handleRemove(type, item.id)}
                    className="px-3 py-1 text-sm bg-gray-100 rounded-md hover:bg-gray-200"
                  >
                    삭제
                  </button>
                </div>
  
                <div className="grid md:grid-cols-2 gap-3">
                  {fields.map((f: any, index: number) => (
                    <div key={index}>
                      <label className="block text-sm font-medium mb-1">{f.label}</label>
                      {f.type === "textarea" ? (
                        <textarea
                          rows={2}
                          className="w-full border border-gray-300 rounded-md p-2"
                          placeholder={f.placeholder}
                          value={item[f.field] || ""}
                          onChange={(e) => handleUpdate(type, item.id, f.field, e.target.value)}
                        />
                      ) : (
                        <input
                          type={f.type}
                          className="w-full border border-gray-300 rounded-md p-2"
                          placeholder={f.placeholder}
                          value={item[f.field] || ""}
                          onChange={(e) => handleUpdate(type, item.id, f.field, e.target.value)}
                        />
                      )}
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    );
  }

  */
