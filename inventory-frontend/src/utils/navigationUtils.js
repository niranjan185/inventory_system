// Navigation utility functions to handle browser history properly

export const PUBLIC_ROUTES = [
  "/",
  "/login",
  "/register",
  "/forgot-password",
  "/reset-password",
];
export const PROTECTED_ROUTES = [
  "/dashboard",
  "/products",
  "/alerts",
  "/reports",
  "/settings",
  "/users",
  "/audit-logs",
  "/low-stock",
];

export const isPublicRoute = (path) => {
  return PUBLIC_ROUTES.includes(path);
};

export const isProtectedRoute = (path) => {
  return PROTECTED_ROUTES.includes(path);
};

// Clear browser history of authentication pages after login
export const clearAuthHistory = () => {
  // Replace current history state to prevent back navigation to auth pages
  if (window.history.replaceState) {
    window.history.replaceState(null, "", "/dashboard");
  }
};

// Prevent navigation to auth pages when authenticated
export const preventAuthNavigation = (
  isAuthenticated,
  currentPath,
  navigate,
) => {
  if (isAuthenticated && isPublicRoute(currentPath)) {
    navigate("/dashboard", { replace: true });
    return true;
  }
  return false;
};

// Handle browser back button for authenticated users
export const handleAuthenticatedBackNavigation = (navigate) => {
  // Instead of going to potentially unsafe routes, go to dashboard
  navigate("/dashboard", { replace: true });
};

// Safe navigation within authenticated area
export const safeNavigate = (path, navigate, isAuthenticated) => {
  if (!isAuthenticated) {
    navigate("/", { replace: true });
    return;
  }

  if (isPublicRoute(path)) {
    navigate("/dashboard", { replace: true });
    return;
  }

  navigate(path);
};
