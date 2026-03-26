import { useState } from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import { Toaster } from "react-hot-toast";

import { AuthProvider } from "./context/AuthContext";
import { NotificationProvider } from "./context/NotificationContext";
import NavigationGuard from "./components/NavigationGuard";
import ProtectedRoute from "./components/ProtectedRoute";
import RoleBasedRoute from "./components/RoleBasedRoute";
import Sidebar from "./components/Sidebar";
import Navbar from "./components/Navbar";

// Pages
import Landing from "./pages/Landing";
import Login from "./pages/Login";
import Register from "./pages/Register";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPassword from "./pages/ResetPassword";
import Dashboard from "./pages/Dashboard";
import Products from "./pages/Products";
import Alerts from "./pages/Alerts";
import Reports from "./pages/Reports";
import Users from "./pages/Users";
import AuditLogs from "./pages/AuditLogs";
import Settings from "./pages/Settings";
import LowStock from "./pages/LowStock";
import NotFound from "./pages/NotFound";

import { ROLES } from "./utils/constants";

const AppLayout = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="flex h-screen bg-gray-100">
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <div className="flex-1 flex flex-col overflow-hidden">
        <Navbar onMenuClick={() => setSidebarOpen(true)} />

        <main className="flex-1 overflow-x-hidden overflow-y-auto bg-gray-100">
          <div className="container mx-auto px-6 py-8">{children}</div>
        </main>
      </div>
    </div>
  );
};

function App() {
  return (
    <AuthProvider>
      <NotificationProvider>
        <Router
          future={{
            v7_startTransition: true,
            v7_relativeSplatPath: true,
          }}
        >
          <NavigationGuard>
            <div className="App">
              <Routes>
                {/* Landing page - public route */}
                <Route path="/" element={<Landing />} />

                {/* Public routes */}
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="/forgot-password" element={<ForgotPassword />} />
                <Route path="/reset-password" element={<ResetPassword />} />

                {/* Protected routes */}
                <Route
                  path="/dashboard"
                  element={
                    <ProtectedRoute>
                      <AppLayout>
                        <Dashboard />
                      </AppLayout>
                    </ProtectedRoute>
                  }
                />

                <Route
                  path="/products"
                  element={
                    <ProtectedRoute>
                      <AppLayout>
                        <Products />
                      </AppLayout>
                    </ProtectedRoute>
                  }
                />

                <Route
                  path="/reports"
                  element={
                    <ProtectedRoute>
                      <AppLayout>
                        <Reports />
                      </AppLayout>
                    </ProtectedRoute>
                  }
                />

                <Route
                  path="/settings"
                  element={
                    <ProtectedRoute>
                      <AppLayout>
                        <Settings />
                      </AppLayout>
                    </ProtectedRoute>
                  }
                />

                {/* Alerts route - accessible by both USER and ADMIN */}
                <Route
                  path="/alerts"
                  element={
                    <ProtectedRoute>
                      <RoleBasedRoute allowedRoles={[ROLES.USER, ROLES.ADMIN]}>
                        <AppLayout>
                          <Alerts />
                        </AppLayout>
                      </RoleBasedRoute>
                    </ProtectedRoute>
                  }
                />

                {/* Admin-only routes */}
                <Route
                  path="/users"
                  element={
                    <ProtectedRoute>
                      <RoleBasedRoute allowedRoles={[ROLES.ADMIN]}>
                        <AppLayout>
                          <Users />
                        </AppLayout>
                      </RoleBasedRoute>
                    </ProtectedRoute>
                  }
                />

                <Route
                  path="/audit-logs"
                  element={
                    <ProtectedRoute>
                      <RoleBasedRoute allowedRoles={[ROLES.ADMIN]}>
                        <AppLayout>
                          <AuditLogs />
                        </AppLayout>
                      </RoleBasedRoute>
                    </ProtectedRoute>
                  }
                />

                <Route
                  path="/low-stock"
                  element={
                    <ProtectedRoute>
                      <AppLayout>
                        <LowStock />
                      </AppLayout>
                    </ProtectedRoute>
                  }
                />

                {/* 404 route */}
                <Route path="/404" element={<NotFound />} />
                <Route path="*" element={<Navigate to="/404" replace />} />
              </Routes>

              {/* Toast notifications */}
              <Toaster
                position="top-right"
                toastOptions={{
                  duration: 4000,
                  style: {
                    background: "#363636",
                    color: "#fff",
                  },
                  success: {
                    duration: 3000,
                    theme: {
                      primary: "#4ade80",
                    },
                  },
                  error: {
                    duration: 5000,
                    theme: {
                      primary: "#ef4444",
                    },
                  },
                }}
              />
            </div>
          </NavigationGuard>
        </Router>
      </NotificationProvider>
    </AuthProvider>
  );
}

export default App;
