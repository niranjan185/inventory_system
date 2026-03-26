import { useState, useEffect } from "react";
import { MagnifyingGlassIcon } from "@heroicons/react/24/outline";
import { toast } from "react-hot-toast";

import DataTable from "../components/DataTable";
import LoadingSpinner from "../components/LoadingSpinner";
import { auditService } from "../services/auditService";
import { AUDIT_ACTIONS } from "../utils/constants";
import { formatDateTime } from "../utils/helpers";

const AuditLogs = () => {
  const [auditLogs, setAuditLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({
    entityType: "",
    action: "",
    userEmail: "",
    startDate: "",
    endDate: "",
  });

  const fetchAuditLogs = async () => {
    try {
      setLoading(true);
      let data;

      if (filters.entityType) {
        data = await auditService.getAuditLogsByEntityType(filters.entityType);
      } else if (filters.userEmail) {
        data = await auditService.getAuditLogsByUser(filters.userEmail);
      } else if (filters.startDate && filters.endDate) {
        data = await auditService.getAuditLogsByDateRange(
          filters.startDate,
          filters.endDate,
        );
      } else {
        data = await auditService.getAllAuditLogs();
      }

      // Apply client-side filtering for action
      if (filters.action) {
        data = data.filter((log) => log.action === filters.action);
      }

      setAuditLogs(data);
    } catch (error) {
      toast.error("Failed to fetch audit logs");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAuditLogs();
  }, [filters]);

  const handleFilterChange = (key, value) => {
    setFilters((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  const clearFilters = () => {
    setFilters({
      entityType: "",
      action: "",
      userEmail: "",
      startDate: "",
      endDate: "",
    });
  };

  const columns = [
    {
      key: "timestamp",
      label: "Timestamp",
      sortable: true,
      render: (value) => formatDateTime(value),
    },
    {
      key: "action",
      label: "Action",
      sortable: true,
      render: (value) => (
        <span
          className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
            value === "CREATE"
              ? "bg-green-100 text-green-800"
              : value === "UPDATE"
                ? "bg-blue-100 text-blue-800"
                : value === "DELETE"
                  ? "bg-red-100 text-red-800"
                  : "bg-gray-100 text-gray-800"
          }`}
        >
          {value}
        </span>
      ),
    },
    {
      key: "entityType",
      label: "Entity Type",
      sortable: true,
    },
    {
      key: "entityId",
      label: "Entity ID",
      sortable: true,
    },
    {
      key: "userEmail",
      label: "User",
      sortable: true,
    },
    {
      key: "ipAddress",
      label: "IP Address",
      sortable: true,
    },
    {
      key: "changes",
      label: "Changes",
      render: (_, log) => {
        if (!log.oldValues && !log.newValues) return "-";

        return (
          <div className="text-xs">
            {log.oldValues && (
              <div className="text-red-600">
                Old:{" "}
                {JSON.stringify(JSON.parse(log.oldValues), null, 2).substring(
                  0,
                  50,
                )}
                ...
              </div>
            )}
            {log.newValues && (
              <div className="text-green-600">
                New:{" "}
                {JSON.stringify(JSON.parse(log.newValues), null, 2).substring(
                  0,
                  50,
                )}
                ...
              </div>
            )}
          </div>
        );
      },
    },
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Audit Logs</h1>
        <div className="text-sm text-gray-500">
          Total: {auditLogs.length} entries
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white p-4 rounded-lg shadow">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Entity Type
            </label>
            <select
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-primary-500 focus:border-primary-500"
              value={filters.entityType}
              onChange={(e) => handleFilterChange("entityType", e.target.value)}
            >
              <option value="">All Types</option>
              <option value="Product">Product</option>
              <option value="User">User</option>
              <option value="Alert">Alert</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Action
            </label>
            <select
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-primary-500 focus:border-primary-500"
              value={filters.action}
              onChange={(e) => handleFilterChange("action", e.target.value)}
            >
              <option value="">All Actions</option>
              {Object.values(AUDIT_ACTIONS).map((action) => (
                <option key={action} value={action}>
                  {action}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              User Email
            </label>
            <input
              type="email"
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-primary-500 focus:border-primary-500"
              placeholder="user@example.com"
              value={filters.userEmail}
              onChange={(e) => handleFilterChange("userEmail", e.target.value)}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Start Date
            </label>
            <input
              type="date"
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-primary-500 focus:border-primary-500"
              value={filters.startDate}
              onChange={(e) => handleFilterChange("startDate", e.target.value)}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              End Date
            </label>
            <input
              type="date"
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-primary-500 focus:border-primary-500"
              value={filters.endDate}
              onChange={(e) => handleFilterChange("endDate", e.target.value)}
            />
          </div>

          <div className="flex items-end">
            <button
              onClick={clearFilters}
              className="w-full px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
            >
              Clear Filters
            </button>
          </div>
        </div>
      </div>

      {/* Audit Logs Table */}
      <div className="bg-white shadow rounded-lg">
        <DataTable
          data={auditLogs}
          columns={columns}
          emptyMessage="No audit logs found"
        />
      </div>
    </div>
  );
};

export default AuditLogs;
