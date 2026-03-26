import { useState, useEffect } from "react";
import {
  DocumentArrowDownIcon,
  ChartBarIcon,
} from "@heroicons/react/24/outline";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  LineChart,
  Line,
} from "recharts";
import { toast } from "react-hot-toast";

import LoadingSpinner from "../components/LoadingSpinner";
import StatCard from "../components/StatCard";
import { useAuth } from "../context/AuthContext";
import { reportService } from "../services/reportService";
import { ROLES } from "../utils/constants";
import { formatCurrency, formatDateTime, hasRole } from "../utils/helpers";

const Reports = () => {
  const { user } = useAuth();
  const [inventoryReport, setInventoryReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [exportLoading, setExportLoading] = useState({
    products: false,
    lowStock: false,
  });

  const isAdmin = hasRole(user?.role, [ROLES.ADMIN]);

  const fetchReports = async () => {
    try {
      setLoading(true);
      const data = await reportService.getInventoryReport();
      setInventoryReport(data);
    } catch (error) {
      toast.error("Failed to fetch reports");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReports();
  }, []);

  const handleExportProducts = async () => {
    setExportLoading((prev) => ({ ...prev, products: true }));
    try {
      await reportService.exportProductsCSV();
      toast.success("Products exported successfully");
    } catch (error) {
      toast.error(error.message || "Failed to export products");
    } finally {
      setExportLoading((prev) => ({ ...prev, products: false }));
    }
  };

  const handleExportLowStock = async () => {
    setExportLoading((prev) => ({ ...prev, lowStock: true }));
    try {
      await reportService.exportLowStockCSV();
      toast.success("Low stock products exported successfully");
    } catch (error) {
      toast.error(error.message || "Failed to export low stock products");
    } finally {
      setExportLoading((prev) => ({ ...prev, lowStock: false }));
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  // Prepare chart data
  const categoryChartData =
    inventoryReport?.categorySummaries?.map((cat) => ({
      name: cat.category,
      products: cat.productCount,
      value: cat.totalValue,
    })) || [];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Reports</h1>
        {isAdmin && (
          <div className="flex space-x-3">
            <button
              onClick={handleExportProducts}
              disabled={exportLoading.products}
              className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
            >
              {exportLoading.products ? (
                <LoadingSpinner size="sm" />
              ) : (
                <DocumentArrowDownIcon className="h-4 w-4 mr-2" />
              )}
              Export Products
            </button>
            <button
              onClick={handleExportLowStock}
              disabled={exportLoading.lowStock}
              className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
            >
              {exportLoading.lowStock ? (
                <LoadingSpinner size="sm" />
              ) : (
                <DocumentArrowDownIcon className="h-4 w-4 mr-2" />
              )}
              Export Low Stock
            </button>
          </div>
        )}
      </div>

      {/* Report Summary */}
      <div className="bg-white p-6 rounded-lg shadow">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-medium text-gray-900">
            Inventory Summary
          </h2>
          <div className="text-sm text-gray-500">
            Generated: {formatDateTime(inventoryReport?.generatedAt)}
          </div>
        </div>

        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
          <StatCard
            title="Total Products"
            value={inventoryReport?.totalProducts || 0}
            icon={ChartBarIcon}
            color="blue"
          />
          <StatCard
            title="Low Stock Products"
            value={inventoryReport?.lowStockProducts || 0}
            icon={ChartBarIcon}
            color="yellow"
          />
          <StatCard
            title="Total Categories"
            value={inventoryReport?.categorySummaries?.length || 0}
            icon={ChartBarIcon}
            color="purple"
          />
          <StatCard
            title="Total Value"
            value={formatCurrency(inventoryReport?.totalInventoryValue || 0)}
            icon={ChartBarIcon}
            color="green"
          />
        </div>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Category Products Chart */}
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-lg font-medium text-gray-900 mb-4">
            Products by Category
          </h3>
          {categoryChartData.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={categoryChartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="products" fill="#3B82F6" />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-64 text-gray-500">
              No data available
            </div>
          )}
        </div>

        {/* Category Value Chart */}
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-lg font-medium text-gray-900 mb-4">
            Value by Category
          </h3>
          {categoryChartData.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={categoryChartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis
                  tickFormatter={(value) => `$${(value / 1000).toFixed(0)}K`}
                />
                <Tooltip
                  formatter={(value) => [formatCurrency(value), "Value"]}
                />
                <Bar dataKey="value" fill="#10B981" />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-64 text-gray-500">
              No data available
            </div>
          )}
        </div>
      </div>

      {/* Category Summary Table */}
      <div className="bg-white shadow rounded-lg">
        <div className="px-4 py-5 sm:p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">
            Category Summary
          </h3>
          <div className="overflow-hidden">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Category
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Product Count
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Total Value
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Average Value
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {inventoryReport?.categorySummaries?.map((category) => (
                  <tr key={category.category}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {category.category}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {category.productCount}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {formatCurrency(category.totalValue)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {formatCurrency(
                        category.totalValue / category.productCount,
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Reports;
