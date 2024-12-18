import axios from "axios";
import axiosInstance from "./axios/axiosInstance";
import { USER_API } from "./axios/requests";
import * as UserTypes from "../types/user";

// 회원가입
export const join = async (
  email: string,
  password: string,
  username: string
) => {
  try {
    const response = await axiosInstance.post(USER_API.POST_REQUEST.join, {
      email,
      password,
      username,
    });

    // 회원가입 성공
    console.log("회원가입 성공:", response.data);
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      let errorMessage = "회원가입 과정에서 문제가 발생했습니다.";
      if (error.response.data.message === "Email already exists") {
        errorMessage = "이미 존재하는 이메일입니다. 다시 시도해주세요.";
      } else {
        errorMessage = error.response.data.message || errorMessage;
      }
    }
  }
};

// 로그인
export const login = async (email: string, password: string) => {
  try {
    const response = await axiosInstance.post(USER_API.POST_REQUEST.login, {
      email,
      password,
    });

    const data = response.data;
    if (!data.data.accessToken || !data.data.refreshToken) {
      throw new Error("토큰오류발생! 로그인박스");
    }
    console.log("로그인 성공:", data);
    return data.data;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      if (error.response) {
        // 서버 응답이 있지만, 응답 코드가 2xx가 아님
        console.error("로그인 실패:", error.response.data);
      } else if (error.request) {
        // 요청이 이루어졌으나 응답을 받지 못함
        console.error("응답 없음:", error.request);
      } else {
        // 요청 설정 중에 문제 발생
        console.error("API 호출 중 오류 발생:", error.message);
      }
    } else {
      // axios 에러가 아닌 경우
      console.error("예상치 못한 오류 발생:", error);
    }
    return null;
  }
};

// 회원 검색
export const searchUser = async (searchText: string, token: string) => {
  try {
    if (searchText) {
      const response = await axiosInstance(USER_API.GET_REQUEST.fetchUsers, {
        params: {
          search: searchText,
        },
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      console.log(response);
    }
  } catch (error) {
    console.log(error);
  }
};

// 유저의 프로필 조회하기
export const getUserProfile = async (userId: string, token: string) => {
  try {
    if (userId) {
      const response = await axiosInstance(
        USER_API.GET_REQUEST.fetchProfile(userId),
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      return response.data as UserTypes.UserProfileType;
    }
  } catch (error) {
    console.error(error);
  }
};

// * 친구 신청 api
export const requestFriend = async (token: string | null) => {
  const url = `${process.env.REACT_APP_BASE_URL}/user/api/v1/user/4/friend/2`;

  try {
    const response = await axiosInstance({
      method: "post",
      url,
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    console.log(response.data);
    return response.data as UserTypes.RequestFriendType;
  } catch (error) {
    console.log(error);
  }
};

// * 친구 신청 조회 api
export const viewFriendRequest = async (token: string) => {
  const url = `${process.env.REACT_APP_BASE_URL}/user/api/v1/user/2/requests`;
  try {
    const response = await axiosInstance({
      method: "get",
      url,
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    return response.data;
  } catch (error) {
    console.log(error);
  }
};
