import axios, {
  AxiosError,
  AxiosResponse,
  InternalAxiosRequestConfig,
} from "axios";
import { NavigateFunction } from "react-router-dom";
import { refreshAccessToken, validateAccessToken } from "../token";
import { LoginState } from "../../store/useLoginStore";

interface RetryConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

// Axios 인터셉터 설정 함수
const interceptorSetup = (store: LoginState, navigate: NavigateFunction) => {
  // 요청 인터셉터 설정
  axios.interceptors.request.use(
    async (config: RetryConfig) => {
      const token = store.accessToken;
      console.log("요청 인터셉터의 Token :", token);

      if (token) {
        // 액세스 토큰 유효성 검사
        const isTokenValid = await validateAccessToken(token);
        console.log("유효한 토큰 : ", isTokenValid);

        if (!isTokenValid) {
          console.log("토큰이 유효하지 않습니다. 새로운 토큰 발급 시도");
          // 액세스 토큰이 유효하지 않으면 리프레시 토큰을 사용해 새로운 액세스 토큰 발급
          const newTokens = await refreshAccessToken(store.refreshToken);
          if (newTokens) {
            // 새로운 토큰을 상태와 로컬 스토리지에 저장
            store.setTokens(newTokens.accessToken, newTokens.refreshToken);
            // 요청 헤더에 새로운 액세스 토큰 추가
            config.headers.Authorization = `Bearer ${newTokens.accessToken}`;
            console.log("새로운 토큰:", newTokens);
          } else {
            // 새로운 토큰 발급 실패 시 로그인 페이지로 이동
            console.error(
              "리프레시 토큰을 이용한 발급이 실패, 로그인 페이지로 이동"
            );
            store.clearTokens();
            navigate("/login");
            return Promise.reject(new Error("토큰이 만료되었습니다."));
          }
        } else {
          // 액세스 토큰이 유효하면 요청 헤더에 추가
          config.headers.Authorization = `Bearer ${token}`;
        }
      }
      return config;
    },
    (error: AxiosError) => {
      // 요청 에러 처리
      console.error("Request Error:", error);
      return Promise.reject(error);
    }
  );
  // 응답 인터셉터 설정
  axios.interceptors.response.use(
    (response: AxiosResponse) => {
      console.log("응답 인터셉터의 response:", response);
      return response;
    },
    async (error: AxiosError) => {
      const originalRequest = error.config as RetryConfig;
      if (
        error.response &&
        // 인증 오류 (401) 처리
        error.response.status === 401 &&
        originalRequest &&
        // 요청이 재시도되지 않았는지 확인
        !originalRequest._retry
      ) {
        originalRequest._retry = true;
        console.log(
          "응답 인터셉터 401 오류, 리프레시 토큰을 토큰을 사용해 새로운 액세스 토큰 발급 "
        );
        // 리프레시 토큰을 사용해 새로운 액세스 토큰 발급
        const newTokens = await refreshAccessToken(store.refreshToken);
        if (newTokens) {
          // 새로운 토큰을 상태와 로컬 스토리지에 저장
          store.setTokens(newTokens.accessToken, newTokens.refreshToken);
          // 원래 요청 헤더에 새로운 액세스 토큰 추가
          originalRequest.headers.Authorization = `Bearer ${newTokens.accessToken}`;
          console.log("401 오류 이후의 새로운 토큰 :", newTokens);
          // 원래 요청을 재시도
          return axios(originalRequest);
        } else {
          // 새로운 토큰 발급 실패 시 로그인 페이지로 이동
          console.error(
            "401 오류 이후 토큰을 새로운 토큰 발급 실패, 로그인페이지로 이동"
          );
          store.clearTokens();
          navigate("/login");
          return Promise.reject(new Error("토큰이 만료되었습니다."));
        }
      }
      // 그 외의 응답 에러 처리
      console.error("응답 오류:", error);
      return Promise.reject(error);
    }
  );
};

export default interceptorSetup;
