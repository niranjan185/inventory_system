import axios from "axios";
import { API_BASE_URL } from "../utils/constants";
import { getErrorMessage } from "../utils/helpers";

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000, // Increased to 30 seconds
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    console.error("Request interceptor error:", error);
    return Promise.reject(error);
  },
);

// Response interceptor to handle errors
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // Handle 401 Unauthorized
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      window.location.href = "/login";
      return Promise.reject(new Error("Session expired. Please login again."));
    }

    // Handle 403 Forbidden
    if (error.response?.status === 403) {
      return Promise.reject(
        new Error("You do not have permission to perform this action."),
      );
    }

    // Handle timeout errors
    if (error.code === "ECONNABORTED" || error.message.includes("timeout")) {
      return Promise.reject(
        new Error(
          "Request timed out. The server may be busy. Please try again.",
        ),
      );
    }

    // Handle network errors
    if (!error.response) {
      return Promise.reject(
        new Error(
          "Network error. Please check your connection and ensure the backend server is running.",
        ),
      );
    }

    // Return formatted error message
    const message = getErrorMessage(error);
    return Promise.reject(new Error(message));
  },
);

export default api;
