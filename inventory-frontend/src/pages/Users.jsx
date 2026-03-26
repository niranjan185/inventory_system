import { useState, useEffect } from "react";
import { UserIcon, TrashIcon } from "@heroicons/react/24/outline";
import { toast } from "react-hot-toast";

import DataTable from "../components/DataTable";
import LoadingSpinner from "../components/LoadingSpinner";
import ConfirmDialog from "../components/ConfirmDialog";
import { userService } from "../services/userService";
import { ROLES } from "../utils/constants";
import { formatDateTime } from "../utils/helpers";

const Users = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [deleteUser, setDeleteUser] = useState(null);
  const [updatingRole, setUpdatingRole] = useState(null);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const data = await userService.getAllUsers();
      setUsers(data);
    } catch (error) {
      toast.error("Failed to fetch users");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleUpdateRole = async (userId, newRole) => {
    setUpdatingRole(userId);
    try {
      await userService.updateUserRole(userId, newRole);
      toast.success("User role updated successfully");
      fetchUsers();
    } catch (error) {
      toast.error(error.message || "Failed to update user role");
    } finally {
      setUpdatingRole(null);
    }
  };

  const handleDeleteUser = async () => {
    try {
      await userService.deleteUser(deleteUser.id);
      toast.success("User deleted successfully");
      setDeleteUser(null);
      fetchUsers();
    } catch (error) {
      toast.error(error.message || "Failed to delete user");
    }
  };

  const columns = [
    {
      key: "name",
      label: "Name",
      sortable: true,
      render: (value, user) => (
        <div className="flex items-center">
          <div className="flex-shrink-0 h-8 w-8">
            <div className="h-8 w-8 rounded-full bg-primary-100 flex items-center justify-center">
              <UserIcon className="h-4 w-4 text-primary-600" />
            </div>
          </div>
          <div className="ml-3">
            <div className="text-sm font-medium text-gray-900">{value}</div>
            <div className="text-sm text-gray-500">{user.email}</div>
          </div>
        </div>
      ),
    },
    {
      key: "role",
      label: "Role",
      sortable: true,
      render: (value, user) => (
        <div className="flex items-center space-x-2">
          <select
            value={value}
            onChange={(e) => handleUpdateRole(user.id, e.target.value)}
            disabled={updatingRole === user.id}
            className="text-sm border border-gray-300 rounded px-2 py-1 focus:ring-primary-500 focus:border-primary-500"
          >
            <option value={ROLES.USER}>User</option>
            <option value={ROLES.ADMIN}>Admin</option>
          </select>
          {updatingRole === user.id && <LoadingSpinner size="sm" />}
        </div>
      ),
    },
    {
      key: "createdAt",
      label: "Joined",
      sortable: true,
      render: (value) => (value ? formatDateTime(value) : "N/A"),
    },
    {
      key: "actions",
      label: "Actions",
      render: (_, user) => (
        <button
          onClick={() => setDeleteUser(user)}
          className="text-red-600 hover:text-red-900 text-sm font-medium"
        >
          <TrashIcon className="h-4 w-4" />
        </button>
      ),
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
        <h1 className="text-2xl font-bold text-gray-900">Users</h1>
        <div className="text-sm text-gray-500">Total: {users.length} users</div>
      </div>

      {/* Users Table */}
      <div className="bg-white shadow rounded-lg">
        <DataTable
          data={users}
          columns={columns}
          emptyMessage="No users found"
        />
      </div>

      {/* Delete Confirmation */}
      <ConfirmDialog
        isOpen={!!deleteUser}
        onClose={() => setDeleteUser(null)}
        onConfirm={handleDeleteUser}
        title="Delete User"
        message={`Are you sure you want to delete "${deleteUser?.name}"? This action cannot be undone.`}
        confirmText="Delete"
        confirmColor="red"
      />
    </div>
  );
};

export default Users;
