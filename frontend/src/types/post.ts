// post.ts

// 공통 타입
export type PostStatus = "ING" | "CLOSED";
export type PinStatus = "PINNED" | "NOT_PINNED";
export type PostCategoryType = "PROJECT" | "STUDY";

// 요청 DTO

// 게시글 생성
export type PostAddRequest = {
  title: string; // 2~255자
  content: string; // 최소 10자
  introduction: string; // 최소 10자
  deadline: string; // ISO 문자열
  status: PostStatus;
  pinStatus: PinStatus;
  recruitCount: number; // 최소 1
  categoryType: PostCategoryType;
};

// 게시글 수정
export type PostUpdateRequest = {
  title: string;
  introduction: string;
  content: string;
  deadline: string;
  status: PostStatus;
  pinStatus: PinStatus;
  recruitCount: number;
  categoryType: PostCategoryType;
};

// 게시글 관리자: 상단 고정 변경
export type AdminPostPinRequest = {
  pinStatus: PinStatus;
};

// 게시글 관리자: 상태 변경
export type AdminPostStatusRequest = {
  status: PostStatus;
};

// 응답 DTO

export type PostResponse = {
  postId?: number;
  id?: number;
  title: string;
  introduction: string;
  content: string;
  deadline: string; // ISO 문자열
  createDate: string; // ISO 문자열
  modifyDate: string; // ISO 문자열
  status: PostStatus;
  pinStatus: PinStatus;
  recruitCount: number;
  nickName: string;
  categoryType: PostCategoryType;
  isMine: boolean | null;
};

// 페이징 응답
export type PostPageResponse<T = PostResponse> = {
  posts: T[];
  currentPage: number;
  totalPages: number;
  totalCount: number;
  pageSize: number;
};
