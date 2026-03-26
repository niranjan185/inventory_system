import { useState, useEffect } from "react";
import {
  ExclamationTriangleIcon,
  ArrowDownTrayIcon,
  FunnelIcon,
  CalendarIcon,
  CheckCircleIcon,
  XCircleIcon,
} from "@heroicons/react/24/outline";
import { toast } from "react-hot-toast";

import DataTable from "../components/DataTable";
import LoadingSpinner from "../components/LoadingSpinner";
import StatCard from "../components/StatCard";
import Modal from "../components/Modal";
import { useAuth } from "../context/AuthContext";
import { lowStockLogService } from "../services/lowStockLogService";
import { ROLES } from "../utils/constants";
import { formatCurrency, formatDateTime, hasRole } from "../utils/helpers";

const ResolveModal = ({ log, onClose, onResolve, isLoading }) => {
  const [notes, setNotes] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    onResolve(log.productId, notes);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <h3 className="text-lg font-medium text-gray-900 mb-2">
          Resolve Low Stock: {log.productName}
        </h3>
        <div className="bg-gray-50 p-3 rounded-md text-sm text-gray-600">
          <p>Current Quantity: {log.currentQuantity}</p>
          <p>Reorder Level: {log.reorderLevel}</p>
          <p>Detected: {formatDateTime(log.detectedAt)}</p>
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">
          Resolution Notes (Optional)
        </label>
        <textarea
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500"
          rows={3}
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          placeholder="Add notes about how this was resolved..."
        />
      </div>

      <div className="flex justify-end space-x-3 pt-4">
        <button
          type="button"
          onClick={onClose}
          className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
        >
          Cancel
        </button>
        <button
          type="submit"
          disabled={isLoading}
          className="px-4 py-2 text-sm font-medium text-white bg-green-600 border border-transparent rounded-md hover:bg-green-700 disabled:opacity-50"
        >
          {isLoading ? <LoadingSpinner size="sm" /> : "Resolve"}
        </button>
      </div>
    </form>
  );
};

const LowStock = () => {
  const { user } = useAuth();
  const [logs, setLogs] = useState([]);
  const [statistics, setStatistics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [exportLoading, setExportLoading] = useState(false);
  const [resolveLoading, setResolveLoading] = useState(false);
  const [selectedLog, setSelectedLog] = useState(null);
  const [showResolveModal, setShowResolveModal] = useState(false);
  const [filters, setFilters] = useState({
    status: "all",
    resolved: "unresolved",
    category: "all",
    dateRange: "all",
  });

  const isAdmin = hasRole(user?.role, [ROLES.ADMIN]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [logsData, statsData] = await Promise.all([
        filters.resolved === "unresolved"
          ? lowStockLogService.getUnresolvedLogs()
          : lowStockLogService.getAllLogs(),
        lowStockLogService.getStatistics(),
      ]);

      setLogs(logsData);
      setStatistics(statsData);
    } catch (error) {
      console.error("Error fetching low stock data:", error);
      toast.error("Failed to load low stock data: " + error.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [filters.resolved]);

  const handleExportLogs = async () => {
    setExportLoading(true);
    try {
      await lowStockLogService.exportLowStockLogsCsv();
      toast.success("Low stock logs exported successfully!");
    } catch (error) {
      console.error("Error exporting logs:", error);
      toast.error("Failed to export logs: " + error.message);
    } finally {
      setExportLoading(false);
    }
  };

  const handleResolveLog = async (productId, notes) => {
    setResolveLoading(true);
    try {
      await lowStockLogService.resolveLowStock(productId, notes);
      toast.success("Low stock log resolved successfully!");
      setShowResolveModal(false);
      setSelectedLog(null);
      fetchData(); // Refresh data
    } catch (error) {
      console.error("Error resolving log:", error);
      toast.error("Failed to resolve log: " + error.message);
    } finally {
      setResolveLoading(false);
    }
  };

  const getSeverityColor = (severity) => {
    switch (severity) {
      case "CRITICAL":
        return "bg-red-100 text-red-800";
      case "HIGH":
        return "bg-orange-100 text-orange-800";
      case "MEDIUM":
        return "bg-yellow-100 text-yellow-800";
      case "LOW":
        return "bg-blue-100 text-blue-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case "OUT_OF_STOCK":
        return "bg-red-100 text-red-800";
      case "LOW_STOCK":
        return "bg-yellow-100 text-yellow-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const filteredLogs = logs.filter((log) => {
    if (filters.status !== "all" && log.status !== filters.status) return false;
    if (filters.category !== "all" && log.category !== filters.category)
      return false;
    return true;
  });

  const categories = [...new Set(logs.map((log) => log.category))];

  const columns = [
    {
      key: "productName",
      label: "Product",
      sortable: true,
    },
    {
      key: "category",
      label: "Category",
      sortable: true,
    },
    {
      key: "currentQuantity",
      label: "Current Qty",
      sortable: true,
      render: (value) => (
        <span className={value === 0 ? "text-red-600 font-semibold" : ""}>
          {value}
        </span>
      ),
    },
    {
      key: "reorderLevel",
      label: "Reorder Level",
      sortable: true,
    },
    {
      key: "status",
      label: "Status",
      render: (value) => (
        <span
          className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(value)}`}
        >
          {value.replace("_", " ")}
        </span>
      ),
    },
    {
      key: "severity",
      label: "Severity",
      render: (value) => (
        <span
          className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getSeverityColor(value)}`}
        >
          {value}
        </span>
      ),
    },
    {
      key: "detectedAt",
      label: "Detected",
      sortable: true,
      render: (value) => formatDateTime(value),
    },
    {
      key: "daysUnresolved",
      label: "Days",
      sortable: true,
      render: (value, log) => (
        <span className={value > 7 ? "text-red-600 font-semibold" : ""}>
          {value} {log.isResolved ? "(resolved)" : ""}
        </span>
      ),
    },
    {
      key: "totalValue",
      label: "Value",
      sortable: true,
      render: (value) => formatCurrency(value),
    },
  ];

  if (isAdmin) {
    columns.push({
      key: "actions",
      label: "Actions",
      render: (_, log) => (
        <div className="flex space-x-2">
          {!log.isResolved && (
            <button
              onClick={() => {
                setSelectedLog(log);
                setShowResolveModal(true);
              }}
              className="text-green-600 hover:text-green-900 text-sm font-medium"
            >
              Resolve
            </button>
          )}
          {log.isResolved && (
            <span className="text-green-600 text-sm">
              <CheckCircleIcon className="h-4 w-4 inline" />
            </span>
          )}
        </div>
      ),
    });
  }

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
        <h1 className="text-2xl font-bold text-gray-900">
          Low Stock Management
        </h1>
        <div className="flex items-center space-x-3">
          <button
            onClick={handleExportLogs}
            disabled={exportLoading}
            className="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50"
          >
            {exportLoading ? (
              <LoadingSpinner size="sm" />
            ) : (
              <ArrowDownTrayIcon className="h-4 w-4 mr-2" />
            )}
            Export Logs
          </button>
        </div>
      </div>

      {/* Statistics Cards */}
      {statistics && (
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-3">
          <StatCard
            title="Unresolved Issues"
            value={statistics.totalUnresolved}
            icon={ExclamationTriangleIcon}
            color="yellow"
          />
          <StatCard
            title="Critical (Out of Stock)"
            value={statistics.criticalCount}
            icon={XCircleIcon}
            color="red"
          />
          <StatCard
            title="Low Stock Items"
            value={statistics.lowStockCount}
            icon={ExclamationTriangleIcon}
            color="orange"
          />
        </div>
      )}

      {/* Filters */}
      <div className="bg-white p-4 rounded-lg shadow">
        <div className="flex items-center space-x-4">
          <FunnelIcon className="h-5 w-5 text-gray-400" />
          <div className="flex space-x-4">
            <select
              className="border border-gray-300 rounded-md px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
              value={filters.resolved}
              onChange={(e) =>
                setFilters({ ...filters, resolved: e.target.value })
              }
            >
              <option value="unresolved">Unresolved Only</option>
              <option value="all">All Logs</option>
            </select>

            <select
              className="border border-gray-300 rounded-md px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
              value={filters.status}
              onChange={(e) =>
                setFilters({ ...filters, status: e.target.value })
              }
            >
              <option value="all">All Status</option>
              <option value="OUT_OF_STOCK">Out of Stock</option>
              <option value="LOW_STOCK">Low Stock</option>
            </select>

            <select
              className="border border-gray-300 rounded-md px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
              value={filters.category}
              onChange={(e) =>
                setFilters({ ...filters, category: e.target.value })
              }
            >
              <option value="all">All Categories</option>
              {categories.map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Low Stock Logs Table */}
      <div className="bg-white shadow rounded-lg">
        <div className="px-4 py-5 sm:p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">
            Low Stock Logs ({filteredLogs.length})
          </h3>
          <DataTable
            data={filteredLogs}
            columns={columns}
            emptyMessage="No low stock logs found"
          />
        </div>
      </div>

      {/* Resolve Modal */}
      <Modal
        isOpen={showResolveModal}
        onClose={() => {
          setShowResolveModal(false);
          setSelectedLog(null);
        }}
        title="Resolve Low Stock Issue"
      >
        {selectedLog && (
          <ResolveModal
            log={selectedLog}
            onClose={() => {
              setShowResolveModal(false);
              setSelectedLog(null);
            }}
            onResolve={handleResolveLog}
            isLoading={resolveLoading}
          />
        )}
      </Modal>
    </div>
  );
};

export default LowStock;
