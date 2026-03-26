import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export const useAppNavigation = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated } = useAuth();

  // Navigate within the authenticated area
  const navigateToPage = (path, options = {}) => {
    if (isAuthenticated) {
      navigate(path, options);
    }
  };

  // Navigate with history replacement (won't add to browser history)
  const replaceCurrentPage = (path) => {
    if (isAuthenticated) {
      navigate(path, { replace: true });
    }
  };

  // Go back within the authenticated area only
  const goBack = () => {
    if (isAuthenticated) {
      const protectedRoutes = [
        "/dashboard",
        "/products",
        "/alerts",
        "/reports",
        "/settings",
        "/users",
        "/audit-logs",
        "/low-stock",
      ];

      // Check if there's a previous page in history that's also protected
      if (window.history.length > 1) {
        navigate(-1);
      } else {
        // If no history or unsafe to go back, go to dashboard
        navigate("/dashboard", { replace: true });
      }
    }
  };

  // Navigate to dashboard (safe default)
  const goToDashboard = () => {
    if (isAuthenticated) {
      navigate("/dashboard", { replace: true });
    }
  };

  // Check if current route is protected
  const isProtectedRoute = () => {
    const protectedRoutes = [
      "/dashboard",
      "/products",
      "/alerts",
      "/reports",
      "/settings",
      "/users",
      "/audit-logs",
      "/low-stock",
    ];
    return protectedRoutes.includes(location.pathname);
  };

  return {
    navigateToPage,
    replaceCurrentPage,
    goBack,
    goToDashboard,
    isProtectedRoute,
    currentPath: location.pathname,
  };
};
