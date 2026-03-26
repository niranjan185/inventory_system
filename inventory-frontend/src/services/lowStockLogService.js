import api from "./api";

export const lowStockLogService = {
  // Get all unresolved low stock logs
  getUnresolvedLogs: async () => {
    const response = await api.get("/low-stock-logs/unresolved");
    return response.data;
  },

  // Get all low stock logs
  getAllLogs: async () => {
    const response = await api.get("/low-stock-logs");
    return response.data;
  },

  // Get logs by product ID
  getLogsByProduct: async (productId) => {
    const response = await api.get(`/low-stock-logs/product/${productId}`);
    return response.data;
  },

  // Get logs by date range
  getLogsByDateRange: async (startDate, endDate) => {
    const response = await api.get("/low-stock-logs/date-range", {
      params: {
        startDate: startDate.toISOString(),
        endDate: endDate.toISOString(),
      },
    });
    return response.data;
  },

  // Get logs by status
  getLogsByStatus: async (status) => {
    const response = await api.get(`/low-stock-logs/status/${status}`);
    return response.data;
  },

  // Get logs by category
  getLogsByCategory: async (category) => {
    const response = await api.get(`/low-stock-logs/category/${category}`);
    return response.data;
  },

  // Get recent logs (last 30 days)
  getRecentLogs: async () => {
    const response = await api.get("/low-stock-logs/recent");
    return response.data;
  },

  // Get low stock statistics
  getStatistics: async () => {
    const response = await api.get("/low-stock-logs/statistics");
    return response.data;
  },

  // Resolve low stock log (Admin only)
  resolveLowStock: async (productId, notes = "") => {
    const response = await api.put(
      `/low-stock-logs/resolve/${productId}`,
      null,
      {
        params: { notes },
      },
    );
    return response.data;
  },

  // Export low stock logs to CSV
  exportLowStockLogsCsv: async () => {
    const response = await api.get("/low-stock-logs/export/csv", {
      responseType: "blob",
    });

    // Create download link
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute(
      "download",
      `low-stock-logs-${new Date().toISOString().split("T")[0]}.csv`,
    );
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);

    return response.data;
  },
};
