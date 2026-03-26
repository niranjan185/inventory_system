import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
  CubeIcon,
  ExclamationTriangleIcon,
  CurrencyDollarIcon,
  TagIcon,
  ArrowDownTrayIcon,
} from "@heroicons/react/24/outline";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from "recharts";
import { toast } from "react-hot-toast";

import StatCard from "../components/StatCard";
import LoadingSpinner from "../components/LoadingSpinner";
import { useAuth } from "../context/AuthContext";
import { reportService } from "../services/reportService";
import { productService } from "../services/productService";
import { alertService } from "../services/alertService";
import { DASHBOARD_REFRESH_INTERVAL, ROLES } from "../utils/constants";
import { formatCurrency } from "../utils/helpers";

const COLORS = ["#0088FE", "#00C49F", "#FFBB28", "#FF8042", "#8884D8"];

const Dashboard = () => {
  const { user } = useAuth();
  const [dashboardData, setDashboardData] = useState({
    inventoryReport: null,
    lowStockProducts: [],
    alerts: [],
    loading: true,
  });
  const [exportLoading, setExportLoading] = useState({
    products: false,
    lowStock: false,
  });

  const fetchDashboardData = async () => {
    try {
      console.log("Fetching dashboard data...");
      console.log("User role:", user?.role);
      console.log("Is authenticated:", user && localStorage.getItem("token"));

      // Check if user is authenticated before making API calls
      if (!user || !localStorage.getItem("token")) {
        console.log("User not authenticated, skipping dashboard data fetch");
        setDashboardData((prev) => ({ ...prev, loading: false }));
        return;
      }

      const promises = [
        reportService.getInventoryReport().catch((error) => {
          console.log("Inventory report error:", error.message);
          // Return empty report structure if access denied
          return {
            totalProducts: 0,
            totalInventoryValue: 0,
            categorySummaries: [],
          };
        }),
        // Only fetch low stock products for admin users
        user?.role === ROLES.ADMIN
          ? productService.getLowStockProducts().catch((error) => {
              console.log("Low stock products error:", error.message);
              return [];
            })
          : Promise.resolve([]),
        // Fetch alerts for both admin and user roles
        alertService.getAllAlerts().catch((error) => {
          console.log("Alerts error:", error.message);
          return [];
        }),
      ];

      console.log("Executing API calls...");
      const [inventoryReport, lowStockProducts, alerts] =
        await Promise.all(promises);

      console.log("API responses received:", {
        inventoryReport,
        lowStockProducts,
        alerts,
      });

      setDashboardData({
        inventoryReport,
        lowStockProducts,
        alerts,
        loading: false,
      });
    } catch (error) {
      console.error("Error fetching dashboard data:", error);

      // Handle authentication errors gracefully
      if (
        error.message.includes("permission") ||
        error.message.includes("403")
      ) {
        console.log("Permission error detected, user might need to re-login");
        toast.error("Session may have expired. Please try logging in again.");
      } else {
        toast.error("Failed to load dashboard data: " + error.message);
      }

      setDashboardData((prev) => ({
        ...prev,
        loading: false,
        inventoryReport: prev.inventoryReport || {
          totalProducts: 0,
          totalInventoryValue: 0,
          categorySummaries: [],
        },
      }));
    }
  };

  const handleExportProducts = async () => {
    setExportLoading((prev) => ({ ...prev, products: true }));
    try {
      await reportService.exportProductsCSV();
      toast.success("Products exported successfully!");
    } catch (error) {
      console.error("Error exporting products:", error);
      toast.error("Failed to export products: " + error.message);
    } finally {
      setExportLoading((prev) => ({ ...prev, products: false }));
    }
  };

  const handleExportLowStock = async () => {
    setExportLoading((prev) => ({ ...prev, lowStock: true }));
    try {
      await reportService.exportLowStockCSV();
      toast.success("Low stock products exported successfully!");
    } catch (error) {
      console.error("Error exporting low stock products:", error);
      toast.error("Failed to export low stock products: " + error.message);
    } finally {
      setExportLoading((prev) => ({ ...prev, lowStock: false }));
    }
  };

  useEffect(() => {
    // Only fetch data if user is authenticated
    if (user && localStorage.getItem("token")) {
      fetchDashboardData();

      // Set up auto-refresh
      const interval = setInterval(
        fetchDashboardData,
        DASHBOARD_REFRESH_INTERVAL,
      );
      return () => clearInterval(interval);
    } else {
      // If not authenticated, stop loading
      setDashboardData((prev) => ({ ...prev, loading: false }));
    }
  }, [user?.role, user?.email]);

  if (dashboardData.loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  // Check if user is authenticated
  if (!user || !localStorage.getItem("token")) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-center">
          <h2 className="text-xl font-semibold text-gray-900 mb-2">
            Authentication Required
          </h2>
          <p className="text-gray-600">Please log in to view the dashboard.</p>
        </div>
      </div>
    );
  }

  const { inventoryReport, lowStockProducts, alerts } = dashboardData;

  // Prepare chart data
  const categoryChartData =
    inventoryReport?.categorySummaries?.map((cat) => ({
      name: cat.category,
      value: cat.totalValue,
      count: cat.productCount,
    })) || [];

  const stockStatusData = [
    {
      name: "In Stock",
      value:
        (inventoryReport?.totalProducts || 0) - (lowStockProducts?.length || 0),
      color: "#10B981",
    },
    {
      name: "Low Stock",
      value: lowStockProducts?.length || 0,
      color: "#F59E0B",
    },
  ];

  // Only show stock status chart for admin users who have low stock data
  const showStockStatusChart = user?.role === ROLES.ADMIN;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <div className="flex items-center space-x-4">
          <div className="text-sm text-gray-500">
            Last updated: {new Date().toLocaleTimeString()}
          </div>

          {/* Export Buttons */}
          <div className="flex space-x-2">
            <button
              onClick={handleExportProducts}
              disabled={exportLoading.products}
              className="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50"
            >
              {exportLoading.products ? (
                <LoadingSpinner size="sm" />
              ) : (
                <ArrowDownTrayIcon className="h-4 w-4 mr-2" />
              )}
              Export Products
            </button>

            {user?.role === ROLES.ADMIN && (
              <button
                onClick={handleExportLowStock}
                disabled={exportLoading.lowStock}
                className="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50"
              >
                {exportLoading.lowStock ? (
                  <LoadingSpinner size="sm" />
                ) : (
                  <ArrowDownTrayIcon className="h-4 w-4 mr-2" />
                )}
                Export Low Stock
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          title="Total Products"
          value={inventoryReport?.totalProducts || 0}
          icon={CubeIcon}
          color="blue"
        />
        {user?.role === ROLES.ADMIN && (
          <StatCard
            title="Low Stock Items"
            value={lowStockProducts?.length || 0}
            icon={ExclamationTriangleIcon}
            color="yellow"
            link="/products?filter=low-stock"
          />
        )}
        <StatCard
          title="Active Alerts"
          value={alerts?.length || 0}
          icon={ExclamationTriangleIcon}
          color="red"
          link={user?.role === ROLES.ADMIN ? "/alerts" : undefined}
        />
        <StatCard
          title="Total Inventory Value"
          value={formatCurrency(inventoryReport?.totalInventoryValue || 0)}
          icon={CurrencyDollarIcon}
          color="green"
        />
        <StatCard
          title="Categories"
          value={inventoryReport?.categorySummaries?.length || 0}
          icon={TagIcon}
          color="purple"
        />
      </div>

      {/* Alerts Section (All users can see alerts) */}
      {alerts?.length > 0 && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-lg font-medium text-red-800">Active Alerts</h3>
            {user?.role === ROLES.ADMIN && (
              <Link
                to="/alerts"
                className="text-sm text-red-600 hover:text-red-500 font-medium"
              >
                View All
              </Link>
            )}
          </div>
          <div className="space-y-2">
            {alerts.slice(0, 3).map((alert) => (
              <div key={alert.id} className="text-sm text-red-700">
                <span className="font-medium">{alert.productName}:</span>{" "}
                {alert.message}
              </div>
            ))}
            {alerts.length > 3 && (
              <div className="text-sm text-red-600">
                +{alerts.length - 3} more alerts
              </div>
            )}
          </div>
        </div>
      )}

      {/* Charts */}
      <div
        className={`grid grid-cols-1 ${showStockStatusChart ? "lg:grid-cols-2" : ""} gap-6`}
      >
        {/* Category Value Chart */}
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-lg font-medium text-gray-900 mb-4">
            Inventory Value by Category
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
                <Bar dataKey="value" fill="#3B82F6" />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-64 text-gray-500">
              No data available
            </div>
          )}
        </div>

        {/* Stock Status Chart - Admin only */}
        {showStockStatusChart && (
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-medium text-gray-900 mb-4">
              Stock Status Distribution
            </h3>
            {stockStatusData.some((d) => d.value > 0) ? (
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={stockStatusData}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ name, value }) => `${name}: ${value}`}
                    outerRadius={80}
                    fill="#8884d8"
                    dataKey="value"
                  >
                    {stockStatusData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex items-center justify-center h-64 text-gray-500">
                No data available
              </div>
            )}
          </div>
        )}
      </div>

      {/* Low Stock Products - Admin only */}
      {user?.role === ROLES.ADMIN && lowStockProducts?.length > 0 && (
        <div className="bg-white shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-medium text-gray-900">
                Low Stock Products
              </h3>
              <div className="flex space-x-2">
                <button
                  onClick={handleExportLowStock}
                  disabled={exportLoading.lowStock}
                  className="inline-flex items-center px-2 py-1 border border-gray-300 shadow-sm text-xs font-medium rounded text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
                >
                  {exportLoading.lowStock ? (
                    <LoadingSpinner size="sm" />
                  ) : (
                    <ArrowDownTrayIcon className="h-3 w-3 mr-1" />
                  )}
                  Export
                </button>
                <Link
                  to="/low-stock"
                  className="text-sm text-primary-600 hover:text-primary-500 font-medium"
                >
                  View All
                </Link>
              </div>
            </div>
            <div className="overflow-hidden">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Product
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Category
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Current Stock
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Reorder Level
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {lowStockProducts.slice(0, 5).map((product) => (
                    <tr key={product.id}>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        {product.name}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {product.category}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-red-600 font-medium">
                        {product.quantity}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {product.reorderLevel}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;
