import api from "./api";

export const alertService = {
  // Get all alerts (Admin only)
  getAllAlerts: async () => {
    const response = await api.get("/alerts");
    return response.data;
  },

  // Get alerts by product name (Admin only)
  getAlertsByProduct: async (productName) => {
    const response = await api.get(`/alerts/product/${productName}`);
    return response.data;
  },

  // Create alert (Admin only)
  createAlert: async (alertData) => {
    const response = await api.post("/alerts", alertData);
    return response.data;
  },

  // Delete alert (Admin only)
  deleteAlert: async (id) => {
    const response = await api.delete(`/alerts/${id}`);
    return response.data;
  },

  // Clear all alerts (Admin only)
  clearAllAlerts: async () => {
    const response = await api.delete("/alerts/clear");
    return response.data;
  },

  // Refresh alert messages (Admin only)
  refreshAlertMessages: async () => {
    const response = await api.post("/alerts/refresh-messages");
    return response.data;
  },
};
