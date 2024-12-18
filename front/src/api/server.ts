import * as ServerType from "../types/server";
import axiosInstance from "./axios/axiosInstance";
import { SERVER_API } from "./axios/requests";

export const fetchServers = async (token: string | null) => {
  try {
    const response = await axiosInstance(SERVER_API.GET_REQUEST.fetchServers, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    console.log(response.data);
    return response.data as ServerType.FetchedServerType;
  } catch (error) {
    console.error(error);
  }
};
