const AUTH_URL = "/auth/api/v1";
const USER_URL = "/user/api/v1";
const SERVER_URL = "/server/api/v1";

export const AUTH_API = {
  POST_REQUEST: {
    accessToken: `${AUTH_URL}/access_token`,
    token: `${AUTH_URL}/token`,
    validation: `${AUTH_URL}/validation`,
  },
  DELETE_REQUEST: {
    deleteToken: (jwtToken: string) => `${AUTH_URL}/token/${jwtToken}`,
  },
};

export const USER_API = {
  GET_REQUEST: {
    fetchUsers: `${USER_URL}/user`,
    fetchProfile: (userId: string) => `${USER_URL}/user/${userId}`,
    fetchFriends: (userId: string) => `${USER_URL}/user/${userId}/friends`,
    fetchFriendRequests: (userId: string) =>
      `${USER_URL}/user/${userId}/requests`,
  },
  POST_REQUEST: {
    login: `${USER_URL}/login`,
    logout: `${USER_URL}/logout`,
    join: `${USER_URL}/user`,
    requestFriend: (userId: string, friendId: string) =>
      `${USER_URL}/user/${userId}/friend/${friendId}`,
  },
  PATCH_REQUEST: {
    editProfile: (userId: string) => `${USER_URL}/user/${userId}`,
    acceptFriendRequest: (userId: string, friendId: string) =>
      `${USER_URL}/user/${userId}/friend/${friendId}`,
  },

  DELETE_REQUEST: {
    deleteAccount: (userId: string) => `${USER_URL}/user/${userId}`,
    deleteFriend: (userId: string, friendId: string) =>
      `${USER_URL}/user/${userId}/friend/${friendId}`,
  },
};

export const SERVER_API = {
  GET_REQUEST: {
    fetchServerCategories: `${SERVER_URL}/category`,
    fetchServers: `${SERVER_URL}/server`,
    fetchSingleServer: (serverId: string) => `${SERVER_URL}/server/${serverId}`,
  },
  POST_REQUEST: {
    createServer: `${SERVER_URL}/server`,
  },
  PUT_REQUEST: {
    joinInServer: (serverId: string) => `${SERVER_URL}/server/${serverId}/user`,
    inviteToServer: (serverId: string, userId: string) =>
      `${SERVER_URL}/server/${serverId}/invitation/${userId}`,
  },
  PATCH_REQUEST: {
    inheritHostRole: (serverId: string) => `${SERVER_URL}/server/${serverId}`,
  },
  DELETE_REQUEST: {
    deleteServer: (serverId: string) => `${SERVER_URL}/server/${serverId}`,
    leaveServer: (serverId: string) => `${SERVER_URL}/server/${serverId}/exit`,
  },
};
