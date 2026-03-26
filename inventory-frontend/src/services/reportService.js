import api from "./api";

export const reportService = {
  // Get inventory summary report
  getInventoryReport: async () => {
    const response = await api.get("/reports/inventory");
    return response.data;
  },

  // Export products to CSV (Available to all users)
  exportProductsCSV: async () => {
    const response = await api.get("/reports/products/csv", {
      responseType: "blob",
    });

    // Create download link
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute(
      "download",
      `products-${new Date().toISOString().split("T")[0]}.csv`,
    );
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);

    return response.data;
  },

  // Export low stock products to CSV (Admin only)
  exportLowStockCSV: async () => {
    const response = await api.get("/reports/low-stock/csv", {
      responseType: "blob",
    });

    // Create download link
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute(
      "download",
      `low-stock-${new Date().toISOString().split("T")[0]}.csv`,
    );
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);

    return response.data;
  },
};
