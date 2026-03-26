import api from "./api";

export const userService = {
  // Get all users (Admin only)
  getAllUsers: async () => {
    const response = await api.get("/admin/users");
    return response.data;
  },

  // Get user by ID (Admin only)
  getUserById: async (id) => {
    const response = await api.get(`/admin/users/${id}`);
    return response.data;
  },

  // Update user role (Admin only)
  updateUserRole: async (id, role) => {
    const response = await api.put(`/admin/users/${id}/role?role=${role}`);
    return response.data;
  },

  // Delete user (Admin only)
  deleteUser: async (id) => {
    const response = await api.delete(`/admin/users/${id}`);
    return response.data;
  },

  // Get notification preferences
  getNotificationPreferences: async () => {
    const response = await api.get("/notifications/preferences");
    return response.data;
  },

  // Update notification preferences
  updateNotificationPreferences: async (preferences) => {
    const response = await api.put("/notifications/preferences", preferences);
    return response.data;
  },

  // Test email (Admin only)
  testEmail: async () => {
    const response = await api.post("/notifications/test-email");
    return response.data;
  },
};
