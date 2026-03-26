import api from "./api";

export const auditService = {
  // Get all audit logs (Admin only)
  getAllAuditLogs: async () => {
    const response = await api.get("/audit");
    return response.data;
  },

  // Get audit logs by entity type (Admin only)
  getAuditLogsByEntityType: async (entityType) => {
    const response = await api.get(`/audit/entity-type/${entityType}`);
    return response.data;
  },

  // Get audit logs by entity (Admin only)
  getAuditLogsByEntity: async (entityType, entityId) => {
    const response = await api.get(`/audit/entity/${entityType}/${entityId}`);
    return response.data;
  },

  // Get audit logs by user (Admin only)
  getAuditLogsByUser: async (userEmail) => {
    const response = await api.get(`/audit/user/${userEmail}`);
    return response.data;
  },

  // Get audit logs by date range (Admin only)
  getAuditLogsByDateRange: async (startDate, endDate) => {
    const response = await api.get(
      `/audit/date-range?startDate=${startDate}&endDate=${endDate}`,
    );
    return response.data;
  },

  // Get inventory movements (Admin only)
  getInventoryMovements: async () => {
    const response = await api.get("/inventory-movements");
    return response.data;
  },

  // Get inventory movements by product
  getInventoryMovementsByProduct: async (productId) => {
    const response = await api.get(`/inventory-movements/product/${productId}`);
    return response.data;
  },

  // Get inventory movements by type (Admin only)
  getInventoryMovementsByType: async (movementType) => {
    const response = await api.get(`/inventory-movements/type/${movementType}`);
    return response.data;
  },

  // Get inventory movements by user (Admin only)
  getInventoryMovementsByUser: async (userEmail) => {
    const response = await api.get(`/inventory-movements/user/${userEmail}`);
    return response.data;
  },

  // Get inventory movements by date range (Admin only)
  getInventoryMovementsByDateRange: async (startDate, endDate) => {
    const response = await api.get(
      `/inventory-movements/date-range?startDate=${startDate}&endDate=${endDate}`,
    );
    return response.data;
  },
};
