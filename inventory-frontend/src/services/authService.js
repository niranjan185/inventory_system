import api from "./api";

export const authService = {
  async login(credentials) {
    const response = await api.post("/auth/login", credentials);
    return response.data;
  },

  async register(userData) {
    const response = await api.post("/auth/register", userData);
    return response.data;
  },

  async adminLogin(credentials) {
    const response = await api.post("/auth/admin-login", credentials);
    return response.data;
  },

  // ==================== FORGOT PASSWORD METHODS ====================

  /**
   * Step 1: Send OTP to email
   */
  async forgotPassword(email) {
    const response = await api.post("/auth/forgot-password", { email });
    return response.data;
  },

  /**
   * Step 2: Verify OTP
   */
  async verifyOtp(email, otp) {
    const response = await api.post("/auth/verify-otp", { email, otp });
    return response.data;
  },

  /**
   * Step 3: Reset password
   */
  async resetPassword(email, otp, newPassword) {
    const response = await api.post("/auth/reset-password", {
      email,
      otp,
      newPassword,
    });
    return response.data;
  },

  /**
   * Check OTP status (optional)
   */
  async getOtpStatus(email) {
    const response = await api.get(
      `/auth/otp-status?email=${encodeURIComponent(email)}`,
    );
    return response.data;
  },

  // ==================== LEGACY PASSWORD RESET (Token-based) ====================

  async requestPasswordReset(email) {
    const response = await api.post("/password-reset/request", { email });
    return response.data;
  },

  async validateResetToken(token) {
    const response = await api.get(`/password-reset/validate/${token}`);
    return response.data;
  },

  async confirmPasswordReset(data) {
    const response = await api.post("/password-reset/confirm", data);
    return response.data;
  },

  // ==================== UTILITY METHODS ====================

  logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
  },

  getCurrentUser() {
    const userStr = localStorage.getItem("user");
    return userStr ? JSON.parse(userStr) : null;
  },

  getToken() {
    return localStorage.getItem("token");
  },

  isAuthenticated() {
    const token = this.getToken();
    const user = this.getCurrentUser();
    return !!(token && user);
  },
};
