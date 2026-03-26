import { useState, useEffect } from "react";
import {
  PlusIcon,
  TrashIcon,
  ArrowPathIcon,
} from "@heroicons/react/24/outline";
import { toast } from "react-hot-toast";

import DataTable from "../components/DataTable";
import LoadingSpinner from "../components/LoadingSpinner";
import Modal from "../components/Modal";
import ConfirmDialog from "../components/ConfirmDialog";
import { useAuth } from "../context/AuthContext";
import { alertService } from "../services/alertService";
import { formatDateTime } from "../utils/helpers";
import { ROLES } from "../utils/constants";

const AlertForm = ({ onSubmit, onCancel, isLoading }) => {
  const [formData, setFormData] = useState({
    productName: "",
    message: "",
  });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(formData);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-gray-700">
          Product Name
        </label>
        <input
          type="text"
          name="productName"
          required
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500"
          value={formData.productName}
          onChange={handleChange}
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">
          Message
        </label>
        <textarea
          name="message"
          rows={3}
          required
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500"
          value={formData.message}
          onChange={handleChange}
        />
      </div>

      <div className="flex justify-end space-x-3 pt-4">
        <button
          type="button"
          onClick={onCancel}
          className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
        >
          Cancel
        </button>
        <button
          type="submit"
          disabled={isLoading}
          className="px-4 py-2 text-sm font-medium text-white bg-primary-600 border border-transparent rounded-md hover:bg-primary-700 disabled:opacity-50"
        >
          {isLoading ? <LoadingSpinner size="sm" /> : "Create Alert"}
        </button>
      </div>
    </form>
  );
};

const Alerts = () => {
  const { user } = useAuth();
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [deleteAlert, setDeleteAlert] = useState(null);
  const [formLoading, setFormLoading] = useState(false);

  const fetchAlerts = async () => {
    try {
      setLoading(true);
      const data = await alertService.getAllAlerts();
      setAlerts(data);
    } catch (error) {
      toast.error("Failed to fetch alerts");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAlerts();
  }, []);

  const handleCreateAlert = async (alertData) => {
    setFormLoading(true);
    try {
      await alertService.createAlert(alertData);
      toast.success("Alert created successfully");
      setShowModal(false);
      fetchAlerts();
    } catch (error) {
      toast.error(error.message || "Failed to create alert");
    } finally {
      setFormLoading(false);
    }
  };

  const handleDeleteAlert = async () => {
    try {
      await alertService.deleteAlert(deleteAlert.id);
      toast.success("Alert deleted successfully");
      setDeleteAlert(null);
      fetchAlerts();
    } catch (error) {
      toast.error(error.message || "Failed to delete alert");
    }
  };

  const handleClearAllAlerts = async () => {
    try {
      await alertService.clearAllAlerts();
      toast.success("All alerts cleared successfully");
      fetchAlerts();
    } catch (error) {
      toast.error(error.message || "Failed to clear alerts");
    }
  };

  const handleRefreshMessages = async () => {
    try {
      await alertService.refreshAlertMessages();
      toast.success("Alert messages refreshed successfully");
      fetchAlerts();
    } catch (error) {
      toast.error(error.message || "Failed to refresh alert messages");
    }
  };

  const columns = [
    {
      key: "productName",
      label: "Product",
      sortable: true,
    },
    {
      key: "message",
      label: "Message",
      sortable: true,
    },
    {
      key: "createdAt",
      label: "Created",
      sortable: true,
      render: (value) => formatDateTime(value),
    },
    // Only show actions column for admin users
    ...(user?.role === ROLES.ADMIN
      ? [
          {
            key: "actions",
            label: "Actions",
            render: (_, alert) => (
              <button
                onClick={() => setDeleteAlert(alert)}
                className="text-red-600 hover:text-red-900 text-sm font-medium"
              >
                <TrashIcon className="h-4 w-4" />
              </button>
            ),
          },
        ]
      : []),
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
        <h1 className="text-2xl font-bold text-gray-900">Alerts</h1>
        {user?.role === ROLES.ADMIN && (
          <div className="flex space-x-3">
            {alerts.length > 0 && (
              <>
                <button
                  onClick={handleRefreshMessages}
                  className="inline-flex items-center px-4 py-2 border border-blue-300 text-sm font-medium rounded-md text-blue-700 bg-blue-50 hover:bg-blue-100"
                >
                  <ArrowPathIcon className="h-4 w-4 mr-2" />
                  Refresh Messages
                </button>
                <button
                  onClick={handleClearAllAlerts}
                  className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                >
                  <TrashIcon className="h-4 w-4 mr-2" />
                  Clear All
                </button>
              </>
            )}
            <button
              onClick={() => setShowModal(true)}
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
            >
              <PlusIcon className="h-4 w-4 mr-2" />
              Create Alert
            </button>
          </div>
        )}
      </div>

      {/* Alerts Table */}
      <div className="bg-white shadow rounded-lg">
        <DataTable
          data={alerts}
          columns={columns}
          emptyMessage="No alerts found"
        />
      </div>

      {/* Alert Form Modal - Admin only */}
      {user?.role === ROLES.ADMIN && (
        <Modal
          isOpen={showModal}
          onClose={() => setShowModal(false)}
          title="Create Alert"
        >
          <AlertForm
            onSubmit={handleCreateAlert}
            onCancel={() => setShowModal(false)}
            isLoading={formLoading}
          />
        </Modal>
      )}

      {/* Delete Confirmation - Admin only */}
      {user?.role === ROLES.ADMIN && (
        <ConfirmDialog
          isOpen={!!deleteAlert}
          onClose={() => setDeleteAlert(null)}
          onConfirm={handleDeleteAlert}
          title="Delete Alert"
          message={`Are you sure you want to delete this alert for "${deleteAlert?.productName}"?`}
          confirmText="Delete"
          confirmColor="red"
        />
      )}
    </div>
  );
};

export default Alerts;
