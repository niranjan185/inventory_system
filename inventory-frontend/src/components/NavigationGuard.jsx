import { useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { isPublicRoute, isProtectedRoute } from "../utils/navigationUtils";

const NavigationGuard = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, isLoading } = useAuth();

  // Handle route protection
  useEffect(() => {
    if (isLoading) return;

    // Prevent authenticated users from accessing public routes
    if (isAuthenticated && isPublicRoute(location.pathname)) {
      navigate("/dashboard", { replace: true });
      return;
    }

    // Redirect unauthenticated users from protected routes to landing
    if (!isAuthenticated && isProtectedRoute(location.pathname)) {
      navigate("/", { replace: true });
      return;
    }
  }, [isAuthenticated, isLoading, location.pathname, navigate]);

  // Handle browser navigation (back/forward buttons)
  useEffect(() => {
    if (isLoading || !isAuthenticated) return;

    let isNavigating = false;

    const handlePopState = (event) => {
      if (isNavigating) return;

      const currentPath = window.location.pathname;

      // If authenticated user navigates back to public routes, redirect to dashboard
      if (isPublicRoute(currentPath)) {
        isNavigating = true;

        // Use setTimeout to avoid conflicts with browser navigation
        setTimeout(() => {
          navigate("/dashboard", { replace: true });
          isNavigating = false;
        }, 0);
      }
    };

    // Override browser back button behavior for authenticated users
    window.addEventListener("popstate", handlePopState);

    return () => {
      window.removeEventListener("popstate", handlePopState);
    };
  }, [isAuthenticated, isLoading, navigate]);

  // Clear authentication history after login
  useEffect(() => {
    if (isAuthenticated && !isLoading && isProtectedRoute(location.pathname)) {
      // Mark this state to prevent back navigation to auth pages
      const currentState = window.history.state;
      if (!currentState || !currentState.preventBackToAuth) {
        window.history.replaceState(
          { ...currentState, preventBackToAuth: true },
          "",
          location.pathname,
        );
      }
    }
  }, [isAuthenticated, isLoading, location.pathname]);

  return children;
};

export default NavigationGuard;
