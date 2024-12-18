// 로그인
export type LoginFormValues = {
  email: string;
  password: string;
};

export type LoginFormValidateErrors = {
  emailError: string;
  passwordError: string;
};

// 회원가입
export type JoinFormValues = {
  nickname: string;
  email: string;
  password: string;
  passwordConfirm: string;
};

export type JoinFormValidateErrors = {
  nicknameError: string;
  emailError: string;
  passwordError: string;
  passwordConfirmError: string;
};

//  로그인 및 회원가입 유효성 폼

//  회원검색

//  유저 프로필 조회
export type UserProfileType = {
  email: string;
  userId: string;
  username: string;
};

//  회원 정보 수정

//  친구 조회, 신청, 삭제, 수락 관련 타입 * //
export type FriendsType = {
  friendId: number;
  username: string;
  email: string;
  imagePath: string;
};

export type RequestFriendType = {
  userId: string;
  friends: FriendsType[];
};
